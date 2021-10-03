#!/bin/bash
#Shell script to start a fresh geth chain

# Remove old chain data so we can start fresh
# Use the first set of lines if you are on Unix-based OS, and the second if you are on Windows. You will have to adjust the second set to reflect your own filepath

rm -rf ~/.ethereum/
rm -rf ~/.ethash/

#rm -rf C:/Users/jefft/AppData/Roaming/Ethereum
#rm -rf C:/Users/jefft/AppData/Ethash

# Check that geth is properly installed. If this does not print out geth version 1.8.11, things may not work properly.
geth version

# Create a new account for geth using the password in pwd.txt (you can change this to whatever you would like, but will have to change it in CedrtificateDBSC.java as well)
geth --password ./pwd.txt account new

# Initialize a new chain with the parameters specified in CustomGenesis.json
geth init ./CustomGenesis.json

# Start geth and begin mining
echo "Starting geth..."
geth --verbosity 3 --mine --nodiscover --maxpeers 1 --networkid 13 --rpc --rpcapi "db,eth,net,web3,personal" --rpccorsdomain "*" --targetgaslimit 800000000 console

# Wait for geth to generate the DAG before attempting to deploy anything to the chain. This might take a couple minutes, but probably not more than 5.

# Potentially helpful commands for the geth console:
# eth.defaultAccount = eth.accounts[0]
# personal.unlockAccount(eth.accounts[0],"dbmi")
# eth.getBlock("latest").gasLimit
