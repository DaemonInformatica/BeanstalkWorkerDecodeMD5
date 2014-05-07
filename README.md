BeanstalkWorkerDecodeMD5
========================

Part of a network of programs to bruteforce MD5 hashes using beanstalkd. This is the decoder


Why did I write this: 
One of my favorite branches of computer science is parallel computing. The art of dividing CPU-resource intensive projects
over multiple / many computers and gathering the results as pieces of the puzzle to put back together. Somebody introduced me
to the simple beanstalk-queue and I figured 'Lets think of an excuse to implement something around this. 

Thus the MD5 bruteforce hash-cracker was born. 

To use this, you need to download 3 projects in total: 
- BeanstalkManagerMD5 
- BeanstalkWorkerDecodeMD5 (This project) 
- BeanstalkWorkerResult
- webservice database installation and webconsole. 


What does the BeanstalkWorkerDecodeMD5 do? 
The worker listens to a preconfigured queue on the beanstalk for jobs that are submitted by the manager. 
Each job contains the MD5 of a password and the start and end of a range  of values for which to check if they correspond to the hash. 
for each element in the range it will create the md5, compare it to the given hash and if they correspond, the worker will return this
in the result job. 

So the decoder-worker also creates a beanstalk job, but in the 'result' queue. 


HOWTO build and use it: 
Build it: 
- Add a reference to beanstemc.jar and json-simple-1.1.1.jar to the project. 
- Build it into a jar file. 

Start it: 
- execute: java -jar BeanstalkWorkerDecodeMD5.jar <host> <port> <tube>

Host port and tube are all settings for the beanstalk daemon. 
By default the host is localhost, the port is 9000 and the tube is 'longtube'. As you can see in the README.md of the manager, the only tube
currently supported is 'longtube'. 

TODO: 
- cleanup the debug information. 
- implement feedback about status of the worker to the server. (the database already has a table for this, but the listener has not yet been implemented). 
- implement a measure of control over the beanstalk. 
- Fix an out of memory Error that comes up after running the client a while... 
- Fix the uber-ugly again and again re-connecting of beanstalk. It's a stateful connection for chrissake! 
