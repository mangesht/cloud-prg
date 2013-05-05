SRC_DIR=src
BIN_DIR=bin
AWS_SDK_VERSION=1.4.3
AWS_SDK_HOME=/home/ubuntu/aws-java-sdk-${AWS_SDK_VERSION}
JAVA_CLASS_FILES=commonInfo.class  executor.class watchDogTimer.class remoteWorker.class
INPSORT_CLASS_FILES= InPlaceSort.class 
MAPREDUCE_CLASS_FILES=WordCountMR.class
CLASSPATH=${SRC_DIR}:${BIN_DIR}:${AWS_SDK_HOME}/lib/aws-java-sdk-flow-build-tools-1.4.3.jar:${AWS_SDK_HOME}/lib/aws-java-sdk-1.4.3.jar:${AWS_SDK_HOME}/lib/aws-java-sdk-1.4.3-sources.jar:${AWS_SDK_HOME}/lib/aws-java-sdk-1.4.3-javadoc.jar:${AWS_SDK_HOME}/third-party/jackson-core-1.8/jackson-core-asl-1.8.7.jar:${AWS_SDK_HOME}/third-party/freemarker-2.3.18/freemarker-2.3.18.jar:${AWS_SDK_HOME}/third-party/commons-logging-1.1.1/commons-logging-1.1.1.jar:${AWS_SDK_HOME}/third-party/spring-3.0/spring-core-3.0.7.jar:${AWS_SDK_HOME}/third-party/spring-3.0/spring-context-3.0.7.jar:${AWS_SDK_HOME}/third-party/spring-3.0/spring-beans-3.0.7.jar:${AWS_SDK_HOME}/third-party/aspectj-1.6/aspectjrt.jar:${AWS_SDK_HOME}/third-party/aspectj-1.6/aspectjweaver.jar:${AWS_SDK_HOME}/third-party/stax-ri-1.2.0/stax-1.2.0.jar:${AWS_SDK_HOME}/third-party/java-mail-1.4.3/mail-1.4.3.jar:${AWS_SDK_HOME}/third-party/httpcomponents-client-4.1.1/httpcore-4.1.jar:${AWS_SDK_HOME}/third-party/httpcomponents-client-4.1.1/httpclient-4.1.1.jar:${AWS_SDK_HOME}/third-party/stax-api-1.0.1/stax-api-1.0.1.jar:${AWS_SDK_HOME}/third-party/jackson-mapper-1.8/jackson-mapper-asl-1.8.7.jar:${AWS_SDK_HOME}/third-party/commons-codec-1.3/commons-codec-1.3.jar

OUTPUT_FOLDER=output

remoteWorker:${JAVA_CLASS_FILES}
	jar cvf ${OUTPUT_FOLDER}/$@.jar $(addprefix $(BIN_DIR)/, $@*.class)

all:remoteWorker

%.class : ${SRC_DIR}/%.java
	javac -Xlint -d ${BIN_DIR} -classpath ${CLASSPATH}  $<

run:remoteWorker.class
	java -classpath ${CLASSPATH} remoteWorker ${OPT} 

clean:
	rm -rf bin/*.class 
cleanMR:
	rm -rf  $(addprefix ${SRC_DIR}/, ${MAPREDUCE_CLASS_FILES})

clean_all: cleanJ cleanMR
