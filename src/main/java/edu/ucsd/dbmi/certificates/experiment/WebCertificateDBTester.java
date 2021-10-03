package edu.ucsd.dbmi.certificates.experiment;

import edu.ucsd.dbmi.certificates.contract.WebCertificateDB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;

public class WebCertificateDBTester {

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

	private final static String TAG_TEST = ANSI_GREEN + "[TEST] " + ANSI_RESET;
	private final static String TAG_EXCEPTION = ANSI_RED + "[EXCEPTION] " + ANSI_RESET;

	private final static long MEGABYTE = 1024L * 1024L;

	//==============================================================================

	private final static int NUM_TO_ADD = 100;
	private final static int ADD_INCREMENT = 1;
	private final static boolean PDF_ENABLED = true;

	private static String resultFilePath;
	private static WebCertificateDB webDB;

	//==============================================================================
    // MAIN METHOD
    //==============================================================================

	public static void main(String[] args) {

		//Initialize the WebCertificateDBTester and generate test file for output
		init();

		try {
			Path path = Paths.get(resultFilePath);

			long startTime = System.nanoTime();
			long endTime;

			for(int certificatesAdded = 0; certificatesAdded < NUM_TO_ADD; certificatesAdded += ADD_INCREMENT) {
				testAddCertificates(ADD_INCREMENT);
				endTime = System.nanoTime();

				String result = (certificatesAdded + ADD_INCREMENT) + "," + msTimeFormat((endTime - startTime) / 1000000) + ",NA,NA\n";

				Files.write(path, result.getBytes(), StandardOpenOption.APPEND);
			}
		}
		catch(IOException e) {
			System.out.println(TAG_EXCEPTION + "Result file not found. Result not written.");
		}
	}

	//==============================================================================
    // TESTING METHODS
    //==============================================================================

	/**
	 * Adds certificates to the chain and records some statistics about the process, including time and memory usage
	 * @param num The number of certificates to add to the chain
	 * @throws IllegalArgumentException The number of certificates cannot be negative
	 */

	private static void testAddCertificates(int num) throws IllegalArgumentException {
		if(num < 0) {
			throw new IllegalArgumentException("Cannot add negative number of certificates!");
		}

		printTestStart("testAddCertificates(" + num + ")");
		System.out.println();

		long start = System.nanoTime();

		for(int i = 0; i < num; i++) {
			addRandomCertificate();
		}

		long end = System.nanoTime();

		//TODO: Figure out a way to measure the actual memory used, not just the usage for Java objects
		long memory = getMemoryUsageMB();

		printTestResult("testAddCertificates(" + num + ")", start, end, memory);
		System.out.println();
	}

	//==============================================================================
    // PRIVATE HELPER METHODS
    //==============================================================================

	/**
	 * Initializes anything necessary for a new trial
	 */

	private static void init() {
		webDB = new WebCertificateDB();
		resultFilePath = generateResultFile();
	}

	//==============================================================================

	/**
	 * Generates a result file containing the data for a trial
	 * @return Returns the path to the generated result file
	 */

	private static String generateResultFile() {
		try {
			String header = "certificates_added,time,memory,gas\n0,00:00:00.000,NA,NA";

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			df.setTimeZone(TimeZone.getTimeZone("PST"));

			String timestamp = df.format(new Date());
			String pathString = "/home/jtellew/dbmi-19/results/test_add" + NUM_TO_ADD + "_" + timestamp + ".csv";

			Path path = Paths.get(pathString);
			Files.write(path, header.getBytes());

			return pathString;
		} catch(IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	//==============================================================================

	/**
	 * Private helper method to generate a random certificate and add it to the chain
	 */
	private static void addRandomCertificate() {
		String firstName;
		String lastName;
		String email;
		Integer certificateType;
		Integer recordID;
		String courseName;
		Date endDate;

		try {
			firstName = getRandomName();
			lastName = getRandomName();

			certificateType = getRandomNumberInRange(0, 1);
			recordID = getRandomNumberInRange(1000000, 9999999);

			courseName = getRandomCourseName();

			int endDateYear = getRandomNumberInRange(2019, 2021);
			int endDateMonth = getRandomNumberInRange(1, 12);
			int endDateDay = getRandomNumberInRange(1, 28);
			endDate = new Date(endDateYear, endDateMonth, endDateDay);

			email = firstName + lastName + recordID + "@ucsd.edu";

			System.out.println("Adding random certificate with parameters:" +
									"\n\tfirstName = " + firstName +
									"\n\tlastName = " + lastName +
									"\n\temail = " + email +
									"\n\tcertifificateType = " + certificateType +
									"\n\trecordID = " + recordID +
									"\n\tcourseName = " + courseName +
									"\n\tendDate = " + endDate
			);
			System.out.println();

			byte[] pdfBytes = null;

			if(PDF_ENABLED) {
				try {
					File pdf = new File("/home/jtellew/dbmi-19/src/main/resources/static/Jeff_Tellew_CITI_Certificate_Basic.pdf");
					pdfBytes = Files.readAllBytes(pdf.toPath());
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else {
				pdfBytes = new byte[0];
			}

			webDB.addNewCertificateFromWeb(firstName, lastName, email, certificateType, recordID, courseName, endDate, pdfBytes);

		} catch(IllegalArgumentException e) {
			System.out.println("Error generating random number");
		} catch(FileNotFoundException e) {
			System.out.println("Error generating random name");
		}
	}

	//==============================================================================

	/**
	 * Generates a random number between min and max
	 * @param min The lowest value of the range (inclusive)
	 * @param max The highest value of the range (inclusive)
	 * @return Returns a random integer between min and max (inclusive)
	 */
	private static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("Max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	//==============================================================================


	/**
	 * From https://stackoverflow.com/questions/2218005/how-to-get-a-random-line-of-a-text-file-in-java
	 * Picks a random name from the file of names
	 * Currently hardcoded to access a file of about 300,000 of the most popular names in the history of California
	 * Names were provided by the US Social Security Administration (https://www.ssa.gov/oact/babynames/limits.html)
	 */
	private static String getRandomName() throws FileNotFoundException {
		File f = new File("/home/jtellew/dbmi-19/src/main/resources/static/names.csv");
    	String result = null;
    	Random rand = new Random();
    	int n = 0;

    	for(Scanner sc = new Scanner(f); sc.hasNext();) {
        	String line = sc.nextLine();
        	if(rand.nextInt(++n) == 0)
        		result = line;
    	}

    	return result;
	}

	//==============================================================================

	/**
	 * Randomly provides one of ten possible dummy course names
	 */
	private static String getRandomCourseName() {
		int courseNum = getRandomNumberInRange(1, 10);

		switch(courseNum) {
			case 1:
				return "Intro Course 1";
			case 2:
				return "Basic Course 2";
			case 3:
				return "Beginner Course 3";
			case 4:
				return "Novice Course 4";
			case 5:
				return "Intermediate Course 5";
			case 6:
				return "Advanced Course 6";
			case 7:
				return "Expert Course 7";
			case 8:
				return "World Class Course 8";
			case 9:
				return "Galactic Leader Course 9";
			case 10:
				return "Universal Master Course 10";
			default:
				return "Course 9001";
		}
	}

	//==============================================================================

	/**
	 * Prints out a formatted line with the [TEST] tag and the test name
	 * Ex. [TEST] Starting test testAddCertificates(5)
	 */
	private static void printTestStart(String testName) {
		System.out.println(TAG_TEST + ANSI_CYAN + "Starting test " + ANSI_YELLOW + testName + ANSI_RESET);
	}

	//==============================================================================

	/**
	 * Takes a time in milliseconds and converts it to standard formatted time string
	 * @param ms The time in milliseconds to convert
	 * @return Returns ms formatted in a string of form HH:MM:SS.sss
	 */
	private static String msTimeFormat(long ms) {
		long millis = ms % 1000;
		long second = (ms / 1000) % 60;
		long minute = (ms / (1000 * 60)) % 60;
		long hour = (ms / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
	}

	//==============================================================================

	//TODO: Test this out more
	/**
	 * Calculates the memory used by a function
	 * @return Returns the memory used by the function in which this is called
	 */
	private static long getMemoryUsageMB() {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		return memory / MEGABYTE;
	}

	//==============================================================================

	/**
	 * Prints out a test result for a trial
	 * @param testName The name of the test
	 * @param startTime The start time of the test
	 * @param endTime The end time of the test
	 * @param memoryUsage The memory usage of the test
	 */

	private static void printTestResult(String testName, long startTime, long endTime, long memoryUsage) {
		System.out.println(TAG_TEST + ANSI_CYAN + "Test " + ANSI_YELLOW + testName
									+ ANSI_CYAN + " took " + ANSI_YELLOW + msTimeFormat((endTime - startTime) / 1000000)
									+ ANSI_CYAN + " and used " + ANSI_YELLOW + memoryUsage + " mb" + ANSI_RESET);
	}


}
