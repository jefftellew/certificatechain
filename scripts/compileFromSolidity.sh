solc -o . --bin --abi CertificateDB.sol --overwrite
bin/web3j solidity generate CertificateDB.bin CertificateDB.abi -o . -p .
echo "$(tail -n +3 CertificateDB.java)" > CertificateDB.java
javac -cp .:lib/* CertificateDB.java
