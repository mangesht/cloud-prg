#!/bin/sh
touch /home/ubuntu/mangesht.txt
sleep 30
cd /home/ubuntu/remoteWorker
svn up
make clean
make run OPT="-i 100" >  ~/log1.log 
sudo shutdown now

