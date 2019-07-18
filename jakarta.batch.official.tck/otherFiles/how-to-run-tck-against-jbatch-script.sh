#!/bin/bash
set -x

####
# Running Jakarta Batch TCK Version 1.0.2 against com.ibm.jbatch V1.0.1
####

# This is a documented script that can be used to execute the Jakarta Batch TCK against the com.ibm.jbatch implementation.  By using "set -x" we allow the 'script' command to be used
# to record output, further documenting the environment.  This is not fully parameterized such that it will be easily runnable with future versions of the related software, 
# but is rather tied to the specific version under test.

##################
# REQUIRED SETUP
##################

# 1. Point to JAVA_HOME so that the signature test command below can find the runtime JAR (rt.jar):
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.212.b04-0.el7_6.x86_64/jre/

# 2. For now, assume you get TCK from an EE4J download, "out of band" or external to this script, and you manually by whatever means copy it into your home directory:
TCK_ZIP_LOCATION=~/jakarta.batch.official.tck-1.0.2.tar.gz

#-------------------------------------------------------------------------------
# You can skip this section.
#
# It may help to leave in how we would build the official TCK archive informally in a local build:
#
# TCK_EXECUTION_BRANCH=1.0.2-api-tck-execution
# git clone git@github.com:eclipse-ee4j/batch-tck.git; cd batch-tck
# git checkout $TCK_EXECUTION_BRANCH
# mvn clean install -DskipTests=true  -DskipSigTests=true
# TCK_ZIP_LOCATION=~/.m2/repository/jakarta/batch/jakarta.batch.official.tck/1.0.2/jakarta.batch.official.tck-1.0.2.tar.gz
#-------------------------------------------------------------------------------


#######################
# Don't change the rest 
#######################

# Variables like this just here for readability
BATCH_API_STAGING_REPO_URL=https://oss.sonatype.org/service/local/repositories/jakartabatch-1007/content/jakarta/batch/jakarta.batch-api/1.0.2/jakarta.batch-api-1.0.2.jar

# Basic info
uname -a
# Echo contents
ls -la .

# Since these are Maven coordinates of already-released artifacts, we take the shortcut of copying them locally
cp ~/.m2/repository/org/apache/derby/derby/10.10.1.1/derby-10.10.1.1.jar .
cp ~/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.container/1.0.1/com.ibm.jbatch.container-1.0.1.jar .
cp ~/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.spi/1.0.1/com.ibm.jbatch.spi-1.0.1.jar .
cp ~/.m2/repository/net/java/sigtest/sigtestdev/3.0-b12-v20140219/sigtestdev-3.0-b12-v20140219.jar ./sigtestdev.jar

chmod +rx *.jar

# Get API JAR under test from staging repo
wget $BATCH_API_STAGING_REPO_URL

# Echo contents
ls -la .

#  unzip new from peer to 'batch-tck'
tar xzvf  $TCK_ZIP_LOCATION
cd jakarta.batch.official.tck-1.0.2

# Show java
ls -l $(which java) $(which javac)
ls -l /etc/alternatives/java*    # Help navigate links
echo $JAVA_HOME
ls -l $JAVA_HOME/lib/rt.jar
java -version

# Run SigTest
java -jar ../sigtestdev.jar SignatureTest -static -package javax.batch \
-filename artifacts/batch-api-sigtest-java8.sig \
-classpath ../jakarta.batch-api-1.0.2.jar:$JAVA_HOME/lib/rt.jar:lib/javax.inject-1.jar:lib/cdi-api-1.1-20121030.jar

#
# Run SigTest forcing error
#
echo 
echo ---------------------------------------------------
echo --- expecting failure to show tests are working ---
echo ---------------------------------------------------
echo 
java -jar ../sigtestdev.jar SignatureTest -static -package javax.batch \
-filename artifacts/batch-api-sigtest-java8.sig \
-classpath ../jakarta.batch-api-1.0.2.jar:$JAVA_HOME/lib/rt.jar:lib/javax.inject-1.jar
echo 
echo ---------------------------------------------------
echo --- done expecting failure,tests should work now---
echo ---------------------------------------------------
echo 

# Run TestNG bucket with properties to configure com.ibm.jbatch implementation

ant -f build.xml -Dbatch.impl.classes=../jakarta.batch-api-1.0.2.jar:../com.ibm.jbatch.container-1.0.1.jar:../com.ibm.jbatch.spi-1.0.1.jar:../derby-10.10.1.1.jar  -Djvm.options="-Dcom.ibm.jbatch.spi.ServiceRegistry.BATCH_THREADPOOL_SERVICE=com.ibm.jbatch.container.services.impl.GrowableThreadPoolServiceImpl -Dcom.ibm.jbatch.spi.ServiceRegistry.J2SE_MODE=true -Dcom.ibm.jbatch.spi.ServiceRegistry.CONTAINER_ARTIFACT_FACTORY_SERVICE=com.ibm.jbatch.container.services.impl.DelegatingBatchArtifactFactoryImpl"
