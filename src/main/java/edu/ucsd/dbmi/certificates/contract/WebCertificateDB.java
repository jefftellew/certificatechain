package edu.ucsd.dbmi.certificates.contract;

import edu.ucsd.dbmi.certificates.beans.Certificate;

import java.util.*;

public class WebCertificateDB implements WebCertificateDBInterface {

    //==============================================================================
    // INSTANCE VARIABLES
    //==============================================================================

    private final static int MAX_RESEARCHER_ID = 65535;
    private final static int MIN_RESEARCHER_ID = 10000;

    private HashMap<String, Integer> mapStringToInt;
    private HashMap<Integer, String> mapIntToString;

    //==============================================================================
    // CONSTRUCTORS
    //==============================================================================

    /**
     * Creates a new contract and deploys it to the chain to create a certificate database
     */

    public WebCertificateDB() {
        mapStringToInt = new HashMap<>();
        mapIntToString = new HashMap<>();

        try {
            CertificateDBSC.deploy();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //==============================================================================

    /**
     * Loads in an existing contract to create a certificate database
     * @param address The address of the currently deployed contract
     */

    public WebCertificateDB(String address) {
        mapStringToInt = new HashMap<>();
        mapIntToString = new HashMap<>();

        try {
            CertificateDBSC.load(address);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //==============================================================================
    // PRIVATE METHODS
    //==============================================================================

    /**
     * Generates a random ID within the bounds of the ID range specified by MIN_RESEARCHER_ID and MAX_RESEARCHER_ID
     * @return A random ID between MIN_RESEARCHER_ID and MAX_RESEARCHER_ID
     */

    private int getRandomID() {
        Random r = new Random();

        //Generate a random ID between MIN_RESEARCHER_ID (inclusive) and MAX_RESEARCHER_ID (inclusive)
        return r.nextInt(MAX_RESEARCHER_ID - MIN_RESEARCHER_ID + 1) + MIN_RESEARCHER_ID;
    }

    //==============================================================================

    /**
     * Initializes a numeric ID for a string value. If the value already has an ID, simply returns the existing value instead.
     * @param stringID The string to generate an ID for
     * @return Returns a new unique ID for a string key. If the string key already had a numeric ID, returns the existing ID instead.
     */

    private int initID(String stringID) {

        int numID;

        if(mapStringToInt.get(stringID) != null) {
            return mapStringToInt.get(stringID);
        }

        //Generate random number IDs until we find one that has not been used before
        do{
            numID = getRandomID();
        } while(mapIntToString.get(numID) != null);

        //Add the pair to the mappings
        mapStringToInt.put(stringID, numID);
        mapIntToString.put(numID, stringID);

        return numID;
    }

    //==============================================================================

    /**
     * Converts a java.util.Date to Unix time (time since midnight, January 1, 1970)
     * @param date The Java Date object to convert to Unix time
     * @return The Date passed converted to Unix time
     */

    private long dateToUnixTime(Date date) {
        return (long) date.getTime() / 1000;
    }

    //==============================================================================
    // PUBLIC METHODS
    //==============================================================================

    /**
     * Adds a new certificate to the database from the web app
     * @param firstName The first name of the researcher on the certificate
     * @param lastName The last name of the researcher on the certificate
     * @param email The email of the researcher on the certificate
     * @param certificateType The type of certificate (Currently CITI and HIPAA)
     * @param recordID The unique record ID for the certificate
     * @param courseName The name of the course that the certificate is for
     * @param endDate The expiry date of the certificate
     * @param pdf The actual PDF certificate, converted to a byte array
     */

    @Override
    public boolean addNewCertificateFromWeb(
            String firstName,
            String lastName,
            String email,
            Integer certificateType,
            Integer recordID,
            String courseName,
            Date endDate,
            byte[] pdf
    ) {

        try {
            //Create numeric IDs for the strings that need unique identifiers (email, first and last name)
            int researcherID = initID(email);
            int firstNameKey = initID(firstName);
            int lastNameKey = initID(lastName);

            //Call the smart contract to add the new Certificate to the blockchain
            CertificateDBSC.addNewCertificate(email, researcherID, firstName, lastName, firstNameKey, lastNameKey, certificateType, recordID, courseName, dateToUnixTime(endDate), pdf);

        } catch(Exception e) {
            int len = pdf == null ? 0 : pdf.length;
            System.out.println("pdf.length = " + len);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //==============================================================================

    /**
     * Allows the user to search the database for certificates associated with a certain first name, last name, email, and/or range of expiry dates.
     * Searches the database for certificate matching any of the parameters. That is, if the certificate matches one of more of the parameters, it will be returned in the search results.
     * @param firstName The first name to search the certificate database for
     * @param lastName The last name to search the certificate database for
     * @param email The email to search the certificate database for
     * @param endDateRangeLow The starting date of the range to search expiry dates for
     * @param endDateRangeHigh The ending date of the range to search expiry dates for
     * @return An ArrayList containing all of the certificates in the database that match any of the search parameters
     */

    @Override
    public ArrayList<Certificate> queryAnyFromWeb(
            String firstName,
            String lastName,
            String email,
            Date endDateRangeLow,
            Date endDateRangeHigh
    ) {

        int firstNameVerified = !firstName.equals("") && mapStringToInt.containsKey(firstName) ? mapStringToInt.get(firstName) : 0;
        int lastNameVerified = !lastName.equals("") && mapStringToInt.containsKey(lastName) ? mapStringToInt.get(lastName) : 0;
        int emailVerified = !email.equals("") && mapStringToInt.containsKey(email) ? mapStringToInt.get(email) : 0;
        long low = endDateRangeLow == null ? 0 : dateToUnixTime(endDateRangeLow);
        long high = endDateRangeHigh == null ? 0 : dateToUnixTime(endDateRangeHigh);

        try {
            //Call the query on the smart contract in order to get a JSON string containing our query results
            return CertificateDBSC.queryAny(firstNameVerified, lastNameVerified, emailVerified, low, high);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //==============================================================================

    /**
     * Allows the user to search the database for certificates associated with a certain first name, last name, email, and/or range of expiry dates.
     * Searches the database for certificate matching all of the parameters. That is, if and only if the certificate matches every single of the parameters, it will be returned in the search results.
     * @param firstName The first name to search the certificate database for
     * @param lastName The last name to search the certificate database for
     * @param email The email to search the certificate database for
     * @param endDateRangeLow The starting date of the range to search expiry dates for
     * @param endDateRangeHigh The ending date of the range to search expiry dates for
     * @return An ArrayList containing all of the certificates in the database that match every one of the search parameters
     */

    @Override
    public ArrayList<Certificate> queryAllFromWeb(
            String firstName,
            String lastName,
            String email,
            Date endDateRangeLow,
            Date endDateRangeHigh
    ) {

        int firstNameVerified = !firstName.equals("") && mapStringToInt.containsKey(firstName) ? mapStringToInt.get(firstName) : 0;
        int lastNameVerified = !lastName.equals("") && mapStringToInt.containsKey(lastName) ? mapStringToInt.get(lastName) : 0;
        int emailVerified = !email.equals("") && mapStringToInt.containsKey(email) ? mapStringToInt.get(email) : 0;
        long low = endDateRangeLow == null ? 0 : dateToUnixTime(endDateRangeLow);
        long high = endDateRangeHigh == null ? 0 : dateToUnixTime(endDateRangeHigh);

        try {
            //Call the query on the smart contract in order to get a JSON string containing our query results
            ArrayList<Certificate> result = CertificateDBSC.queryAll(firstNameVerified, lastNameVerified, emailVerified, low, high);

            Iterator<Certificate> iter = result.iterator();

            while(iter.hasNext()) {
                Certificate temp = iter.next();
                
                if(!firstName.equals("") && !firstName.equals(temp.getFirstName())) {
                    iter.remove();
                }
                else if(!lastName.equals("") && !lastName.equals(temp.getLastName())) {
                    iter.remove();
                }
                else if(!email.equals("") && !email.equals(temp.getEmail())) {
                    iter.remove();
                }
                else if(endDateRangeLow != null && !endDateRangeLow.before(temp.getEndDate())) {
                    iter.remove();
                }
                else if(endDateRangeHigh != null && endDateRangeHigh.before(temp.getEndDate())) {
                    iter.remove();
                }
            }

            return result;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //==============================================================================

    public ArrayList<Certificate> getAllCertificates() {
        try {
            return CertificateDBSC.getAllCertificates();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //==============================================================================

    /**
     * Retrieves the full PDF byte array from the chain in slices and combines it into one byte array
     * @param recordID The PDF record ID for which the bytes should be retrieved
     * @return Returns a byte array containing the full PDF data for the specified record ID
     * @throws Exception Throws an Exception if there is a problem retrieving the certificate bytes from the chain
     */

    @Override
    public byte[] getPDFBytes(int recordID) throws Exception {
        return CertificateDBSC.getPDFBytes(recordID);
    }

    //==============================================================================

    /**
     * Checks the database to see if the researcher with the passed ID has any valid certificates in the database.
     * @param researcherID The ID to check for valid certificates for
     * @return Returns true if the specified researcher has a valid certificate, and false if they do not
     */

    @Override
    public boolean hasValidCertificateFromWeb(String researcherID) {

        try {
            return CertificateDBSC.hasValidCertificate(mapStringToInt.get(researcherID));
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //==============================================================================

    public int getCertificateCount() {
        try {
            return CertificateDBSC.getCertificateCount();
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
