#!/bin/bash
#Shell script to run the tester program

# Uncomment these lines if you are going to run without using runExperiment.sh
# cd ../
# mvn clean install -DskipTests
# cd scripts

TARGET_PATH="../target/classes/"
LIB_PATH="../src/main/java/lib/*"

echo -e "\e[31mRunning WebCertificateDBTester...\e[39m"
pwd
java -Xms2048m -Xmx8192m -cp "$TARGET_PATH:$LIB_PATH" edu.ucsd.dbmi.certificates.experiment.WebCertificateDBTester
