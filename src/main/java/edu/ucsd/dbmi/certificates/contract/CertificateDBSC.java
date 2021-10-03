package edu.ucsd.dbmi.certificates.contract;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsd.dbmi.certificates.beans.Certificate;
import edu.ucsd.dbmi.certificates.utils.CertificateUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertificateDBSC {

    //==============================================================================
    // INSTANCE VARIABLES
    //==============================================================================

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    //TODO: Probably shouldn't have the password hardcoded in the source code... Can be retrieved from /scripts/pwd.txt instead
    private final static String pw = "dbmi";

    //TODO: Comment out Windows version and uncomment Linux version if switching
    private static String dir = "/root/.ethereum/keystore/"; //LINUX
//    private static String dir = "C:\\Users\\jefft\\AppData\\Roaming\\Ethereum\\keystore\\"; //WINDOWS
    private static String path = "";

    private static BigInteger gasPrice = BigInteger.valueOf(18000000000L);
    private static BigInteger gasLimit = BigInteger.valueOf(40000000L); //40 million

    private final static int KILOBYTE_IN_BYTES = 1024;
    private final static int BYTE_SIZE_OF_PDF_SLICE = 30 * KILOBYTE_IN_BYTES;

    private static Admin admin;
    private static Credentials credentials;
    private static String address;

    private static CertificateDB contract;
    private static ObjectMapper objectMapper;

    //==============================================================================
    // PRIVATE METHODS
    //==============================================================================

    /**
     * Updates the path of the currently deployed chain so the contract can properly deploy itself
     * @throws FileNotFoundException If the chain data is not found in the proper directory, a FileNotFoundException will be thrown
     */

    private static void updatePath() throws FileNotFoundException {
        File f = new File(dir);
        String[] list = f.list();

        if (list.length < 1) {
            throw new FileNotFoundException("Could not find chain path");
        } else if (list.length > 1) {
            throw new FileNotFoundException("Too many chains in " + dir);
        }

        path = dir + list[0];
    }

    //==============================================================================

    /**
     * Uploads a PDF to the chain
     * @param recordID The record ID of the PDF to upload
     * @param pdf The actual byte data of the PDF
     * @throws Exception Throws an exception if there is an error uploading to the chain or the thread is interrupted while sleeping
     */

    private static void uploadFullPDF(int recordID, byte[] pdf) throws Exception {

        //Calculate the number of slices necessary to submit 32 KB at a time
        int numSlices = (int) Math.ceil(pdf.length / (double) BYTE_SIZE_OF_PDF_SLICE);

        //This is not necessary and is only here for debugging purposes
        double kbSizeOfPDF = pdf.length / (double) KILOBYTE_IN_BYTES;
        CertificateUtils.debugOut("Certificate " + recordID + " of size " + ANSI_GREEN + kbSizeOfPDF + " KB" + ANSI_CYAN + " will be processed in " + ANSI_GREEN + numSlices + " slices");
        System.out.println();

        //Keeps track of the position that each slices starts from within the full PDF bytes that we are submitting
        int currentRangeStart = 0;

        for(int i = 0; i < numSlices; i++) {

            CertificateUtils.debugOut("Adding slice " + ANSI_YELLOW + (i + 1) + ANSI_CYAN + " of " + ANSI_YELLOW + numSlices);

            //If there are enough bytes left to populate a full slice, make a full slice. If not, make a slice with whatever we have left.
            int size = (currentRangeStart + BYTE_SIZE_OF_PDF_SLICE < pdf.length) ? BYTE_SIZE_OF_PDF_SLICE : pdf.length - currentRangeStart - 1;

            CertificateUtils.debugVarOut("size", size);

            //Create the array for the slice with the proper size and copy the relevant data to the array
            byte[] temp = new byte[size];
            System.arraycopy(pdf, currentRangeStart, temp, 0, size);

            //Send transaction with the data as a payload and record the transaction address
            uploadPDFSliceAsync(recordID, i, temp);

            //Useful for debugging, but commented for now
//            String data = CertificateUtils.bytesToHex(temp);
//            CertificateUtils.debugVarOut("data = ", data.substring(0, 15) + "..." + data.substring(data.length() - 16));

            System.out.println();

            currentRangeStart += BYTE_SIZE_OF_PDF_SLICE;

        }
    }

    //==============================================================================

    /**
     * Uploads a slice of a PDF to the chain and returns the transaction address
     * @param recordID The ID of the certificate whose slice is being added
     * @param data The data payload
     * @return Returns the address of the transaction that is submitted
     */

    private static void uploadPDFSliceAsync(int recordID, int index, byte[] data) throws Exception {

        RemoteCall<TransactionReceipt> call = contract.addCertificateBytes(BigInteger.valueOf(recordID), BigInteger.valueOf(index), data);

        while(call == null) {
            CertificateUtils.debugOut("Attempting to re-create transaction call...");
            call = contract.addCertificateBytes(BigInteger.valueOf(recordID), BigInteger.valueOf(index), data);
            Thread.sleep(100);
        }

        call.sendAsync();
    }

    //==============================================================================
    // PROTECTED METHODS
    //==============================================================================

    /**
     * Deploys a new contract to the chain
     * @return The address of the deployed contract
     * @throws Exception If the contract is not deployed properly, an Exception will be thrown
     */

    protected static String deploy() throws Exception {

        //Update the path to the chain
        CertificateUtils.startupStateOut("Updating chain path...");
        System.out.println();
        updatePath();

        //Connect to the network
        CertificateUtils.startupStateOut("Connecting to network...");
        System.out.println();
        admin = Admin.build(new HttpService());
        CertificateUtils.startupVarOut("My Ethereum client version = ", admin.web3ClientVersion().send().getWeb3ClientVersion());
        CertificateUtils.startupVarOut("My net listening = ", "" + admin.netListening().send().getResult());
        CertificateUtils.startupVarOut("My net peer count = ", admin.netPeerCount().send().getResult());
        credentials = WalletUtils.loadCredentials(pw, path);

        System.out.println();

        final int POLLING_INTERVAL = 1000; //Poll once per second
        final int POLLING_ATTEMPTS = 120; //Will try 120 times to perform the transaction (try for ~2 minutes)
        FastRawTransactionManager fastRawTxMgr = new FastRawTransactionManager(admin, credentials, new PollingTransactionReceiptProcessor(admin, POLLING_INTERVAL, POLLING_ATTEMPTS));

        // Deploy
        CertificateUtils.startupStateOut("Deploying contract...");
        System.out.println();
//        contract = CertificateDB.deploy(admin, credentials, gasPrice, gasLimit).send(); //Commented for now because we want to use a faster transaction manager
        contract = CertificateDB.deploy(admin, fastRawTxMgr, gasPrice, gasLimit).send();
        CertificateUtils.startupVarOut("Contract Deploy valid = ", "" + contract.isValid());

        if (!contract.isValid()) {
            throw new Exception("Invalid contract deploy");
        }

        address = contract.getContractAddress();
        CertificateUtils.startupVarOut("Contract Deploy address = ", address);
        System.out.println();

        //Unlock the account
        CertificateUtils.startupStateOut("Unlocking account...");
        String accountAddress = admin.ethAccounts().send().getResult().get(0);
        PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(accountAddress, pw).send();
        if(personalUnlockAccount.accountUnlocked()) {
            CertificateUtils.startupVarOut("Account Unlocked = ", personalUnlockAccount.accountUnlocked().toString());
        }

        objectMapper = new ObjectMapper();
        objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

        return address;
    }

    //==============================================================================

    /**
     * Loads an existing contract from an address
     * @param addr The address to load a contract from
     * @return Returns true if the contract load is valid, and false if it is not
     * @throws Exception If the contract does not load properly, an Exception will be thrown
     */

    protected static boolean load(String addr) throws Exception {

        updatePath();

        // Load
        contract = CertificateDB.load(addr, admin, credentials, gasPrice, gasLimit);
        CertificateUtils.startupStateOut("Loading contract...");
        System.out.println();
        CertificateUtils.startupVarOut("Contract Load valid = ", "" + contract.isValid());

        objectMapper = new ObjectMapper();
        objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

        return contract.isValid();
    }

    //==============================================================================

    /**
     * Adds a new certificate to the mappings
     * Must be called with a custom gas limit because it will exceed the normal transaction gas limit
     * Estimated gas cost is usually about 1 million (1,000,000)
     *
     * @param researcherID The ID on the certificate to add to the mappings
     * @param firstName The first name on the certificate to add to the mappings
     * @param lastName The last name on the certificate to add to the mappings
     * @param certificateType The type of certificate (CITI or HIPAA)
     * @param recordID The unique record ID assigned to each certificate upon issuance
     * @param courseName The course name on the certificate to add to the mappings
     * @param endDate The end or expiry date on the certificate to add to the mappings
     * @param pdf The PDF to store in the byte form
     */

    protected static void addNewCertificate(
        String email,
        Integer researcherID,
        String firstName,
        String lastName,
        Integer firstNameKey,
        Integer lastNameKey,
        Integer certificateType,
        Integer recordID,
        String courseName,
        long endDate,
        byte[] pdf
    ) throws Exception {

        contract.addNewCertificate(
            email,
            BigInteger.valueOf(researcherID),
            firstName,
            lastName,
            BigInteger.valueOf(firstNameKey),
            BigInteger.valueOf(lastNameKey),
            BigInteger.valueOf(certificateType),
            BigInteger.valueOf(recordID),
            courseName,
            BigInteger.valueOf(endDate)
        ).sendAsync();

        //Add the PDF to the chain
        uploadFullPDF(recordID, pdf);
        CertificateUtils.debugOut("Added new certificate");
        CertificateUtils.debugCertificateOut(email, researcherID, firstName, lastName, firstNameKey, lastNameKey, certificateType, recordID, courseName, endDate, pdf);
        System.out.println();
    }

    //==============================================================================

    /**
     * Queries the database for certificates matching ALL of the criteria passed
     * If a field is not specified in the search, it will be passed as 0
     *
     * @param firstNameKey The first name key to check the certificate for
     * @param lastNameKey The last name key to check the certificate for
     * @param researcherID The ID to check the certificate for
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns a JSON string containing all of the certificates that match ALL of the given criteria (may include duplicates!)
     */

    protected static ArrayList<Certificate> queryAll(
        Integer firstNameKey,
        Integer lastNameKey,
        Integer researcherID,
        long endDateRangeLow,
        long endDateRangeHigh
    ) throws Exception {

        CertificateUtils.debugOut("Searching chain...");
        CertificateUtils.debugVarOut("query mode", "all");
        CertificateUtils.debugVarOut("address", address);
        CertificateUtils.debugOut("=================================================================");
        CertificateUtils.debugVarOut("firstNameKey", firstNameKey);
        CertificateUtils.debugVarOut("lastNameKey", lastNameKey);
        CertificateUtils.debugVarOut("researcherID", researcherID);
        CertificateUtils.debugVarOut("endDateRangeLow", String.valueOf(endDateRangeLow));
        CertificateUtils.debugVarOut("endDateRangeHigh", String.valueOf(endDateRangeHigh));
        CertificateUtils.debugOut("=================================================================");
        System.out.println();

        //Get a JSON string from the chain containing query results
        String resultString = contract.queryAll(
                BigInteger.valueOf(firstNameKey),
                BigInteger.valueOf(lastNameKey),
                BigInteger.valueOf(researcherID),
                BigInteger.valueOf(endDateRangeLow),
                BigInteger.valueOf(endDateRangeHigh)
        ).send();

        //Parse the JSON string into an ArrayList of Certificate objects and remove any duplicate results from the list
        ArrayList<Certificate> certificates = CertificateUtils.removeDuplicates(objectMapper.readValue(resultString, new TypeReference<List<Certificate>>() {}));

        for (Certificate certificate : certificates) {
            //This is pretty silly looking but we have to convert from seconds to milliseconds for Java
            certificate.setEndDate(new Date(certificate.getEndDate().getTime() * 1000));
        }

        return certificates;
    }

    //==============================================================================

    /**
     * Queries the database for certificates matching ANY of the criteria passed
     * If a field is not specified in the search, it will be passed as 0
     *
     * @param firstNameKey The first name key to check the certificate for
     * @param lastNameKey The last name key to check the certificate for
     * @param researcherID The ID to check the certificate for
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns a JSON string containing all of the certificates that match ANY of the given criteria (may include duplicates!)
     */

    protected static ArrayList<Certificate> queryAny(
        int firstNameKey,
        int lastNameKey,
        int researcherID,
        long endDateRangeLow,
        long endDateRangeHigh
    ) throws Exception {

        CertificateUtils.debugOut("Searching chain...");
        CertificateUtils.debugVarOut("query mode", "any");
        CertificateUtils.debugVarOut("address", address);
        CertificateUtils.debugOut("=================================================================");
        CertificateUtils.debugVarOut("firstNameKey", firstNameKey);
        CertificateUtils.debugVarOut("lastNameKey", lastNameKey);
        CertificateUtils.debugVarOut("researcherID", researcherID);
        CertificateUtils.debugVarOut("endDateRangeLow", String.valueOf(endDateRangeLow));
        CertificateUtils.debugVarOut("endDateRangeHigh", String.valueOf(endDateRangeHigh));
        CertificateUtils.debugOut("=================================================================");
        System.out.println();

        //Get a JSON string from the chain containing query results
        String resultString = contract.queryAny(
            BigInteger.valueOf(firstNameKey),
            BigInteger.valueOf(lastNameKey),
            BigInteger.valueOf(researcherID),
            BigInteger.valueOf(endDateRangeLow),
            BigInteger.valueOf(endDateRangeHigh)
        ).send();

        //Parse the JSON string into an ArrayList of Certificate objects and remove any duplicate results from the list
        ArrayList<Certificate> certificates = CertificateUtils.removeDuplicates(objectMapper.readValue(resultString, new TypeReference<List<Certificate>>() {}));

        for (Certificate certificate : certificates) {
            //This is pretty silly looking but we have to convert from seconds to milliseconds for Java
            certificate.setEndDate(new Date(certificate.getEndDate().getTime() * 1000));
        }

        return certificates;
    }

    //==============================================================================

    protected static ArrayList<Certificate> getAllCertificates() throws Exception {
        String resultString = contract.getAllCertificates().send();

        ArrayList<Certificate> certificates = CertificateUtils.removeDuplicates(objectMapper.readValue(resultString, new TypeReference<List<Certificate>>() {}));

        for (Certificate certificate : certificates) {
            //This is pretty silly looking but we have to convert from seconds to milliseconds for Java
            certificate.setEndDate(new Date(certificate.getEndDate().getTime() * 1000));
        }

        return certificates;
    }

    //==============================================================================

    /**
     * Retrieves the full PDF byte array from the chain in slices and combines it into one byte array
     * @param recordID The PDF record ID for which the bytes should be retrieved
     * @return Returns a byte array containing the full PDF data for the specified record ID
     * @throws Exception Throws an Exception if there is a problem retrieving the certificate bytes from the chain
     */

    protected static byte[] getPDFBytes(int recordID) throws Exception {

        CertificateUtils.debugOut("Retrieving bytes for certificate " + ANSI_YELLOW + recordID);

        //Retrieve the number of slices that the PDF is stored in so we can determine how many calls to make to retrieve the actual byte slices
        int numSlices = contract.getCertificateBytesLength(BigInteger.valueOf(recordID)).send().intValue();

        CertificateUtils.debugVarOut("numSlices", numSlices);
        System.out.println();

        //Create an ArrayList to hold the byte arrays so we can determine how long the final length will be before combining them
        ArrayList<byte[]> bytes = new ArrayList<>();
        int totalLength = 0;

        //Retrieve the actual PDF slices from the chain
        for(int i = 0; i < numSlices; i++) {
            CertificateUtils.debugOut("Retrieving slice " + ANSI_YELLOW + (i + 1) + ANSI_CYAN + " of " + ANSI_YELLOW + numSlices);

            byte[] temp = contract.getCertificateBytes(BigInteger.valueOf(recordID), BigInteger.valueOf(i)).send();

            totalLength += temp.length;

            CertificateUtils.debugVarOut("size", temp.length);

            String data = CertificateUtils.bytesToHex(temp);
            CertificateUtils.debugVarOut("data = ", data.substring(0, 15) + "..." + data.substring(data.length() - 16));
            System.out.println();

            bytes.add(temp);
        }

        //Create the final file byte array with the length we found
        byte[] result = new byte[totalLength];

        //Keep track of the current position within the final byte array to copy the bytes to from the slice arrays
        int currentPosition = 0;

        //Copy the slices to the final array
        for (byte[] slice : bytes) {
            System.arraycopy(slice, 0, result, currentPosition, slice.length);
            currentPosition += slice.length;
        }

        CertificateUtils.debugOut("Finished retrieving bytes for certificate " + ANSI_YELLOW + recordID);
        CertificateUtils.debugVarOut("result.length", result.length);
        System.out.println();

        return result;
    }

    //==============================================================================

    /**
     * Checks to see if a researcher has any valid certificates stored on the chain
     *
     * @param researcherID The ID to look check for a valid certificate for
     * @return Returns true if the ID has a valid certificate, and false if it does not
     */

    protected static boolean hasValidCertificate(Integer researcherID) throws Exception {
        return contract.hasValidCertificate(BigInteger.valueOf(researcherID)).send();
    }

    //==============================================================================

    protected static int getCertificateCount() throws Exception {
        return contract.getCertificateCount().send().intValue();
    }

    //==============================================================================
    // PUBLIC METHODS
    //==============================================================================

    /**
     * Returns the address of the deployed contract
     * @return Returns the address of the deployed contract
     */

    public static String getContractAddress() {
        return address;
    }
}
