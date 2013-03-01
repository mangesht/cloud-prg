SRC_DIR=src/edu/cs/iit/cs553
HADOOP_VERSION=1.0.4
HADOOP_HOME=../hadoop/hadoop-${HADOOP_VERSION}
JAVA_CLASS_FILES= WordCountJ.class 
MAPREDUCE_CLASS_FILES=WordCountMR.class
CLASSPATH=${HADOOP_HOME}/hadoop-core-${HADOOP_VERSION}.jar

WordCountJ:${JAVA_CLASS_FILES}
	jar cvf $@.jar $(addprefix $(SRC_DIR)/, $@*.class)

WordCountMR:${MAPREDUCE_CLASS_FILES}
	jar cvf $@.jar  $(addprefix $(SRC_DIR)/, $@*.class)

all:WordCountJ WordCountMR

%.class : ${SRC_DIR}/%.java
	javac -Xlint -classpath ${CLASSPATH}  $<
cleanJ:
	rm -rf  $(addprefix ${SRC_DIR}/, ${JAVA_CLASS_FILES})
cleanMR:
	rm -rf  $(addprefix ${SRC_DIR}/, ${MAPREDUCE_CLASS_FILES})

clean_all: cleanJ cleanMR
