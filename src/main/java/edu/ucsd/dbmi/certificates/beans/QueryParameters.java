package edu.ucsd.dbmi.certificates.beans;

public class QueryParameters {

    private String firstName;
    private String lastName;

    private String email;

    private String endDateRangeLow;
    private String endDateRangeHigh;

    public QueryParameters() {
    }

    public QueryParameters(String firstName, String lastName, String email, String endDateRangeLow, String endDateRangeHigh) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.endDateRangeLow = endDateRangeLow;
        this.endDateRangeHigh = endDateRangeHigh;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndDateRangeLow() {
        return endDateRangeLow;
    }

    public void setEndDateRangeLow(String endDateRangeLow) {
        this.endDateRangeLow = endDateRangeLow;
    }

    public String getEndDateRangeHigh() {
        return endDateRangeHigh;
    }

    public void setEndDateRangeHigh(String endDateRangeHigh) {
        this.endDateRangeHigh = endDateRangeHigh;
    }
}
