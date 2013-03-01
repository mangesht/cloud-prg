#!/bin/bash
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
currentPath=`pwd`
cd ${HADOOP_HOME}
hadoop dfs -rmr /proj2/wordcount/BuiltInExamplesOutput
hadoop jar hadoop-examples-1.0.4.jar wordcount /proj2/wordcount/input /proj2/wordcount/BuiltInExamplesOutput
hadoop dfs -ls /proj2/wordcount/BuiltInExamplesOutput
cd ${currentpath}
