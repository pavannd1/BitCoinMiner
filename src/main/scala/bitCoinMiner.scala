import akka.actor._
import Array._

case object Ready
case class Assign(list: Array[Int])
case class CompletedList(list: Array[Array[String]])

object bitCoinMiner {
    def main(args: Array[String]) {
        if(args.length > 0) {
            if(args(0).contains(".")) {
	    		println("You are a bitCoin Miner, please wait until work is assigned to you")
	    		println("Connecting to server : "+args(0))
  		        println("akka.tcp://BitCoinMiners@"+args(0)+":5227/user/BigBoss")
  		        implicit val system = ActorSystem("LocalSystem")
  		        val remoteActor = system.actorOf(Props(new RemoteMiner(args(0),true)), name = "RemoteActor")  
  		        remoteActor ! "START"
  		        remoteActor ! "REQUEST"
  		        system.awaitTermination()
            } else {
                println("Hello Big Boss, Lets mine some bitCoins today k value is "+args(0))
  		        val system = ActorSystem("BitCoinMiners")
  		        val bBoss = system.actorOf(Props(new BigBoss(Integer.parseInt(args(0)))), name = "BigBoss")
  		        println(" BigBoss has started ");
                bBoss ! "Namaskara"
                var localActor: Array[ActorRef] = new Array[ActorRef](3)
                //siphon local Actors
                for(i <- 0 to localActor.length-1) {
  			        localActor(i) = system.actorOf(Props(new RemoteMiner(args(0),false)), name = ("LocalActor"+i)) 
			        bBoss.tell("Hello I am a local miner "+i+". Can I start work?",localActor(i)) 
			        bBoss.tell(Ready,localActor(i)) 
		        }
		        system.awaitTermination()
	        }	
        } else {
            println("ERROR!Please enter a parameter value and retry")
        }
    }                                                     
}

class RemoteMiner(serverIP: String,rem: Boolean) extends Actor {
    val remote = context.actorFor("akka.tcp://BitCoinMiners@"+serverIP+":5227/user/BigBoss")
    var bitCoinsList = Array(Array("0","0"),Array("1","1"))

    def randomAlphaNumericString(length: Int): String = {
        val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
        randomStringFromCharList(length, chars)
    }
    
    def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
        val sb = new StringBuilder
        for (i <- 1 to length) {
            val randomNum = util.Random.nextInt(chars.length)
            sb.append(chars(randomNum))
        }
        sb.toString
    }

    def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
        sep match {
            case None => bytes.map("%02x".format(_)).mkString
            case _ => bytes.map("%02x".format(_)).mkString(sep.get)
        }
    }

    def receive = {
        case "START" =>
	        if(rem) {
                remote ! "Hello from the Miner at remote Site "
	        } else {
		        sender ! "Hello from Miner at local site"
	        }
        case "REQUEST" =>
	        if(rem) {
		        remote ! Ready
	        } else {
		        sender ! Ready
	        }
        case Assign(req) =>
	    	// println("Receiver k value is "+req(0)+" and multiplier is "+req(1))
            var zeros = ""
            val md = java.security.MessageDigest.getInstance("SHA-256")
	    	for(j <- 1 to req(0)) {
		        zeros = zeros + "0"
	    	}
	    	// println("Total zeros = "+zeros)
            val start: Long = System.currentTimeMillis
            var count: Int = 0
            var workUnit = 1000000
            for (k <- 1 to workUnit) {
                var text = "aholla;"+randomAlphaNumericString(req(1))
                md.update(text.getBytes())
                var result = bytes2hex(md.digest(), Option(""))
                if(result.startsWith(zeros)) {
                    // println("Yes, Found Bitcoin")
                    // println("Result is = "+result)
                    // println("String = "+text)
                    // println("----------------- ")
			        bitCoinsList = concat(bitCoinsList,Array(Array(text,result)))
			        count = count + 1
                }
		        // println("time is "+(System.currentTimeMillis - start))
            }
	        // println("Done! Total bit coins found is "+count)
	        var export = ofDim[Array[String]](bitCoinsList.length - 2)
	        for(i <- 2 to bitCoinsList.length-1) {
		        export(i-2)=bitCoinsList(i)
	        }
	        // println("Export length is "+export.length)
	        if(rem) {
		        remote ! CompletedList(export)
		        context.system.shutdown()
	        } else {
		        sender ! CompletedList(export)
	        }
    }
}

class BigBoss(k: Int) extends Actor {
    var miners = 0;
    var ttlminers = 0;
    var multiplier = 7;
    var bitCoinsList = Array(Array("0","0"),Array("1","1"))
    def receive = {
        case msg: String =>
            println(s"RemoteActor received message '$msg'")
            // println(" Recevied K value is "+k)
        case Ready =>
            miners = miners + 1; 
	        ttlminers = miners;
   	        sender !  Assign(Array(k,(miners+multiplier)))
        case CompletedList(rxList) =>
	        println("A Miner Completed its task")
	        miners = miners - 1
            bitCoinsList = concat(bitCoinsList,rxList)
	        if(miners == 0) {
		        val ttl = bitCoinsList.length -  2 
		        println("Total Bit Coins Found are "+ttl)
	            var export = ofDim[Array[String]](bitCoinsList.length - 2)
       		    for(i <- 2 to bitCoinsList.length-1) {
        	        export(i-2)=bitCoinsList(i)
	            }
		        for(i <- 0 to export.length - 1) {
		            // println("Coin "+i+" "+export(i)(0)+" : "+export(i)(1))
		 	        println(export(i)(0)+" "+export(i)(1))
		        }
		        println("Total Bit Coins Found: "+ttl+" and Total number of miners: "+ttlminers)
		        context.system.shutdown()
	        }
    }
}
