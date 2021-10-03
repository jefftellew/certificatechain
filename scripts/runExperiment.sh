#!/bin/bash

# Use the following commands to run this script:

# TODO: Figure out a way to get screenlog.0 to go into the right place from inside the scripts folder
# cd /home/jtellew/dbmi-19/results/
# rm *.*
# cd /home/jtellew/dbmi-19/scripts/
# rm screenlog.0
# screen -S experiment -L
# /home/jtellew/dbmi-19/scripts/runExperiment.sh
# CTRL+a+d

# Old TIMEPATH was /home/jtellew/dbmi-19/results/time.txt
TIMEPATH="../results/time.txt"
TARGET_PATH="../target/classes/"

# Print out the start time of the experiment in the console
echo -e "\e[36m=============================================================================="
echo -e "\e[35mExperiment initiating at:\e[34m" > $TIMEPATH
date +"%Y-%m-%d %T" >> $TIMEPATH
echo -e "\e[35mExperiment initiating at:\e[34m"
date +"%Y-%m-%d %T"
echo -e "\e[36m==============================================================================\e[39m"

echo ""
echo -e "\e[34mCompiling ExperimentTrial.java...\e[39m"
echo ""

# Compile the files under the directory we are in
cd ../
mvn clean install -DskipTests

# Change back into scripts directory so we can initialize geth with custom settings
cd scripts

echo ""

# Loop to run the trial specified in ExperimentTrial.java several times (usually 30)
for trial in {1..30..1}
do
    echo -e "\e[31mRunning ExperimentTrial #$trial...\e[39m"
    echo ""

    # Record the start time of the trial in time.txt
    echo "==============================================================================" >> $TIMEPATH
    echo "Trial #$trial initiating at:"  >> $TIMEPATH
    date +"%Y-%m-%d %T" >> $TIMEPATH
    echo "==============================================================================" >> $TIMEPATH

    # Record the detailed time statistics in time.txt and run ExperimentTrial
    pwd
    command /usr/bin/time -v -o $TIMEPATH --append java -Xms2048m -Xmx8192m -cp $TARGET_PATH edu.ucsd.dbmi.certificates.experiment.ExperimentTrial

    sleep 5
    # Kill any processes listening on port 30303, which includes geth if you do not change the default port
    lsof -ti tcp:30303 | xargs kill -9
    sleep 5
done

sleep 5

# Print out the end time of the experiment in the console
echo -e "\e[36m=============================================================================="
echo -e "\e[35mCompleted experiment at:\e[34m" >> $TIMEPATH
date +"%Y-%m-%d %T" >> $TIMEPATH
echo -e "\e[35mCompleted experiment at:\e[34m"
date +"%Y-%m-%d %T"
echo -e "\e[35mType 'reset' in console to continue (not sure why it requires that still)."
echo -e "\e[36m==============================================================================\e[39m"
echo ""
