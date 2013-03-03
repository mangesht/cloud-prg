#!/bin/bash
#set -x
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
export PATH=$PATH:$HADOOP_HOME/bin
currentPath=`pwd`
OUTPUT_FOLDER=output
which hadoop
#cp ${currentPath}/WordCountMR.jar ${HADOOP_HOME}
cd ${HADOOP_HOME}
bin/hadoop dfs -rmr /proj2/WordCountMR/output
rm logs/*.*
bin/hadoop jar ${currentPath}/${OUTPUT_FOLDER}/WordCountMR.jar src.edu.cs.iit.cs553.WordCountMR /proj2/WordCountMR/input/ /proj2/WordCountMR/output 2>&1 \
#| tee ${currentPath}/WordCountMR.log
bin/hadoop dfs -ls /proj2/WordCountMR/output
#bin/hadoop dfs -getmerge /projects/WordCountMR/output 
#cp ${HADOOP_HOME}/logs/*jobtracker*.* ${currentPath}
cd ${currentPath}
