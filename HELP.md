# What is this document?
This document is meant to provide some answers to questions you may have about the project files. It is not a definitive guide to everything about the project, but should help with some of the more important aspects of the project.

Instructions and commands generally assume that you are using a system with a BASH command line. The original project was mostly done on a machine running Ubuntu.

## Contents

1. Running an Experiment
2. About the Bash Scripts
3. About the Contract Files
4. About the Web App Files
5. Generating Javadocs

# Running an Experiment

This sections covers how to run an automated experiment like the ones that were used to generate the graphs in the R project. If you would like to modify the experiment parameters, see the file `Project_Documentation_7-12-2019.pdf` in the root of the repo. It also contains information on various other aspects of the project, but may be slightly outdated in different areas.

To run an experiment, enter the following commands in the terminal:

1. Change into the `results` directory.

    `cd /dbmi-19/results/`

2. Remove any previously generated result files. Back them up before you remove them if you want to save them!

    `rm *.*` 

3. Change into the scripts directory

    `cd /dbmi-19/scripts/`

4. Remove the logfile from any previously run screens. Back them up before you remove them if you want to save them!

    `rm screenlog.*`

5. Create a new screen

    `screen -S experiment -L`

6. Run the script

    `/dbmi-19/scripts/runExperiment.sh`

7. Detach from the screen

    `CTRL + a + d`

# About the Bash Scripts

The Bash scripts are mostly used for running experiments. However, they can also be used on their own for the various utilities they provide.

## `runExperiment.sh`

This script is the script you use to run experiments. It is fairly straightforward -- you run it and an experiment runs. It will compile and execute various Java files and some of the other Bash scripts. This script should not require any modification when switching between users or operating systems.

## `startGeth.sh`

This script is used to start a new geth chain. It will delete any old chain data if you use it, so be careful! This script requires modification to switch between Windows and other operating systems. You will also need to change the filepath if you are using it with Windows.

## `runTester.sh`

This script will run the file WebCertificateDBTester.java when it is executed. It requires no modification for use with different users and operating systems.

## `compileFromSolidity.sh`

This script is deprecated and does not work unless it is in the same directory as the Solidity contract and web3j's `.jar` files.

# About the Contract Files

There are two components to the contract:
1. The Solidity smart contract
2. The Java contract wrappers

This section will cover both of them.

## The Solidity Contract

The Solidity file that defines the smart contract is located in `dbmi-19/src/main/sol/` and is called `CertificateDB.sol`. The file is filled with comments that explain most of the methods and variables inside. The public functions deal only with submitting, searching, and downloading certificates. Modifications to this file require recompiling into Java using web3j.

## The Java Wrappers

There are several different Java files that all link together to make the Solidity contract usable from Java. All files are located in the `contract` package. They are as follows:
1. `CertificateDB.java`
2. `CertificateDBSC.java`
3. `WebCertificateDB.java`

The names are, in retrospect, not very clear, so this section will attempt to provide some insight into what each file actually does.

### `CertificateDB.java`

This is the Java file that web3j generates. You should not modify it unless you absolutely need to. If you modify `CertificateDB.sol`, you will need to recompile and replace this file.

### `CertificateDBSC.java`

This file bridges the gap between Java and the code that web3j generates. It deals with a lot of things that would make using the contract inconvenient otherwise. You should not be trying to create instances of this as an object. Rather, its purpose is to abstract a lot of the messy code away from the user.

If you add or remove methods from the contract, you will need to do the same here. This generally just consists of creating a method with the correct parameters, handling any messy conversions inside, and then calling the corresponding method in `CertificateDB.java`.

**You will need to modify this file if you switch between Windows and any other operating system. By default, it is configured for Ubuntu and other operating systems that share the same directory structure.**

### `WebCertificateDB.java`

This is the main file you will be interacting with. It is a Plain Old Java Object (POJO) and can be used as such. As was the case with `CertificateDBSC.java`, you will need to make modifications to this file if you add or remove methods. This is also the file that handles the IDs for the database.

# About the Web App Files

The web app files are located in several different packages. The most important one is the Spring controller, `CertificatesController.java`. It handles requests for the web app pages and some of the logic behind the queries and submissions. The rest of the files are pretty standard Spring stuff.

# Generating Javadocs

Using Maven, you can easily generate Javadocs for the project files that you can browse like a web page. Follow these instructions to generate Javadocs:

1. Navigate to the repo root `/dbmi-19/`
2. Run the Maven command `mvn javadoc:javadoc`
3. The files should be available somewhere in the `/dbmi-19/src/test/` directory. The console output should specify the exact location.
