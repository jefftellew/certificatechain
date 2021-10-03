# CertificateChain: Decentralized Healthcare Training Certificate Management System using Blockchain and Smart Contracts
Repo for our project CertificateChain. This project aims to create a blockchain-based database system to store certificates, as well as creating a java-based web app for users to interact with the system through.

# Authors

Jeff Tellew<sup>1</sup> and Tsung-Ting Kuo<sup>2</sup> †

`1` University of California Santa Barbara, Santa Barbara, CA, USA

`2` UCSD Health Department of Biomedical Informatics, University of California San Diego, La Jolla, CA, USA

`†` Corresponding Author

# Introduction

This repo contains all code for our project, CertificateChain. Source code as well as testing scripts used to automate our experiments are both contained within this repo. There is also code for the web app that we tested with other DBMI members at the same time. Lastly, there are some example certificate PDF files that were used during experiments.

# Contact

Thank you for using our software. If you have any questions or suggestions, please kindly contact Jeff Tellew (jefftellew at gmail dot com), University of California Santa Barbara, Santa Barbara, CA, USA.

# Installation
This section will go over installing all of the necessary components to run the web app.

These steps include commands that are specifically for a `BASH` shell and directory structures that match **Unix** directories. If you are on Windows, you will need to adjust accordingly.

## Installing geth

First, you will need to install geth. This can be done with the following steps.

1. Download geth 1.8.11, available from [here](https://geth.ethereum.org/downloads/). You should download the binary file, not the installer. If you are on Ubuntu, [this is a direct download link](https://gethstore.blob.core.windows.net/builds/geth-linux-amd64-1.8.11-dea1ce05.tar.gz).
4. Unpack the archive by running the command `tar xvzf geth-linux-amd64-1.8.11-dea1ce05.tar.gz`
5. Install geth by running the command `cp geth-linux-amd64-1.8.11-dea1ce05/geth /usr/bin/geth` and `cp geth-linux-amd64-1.8.11-dea1ce05/geth /usr/local/bin`
6. Run the command `geth version` to ensure that geth is properly installed.

## Installing ethereum

Next, you will need to install some things for Ethereum. This is just a sequence of commands.

1. `apt-get install software-properties-common`
2. `add-apt-repository -y ppa:ethereum/ethereum`
3. `add-apt-repository -y ppa:openjdk-r/ppa`
4. `apt-get update`
5. `apt-get install solc openjdk-8-jdk`

## Clone the repo

Finally, you will need to clone this repo. If you haven't already, and aren't sure how, see [GitHub's help page](https://help.github.com/en/articles/cloning-a-repository). You may need to install git using `apt-get install git` and maven using `apt-get install maven`

Once you're done with that, you're all ready to go!

# Launching the Web App on a Server
The process of launching the web app on a server like an AWS Virtual Machine should be fairly straightforward. This section will explain the steps to take in order to do so.

1. Follow the steps in the [Installation](#Installation) section to set everything up.
2. Change into the root directory of the repo, which should be called `dbmi-19`
3. Start geth by running the script `startGeth.sh`. If you are running this on a Unix machine, you can use `screen` to run geth in the background. This is accomplished by creating a screen, starting geth with the script, and then detaching from the screen. The commands to do this are `screen -S geth` followed by `./scripts/startGeth.sh`. Then, hit the keys `CTRL + a + d` OR enter the command `screen -d` to detach. If you are not able to use `screen` or simply don't want to, you will need to find another way to run the script in the background or you will not be able to keep the web app running.
4. Start the web app. Again, if you have access to it, you can use `screen` to run this in the background. This can be accomplished with the command `screen -S web-app` followed by the command `mvn spring-boot:run`. Once more, use the key sequence `CTRL + a + d` OR use the command `screen -d` to detach from the screen. You will need to be in the repo root directory where `pom.xml` is located, which should be `dbmi-19` unless something has been changed.

# Launching the Web App Locally
You can launch the web app locally either from the command line as a `.jar` or from within an IDE like IntelliJ. This section will briefly cover how to launch the web app using IntelliJ.

1. Follow the steps in the [Installation](#Installation) section to set everything up.
2. Open IntelliJ and create a new project from an existing source. From the menu bar at the top, this would be `File > New > Project from Existing Sources...`.
3. Navigate to the location you downloaded the repo to and enter the repo root directory.
4. Find the file named `pom.xml` and select it.
5. Click "OK" to finish creating the project.
6. Start geth by running `startGeth.sh`. You can either run it through the file explorer or through the terminal.
7. Run the web app by clicking the green arrow near the top right of the screen. If you can't find it, you can also run it from the menu bar at the top by navigating to `Run > Run 'CertificatesApplication'` or by using the keyboard shortcut (Shift + F10 on Windows, not sure on Mac or Linux)

# Switching between Windows and Unix-based OS
There are a few small changes that need to be made to change between a machine running Windows and one running a Unix-based OS like Linux or a Mac.

In particular, there are modifications that need to be made to two files:
* `startGeth.sh`
* `CertificateDBSC.java`

### `startGeth.sh`
1. Open `startGeth.sh` in your text editor of choice (found in `/dbmi-19/scripts/`)
2. Locate the lines near the top of the file that are marked with `*** UNIX ***` and `*** WINDOWS ***`
3. Make sure that the lines in the section for the OS you are using are uncommented, and that the lines in the section for the other OS are commented out.

### `CertificateDBSC.java`
1. Open `CertificateDBSC.java` in your text editor of choice (found in `/dbmi/src/main/java/edu/ucsd/dbmi/certificates/contract/`)
2. Locate the line near the top of the file in the `INSTANCE VARIABLES` section that defines the variable `private static String dir`
3. Make sure that the line marked with the OS you are using is uncommented, and that the line marked with the OS you are not using is commented out
