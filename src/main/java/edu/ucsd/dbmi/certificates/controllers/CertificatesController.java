package edu.ucsd.dbmi.certificates.controllers;

import edu.ucsd.dbmi.certificates.beans.Certificate;
import edu.ucsd.dbmi.certificates.beans.CertificateSubmission;
import edu.ucsd.dbmi.certificates.beans.QueryParameters;
import edu.ucsd.dbmi.certificates.contract.WebCertificateDB;
import edu.ucsd.dbmi.certificates.utils.CertificateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@Controller
@SessionAttributes({"certificateSubmission", "queryParameters"})
public class CertificatesController {

    WebCertificateDB webDB;

    //==============================================================================

    @ModelAttribute("certificateTypeAllValues")
    public String[] getMultiCheckboxAllValues() {
        return new String[] {
                "CITI","HIPAA"
        };
    }

    //==============================================================================

    @ModelAttribute("certificateSubmission")
    public CertificateSubmission certificateSubmission() {
        return new CertificateSubmission();
    }

    //==============================================================================

    @PostConstruct
    public void init() {
        webDB = new WebCertificateDB();
    }

    //==============================================================================
    // MAPPINGS
    //==============================================================================

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    //==============================================================================

    @PostMapping("/login")
    public String login_submit() {
        return "home";
    }

    //==============================================================================

    @GetMapping("/")
    public String root() {
        return "home";
    }

    //==============================================================================

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    //==============================================================================

    @GetMapping("/submit")
    public String submit(
            Model model,
            @ModelAttribute("certificateSubmission") CertificateSubmission certificateSubmission
    ) {
        model.addAttribute("certificateSubmission", new CertificateSubmission());
        return "submit";
    }

    //==============================================================================

    @PostMapping("/submit")
    public String submit_confirm(@ModelAttribute CertificateSubmission certificateSubmission) {

        byte[] pdfBytes = null;

        try {
            pdfBytes = certificateSubmission.getPdf().getBytes();
        } catch(IOException e) {
            e.printStackTrace();
        }

        certificateSubmission.setPdfBytes(pdfBytes);

        return "submit-confirm";
    }

    //==============================================================================

    @PostMapping("/submit-attempt")
    public String submit_attempt(@ModelAttribute CertificateSubmission confirmedSubmission) {

        boolean successful = webDB.addNewCertificateFromWeb(
            confirmedSubmission.getFirstName().toUpperCase(),
            confirmedSubmission.getLastName().toUpperCase(),
            confirmedSubmission.getEmail().toLowerCase(),
            confirmedSubmission.getCertificateTypeInt(),
            confirmedSubmission.getRecordID(),
            confirmedSubmission.getCourseName(),
            confirmedSubmission.getEndDate(),
            confirmedSubmission.getPdfBytes()
        );

        if(successful) {
            return "submit-success";
        } else {
            return "submit-error";
        }

    }

    //==============================================================================

    @GetMapping("/search")
    public String search_form(Model model) {
        model.addAttribute("queryParameters", new QueryParameters());
        return "search";
    }

    //==============================================================================

    @PostMapping("/search-results")
    public String search_results(
            Model model,
            @ModelAttribute("queryParameters") QueryParameters queryParameters
    ) {
        ArrayList<Certificate> result = queryFromQueryParameters(queryParameters);
        model.addAttribute("result", result);
        return "search-results";
    }

    //==============================================================================

    @RequestMapping(value = "/search-results/{recordID}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getFile(@PathVariable("recordID") String recordID) {

        byte[] temp = null;

        try {
            temp = webDB.getPDFBytes(Integer.parseInt(recordID));
        } catch(Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "certificate_" + recordID + ".pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(temp, headers, HttpStatus.OK);
        return response;
    }

    //==============================================================================
    // PRIVATE METHODS
    //==============================================================================

    private ArrayList<Certificate> queryFromQueryParameters(QueryParameters queryParameters) {

        boolean emailIsEmpty = queryParameters.getEmail().equals("");
        boolean firstNameIsEmpty = queryParameters.getFirstName().equals("");
        boolean lastNameIsEmpty = queryParameters.getLastName().equals("");
        boolean lowDateIsEmpty = queryParameters.getEndDateRangeLow().equals("");
        boolean highDateIsEmpty = queryParameters.getEndDateRangeHigh().equals("");

        // If all of the fields are empty, return everything in the database
        if(emailIsEmpty && firstNameIsEmpty && lastNameIsEmpty && lowDateIsEmpty && highDateIsEmpty) {
            CertificateUtils.debugOut("Getting all certificates...");
            CertificateUtils.debugVarOut("getCertificateCount()", webDB.getCertificateCount());
            System.out.println();
            return webDB.getAllCertificates();
        }

        // Otherwise, search according to the parameters

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Date rangeLow = null;
        Date rangeHigh = null;

        // If the low date field is not empty, try and parse the date
        if(!lowDateIsEmpty) {
            try {
                rangeLow = df.parse(queryParameters.getEndDateRangeLow());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        // If the high date field is not empty, try and parse the date
        if(!highDateIsEmpty) {
            try {
                rangeHigh = df.parse(queryParameters.getEndDateRangeHigh());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        //TODO: For some reason, dates earlier than 11/02/2020 have a good chance breaking the search function.
        // Because of this, I have made the last-minute decision to do some extra date handling in Java.
        // There is probably a nicer way of doing this, because now it is awkwardly split between the Solidity contract and this piece of code.

        //If all of the fields are empty except the date fields, just handle the dates manually in Java...
        if(firstNameIsEmpty && lastNameIsEmpty && emailIsEmpty && !lowDateIsEmpty && !highDateIsEmpty) {

            ArrayList<Certificate> certificates = webDB.getAllCertificates();
            Iterator<Certificate> iter = certificates.iterator();

            while(iter.hasNext()) {
                Certificate temp = iter.next();

                if(!rangeLow.before(temp.getEndDate())) {
                    iter.remove();
                }
                else if(rangeHigh.before(temp.getEndDate())) {
                    iter.remove();
                }
            }

            return certificates;
        }

        //TODO: These might actually never evaluate to true, but I don't have time to check at the moment
        // If you are a future student or intern and these look like gibberish to you, search for "Java ternary operator"
        String firstName = queryParameters.getFirstName() == (null) ? "" : queryParameters.getFirstName();
        String lastName = queryParameters.getLastName() == (null) ? "" : queryParameters.getLastName();
        String email = queryParameters.getEmail() == (null) ? "" : queryParameters.getEmail();

        ArrayList<Certificate> result = webDB.queryAllFromWeb(
                firstName.toUpperCase(),
                lastName.toUpperCase(),
                email.toLowerCase(),
                rangeLow,
                rangeHigh
        );

        return result;
    }

}
