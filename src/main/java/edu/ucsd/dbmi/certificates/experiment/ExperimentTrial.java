/**
 * Runs an experiment trial to test out the certificate system. This essentially boils down to running two scripts.
 * The first script will restart geth, wiping all old data for a fresh start. The second runs an actual test, written in WebCertificateDBTester.java.
 *
 * @author J. Tellew
 * @version 2.0
 */

package edu.ucsd.dbmi.certificates.experiment;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExperimentTrial {

    //Colors for printing to console
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    //Stuff for running the actual tests
    private final static int ONE_SECOND = 1000; //1000 ms
    private final static int NUM_THREADS = 2;
    private final static int GETH_START_TIME_SECONDS = 300; //If changed back to 180, it is possible that the DAG will not be generated before contract deployment, resulting in a faulty trial
    private final static boolean GETH_VERBOSITY_ENABLED = true;

    //Linux
    private final static String START_GETH_PATH = "/home/jtellew/dbmi-19/scripts/startGeth.sh";
    private final static String RUN_TESTER_PATH = "/home/jtellew/dbmi-19/scripts/runTester.sh";

    //Windows
//    private final static String START_GETH_PATH = "C:\\Users\\jefft\\Documents\\College\\DBMI 2019\\dbmi-19\\scripts\\startGeth.sh";
//    private final static String RUN_TESTER_PATH = "C:\\Users\\jefft\\Documents\\College\\DBMI 2019\\dbmi-19\\scripts\\runTester.sh";

    private final static String TAG_EXPERIMENT = ANSI_BLUE + "[EXPERIMENT] " + ANSI_RESET;
    private final static String TAG_SCRIPT = ANSI_YELLOW + "[SCRIPT] " + ANSI_RESET;

    //==============================================================================

    /**
     * A class to execute a shell script with Java
     */

    public static class ShellScript implements Callable {

        private String scriptPath;

        /**
         * Constructs a new ShellScript object with the path provided
         * @param path The path to the shell script file to be executed
         */

        public ShellScript(String path) {
            scriptPath = path;
        }

        @Override
        public Integer call() throws Exception {
            try {
                System.out.println(TAG_SCRIPT + "Attempting to execute " + scriptPath);
                System.out.println();
                ExperimentTrial.runScript(scriptPath);
            } catch(InterruptedException e) {
                System.out.println(ANSI_RED + "[EXCEPTION] " + ANSI_PURPLE + "Interrupted " + scriptPath + ANSI_RESET);
                e.printStackTrace();
                return 1;
            } catch(Exception e) {
                e.printStackTrace();
                return 2;
            }

            System.out.println(TAG_SCRIPT + "Finished executing " + scriptPath);
            System.out.println();

            return 0;
        }
    }

    //==============================================================================

    public static void main(String[] args) {
        try {
            runTrial();
        } catch(Exception e) {
            System.out.println(ANSI_RED + "[EXCEPTION] " + ANSI_PURPLE + "Trial interrupted" + ANSI_RESET);
            e.printStackTrace();
        }
    }

    //==============================================================================

    private static void runTrial() throws Exception {

        System.out.println(ANSI_RED + "=======================================================================");
        System.out.println(ANSI_BLUE + " *** Running trial from ExperimentTrial.java ***");
        System.out.println(ANSI_RED + "=======================================================================" + ANSI_RESET);
        System.out.println();

        //Create executor to run multithreaded processes
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        //TODO: Change absolute paths to relative paths
        //Create a runnable for each script to be run
        ShellScript restartGeth = new ShellScript(START_GETH_PATH);
        ShellScript test = new ShellScript(RUN_TESTER_PATH);

        //==============================================================================

        //Restart geth and create a new chain
        System.out.println(TAG_EXPERIMENT + ANSI_CYAN + "Restarting geth..." + ANSI_RESET);
        System.out.println();
        Future<Integer> futureRestartGeth = executor.submit(restartGeth);

        //Wait for geth to generate DAG
        Thread.sleep(3 * ONE_SECOND);
        System.out.println();
        System.out.println(TAG_EXPERIMENT + ANSI_GREEN + "Waiting for geth to initialize" + ANSI_RESET);

        if(GETH_VERBOSITY_ENABLED) { //If geth is in a verbose mode, print out the time left every 10 seconds on its own line
            for(int i = GETH_START_TIME_SECONDS; i >= 0; i -= 10) {
                if(i > 30)
                    System.out.println(TAG_EXPERIMENT + ANSI_YELLOW + i + " seconds remaining..." + ANSI_RESET);
                else
                    System.out.println(TAG_EXPERIMENT + ANSI_RED + i + " seconds remaining..." + ANSI_RESET);

                Thread.sleep(10 * ONE_SECOND);
            }
        } else { //If geth is not in a verbose mode, print out a countdown timer on one line
            for(int i = GETH_START_TIME_SECONDS; i >= 0; i--) {
                if(i > 30)
                    System.out.print("\r" + TAG_EXPERIMENT + ANSI_YELLOW + i + " seconds remaining..." + ANSI_RESET);
                else
                    System.out.print("\r" + TAG_EXPERIMENT + ANSI_RED + i + " seconds remaining..." + ANSI_RESET);

                Thread.sleep(ONE_SECOND);
            }
        }

        System.out.println();
        System.out.println();

        //==============================================================================

        //Run the test script
        System.out.println(TAG_EXPERIMENT + ANSI_CYAN + "Executing test..." + ANSI_RESET);
        System.out.println();
        Future<Integer> futureTest = executor.submit(test);

        //Make sure that the test script has finished running
        while(futureTest.get() != 0) {
            Thread.sleep(5 * ONE_SECOND);
            System.out.println(TAG_EXPERIMENT + ANSI_YELLOW + "Waiting for test to finish..." + ANSI_RESET);
            System.out.println();
        }

        //==============================================================================

        //TODO: Make this actually work
        //Shut down the executor
        System.out.println(TAG_EXPERIMENT + ANSI_RED + "Initiating shutdown" + ANSI_RESET);
        executor.shutdownNow();

        while(!executor.isTerminated()) {
            Thread.sleep(1000);
            System.out.println(TAG_EXPERIMENT + ANSI_YELLOW + "Waiting for executor to shut down..." + ANSI_RESET);
        }

        System.out.println(TAG_EXPERIMENT + ANSI_GREEN + "Executor shut down" + ANSI_RESET);
        System.out.println();

        System.out.println(ANSI_RED + "=======================================================================");
        System.out.println(ANSI_BLUE + " *** Finished trial from ExperimentTrial.java ***");
        System.out.println(ANSI_RED + "=======================================================================" + ANSI_RESET);
        System.out.println();
    }

    //==============================================================================

    private static void runScript(String path) throws Exception {

        File tempScript = generateScript(path);

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } finally {
            tempScript.delete();
        }
    }

    //==============================================================================

    private static File generateScript(String path) throws IOException {
        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println(path);

        printWriter.close();

        return tempScript;
    }
}
