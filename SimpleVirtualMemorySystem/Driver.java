import java.io.*;
import java.util.Scanner;

public class Driver {

    private FileReader initFileReader; // String-based stream for file input
    private FileReader inputFileReader; // String-based stream for file input
    private File output1;
    private File output2;
    private FileWriter fileWriterWithoutTLB; // String-based stream for file output
    private FileWriter fileWriterWithTLB;
    private BufferedReader bufferedReader;  // String-based Buffering Reader
    private BufferedWriter bufferedWriterWithoutTLB; // String-based Buffering writer
    private BufferedWriter bufferedWriterWithTLB;

    VirtualMemorySystem vmSystem;

    public Driver() {

        output1 = new File("./", "output1.txt");
        output2 = new File("./", "output2.txt");

        try {
            output1.createNewFile();
            output2.createNewFile();

            fileWriterWithoutTLB = new FileWriter(output1.getAbsoluteFile());
            bufferedWriterWithoutTLB = new BufferedWriter(fileWriterWithoutTLB);

            fileWriterWithTLB = new FileWriter(output2.getAbsoluteFile());
            bufferedWriterWithTLB = new BufferedWriter(fileWriterWithTLB);

        }catch(IOException ioEx) {
            ioEx.printStackTrace();
        }

        vmSystem = new VirtualMemorySystem();

        // initialize Physical Memory using init file
        requestInitFile();

        // read virtual data
        requestInputFile();

    }

    // Request init file from the user
    public void requestInitFile() {

        System.out.print("Please input init file name : ");
        Scanner input = new Scanner(System.in);
        String initFileName = input.nextLine();

        try {
            initFileReader = new FileReader(initFileName);
            bufferedReader = new BufferedReader(initFileReader);
        } catch(IOException ioEx) {
            ioEx.printStackTrace();
        }

        readInitFile();

    }

    // Read init file using user's input
    public void readInitFile() {

        String line ;

        try {

            line = bufferedReader.readLine();

            initFileFirstLineParser(line);  // CALL

            line = bufferedReader.readLine();

            initFileSecondLineParser(line); // CALL


        }catch(IOException ioEx) {

            ioEx.printStackTrace();

        } finally {

            try {

                if (bufferedReader != null)
                    bufferedReader.close();

                if(initFileReader != null)
                    initFileReader.close();

            }catch(IOException ioEx) {
                ioEx.printStackTrace();
            }
        }
    }

    public void initFileFirstLineParser(String line) {

        String[] tokens = line.split(" ");

        /*
         * I assumed that init file is strictly formatted.
         */

        if(tokens.length % 2 != 0) {
            System.err.println("Error @ Driver.initFileFirstLineParser : invalid format of input file");
        }

        for(int i = 0; i < tokens.length; i+=2) {
            vmSystem.updateSegmentTableEntryToPhysicalMemory(
                    Integer.parseInt(tokens[i]), Integer.parseInt(tokens[i+1]) );
        }

    }

    public void initFileSecondLineParser(String line) {

        String[] tokens = line.split(" ");

        /*
         * I assumed that init file is strictly formatted.
         */

        if(tokens.length % 3 != 0) {

            System.err.println("Error @ Driver.initFileSecondLineParser : invalid format of input file");

        }

        //System.out.println("Token Length : " + tokens.length);
        for(int i = 0; i < tokens.length; i += 3) {
            //System.out.println("$$$$");
            // page p of seg s start at f
            vmSystem.updatePageTableEntryToPhysicalMemory(
                    Integer.parseInt(tokens[i]),Integer.parseInt(tokens[i+1]), Integer.parseInt(tokens[i+2])
            );
        }

    }


    public void requestInputFile() {
        System.out.print("Please input input file name : ");
        Scanner input = new Scanner(System.in);
        String inputFileName = input.nextLine();

        try {

            inputFileReader = new FileReader(inputFileName);
            bufferedReader = new BufferedReader(inputFileReader);

        }catch(IOException ioEx) {
            ioEx.printStackTrace();
        }

        readInputFile();
    }

    public void readInputFile() {

        String line = null ;

        try {

            line = bufferedReader.readLine();

        }catch(IOException ioEx) {
            ioEx.printStackTrace();
        } finally {

            try {

                if (bufferedReader != null)
                    bufferedReader.close();

                if(inputFileReader != null)
                    initFileReader.close();

            }catch(IOException ioEx) {
                ioEx.printStackTrace();
            }

        }

        parseInputFile(line);
    }

    public void parseInputFile(String line) {

        String[] tokens = line.split(" ");

        String opCodeStr = null;
        String rawVirtualAddressStr = null;
        int opCode = -1;
        int rawVirtualAddress = -1;

        for(int i = 0; i < tokens.length; i +=2) {

            opCodeStr = tokens[i];
            rawVirtualAddressStr = tokens[i + 1];

            opCode = Integer.parseInt(opCodeStr);

            try {
                // parse virtualAddress part
                rawVirtualAddress = Integer.parseInt(rawVirtualAddressStr);

                // Precomputation: Left most 4 bits will not be used. So just throw-away.
                rawVirtualAddress = (rawVirtualAddress & 0xFFFFFFF);

            }catch(NumberFormatException numberFormatException) {

                // THESE ARE FOR the virtual address that exceeds the range of Integer.
                long temp = Long.parseLong(rawVirtualAddressStr);
                rawVirtualAddress = (int)temp;
                rawVirtualAddress = (rawVirtualAddress & 0xFFFFFFF);



            }

            // OPERATION CODE: Translation start

            if( (opCode) == 0 || (opCode ==1) ) {

                // Case of System doesn't have a TLB
                vmSystem.translateVirtualAddress(opCode, rawVirtualAddress, false);

                // Case of System have a TLB
                vmSystem.translateVirtualAddress(opCode, rawVirtualAddress, true);

            } else {
                Utility.getInstance().appendToTlbOutput("INVALID OP CODE");
                Utility.getInstance().appendToWithoutTlbOutput("INVALIDOPCODE");
            }
        }
        System.out.println();
        // ***** DEBUG PURPOSE OUTPUT *****
        System.out.println();
//        System.out.println("------ Bitmap Information ------");
//        vmSystem.showBitmapInPhysicalMemory();
        System.out.println();
//        System.out.println("------ Segment Table ------");
//        vmSystem.showSegmentTableInPhysicalMemory();

        System.out.println();
        System.out.println();

        // print buffer to sceeen
        Utility.getInstance().printBuffers();

        // Write to file.
        writeToFile();



    }

    public void writeToFile() {
        try {
            Utility.getInstance().writeToFile(bufferedWriterWithoutTLB, bufferedWriterWithTLB);
        } catch(IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            try {
                if (bufferedWriterWithoutTLB != null)
                    bufferedWriterWithoutTLB.close();
                if (fileWriterWithoutTLB != null)
                    fileWriterWithoutTLB.close();

                if (bufferedWriterWithTLB != null)
                    bufferedWriterWithTLB.close();
                if (fileWriterWithTLB != null)
                    fileWriterWithTLB.close();
            }catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        new Driver();

    }

}
