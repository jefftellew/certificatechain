package edu.ucsd.dbmi.certificates.utils;

import edu.ucsd.dbmi.certificates.beans.Certificate;
import edu.ucsd.dbmi.certificates.contract.CertificateDBSC;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class CertificateUtils {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    //==============================================================================
    // INSTANCE VARIABLES
    //==============================================================================

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    //==============================================================================
    // PUBLIC METHODS
    //==============================================================================

    /**
     * Prints a line to console with the [DEBUG] tag attached at the start and colors applied (if console supports)
     *
     * @param msg The message to print to the console
     */

    public static void debugOut(String msg) {
        System.out.println(ANSI_RED + "[DEBUG] " + ANSI_CYAN + msg + ANSI_RESET);
    }

    //==============================================================================

    /**
     * Prints out a variable and its value with the [DEBUG] tag
     * @param varName The name of the variable to print
     * @param varValue The value of the variable to print
     */

    public static void debugVarOut(String varName, String varValue) {
        System.out.println(ANSI_RED + "[DEBUG] " + ANSI_YELLOW + varName + ANSI_RESET + " = " + ANSI_PURPLE + varValue + ANSI_RESET);
    }

    //==============================================================================

    /**
     * Prints out a variable and its value with the [DEBUG] tag
     * @param varName The name of the variable to print
     * @param varValue The value of the variable to print
     */

    public static void debugVarOut(String varName, int varValue) {
        System.out.println(ANSI_RED + "[DEBUG] " + ANSI_YELLOW + varName + ANSI_RESET + " = " + ANSI_PURPLE + varValue + ANSI_RESET);
    }

    //==============================================================================

    /**
     * Prints a line to the console with the [STARTUP] tag and colors applied. Should be used to log the progress of the contract startup.
     *
     * @param state The startup state to print to the console
     */

    public static void startupStateOut(String state) {
        System.out.println(CertificateDBSC.ANSI_PURPLE + "[STARTUP] " + CertificateDBSC.ANSI_BLUE + state + CertificateDBSC.ANSI_RESET);
    }

    //==============================================================================

    /**
     * Prints out certificate information with the [DEBUG] tag
     *
     * @param email           The email to print
     * @param researcherID    The researcher ID to print
     * @param firstName       The first name to print
     * @param lastName        The last name to print
     * @param firstNameKey    The first name key to print
     * @param lastNameKey     The last name key to print
     * @param certificateType The certificate type to print
     * @param recordID        The record ID to print
     * @param courseName      The course name to print
     * @param endDate         The end date to print
     * @param pdf             The PDF bytes to print the length of
     */
    public static void debugCertificateOut(
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
    ) {
        debugVarOut("Contract Address", CertificateDBSC.getContractAddress());
        debugOut("=================================================================");
        debugVarOut("email", email);
        debugVarOut("researcherID", researcherID);
        debugVarOut("firstName", firstName);
        debugVarOut("lastName", lastName);
        debugVarOut("firstNameKey", firstNameKey);
        debugVarOut("lastNameKey", lastNameKey);
        debugVarOut("certificateType", certificateType);
        debugVarOut("recordID", recordID);
        debugVarOut("courseName", courseName);
        debugVarOut("endDate", String.valueOf(endDate));
        debugVarOut("pdf.length", pdf.length);
        debugOut("=================================================================");
    }

    //==============================================================================

    /**
     * Prints a line with a variable to the console with the [STARTUP] tag and specific variable colors preserved. Should be used to check on variables during startup.
     *
     * @param var   The name of the variable to print
     * @param value The value of the variable to print
     */

    public static void startupVarOut(String var, String value) {
        System.out.println(CertificateDBSC.ANSI_PURPLE + "[STARTUP] " + CertificateDBSC.ANSI_CYAN + var + CertificateDBSC.ANSI_RESET + value);
    }

    //==============================================================================

    /**
     * Removes duplicates from an ArrayList of objects. Should take O(N) time.
     *
     * @param list The list to remove duplicates from
     * @param <T>  The data type of the ArrayList (is template function)
     * @return An ArrayList of objects made from the passed list, but with no duplicates
     */

    // Function to remove duplicates from an ArrayList in O(N) time (from <a href="https://www.geeksforgeeks.org/how-to-remove-duplicates-from-arraylist-in-java/">GeeksforGeeks</a>)
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<T>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // Add the elements of set with no duplicates to the list
        list.addAll(set);

        // Return the list
        return list;
    }

    //==============================================================================

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    //==============================================================================

    public static String generateCertificateFilename(Certificate certificate) {
        return certificate.getFirstName() + "_" + certificate.getLastName() + "_" + certificate.getCertificateType() + "_" + certificate.getRecordID() + ".pdf";
    }
}