#!/bin/sh
touch /home/ubuntu/mangesht.txt
sleep 1
cd /home/ubuntu/remoteWorker
svn up src
make clean
make run OPT="-i 0" >  ~/log1.log 
sudo shutdown 1 -h

