BeanstalkWorkerDecodeMD5
========================

Part of a network of programs to bruteforce MD5 hashes using beanstalkd. This is the decoder


Why did I write this: 
One of my favorite branches of computer science is parallel computing. The art of dividing CPU-resource intensive projects
over multiple / many computers and gathering the results as pieces of the puzzle to put back together. Somebody introduced me
to the simple beanstalk-queue and I figured 'Lets think of an excuse to implement something around this. 

Thus the MD5 bruteforce hash-cracker was born. 

To use this, you need to download 3 projects in total: 
- BeanstalkManagerMD5 (This project) 
- BeanstalkWorkerDecodeMD5
- BeanstalkWorkerResult
- webservice database installation and webconsole. 


What does the BeanstalkWorkerDecodeMD5 do? 
The worker listens to a preconfigured queue on the beanstalk for jobs that are submitted by the manager. 
Each job contains the MD5 of a password and the start and end of a range  of values for which to check if they correspond to the hash. 
for each element in the range it will create the md5, compare it to the given hash and if they correspond, the worker will return this
in the result job. 

So the decoder-worker also creates a beanstalk job, but in the 'result' queue. 
