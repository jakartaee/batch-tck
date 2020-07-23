# How To Generate

```bash

# PREREQ

# Assumes your local .m2 repo has been populated with API JARs
MVN_REPO=/home/ibmadmin/.m2/repository

SIGTEST_JAR=$MVN_REPO/net/java/sigtest/sigtestdev/3.0-b12-v20140219/sigtestdev-3.0-b12-v20140219.jar

API_JARS="\
$MVN_REPO/jakarta/batch/jakarta.batch-api/2.0.0-M6/jakarta.batch-api-2.0.0-M6.jar:\
$MVN_REPO/jakarta/enterprise/jakarta.enterprise.cdi-api/3.0.0-M4/jakarta.enterprise.cdi-api-3.0.0-M4.jar:\
$MVN_REPO/jakarta/inject/jakarta.inject-api/2.0.0-RC4/jakarta.inject-api-2.0.0-RC4.jar\
"


# JAVA 8

# Set JAVA_HOME, e.g.:
#  JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.212.b04-0.el7_6.x86_64
#
# NOTE: Use OpenJDK or Oracle JDK but NOT IBM JDK (since it builds its rt.jar a bit differently)


SIGFILE=batch.standalone.tck.sig_2.0_se8
JAVA8_RT=$JAVA_HOME/jre/lib/rt.jar

$JAVA_HOME/bin/java -jar $SIGTEST_JAR   Setup -static -package jakarta.batch -filename \ $SIGFILE  -classpath "$JAVA8_RT:$API_JARS"

# Set 
# JAVA_HOME=... JDK 11

SIGFILE=batch.standalone.tck.sig_2.0_se11
JAVA11_RT="/tmp/extract-classes"
rm -rf $JAVA11_RT;  mkdir $JAVA11_RT; cd $JAVA11_RT;
$JAVA_HOME/bin/jimage extract $JAVA_HOME/lib/modules

$JAVA_HOME/bin/java -jar $SIGTEST_JAR   Setup -static -package jakarta.batch -filename  $SIGFILE  -classpath "$JAVA11_RT/java.base:$API_JARS"
```
