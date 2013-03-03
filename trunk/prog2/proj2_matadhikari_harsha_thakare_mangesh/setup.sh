#!/bin/bash
#set -x
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
export PATH=$PATH:$HADOOP_HOME/bin
currentPath=`pwd`
export JAVA_HOME=/usr
export PATH=$PATH:/usr/lib/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/home/harsha/bin:/usr/local/jdk1.7.0_01//bin
#make WordCountJ
#make WordCountMR
currentPath=`pwd`
cd ${HADOOP_HOME}
bin/hadoop dfs -rmr /proj2/WordCountMR/input
bin/hadoop dfs -mkdir  /proj2
bin/hadoop dfs -mkdir  /proj2/WordCountMR
bin/hadoop dfs -mkdir  /proj2/WordCountMR/input
bin/hadoop dfs -copyFromLocal ${currentPath}/input/input_sample.txt /proj2/WordCountMR/input
cd ${currentPath}

