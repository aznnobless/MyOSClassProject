import java.io.*;

/**
 *  Author: Byung Ho Lee
 *  Student ID#: 60626811
 */

/**
 * Driver class
 *
 * This class provide user interface to its user.
 *
 * Created by aznnobless on 1/6/15.
 */

public class Driver {

    // Singleton pattern instance variable;
    private static Driver sDriver = null;

    private FileSystem fileSystem;

    private FileReader fileReader; // String-based stream for file input
    private FileWriter fileWriter; // String-based stream for file output
    private BufferedReader bufferedReader;  // String-based Buffering Reader
    private BufferedWriter bufferedWriter; // String-based Buffering writer

    /**
     * Default constructor : this is for shell mode.
     */
    private Driver() {

        // instantiate FileSystem() : parameter true for automatic mode, false for shell mode
        fileSystem = new FileSystem(false);
        fileReader = null;
        fileWriter = null;

        bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        runUI();

    }

    /**
     * Constructor with two params: This is for automated mode for class requirement.
     * @param inputFileName
     * @param outputFileName
     */
    private Driver(String inputFileName, String outputFileName) {


        File inputFile = new File("./", inputFileName);
        File outputFile = new File("./", outputFileName);

        try {
            outputFile.createNewFile(); // create an output file

            fileReader = new FileReader(inputFile);
            bufferedReader = new BufferedReader(fileReader);

            fileWriter = new FileWriter(outputFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);

            fileSystem = new FileSystem(true, bufferedWriter);
        }catch(IOException ioEx) {
            //ioEx.printStackTrace();
        }

        automatedMode();

    }

    // Automated mode interface
    private void automatedMode()  {
        String line;
        try {

            line = bufferedReader.readLine();

            while(line != null) {
                // System.out.println(line); // For debug purpose
                commandChecker(line);
                line = bufferedReader.readLine();
            }

        } catch(IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            try {
                if( bufferedReader != null )
                    bufferedReader.close();
                if( fileReader != null )
                    fileReader.close();
                if( bufferedWriter != null )
                    bufferedWriter.close();
                if( fileWriter != null)
                    fileWriter.close();
            }catch(IOException ioEx) {
               //ioEx.printStackTrace();
            }
        }
    }

    /**
     * TODO : If I have spare time before the due date, try to apply command pattern
     */

    // Basic command line interface
    private void runUI() {

        String command = "";

        do {
            try {
                System.out.print("$");
                command = bufferedReader.readLine();
                commandChecker(command);
            }catch (Exception ex) {
                System.err.println("Error occur @ shell interface");
                ex.printStackTrace();
            }

        }while( !(command.equals("q")) );
    }

    // Command Checker;
    private void commandChecker(String command) throws IOException{


        String[] tokens = command.split(" ");

        if(tokens[0].equals("") ) {

            if(fileSystem.isAutomaticMode()) {
                bufferedWriter.newLine();
            } else {
               return;
            }

        }
        else if(tokens[0].equals("cr") && tokens.length == 2 && fileSystem.isInitialized()) {

            fileSystem.create(tokens[1]);

        }
        else if(tokens[0].equals("de") && tokens.length == 2 && fileSystem.isInitialized()) {

           fileSystem.destroy(tokens[1]);

        }
        else if(tokens[0].equals("op") && tokens.length == 2 && fileSystem.isInitialized()) {

           fileSystem.open(tokens[1]);

        }
        else if(tokens[0].equals("cl") && tokens.length == 2 && fileSystem.isInitialized()) {

            try {
                int oftIndex = Integer.parseInt(tokens[1]);

                fileSystem.close(oftIndex);

            }catch(NumberFormatException ex) {

                if(fileSystem.isAutomaticMode()) {

                    HelperUtility.writeMessageTofile(bufferedWriter, FileSystem.ERROR_MESSAGE);

                } else {
                    System.err.println(FileSystem.ERROR_MESSAGE);
                }
            }

        }
        else if(tokens[0].equals("rd") && tokens.length == 3 && fileSystem.isInitialized()) {

            try {

                int index = Integer.parseInt(tokens[1]);
                int number = Integer.parseInt(tokens[2]);

                fileSystem.read(index, number);

            }catch(NumberFormatException ex) {

                if(fileSystem.isAutomaticMode()) {

                    HelperUtility.writeMessageTofile(bufferedWriter, FileSystem.ERROR_MESSAGE);

                } else {
                    System.err.println(FileSystem.ERROR_MESSAGE);
                }
            }


        }

        else if(tokens[0].equals("wr") && tokens.length == 4 && fileSystem.isInitialized()) {

            try {

                int index = Integer.parseInt(tokens[1]);

                char character = tokens[2].charAt(0);

                int count = Integer.parseInt(tokens[3]);

                fileSystem.write(index, character, count);

            }catch(Exception ex) {

                if(fileSystem.isAutomaticMode()) {

                    HelperUtility.writeMessageTofile(bufferedWriter, FileSystem.ERROR_MESSAGE);

                } else {
                    System.err.println(FileSystem.ERROR_MESSAGE);

                }
            }

        }
        else if(tokens[0].equals("sk") && tokens.length == 3 && fileSystem.isInitialized()) {

            try {

                int index = Integer.parseInt(tokens[1]);
                int position = Integer.parseInt(tokens[2]);
                fileSystem.lseek(index, position);

            }catch(NumberFormatException ex) {

                if(fileSystem.isAutomaticMode()) {

                    HelperUtility.writeMessageTofile(bufferedWriter, FileSystem.ERROR_MESSAGE);

                } else {
                    System.err.println(FileSystem.ERROR_MESSAGE);
                }
            }

        }
        else if(tokens[0].equals("dr") && tokens.length == 1 && fileSystem.isInitialized()) {

            fileSystem.directory();
        }
        else if(tokens[0].equals("in") && tokens.length == 1) {

            fileSystem.init("");

        } else if(tokens[0].equals("in") && tokens.length == 2) {

            fileSystem.init(tokens[1]);

        }
        else if(tokens[0].equals("sv") && tokens.length == 2 && fileSystem.isInitialized()) {

            fileSystem.save(tokens[1]);

        }
        else if(tokens[0].equals("q") && tokens.length == 1 && !fileSystem.isAutomaticMode()) {

            System.exit(1);

        }
        else if(tokens[0].equals("SHOW") && tokens.length == 1 && !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {

            fileSystem.displayLdisk();

        }
        else if(tokens[0].equals("OFT") && tokens.length == 1 && !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            fileSystem.displayOpenFileTable();
        }
        else if(tokens[0].equals("OFT1") && tokens.length == 1 && !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            fileSystem.displayOFT1();
        }

        else if(tokens[0].equals("DES") && tokens.length == 1&& !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            fileSystem.displayDescriptorTable();
        }
        else if(tokens[0].equals("DATA") && tokens.length == 1&& !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            fileSystem.displayDataTable();
        }
        else if(tokens[0].equals("AA") && tokens.length == 1 && !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            fileSystem.displayRootDirectoryBuffer();
        }
        else if(tokens[0].equals("AAA") && tokens.length == 1 && !fileSystem.isAutomaticMode() && fileSystem.isInitialized()) {
            //System.out.println(fileSystem.findEmptyFileDescriptor());
            //fileSystem.displayFreeDirectoryEntry();
            // System.out.println( fileSystem.search_directory(tokens[1]) );
            fileSystem.displayPartialDataTable();


        }
        else if(tokens[0].equals("ll") && tokens.length == 1 && fileSystem.isInitialized()) {
            //System.out.println(fileSystem.findEmptyFileDescriptor());
            fileSystem.displayTotalNumberOfFile();
        }
        else {
            if(!fileSystem.isAutomaticMode())
                System.out.println(FileSystem.ERROR_MESSAGE);
        }
    }

    // return instance of Driver. (Instance of Shell Version)
    public static Driver getShellModeInstance() {
        if(sDriver == null) {
            return new Driver();
        }
        else
            return sDriver;
    }


    // return instance of Driver (Instance of Automated Version)
    public static Driver getSimulationModeInstance(String input, String output) {
        if(sDriver == null) {
            return new Driver(input, output);
        } else
            return sDriver;
    }


    // Main method : Program starts here
    public static void main(String[] args) {

        if(args.length == 0)
            Driver.getShellModeInstance();
        if(args.length == 2)
            Driver.getSimulationModeInstance(args[0], args[1]);

    }


}
