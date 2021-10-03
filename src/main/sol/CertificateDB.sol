/**
 * This contract acts as a database to keep track of certificates such as CITI training certificates
 *
 * @author Jeffrey Tellew
 * @version 2.1.0
 * @since 6/25/2019
 */

pragma solidity ^0.5.0;

contract CertificateDB {

    //==============================================================================
    // DATA STRUCTURES
    //==============================================================================

    /**
     * Keeps track of the different mappings in the contract
     * Currently only used for addCertificateToMap()
     */
    enum Map {
        RESEARCHER_ID,
        END_DATE,
        FIRST_NAME,
        LAST_NAME
    }

    //==============================================================================

    /**
     * Enum to keep track of the different types of certificates that the database can store
     * Currently supports CITI and HIPAA certificates
     */
    enum CertificateType {
        CITI,
        HIPAA
    }

    //==============================================================================

    //TODO: Add validation field

    /**
     * A struct to represent a certificate of some type
     */
    struct Certificate {
        //Currently supports 16-bit (65535) IDs, and could probably get away with less.
        //This is because internally, solidity will create a table big enough to hold ***every single possible key value*** and initialize each bucket to the default value type (Certificate in this case)

        string email;
        uint16 researcherID;

        string firstName;
        string lastName;

        uint firstNameKey;
        uint lastNameKey;

        CertificateType certificateType;
        uint recordID;
        string courseName;
        uint endDate;
    }

    //==============================================================================

    struct CertificateBytes {
        mapping(uint=>bytes) rawBytes;
        uint certificateBytesLength;
        uint recordID;
    }

    //==============================================================================

    /**
     * A struct to represent an array of certificates
     * Necessary to keep track of the length of each certificate array at each bucket in the mapping
     * EDIT: Not necessary for keeping track of length since the array has a `.length` member... which means it is not necessary at all. Remove? Keep in case we want to add more fields?
     */
    struct CertificateArray {
        Certificate[] certificates;
        uint certificatesLength;
    }

    //==============================================================================
    // INSTANCE VARIABLES
    //==============================================================================

    //A year converted to seconds
    //Note that this is for a 365 day year, when a year is technically 365.24 days
    //Currently not used because the startDate field is not being used
    //uint internal constant YEAR_IN_SECONDS = 31536000;

    //The maximum possible researcher ID if the ID is an unsigned 16-bit integer
    uint16 internal constant MAX_ID = 65535;

    //TODO: See if this is producing a truncation
    //The maximum possible epoch time if the time is the standard unsigned 256-bit integer
    //Currently, the maximum date falls in the year 2038
    uint internal constant MAX_EPOCH_DATE = (2**256) - 1;

    uint8 internal constant NUMBER_OF_MAPS = 4;

    //Maps the certificates based on the researcher ID given
    //This is meant to enable easy searches by researcher ID
    mapping(uint => CertificateArray) mapByResearcherID;

    //Maps the certificates based on the end date given
    //This is meant to enable easy searches by end date
    mapping(uint => CertificateArray) mapByEndDate;

    //Maps the certificates based on the first name given
    //This is meant to enable easy searches by first name
    mapping(uint => CertificateArray) mapByFirstNameKey;

    //Maps the certificates based on the last name given
    //This is meant to enable easy searches by last name
    mapping(uint => CertificateArray) mapByLastNameKey;

    //Maps the addresses of the transactions that store the actual certificate bytes
    mapping(uint => CertificateBytes) mapCertificateBytes;

    uint16[] usedResearcherIDs;

    uint certificateCount = 0;

    //FOR TESTING
    //    string stringData;
    //    uint uintData;
    //    uint stackCount = 0;

    //==============================================================================
    // PRIVATE HELPER FUNCTIONS
    //==============================================================================

    /**
     * Private helper function to determine whether or not a researcher ID is valid
     * @param ID The researcher ID to check for validity
     * @return Returns true if the researcher ID is valid (between 0 and the maximum researcher ID value), and false if it is not
     */

    function isValidID(uint16 ID) internal pure returns(bool) {
        return ID <= MAX_ID;
    }

    //==============================================================================

    /**
     * Private helper function to determine whether or not a date is valid
     * @param date The date to check for validity
     * @return Returns true if the date is valid (between 0 and the maximum epoch date value), and false if it is not
     */

    function isValidEpochDate(uint date) internal pure returns(bool) {
        return date <= MAX_EPOCH_DATE;
    }

    //==============================================================================

    //TODO: Add a check to make sure that the certificate is a valid object before attempting to access the endDate field
    //      There is not really a great way to do this in the current version of solidity unfortunately

    /**
     * Checks to see if a certificate is valid or if it has expired
     * @param certificate The certificate to check the validity of
     * @return Returns true if the certificate is valid, and false if it is not
     */
    function isValidCertificate(Certificate memory certificate) internal view returns(bool) {
        require(isValidEpochDate(certificate.endDate));    //Require that endDate is a valid epoch date

        return certificate.endDate > now;
    }

    //==============================================================================

    /**
     * Private helper function to add a certificate to a mapping
     * @param map The map to add the certificate to
     * @param certificate The certificate to add to the specified map
     */

    function addCertificateToMap(uint map, Certificate memory certificate) internal {
        if(map == uint(Map.RESEARCHER_ID)) {
            require(isValidID(certificate.researcherID)); //Require that the researcher ID is a valid ID

            if(mapByResearcherID[certificate.researcherID].certificatesLength == 0) {
                usedResearcherIDs.push(certificate.researcherID);
            }

            mapByResearcherID[certificate.researcherID].certificates.push(certificate);
            mapByResearcherID[certificate.researcherID].certificatesLength++;
        }
        else if(map == uint(Map.END_DATE)) {
            require(isValidEpochDate(certificate.endDate));    //Require that endDate is a valid epoch date

            mapByEndDate[certificate.endDate].certificates.push(certificate);
            mapByEndDate[certificate.endDate].certificatesLength++;
        }
        else if(map == uint(Map.FIRST_NAME)) {
            mapByFirstNameKey[certificate.firstNameKey].certificates.push(certificate);
            mapByFirstNameKey[certificate.firstNameKey].certificatesLength++;
        }
        else if(map == uint(Map.LAST_NAME)) {
            mapByLastNameKey[certificate.lastNameKey].certificates.push(certificate);
            mapByLastNameKey[certificate.lastNameKey].certificatesLength++;
        }
    }

    //==============================================================================

    /**
     * Private helper function to concatenate two strings
     * @param a The first string to concatenate. Will be the first part of the final string.
     * @param b The second string to concatenate. Will be appended to the first string to produce the final result.
     * @return Returns the concatenation of the two strings passed as parameters, with the second string (b) appended to the first string (a).
     */

    function concat(string memory a, string memory b) internal pure returns (string memory) {
        return string(abi.encodePacked(bytes(a), bytes(b)));
    }

    //==============================================================================

    /**
     * Private helper function to convert a uint to a string
     * @param v The uint to convert to a string
     * @return Returns a string corresponding to the passed uint
     */

    function uintToString(uint v) internal pure returns (string memory) {
        bytes memory reversed = new bytes(100);
        uint i = 0;
        while (v != 0) {
            uint remainder = v % 10;
            v = v / 10;

            reversed[i++] = byte(48 + uint8(remainder % 10));
        }
        bytes memory s = new bytes(i); // i + 1 is inefficient
        for (uint j = 0; j < i; j++) {
            s[j] = reversed[i - j - 1]; // to avoid the off-by-one error
        }
        string memory str = string(s);  // memory isn't implicitly convertible to storage
        return str;
    }

    //==============================================================================

    /**
     * Private helper function to round a given Unix timestamp to the most recent midnight
     * @param timestamp The Unix timestamp to round
     * @return Returns the timestamp rounded to the most recent midnight
     */

    function roundToMidnight(uint timestamp) internal pure returns (uint) {
        uint numSecondsInDay = 86400;

        //Will round down since Solidity uses integer division. This is equivalent to saying floor(now / numSecondsInDay).
        uint daysSinceEpoch = timestamp / numSecondsInDay;

        //Multiply the number of full days that have passed by the number of seconds in a day to get the timestamp at midnight of the current day
        return daysSinceEpoch * numSecondsInDay;
    }

    //==============================================================================

    /**
     * Private helper function to produce a string in the form of a JSON field
     * @param fieldName The name of the field to write to the JSON string
     * @param fieldValue The value of the corresponding field to write to the JSON string
     * @return Returns a string in JSON form with the name passed as fieldName and the value passed as fieldValue
     */

    function toJSONField(string memory fieldName, string memory fieldValue) internal pure returns(string memory) {
        string memory temp = "\"";
        temp = concat(temp, fieldName);
        temp = concat(temp, "\" : \"");
        temp = concat(temp, fieldValue);
        temp = concat(temp, "\"");

        return temp;
    }

    //==============================================================================

    /**
     * Private helper function to convert a Certificate to a JSON string
     * @param researcherID The ID on the certificate
     * @param firstName The first name on the certificate
     * @param lastName The last name on the certificate
     * @param certificateType The type of certificate (CITI or HIPAA)
     * @param recordID The unique record ID assigned to each certificate upon issuance
     * @param courseName The course name on the certificate
     * @param endDate The end or expiry date on the certificate
     * @return Returns a string of the Certificate in JSON
     */

    function certificateToJSON(
        string memory email,
        uint16 researcherID,
        string memory firstName,
        string memory lastName,
        uint firstNameKey,
        uint lastNameKey,
        CertificateType certificateType,
        uint recordID,
        string memory courseName,
        uint endDate
    )
    internal pure returns(string memory)
    {
        //string memory newField = ",\n\t"; //Uncommenting this will result in an error that says "Stack to deep"
        string memory temp = "{\n\t";

        temp = concat(temp, toJSONField("email", email));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("researcherID", uintToString(researcherID)));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("firstName", firstName));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("lastName", lastName));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("firstNameKey", uintToString(firstNameKey)));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("lastNameKey", uintToString(lastNameKey)));

        temp = concat(temp, ",\n\t");

        if(certificateType == CertificateType.CITI) {
            temp = concat(temp, toJSONField("certificateType", "0"));
        } else {
            temp = concat(temp, toJSONField("certificateType", "1"));
        }

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("recordID", uintToString(recordID)));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("courseName", courseName));

        temp = concat(temp, ",\n\t");
        temp = concat(temp, toJSONField("endDate", uintToString(endDate)));

        temp = concat(temp, "\n}");

        return temp;
    }

    //==============================================================================

    /**
     * Private helper function to convert a CertificateArray struct into a JSON string
     * @param certificateArray The CertificateArray to convert to JSON form
     * @return Returns a string of the parameter certificateArray in JSON form
     */

    function certificateArrayToJSON(CertificateArray memory certificateArray) internal pure returns(string memory) {

        string memory temp = "[";

        if(certificateArray.certificatesLength > 0) {

            //Add first certificate outside loop to avoid fencepost problem

            Certificate memory currentCertificate = certificateArray.certificates[0];

            temp = concat(temp, certificateToJSON(
                    currentCertificate.email,
                    currentCertificate.researcherID,
                    currentCertificate.firstName,
                    currentCertificate.lastName,
                    currentCertificate.firstNameKey,
                    currentCertificate.lastNameKey,
                    currentCertificate.certificateType,
                    currentCertificate.recordID,
                    currentCertificate.courseName,
                    currentCertificate.endDate
                ));

            //Add the rest of the certificates (if there are any)

            for(uint i = 1; i < certificateArray.certificatesLength; i++) {
                temp = concat(temp, ", ");

                currentCertificate = certificateArray.certificates[i];

                temp = concat(temp, certificateToJSON(
                        currentCertificate.email,
                        currentCertificate.researcherID,
                        currentCertificate.firstName,
                        currentCertificate.lastName,
                        currentCertificate.firstNameKey,
                        currentCertificate.lastNameKey,
                        currentCertificate.certificateType,
                        currentCertificate.recordID,
                        currentCertificate.courseName,
                        currentCertificate.endDate
                    ));
            }
        }

        temp = concat(temp, "]");

        return temp;
    }

    //==============================================================================

    /**
     * Checks a certificate to see if it matches ALL search parameters
     * Unspecified parameters will be passed as 0
     * @param firstNameKey The first name key to check the certificate for
     * @param lastNameKey The last name key to check the certificate for
     * @param researcherID The ID to check the certificate for
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns true if a certificate matches all of the specified criteria, and false if it does not
     */

    function matchesCriteria(uint firstNameKey, uint lastNameKey, uint16 researcherID, uint endDateRangeLow, uint endDateRangeHigh, Certificate memory certificate) internal pure returns(bool) {
        if(firstNameKey != 0 && firstNameKey != certificate.firstNameKey) {
            return false;
        }
        else if(lastNameKey != 0 && lastNameKey != certificate.lastNameKey) {
            return false;
        }
        else if(researcherID != 0 && researcherID != certificate.researcherID) {
            return false;
        }
        else if(endDateRangeLow != 0 && endDateRangeLow > certificate.endDate) {
            return false;
        }
        else if(endDateRangeHigh != 0 && endDateRangeHigh < certificate.endDate) {
            return false;
        }

        return true;
    }

    //==============================================================================

    /**
     * Looks up an array of  certificates by ID in the mapByResearcherID mapping
     * @param researcherID The ID to check the certificate for
     * @return Returns the certificates at the bucket for the ID passed
     */

    function getCertificatesByResearcherID(uint16 researcherID) internal view returns(CertificateArray memory) {
        require(isValidID(researcherID)); //Require that the ID is a valid ID

        return mapByResearcherID[researcherID];
    }

    //==============================================================================

    /**
     * Looks up an array of certificates by end date in the mapByEndDate mapping
     * @param endDate The end date to check the certificate for
     * @return Returns an array of certificates at the bucket for the end date passed
     */

    function getCertificatesByEndDate(uint endDate) internal view returns(CertificateArray memory) {
        require(isValidEpochDate(endDate));    //Require that endDate is a valid epoch date

        return mapByEndDate[endDate];
    }

    //==============================================================================

    //TODO: Account for leap-years when searching the next three years. Probably not really that important honestly, only causes an error of one day, but would be nice

    /**
     * Looks up an array of certificates by a range of dates in the mapByEndDate mapping
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns an array of certificates from the buckets for the range of ene dates passed.
     *          If no start date for the range is specified, returns certificates that expire between now (current block timestamp) and endDateRangeHigh.
     *          If no end date for the range is specified, returns certificates that expire between endDateRangeLow and three years from now, which is the latest possible expiry date at time of search.
     *          If both parameters are 0, returns certificates that expire between now and three years from now.
     */

    function getCertificatesByEndDateRange(uint endDateRangeLow, uint endDateRangeHigh) internal view returns(CertificateArray memory) {
        require(isValidEpochDate(endDateRangeLow) && isValidEpochDate(endDateRangeHigh));   //Require dates to be valid epoch dates
        require(endDateRangeLow < endDateRangeHigh || endDateRangeHigh == 0);   //Require start date to be earlier than end date, or end date can be unspecified

        uint numSecondsInDay = 86400;

        //If the start of the range is not specified, search for all certificates that expire between now and the given end of the range
        if(endDateRangeLow == 0) {
            return getCertificatesByEndDateRange(roundToMidnight(now), endDateRangeHigh);
        }

        //If the end of the range is not specified, search for all certificates that expire between the given start of the range and three years from now
        //This is done because a certificate expires at most three years after issuance, so to avoid checking all the way to the maximum epoch
        //  date, we just search until the latest current possible expiry time.
        if(endDateRangeHigh == 0) {
            return getCertificatesByEndDateRange(endDateRangeLow, roundToMidnight(now) + 94672800);
        }

        //Determine the number of results we are going to return
        //We have to do this because unfortunately, you cannot have a dynamic array in memory, and this array is required to be in memory
        //Yes, this is effectively like performing the search twice -- painful, but in this case, I have not figured out a way to get around it
        uint certificatesLengthTemp = 0;

        for(uint currentDate = endDateRangeLow; currentDate < endDateRangeHigh; currentDate += numSecondsInDay) {
            certificatesLengthTemp += getCertificatesByEndDate(currentDate).certificatesLength;
        }

        //Create the temporary array with the length that we found
        Certificate[] memory certificatesTemp = new Certificate[](certificatesLengthTemp);

        //Add the actual certificates to the array
        uint currentIndex = 0;

        for(uint currentDate = endDateRangeLow; currentDate < endDateRangeHigh; currentDate += numSecondsInDay) {
            CertificateArray memory tempArr = getCertificatesByEndDate(currentDate);
            for(uint i = 0; i < tempArr.certificatesLength; i++) {
                certificatesTemp[currentIndex] = tempArr.certificates[i];
                currentIndex++;
            }
        }

        return CertificateArray(certificatesTemp, certificatesLengthTemp);
    }

    //==============================================================================

    /**
     * Looks up an array of certificates by first name key in the mapByFirstNameKey mapping
     * @param firstNameKey The first name key to check the certificate for
     * @return Returns an array of certificates at the bucket for the first name key passed
     */

    function getCertificatesByFirstNameKey(uint firstNameKey) internal view returns(CertificateArray memory) {
        return mapByFirstNameKey[firstNameKey];
    }

    //==============================================================================

    /**
     * Looks up an array of certificates by last name key in the mapByFirstNameKey mapping
     * @param lastNameKey The last name key to check the certificate for
     * @return Returns an array of certificates at the bucket for the last name key passed
     */

    function getCertificatesByLastNameKey(uint lastNameKey) internal view returns(CertificateArray memory) {
        return mapByLastNameKey[lastNameKey];
    }

    //==============================================================================

    function bytesToBytes32(bytes memory b, uint offset) private pure returns (bytes32) {
        bytes32 out;

        for (uint i = 0; i < 32; i++) {
            out |= bytes32(b[offset + i] & 0xFF) >> (i * 8);
        }
        return out;
    }

    //==============================================================================
    // PUBLIC FUNCTIONS
    //==============================================================================

    /**
    * Adds a new certificate to the mappings
    * Must be called with a custom gas limit because it will exceed the normal transaction gas limit
    * Estimated gas cost is usually about 1 million (1,000,000)
    * @param researcherID The ID on the certificate to add to the mappings
    * @param firstName The first name on the certificate to add to the mappings
    * @param lastName The last name on the certificate to add to the mappings
    * @param certificateType The type of certificate (CITI or HIPAA)
    * @param recordID The unique record ID assigned to each certificate upon issuance
    * @param courseName The course name on the certificate to add to the mappings
    * @param endDate The end or expiry date on the certificate to add to the mappings
    */

    function addNewCertificate(
        string memory email,
        uint16 researcherID,
        string memory firstName,
        string memory lastName,
        uint firstNameKey,
        uint lastNameKey,
        CertificateType certificateType,
        uint recordID,
        string memory courseName,
        uint endDate
    )
    public {
        //Iterate over each Map enum value and add the certificate to the map
        for(uint8 i = 0; i < NUMBER_OF_MAPS; i++) {
            addCertificateToMap(i, Certificate(email, researcherID, firstName, lastName, firstNameKey, lastNameKey, certificateType, recordID, courseName, endDate));
        }

        CertificateBytes memory temp;
        mapCertificateBytes[recordID] = temp;
        mapCertificateBytes[recordID].recordID = recordID;

        certificateCount++;
    }

    //==============================================================================

    //TODO: Might not properly account for empty values right now (eg. If you receive 0 as a parameter for firstNameKey or something similar) - needs more testing

    /**
     * Queries the database for certificates matching ANY of the criteria passed
     * If a field is not specified in the search, it will be passed as 0
     * @param firstNameKey The first name key to check the certificate for
     * @param lastNameKey The last name key to check the certificate for
     * @param researcherID The ID to check the certificate for
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns a JSON string containing all of the certificates that match ANY of the given criteria (may include duplicates!)
     */

    function queryAny(
        uint firstNameKey,
        uint lastNameKey,
        uint16 researcherID,
        uint endDateRangeLow,
        uint endDateRangeHigh
    ) public view returns(string memory) {

        //Get all of the search results for each individual field
        CertificateArray memory firstNameKeyArray = getCertificatesByFirstNameKey(firstNameKey);
        CertificateArray memory lastNameKeyArray = getCertificatesByLastNameKey(lastNameKey);
        CertificateArray memory researcherIDArray = getCertificatesByResearcherID(researcherID);
        CertificateArray memory endDateArray = getCertificatesByEndDateRange(endDateRangeLow, endDateRangeHigh);

        uint tempLength = firstNameKeyArray.certificatesLength + lastNameKeyArray.certificatesLength + researcherIDArray.certificatesLength + endDateArray.certificatesLength;

        Certificate[] memory tempArray = new Certificate[](tempLength);

        uint currentIndex = 0;

        for(uint i = 0; i < firstNameKeyArray.certificatesLength; i++) {
            tempArray[currentIndex] = firstNameKeyArray.certificates[i];
            currentIndex++;
        }

        for(uint i = 0; i < lastNameKeyArray.certificatesLength; i++) {
            tempArray[currentIndex] = lastNameKeyArray.certificates[i];
            currentIndex++;
        }

        for(uint i = 0; i < researcherIDArray.certificatesLength; i++) {
            tempArray[currentIndex] = researcherIDArray.certificates[i];
            currentIndex++;
        }

        for(uint i = 0; i < endDateArray.certificatesLength; i++) {
            tempArray[currentIndex] = endDateArray.certificates[i];
            currentIndex++;
        }

        return certificateArrayToJSON(CertificateArray(tempArray, tempLength));
    }

    //==============================================================================

    //TODO: Clean this up! This is gross right now!

    /**
     * Queries the database for certificates matching ALL of the criteria passed
     * If a field is not specified in the search, it will be passed as 0
     * @param firstNameKey The first name key to check the certificate for
     * @param lastNameKey The last name key to check the certificate for
     * @param researcherID The ID to check the certificate for
     * @param endDateRangeLow The earliest end date to search from
     * @param endDateRangeHigh The latest end date to search from
     * @return Returns a JSON string containing all of the certificates that match ALL of the given criteria (may include duplicates!)
     */

    function queryAll(
        uint firstNameKey,
        uint lastNameKey,
        uint16 researcherID,
        uint endDateRangeLow,
        uint endDateRangeHigh
    ) public view returns(string memory) {

        uint tempLength;
        uint currentIndex = 0;

        CertificateArray memory firstNameKeyArray;
        CertificateArray memory lastNameKeyArray;
        CertificateArray memory researcherIDArray;
        CertificateArray memory endDateArray;

        //Get all of the search results for each individual field
        if(firstNameKey != 0) {
            firstNameKeyArray = getCertificatesByFirstNameKey(firstNameKey);
            tempLength = firstNameKeyArray.certificatesLength;

            Certificate[] memory tempArray = new Certificate[](tempLength);

            for(uint i = 0; i < firstNameKeyArray.certificatesLength; i++) {
                if(matchesCriteria(firstNameKey, lastNameKey, researcherID, endDateRangeLow, endDateRangeHigh, firstNameKeyArray.certificates[i])) {
                    tempArray[currentIndex] = firstNameKeyArray.certificates[i];
                    currentIndex++;
                }
            }

            return certificateArrayToJSON(CertificateArray(tempArray, tempLength));
        }
        else if(lastNameKey != 0) {
            lastNameKeyArray = getCertificatesByLastNameKey(lastNameKey);
            tempLength = lastNameKeyArray.certificatesLength;

            Certificate[] memory tempArray = new Certificate[](tempLength);

            for(uint i = 0; i < lastNameKeyArray.certificatesLength; i++) {
                if(matchesCriteria(firstNameKey, lastNameKey, researcherID, endDateRangeLow, endDateRangeHigh, lastNameKeyArray.certificates[i])) {
                    tempArray[currentIndex] = lastNameKeyArray.certificates[i];
                    currentIndex++;
                }
            }

            return certificateArrayToJSON(CertificateArray(tempArray, tempLength));
        }
        else if(researcherID != 0) {
            researcherIDArray = getCertificatesByResearcherID(researcherID);
            tempLength = researcherIDArray.certificatesLength;

            Certificate[] memory tempArray = new Certificate[](tempLength);

            for(uint i = 0; i < researcherIDArray.certificatesLength; i++) {
                if(matchesCriteria(firstNameKey, lastNameKey, researcherID, endDateRangeLow, endDateRangeHigh, researcherIDArray.certificates[i])) {
                    tempArray[currentIndex] = researcherIDArray.certificates[i];
                    currentIndex++;
                }
            }

            return certificateArrayToJSON(CertificateArray(tempArray, tempLength));
        }
        else if(endDateRangeLow != 0 || endDateRangeHigh != 0) {
            endDateArray = getCertificatesByEndDateRange(endDateRangeLow, endDateRangeHigh);
            tempLength = endDateArray.certificatesLength;

            Certificate[] memory tempArray = new Certificate[](tempLength);

            for(uint i = 0; i < endDateArray.certificatesLength; i++) {
                if(matchesCriteria(firstNameKey, lastNameKey, researcherID, endDateRangeLow, endDateRangeHigh, endDateArray.certificates[i])) {
                    tempArray[currentIndex] = endDateArray.certificates[i];
                    currentIndex++;
                }
            }

            return certificateArrayToJSON(CertificateArray(tempArray, tempLength));
        }

        return certificateArrayToJSON(CertificateArray(new Certificate[](0), 0));
    }

    //==============================================================================

    function getAllCertificates() public view returns(string memory) {

        if(getCertificateCount() == 0) {
            return "[]";
        }

        Certificate[] memory tempArray = new Certificate[](getCertificateCount());
        uint currentIndex = 0;

        //TODO: Make this faster! This is pretty inefficient and there must be a better way to do it but I don't have time to figure out one right now.
        // For every possible researcher ID, get the certificates
        for(uint16 i = 0; i < usedResearcherIDs.length; i++) {
            CertificateArray memory temp = getCertificatesByResearcherID(usedResearcherIDs[i]);
            for(uint j = 0; j < temp.certificates.length; j++) {
                tempArray[currentIndex] =  temp.certificates[j];
                currentIndex++;
            }
        }

        return certificateArrayToJSON(CertificateArray(tempArray, currentIndex));
    }

    //==============================================================================

    /**
     * Checks to see if a researcher has any valid certificates stored on the chain
     * @param researcherID The ID to look check for a valid certificate for
     * @return Returns true if the ID has a valid certificate, and false if it does not
     */

    function hasValidCertificate(uint16 researcherID) public view returns(bool) {
        require(isValidID(researcherID)); //Require that the ID is a valid ID

        bool valid = false;
        Certificate[] storage certificates = mapByResearcherID[researcherID].certificates;

        for(uint i = 0; i < mapByResearcherID[researcherID].certificatesLength; i++) {
            if(isValidCertificate(certificates[i])) {
                valid = true;
            }
        }

        return valid;
    }

    //==============================================================================

    /**
     * Adds a 30-kb block to the bytes mapping under the given record ID
     * @param recordID The record ID of the certificate from which the bytes come
     * @param data The actual byte data of the certificate PDF
     */

    function addCertificateBytes(uint recordID, uint index, bytes memory data) public {
        mapCertificateBytes[recordID].rawBytes[index] = data;
        mapCertificateBytes[recordID].certificateBytesLength++;
    }

    //==============================================================================

    function getCertificateBytes(uint recordID, uint index) public view returns(bytes memory) {
        return mapCertificateBytes[recordID].rawBytes[index];
    }

    //==============================================================================

    function getCertificateBytesLength(uint recordID) public view returns(uint) {
        return mapCertificateBytes[recordID].certificateBytesLength;
    }

    //==============================================================================

    function getCertificateCount() public view returns(uint) {
        return certificateCount;
    }

    //==============================================================================
    // TEST FUNCTIONS
    //==============================================================================

    function testByID(uint16 ID) public view returns(string memory) {
        CertificateArray memory arr = getCertificatesByResearcherID(ID);
        if(arr.certificates.length > 0) {
            Certificate memory currentCertificate = arr.certificates[0];
            return certificateToJSON(
                currentCertificate.email,
                currentCertificate.researcherID,
                currentCertificate.firstName,
                currentCertificate.lastName,
                currentCertificate.firstNameKey,
                currentCertificate.lastNameKey,
                currentCertificate.certificateType,
                currentCertificate.recordID,
                currentCertificate.courseName,
                currentCertificate.endDate
            );
        }
        else {
            return "Did not find any certificates";
        }
    }

    //    function setStringData(string memory str) public {
    //        stringData = str;
    //    }
    //
    //    function setUintData(uint x) public {
    //        uintData = x;
    //    }
    //
    //    function getStringData() public view returns(string memory) {
    //        return stringData;
    //    }
    //
    //    function getUintData() public view returns(uint) {
    //        return uintData;
    //    }


    //
    //    function getStackCount() public view returns(uint) {
    //        return stackCount;
    //    }
    //
    //    function incrStackCount() public {
    //        stackCount++;
    //    }

}
