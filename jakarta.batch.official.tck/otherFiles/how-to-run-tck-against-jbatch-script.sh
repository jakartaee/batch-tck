#!/bin/bash
set -x

#------------------------------------------------------------------------------
# Running Jakarta Batch TCK Version 2.0.0 against com.ibm.jbatch 2.0.0
#
# This is a documented script that can be used to execute the Jakarta Batch TCK
# against the com.ibm.jbatch implementation.
#
# Think of this as a template to produce runs that could be use to certify
# com.ibm.jbatch as a compatible implementation.   It's not quite a record of
# what was actually run to certify (the latter should be available linked
# from an issue found here:
#   https://github.com/eclipse-ee4j/batch-api/labels/certification
#
# By using "set -x" we allow the 'script' command to be used to record output,
# documenting the environment.
#
# Note also this is not fully parameterized such that it will be automatically
# runnable with future versions of the related software, but is rather tied to
# the specific version under test.
#------------------------------------------------------------------------------

################
# OK TO MODIFY
################
echo
echo --------------------------------
echo Begin setup using Java 8
echo --------------------------------
echo

# 1. Root location of TCK execution - Also useful for holding this script itself, and its output logs
TCK_HOME_DIR=~/jkbatch

# 2. Point to JAVA_HOME so that the signature test command below can find the runtime JAR (rt.jar) for
# Java 8 (with Java 11 documented later in the script)
#
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.212.b04-0.el7_6.x86_64/

# 3. Copy required JARs obtained via other mechanisms
#------------------------------------------------------------------------------------------------------
# NOTE: Since these are Maven coordinates of already-released artifacts, we take the shortcut of
# priming the local Maven repository, to be copied locally below.  To make sure we don't have a stale
# local copy, we delete them from the local ~/.m2/repository first.
#------------------------------------------------------------------------------------------------------
REQUIRED_JARS="\
 /home/ibmadmin/.m2/repository/org/apache/derby/derby/10.10.1.1/derby-10.10.1.1.jar \
 /home/ibmadmin/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.container/2.0.0/com.ibm.jbatch.container-2.0.0.jar \
 /home/ibmadmin/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.spi/2.0.0/com.ibm.jbatch.spi-2.0.0.jar \
 /home/ibmadmin/.m2/repository/net/java/sigtest/sigtestdev/3.0-b12-v20140219/sigtestdev-3.0-b12-v20140219.jar \
 /home/ibmadmin/.m2/repository/jakarta/batch/jakarta.batch-api/2.0.0/jakarta.batch-api-2.0.0.jar \
 /home/ibmadmin/.m2/repository/com/sun/activation/jakarta.activation/2.0.0/jakarta.activation-2.0.0.jar \
 /home/ibmadmin/.m2/repository/jakarta/inject/jakarta.inject-api/2.0.0/jakarta.inject-api-2.0.0.jar \
 /home/ibmadmin/.m2/repository/jakarta/enterprise/jakarta.enterprise.cdi-api/3.0.0/jakarta.enterprise.cdi-api-3.0.0.jar \
 /home/ibmadmin/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/3.0.0/jakarta.xml.bind-api-3.0.0.jar \
 /home/ibmadmin/.m2/repository/org/glassfish/jaxb/jaxb-runtime/3.0.0-M4/jaxb-runtime-3.0.0-M4.jar \
 /home/ibmadmin/.m2/repository/org/glassfish/jaxb/jaxb-core/3.0.0-M4/jaxb-core-3.0.0-M4.jar \
 /home/ibmadmin/.m2/repository/com/sun/istack/istack-commons-runtime/4.0.0-M3/istack-commons-runtime-4.0.0-M3.jar \
"


#rm $REQUIRED_JARS
#for f in "$REQUIRED_JARS"; do ls -l $f ; done

#mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -Dartifact=org.apache.derby:derby:10.10.1.1 -DrepoUrl=https://repo1.maven.org/maven2/
#mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -Dartifact=jakarta.batch:jakarta.batch-api:2.0.0 -DrepoUrl=https://repo1.maven.org/maven2/
#mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -Dartifact=net.java.sigtest:sigtestdev:3.0-b12-v20140219 -DrepoUrl=https://repo1.maven.org/maven2/
## Gets com.ibm.jbatch.spi as well.
#mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -Dartifact=com.ibm.jbatch:com.ibm.jbatch.container:2.0.0 -DrepoUrl=https://repo1.maven.org/maven2/


#--------------------------------------------------
# Show some basic info about the OS, JDK/JRE, etc.
# This could be tweaked without ruining the
# validity of the test.
#--------------------------------------------------
uname -a
cat /etc/os-release
echo $JAVA_HOME
ls -l $JAVA_HOME/jre/lib/rt.jar
$JAVA_HOME/bin/java -version

#############################################
# DON'T CHANGE remainder of Java 8 portion
# (except comment out official URL override
#  when testing a staged copy of TCK)
#
# When you get to the Java 11 portion, you
#  can set the JAVA_HOME to a different JDK
#  location.
#############################################

TCK_ARTIFACT_ID=jakarta.batch.official.tck-2.0.0

#
# STAGED
#
TCK_DOWNLOAD_URL=https://download.eclipse.org/jakartabatch/tck/eftl/$TCK_ARTIFACT_ID.zip
TCK_DOWNLOAD_URL=https://repo1.maven.org/maven2/jakarta/batch/jakarta.batch.official.tck/2.0.0/$TCK_ARTIFACT_ID.zip
TCK_DOWNLOAD_URL=https://oss.sonatype.org/content/repositories/staging/jakarta/batch/jakarta.batch.official.tck/2.0.0/$TCK_ARTIFACT_ID.zip

#
# OFFICIAL (will look like this)
#
#TCK_DOWNLOAD_URL=https://download.eclipse.org/jakartaee/batch/2.0/jakarta.batch.official.tck-2.0.0.zip

################
# DON'T CHANGE
################
cd $TCK_HOME_DIR

#
# get TCK zip into an empty directory
#
rm -rf tckdir; mkdir tckdir; cd tckdir
ls -la .
wget $TCK_DOWNLOAD_URL

#
# copy prereqs into an empty directory
#
rm -rf prereqs; mkdir prereqs; cd prereqs
ls -la .
for f in "$REQUIRED_JARS"; do cp $f .;  done
chmod +rx *.jar
ls -la .

#
# Show SHA 256 of everything so far.  Validation should be done manually, since this script
# is part of the TCK zip
#
openssl version
openssl dgst -sha256 *.jar
openssl dgst -sha256 ../*.zip

# extract TCK in peer directory
$JAVA_HOME/bin/jar xvf ../$TCK_ARTIFACT_ID.zip
cd $TCK_ARTIFACT_ID

#------------------------------------------------
# Run TestNG bucket with properties to configure
# com.ibm.jbatch implementation
#------------------------------------------------

echo
echo --------------------------------
echo Begin TestNG tests using Java 8
echo --------------------------------
echo


ant -v -f build.xml -Dbatch.impl.testng.path=../jakarta.batch-api-2.0.0.jar:../com.ibm.jbatch.container-2.0.0.jar:../com.ibm.jbatch.spi-2.0.0.jar:../derby-10.10.1.1.jar:../jakarta.activation-2.0.0.jar:../jakarta.inject-api-2.0.0.jar:../jakarta.enterprise.cdi-api-3.0.0.jar:../jakarta.xml.bind-api-3.0.0.jar:../jaxb-runtime-3.0.0-M4.jar:../jaxb-core-3.0.0-M4.jar:../istack-commons-runtime-4.0.0-M3.jar  -Djvm.options="-Dcom.ibm.jbatch.spi.ServiceRegistry.BATCH_THREADPOOL_SERVICE=com.ibm.jbatch.container.services.impl.GrowableThreadPoolServiceImpl -Dcom.ibm.jbatch.spi.ServiceRegistry.J2SE_MODE=true -Dcom.ibm.jbatch.spi.ServiceRegistry.CONTAINER_ARTIFACT_FACTORY_SERVICE=com.ibm.jbatch.container.services.impl.DelegatingBatchArtifactFactoryImpl"


echo
echo --------------------------------
echo Begin SigTest setup using Java 8
echo --------------------------------
echo

#------------------
# SIGNATURE TESTS
# -----------------

cd $TCK_HOME_DIR

BATCH_API_JAR=$TCK_HOME_DIR/tckdir/prereqs/jakarta.batch-api-2.0.0.jar

IMPL_PATH=$BATCH_API_JAR\
:$TCK_HOME_DIR/tckdir/prereqs/jakarta.inject-api-2.0.0.jar\
:$TCK_HOME_DIR/tckdir/prereqs/jakarta.enterprise.cdi-api-3.0.0.jar\
:$TCK_HOME_DIR/tckdir/prereqs/com.ibm.jbatch.container-2.0.0.jar\
:$TCK_HOME_DIR/tckdir/prereqs/com.ibm.jbatch.spi-2.0.0.jar

#------------------------------------------------
# Run Java 8 SigTest portion
# -----------------------------------------------

echo
echo --------------------------------
echo Begin SigTest tests using Java 8
echo --------------------------------
echo

$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/$TCK_ARTIFACT_ID/artifacts/batch.standalone.tck.sig_2.0_se8   -classpath $JAVA_HOME/jre/lib/rt.jar:$IMPL_PATH

echo
echo -------------------------------------------
echo At this point you have completed the TCK
echo running with Java 8
echo -------------------------------------------
echo

#------------------------------------------
# Run SigTest forcing error (not strictly
# necessary, but the signature testing is
#------------------------------------------
echo
echo -------------------------------------------
echo Exclude CDI API JAR
echo expecting failure to show tests are working
echo -------------------------------------------
echo
IMPL_PATH_INCOMPLETE=$BATCH_API_JAR\
:$TCK_HOME_DIR/tckdir/prereqs/jakarta.inject-api-2.0.0.jar\
:$TCK_HOME_DIR/tckdir/prereqs/com.ibm.jbatch.container-2.0.0.jar\
:$TCK_HOME_DIR/tckdir/prereqs/com.ibm.jbatch.spi-2.0.0.jar

# Same command as before, different path
$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/$TCK_ARTIFACT_ID/artifacts/batch.standalone.tck.sig_2.0_se8   -classpath $JAVA_HOME/jre/lib/rt.jar:$IMPL_PATH_INCOMPLETE
echo
echo ---------------------
echo done expected failure
echo ---------------------
echo



#=============
# JAVA 11
#=============


echo
echo --------------------------------
echo Begin setup using Java 11
echo --------------------------------
echo

################
# OK TO MODIFY
################

export JAVA_HOME=/usr/lib/jvm/adoptopenjdk-11-openj9

################
# DO NOT MODIFY
#  THE REST !
################

$JAVA_HOME/bin/java -version

# Cleanup TestNG execution dir, but no need to re-copy prereqs.
cd $TCK_HOME_DIR/tckdir/prereqs

rm -rf $TCK_ARTIFACT_ID
$JAVA_HOME/bin/jar xvf ../$TCK_ARTIFACT_ID.zip
cd $TCK_ARTIFACT_ID



#------------------------------------------------
# Run TestNG bucket with properties to configure
# com.ibm.jbatch implementation
#------------------------------------------------

echo
echo --------------------------------
echo Begin TestNG tests using Java 11
echo --------------------------------
echo

ant -v -f build.xml -Dbatch.impl.testng.path=../jakarta.batch-api-2.0.0.jar:../com.ibm.jbatch.container-2.0.0.jar:../com.ibm.jbatch.spi-2.0.0.jar:../derby-10.10.1.1.jar:../jakarta.activation-2.0.0.jar:../jakarta.inject-api-2.0.0.jar:../jakarta.enterprise.cdi-api-3.0.0.jar:../jakarta.xml.bind-api-3.0.0.jar:../jaxb-runtime-3.0.0-M4.jar:../jaxb-core-3.0.0-M4.jar:../istack-commons-runtime-4.0.0-M3.jar  -Djvm.options="-Dcom.ibm.jbatch.spi.ServiceRegistry.BATCH_THREADPOOL_SERVICE=com.ibm.jbatch.container.services.impl.GrowableThreadPoolServiceImpl -Dcom.ibm.jbatch.spi.ServiceRegistry.J2SE_MODE=true -Dcom.ibm.jbatch.spi.ServiceRegistry.CONTAINER_ARTIFACT_FACTORY_SERVICE=com.ibm.jbatch.container.services.impl.DelegatingBatchArtifactFactoryImpl"

echo
echo --------------------------------
echo Begin SigTest setup using Java 11
echo --------------------------------
echo

cd $TCK_HOME_DIR
JDK11_CLASSES=$TCK_HOME_DIR/jimage-extract
rm -rf $JDK11_CLASSES; mkdir $JDK11_CLASSES; cd $JDK11_CLASSES

# Extract here using `jimage extract`
$JAVA_HOME/bin/jimage extract $JAVA_HOME/lib/modules


echo
echo --------------------------------
echo Begin SigTest tests using Java 11
echo --------------------------------
echo
$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/$TCK_ARTIFACT_ID/artifacts/batch.standalone.tck.sig_2.0_se11  -classpath $JDK11_CLASSES/java.base:$IMPL_PATH


echo
echo -------------------------------------------
echo At this point you have completed the TCK
echo running with Java 11
echo -------------------------------------------
echo


#------------------------------------------
# Run SigTest forcing error (not strictly
# necessary, though the signature testing is
#------------------------------------------
echo
echo -------------------------------------------
echo Exclude CDI API JAR
echo expecting failure to show tests are working
echo -------------------------------------------
echo

# Same command as before, different path
$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/$TCK_ARTIFACT_ID/artifacts/batch.standalone.tck.sig_2.0_se11  -classpath $JDK11_CLASSES/java.base:$IMPL_PATH_INCOMPLETE
echo
echo ---------------------
echo done expected failure
echo ---------------------
echo

