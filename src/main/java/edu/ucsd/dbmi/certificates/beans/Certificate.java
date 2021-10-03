package edu.ucsd.dbmi.certificates.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Certificate {

    enum CertificateType {CITI, HIPAA}

    private String email;
    private Integer researcherID;

    private String firstName;
    private String lastName;

    private Integer firstNameKey;
    private Integer lastNameKey;

    private CertificateType certificateType;
    private Integer recordID;

    private String courseName;

    private Date endDate;

    private byte[] pdf;

    public Certificate() {
        //Do nothing
    }

    public Certificate(Integer researcherID, String firstName, String lastName, Integer firstNameKey, Integer lastNameKey, Integer certificateType, Integer recordID, String courseName, long endDate, byte[] pdf) {
        this.researcherID = researcherID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.firstNameKey = firstNameKey;
        this.lastNameKey = lastNameKey;

        if (certificateType == 0) {
            this.certificateType = CertificateType.CITI;
        } else if (certificateType == 1) {
            this.certificateType = CertificateType.HIPAA;
        }

        this.recordID = recordID;
        this.courseName = courseName;

        //TODO: Make sure this isn't overflowing
        this.endDate = new Date(endDate * 1000L);

        this.pdf = pdf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getResearcherID() {
        return researcherID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getFirstNameKey() {
        return firstNameKey;
    }

    public Integer getLastNameKey() {
        return lastNameKey;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public Integer getRecordID() {
        return recordID;
    }

    public String getCourseName() {
        return courseName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public byte[] getPdf() {
        return pdf;
    }

    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certificate that = (Certificate) o;
        return researcherID.equals(that.researcherID) &&
                firstName.equals(that.firstName) &&
                lastName.equals(that.lastName) &&
                firstNameKey.equals(that.firstNameKey) &&
                lastNameKey.equals(that.lastNameKey) &&
                certificateType == that.certificateType &&
                recordID.equals(that.recordID) &&
                courseName.equals(that.courseName) &&
                endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(researcherID, firstName, lastName, firstNameKey, lastNameKey, certificateType, recordID, courseName, endDate);
    }

    @Override
    public String toString() {
        return "Certificate " + recordID + " for researcher " + firstName + " " + lastName + " (ID = " + researcherID + ") for course " + courseName;
    }

    public String getEndDateFormatted() {
        return new SimpleDateFormat("MM-dd-yyyy").format(endDate);
    }
}