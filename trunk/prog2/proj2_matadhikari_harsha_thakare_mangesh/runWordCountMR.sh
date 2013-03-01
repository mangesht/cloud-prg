#!/bin/bash
set -x
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
currentPath=`pwd`
cp ${currentPath}/WordCountMR.jar ${HADOOP_HOME}
cd ${HADOOP_HOME}
hadoop dfs -rmr /proj2/wordcount/WordCountMROutput
hadoop jar WordCountMR.jar src.edu.cs.iit.cs553.WordCountMR /proj2/wordcount/input/ /proj2/wordcount/WordCountMROutput -D mapred.reduce.tasks=2 
hadoop dfs -ls /proj2/wordcount/WordCountMROutput
hadoop dfs -getmerge /projects/wordcount/WordCountMROutput 
cd ${currentPath}
