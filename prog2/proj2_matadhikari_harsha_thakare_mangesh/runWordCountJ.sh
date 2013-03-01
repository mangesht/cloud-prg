#!/bin/bash
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
currentPath=`pwd`
java src.edu.cs.iit.cs553.WordCountJ ./input/input_02.txt ./output/WordCountJOutput.txt
