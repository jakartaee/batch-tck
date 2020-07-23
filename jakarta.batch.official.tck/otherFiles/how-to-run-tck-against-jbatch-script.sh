#!/bin/bash
set -x

#------------------------------------------------------------------------------
# Running Jakarta Batch TCK Version 2.0.0-M4 against com.ibm.jbatch 2.0.0-M6
#
# This is a documented script that can be used to execute the Jakarta Batch TCK
# against the com.ibm.jbatch implementation.  By using "set -x" we allow the
# 'script' command to be used to record output, further documenting the
# environment.  This is not fully parameterized such that it will be easily
# runnable with future versions of the related software, but is rather tied to
# the specific version under test.
#------------------------------------------------------------------------------

################
# OK TO MODIFY
################

# 1. Root location of TCK execution - Also useful for holding this script itself, and its output logs
TCK_HOME_DIR=~/jkbatch

# 2. Point to JAVA_HOME so that the signature test command below can find the runtime JAR (rt.jar):
#export JAVA_HOME=/usr/lib/jvm/adoptopenjdk-11-openj9-amd64/
export JAVA_HOME=/usr/lib/jvm/adoptopenjdk-11-openj9
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.212.b04-0.el7_6.x86_64/



#------------------------------------------------------------------------------------------------------
# NOTE: Since these are Maven coordinates of already-released artifacts, we take the shortcut of
# priming the local Maven repository, to be copied locally below.  One way of doing this, since the TCK
# project mvn build includes modules executing the TCK against 'jbatch', is to clone and execute the
# TCK project like:
#
#     git clone git@github.com:eclipse-ee4j/batch-tck.git; cd batch-tck
#     mvn clean install -DskipSigTests=true
#------------------------------------------------------------------------------------------------------
# 3. Copy required JARs obtained via other mechanisms
REQUIRED_JARS="\
 /home/ibmadmin/.m2/repository/org/apache/derby/derby/10.10.1.1/derby-10.10.1.1.jar \
 /home/ibmadmin/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.container/2.0.0-M6/com.ibm.jbatch.container-2.0.0-M6.jar \
 /home/ibmadmin/.m2/repository/com/ibm/jbatch/com.ibm.jbatch.spi/2.0.0-M6/com.ibm.jbatch.spi-2.0.0-M6.jar \
 /home/ibmadmin/.m2/repository/net/java/sigtest/sigtestdev/3.0-b12-v20140219/sigtestdev-3.0-b12-v20140219.jar \
 /home/ibmadmin/.m2/repository/jakarta/batch/jakarta.batch-api/2.0.0-M6/jakarta.batch-api-2.0.0-M6.jar \
"

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
# DON'T CHANGE
# (except comment out official URL override
#  when testing a staged copy of TCK)
#############################################

#
# STAGED
#
TCK_DOWNLOAD_URL=https://download.eclipse.org/jakartabatch/tck/eftl/jakarta.batch.official.tck-2.0.0-M4.zip
TCK_DOWNLOAD_URL=https://oss.sonatype.org/content/repositories/staging/jakarta/batch/jakarta.batch.official.tck/2.0.0-M4/jakarta.batch.official.tck-2.0.0-M4.zip
TCK_DOWNLOAD_URL=https://oss.sonatype.org/service/local/repositories/jakartabatch-1030/content/jakarta/batch/jakarta.batch.official.tck/2.0.0-M4/jakarta.batch.official.tck-2.0.0-M4.zip

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
$JAVA_HOME/bin/jar xvf ../jakarta.batch.official.tck-2.0.0-M4.zip
cd jakarta.batch.official.tck-2.0.0-M4



#------------------------------------------------
# Run TestNG bucket with properties to configure
# com.ibm.jbatch implementation
#------------------------------------------------

ant -v -f build.xml -Dbatch.impl.classes=../jakarta.batch-api-2.0.0-M6.jar:../com.ibm.jbatch.container-2.0.0-M6.jar:../com.ibm.jbatch.spi-2.0.0-M6.jar:../derby-10.10.1.1.jar  -Djvm.options="-Dcom.ibm.jbatch.spi.ServiceRegistry.BATCH_THREADPOOL_SERVICE=com.ibm.jbatch.container.services.impl.GrowableThreadPoolServiceImpl -Dcom.ibm.jbatch.spi.ServiceRegistry.J2SE_MODE=true -Dcom.ibm.jbatch.spi.ServiceRegistry.CONTAINER_ARTIFACT_FACTORY_SERVICE=com.ibm.jbatch.container.services.impl.DelegatingBatchArtifactFactoryImpl"

#------------------
# SIGNATURE TESTS
# -----------------

cd $TCK_HOME_DIR

API_JAR=$TCK_HOME_DIR/tckdir/prereqs/jakarta.batch-api-2.0.0-M6.jar

IMPL_PATH=$TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/lib/jakarta.enterprise.cdi-api-3.0.0-M4.jar\
:$TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/lib/jakarta.inject-api-2.0.0-RC4.jar

#------------------------------------------------
# Run Java 8 SigTest portion
# -----------------------------------------------

$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/artifacts/batch.standalone.tck.sig_2.0_se8   -classpath $API_JAR:$JAVA_HOME/jre/lib/rt.jar:$IMPL_PATH

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
IMPL_PATH=$TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/lib/jakarta.inject-api-2.0.0-RC4.jar

$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/artifacts/batch.standalone.tck.sig_2.0_se8   -classpath $API_JAR:$JAVA_HOME/jre/lib/rt.jar:$IMPL_PATH
echo
echo ---------------------
echo done expected failure
echo ---------------------
echo

#---------------
# Java 11
#---------------
#cd $TCK_HOME_DIR
#rm -rf sigtest; mkdir -p sigtest/jimage
#ls -la .
#JDK11_CLASSES=$TCK_HOME_DIR/sigtest/jimage
#cd $JDK11_CLASSES
#
## Extract here using `jimage extract`
#$JAVA_HOME/bin/jimage extract $JAVA_HOME/lib/modules
# Java 11
#$JAVA_HOME/bin/java -jar $TCK_HOME_DIR/tckdir/prereqs/sigtestdev-3.0-b12-v20140219.jar   SignatureTest -static -package jakarta.batch -filename  $TCK_HOME_DIR/tckdir/prereqs/jakarta.batch.official.tck-2.0.0-M4/artifacts/batch.standalone.tck.sig_2.0_se11  -classpath $API_JAR:$JDK11_CLASSES/java.base:$IMPL_PATH



# ------------------------------------------------------------------------------









#------------------------------------------
# Run SigTest forcing error (not strictly
# necessary, but the signature testing is
#------------------------------------------
echo
echo -------------------------------------------
echo expecting failure to show tests are working
echo -------------------------------------------
echo
$JAVA_HOME/bin/java -jar ../sigtestdev-3.0-b12-v20140219.jar SignatureTest -static -package jakarta.batch \
-filename artifacts/batch-api-sigtest-java8.sig \
-classpath ../jakarta.batch-api-2.0.0-M6.jar:$JAVA_HOME/jre/lib/rt.jar:lib/jakarta.inject-api-1.0.jar
echo
echo --------------------------------------------
echo done expecting failure,tests should work now
echo --------------------------------------------
echo


