#!/bin/bash
export JAVA_HOME=/usr
export PATH=/usr/lib/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/home/harsha/bin:/usr/local/jdk1.7.0_01//bin
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
currentPath=`pwd`
timestr="JavaProgram_$1_Threads_4_Cores"
count=$2
while [ $count -gt 0 ]
do
java src.edu.cs.iit.cs553.WordCountJ ./input/input_sample.txt ./output/WordCountJOutput.txt -nt $1 \
 | grep "Time" \
 | awk -F' ' '{ print $4 }' | tee ./time1.txt
time1=`cat time1.txt`
timestr="$timestr,$time1"
count=$[$count-1]
done
echo ${timestr} > TimingsJ.txt
