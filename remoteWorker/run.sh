#!/bin/sh
touch ~/mangesht.txt
sleep 30
cd ~/remoteWorker
svn up
make clean
make run OPT="-i 100" >  ~/log1.log 
sudo shutdown now

