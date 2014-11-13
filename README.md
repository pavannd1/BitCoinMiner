BitCoinMiner
============
Bit coins are defined by hashed strings with a certain number of leading zeroes which is taken as user input. The strings are hashed using SHA-256 technique. The program has a Master AKKA actor who creates multiple actors (workers) and provides them with random strings to mine for bit coins. The default number of strings provided to each worker is set to 1,000,000 (1 Million). The workers complete mining and send the bit coins to the master who then displays all the bit coins once all the workers reply.
____________________________________________________________________________________
File structure:
===============
---build.sbt
--+src
    |
    |-+main
    	|
    	|-+scala
    		|-project1.scala
    	|-+resources
    		|-application.conf
____________________________________________________________________________________
NOTE: CHANGE THE IP ADDRESS OF THE MACHINE IN application.conf

To Run :
========

local machine:
$ sbt
> compile
> run <Number of leading zeroes in the bit coin>

or scala bitCoinMiner <NumberofLeadingZeroes>

remote machine : 
$ sbt
> compile
> run <IP address of the machine running local workers>

or scala bitCoinMiner <IP>
