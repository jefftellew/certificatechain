package edu.ucsd.dbmi.certificates.beans;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CertificateSubmission {

    public enum CertificateType { CITI, HIPAA }

    private String email;
    private String firstName;
    private String lastName;

    private CertificateType certificateType;

    private int recordID;
    private String courseName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private MultipartFile pdf;
    private byte[] pdfBytes;

    public CertificateSubmission() {
    }

    public CertificateSubmission(String email, String firstName, String lastName, CertificateType certificateType, int recordID, String courseName, Date endDate, MultipartFile pdf) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.certificateType = certificateType;
        this.recordID = recordID;
        this.courseName = courseName;
        this.endDate = endDate;
        this.pdf = pdf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public Integer getCertificateTypeInt() {
        if(certificateType == CertificateType.CITI) {
            return 0;
        } else if(certificateType == CertificateType.HIPAA) {
            return 1;
        }
        return -1;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public MultipartFile getPdf() {
        return pdf;
    }

    public void setPdf(MultipartFile pdf) {
        this.pdf = pdf;
    }

    public byte[] getPdfBytes() {
        return pdfBytes;
    }

    public void setPdfBytes(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes;
    }

    public String getEndDateFormatted() {
        return new SimpleDateFormat("MM-dd-yyyy").format(endDate);
    }
}
