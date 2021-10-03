/**
 * This interface is used to interact with the Solidity contract written in CertificateDB.sol through CertificateDBSC.java
 *
 * @author Jeffrey Tellew
 * @version 1.0.0
 * @since 7/3/2019
 */

package edu.ucsd.dbmi.certificates.contract;

import edu.ucsd.dbmi.certificates.beans.Certificate;

import java.util.ArrayList;
import java.util.Date;

//https://www.baeldung.com/web3j

public interface WebCertificateDBInterface {

    boolean addNewCertificateFromWeb(
            String firstName,
            String lastName,
            String email,
            Integer certificateType,
            Integer recordID,
            String courseName,
            Date endDate,
            byte[] pdf
    );

    ArrayList<Certificate> queryAnyFromWeb(
            String firstName,
            String lastName,
            String email,
            Date endDateRangeLow,
            Date endDateRangeHigh
    );

    ArrayList<Certificate> queryAllFromWeb(
            String firstName,
            String lastName,
            String email,
            Date endDateRangeLow,
            Date endDateRangeHigh
    );

    boolean hasValidCertificateFromWeb(String researcherID);

    byte[] getPDFBytes(int recordID) throws Exception;
}
