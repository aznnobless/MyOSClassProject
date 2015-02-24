import com.byunghl.cs143b.project2.command.*;
import com.byunghl.cs143b.project2.core.Manager;
import com.byunghl.cs143b.project2.utility.SystemUtility;

import java.io.*;

public class Driver {

    private static Driver sDriver = null;

    private Manager prManager;
    private boolean runningMode;
    private FileReader fileReader;
    private FileWriter fileWriter;
    private BufferedReader bufferedReader;  // String-based Buffering Reader
    private BufferedWriter bufferedWriter;

    private CommandInvoker commandInvoker;

    private Command commentIgnoreCmd; // comment will be ignored
    private Command showErrorMessageCmd;
    private Command promptCmd;
    private Command initCmd;
    private Command createCmd;
    private Command destroyCmd;
    private Command requestCmd;
    private Command releaseCmd;
    private Command timeoutCmd;
    private Command quitCmd;
    private Command showCurrentProcessStatusCmd;
    private Command highestPriorityCmd;
    private Command displayTreeCmd;

    // Constructor for shell runningMode : Singleton pattern is applied for Driver
    private Driver() {
        runningMode = false;
        prManager = new Manager(runningMode);
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        initCommand();
        runShell();

    }

    // Constructor for automated runningMode : Singleton pattern is applied
    private Driver(String inputFileName, String outputFileName) {

        runningMode = true;

        File inputFile = new File("./", inputFileName);
        File outputFile = new File("./", outputFileName);

        try {
            outputFile.createNewFile(); // create an output file

            fileReader = new FileReader(inputFile);
            bufferedReader = new BufferedReader(fileReader);

            fileWriter = new FileWriter(outputFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);

            prManager = new Manager(runningMode, bufferedWriter);
            initCommand();

            runAutomatedMode();
        }catch (IOException ioEx) {
            System.err.println("IO ERROR");

        }


    }

    // Initialize commands supported by this Driver;
    private void initCommand() {
        commandInvoker = new CommandInvoker();

        commentIgnoreCmd = new CommentIgnoreCommand();
        showErrorMessageCmd = new ShowErrorMessageCommand(prManager);
        initCmd = new InitCommand(prManager, runningMode);
        promptCmd = new PromptCommand(prManager, bufferedWriter);
        createCmd = new CreateCommand(prManager);
        destroyCmd = new DestroyCommand(prManager);
        requestCmd = new RequestCommand(prManager);
        releaseCmd = new ReleaseCommand(prManager);
        timeoutCmd = new TimeOutCommand(prManager);
        quitCmd = new QuitCommand(prManager);
        showCurrentProcessStatusCmd = new ShowCurrentProcessStatusCommand(prManager);
        highestPriorityCmd = new HighestPriorityFinderCommand(prManager);
        displayTreeCmd = new DisplayTreeCommand(prManager);
    }

    public void runShell() {
        String command = "";

        do {
            try {

                System.out.print("$");

                command = bufferedReader.readLine();

                if(commandSyntaxChecker(command)) {
                    commandInvoker.execute();
                } else {
                    System.out.println("Invalid Command");
                }

            }catch (Exception ex) {

                SystemUtility.getInstance().displayMessage(false, "Error occur @ shell interface");

                ex.printStackTrace();
            }

        }while(!(command.equals("quit")));
    }

    public void runAutomatedMode(){

        String line;
        try {
            line = bufferedReader.readLine();

            while(line != null) {

                if(commandSyntaxChecker(line)) {
                   //System.out.println(line);
                    commandInvoker.execute();
                } else {
                    bufferedWriter.write("error@Driver.runAutomated runningMode");
                }
                line = bufferedReader.readLine();
            }

            if(SystemUtility.getInstance().isBufferNotEmpty())
                SystemUtility.getInstance().endCycle(true, bufferedWriter);

        }catch (IOException ioEx) {
            ioEx.printStackTrace();
        }finally {

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


    // Command Checker;
    private boolean commandSyntaxChecker(String command) throws IOException {

        String[] tokens = command.split(" ");

        String commandType = tokens[0];

        boolean isValidCommand = true;

        if(commandType.equals("") ) {
            commandInvoker.setCommand(promptCmd);
        } else if(commandType.equals("#") ) {
            commandInvoker.setCommand(commentIgnoreCmd);
        }else if(commandType.equals("init") && tokens.length == 1) {
            commandInvoker.setCommand(initCmd);
        }
        else if(commandType.equals("quit") && tokens.length == 1) {
            commandInvoker.setCommand(quitCmd);
        }else if(commandType.equals("cr") && tokens.length == 3) {
            isValidCommand = createCmd.setArguments(tokens);
            commandInvoker.setCommand(createCmd);
        }else if(commandType.equals("de") && tokens.length == 2) {
            isValidCommand = destroyCmd.setArguments(tokens);
            commandInvoker.setCommand(destroyCmd);
        }else if(commandType.equals("req") && tokens.length == 3) {
            isValidCommand = requestCmd.setArguments(tokens);
            commandInvoker.setCommand(requestCmd);
        }else if(commandType.equals("rel") && tokens.length == 3) {
            isValidCommand = releaseCmd.setArguments(tokens);
            commandInvoker.setCommand(releaseCmd);
        }else if(commandType.equals("to") && tokens.length == 1) {
            commandInvoker.setCommand(timeoutCmd);
        }
        else if(commandType.equals("show") && tokens.length == 1) {
            commandInvoker.setCommand(showCurrentProcessStatusCmd);
        }else if(commandType.equals("high") && tokens.length == 1) {
            commandInvoker.setCommand(highestPriorityCmd);
        }else if(commandType.equals("tree") && tokens.length == 1) {
            commandInvoker.setCommand(displayTreeCmd);
        } else {
            commandInvoker.setCommand(showErrorMessageCmd);
            isValidCommand = false;
        }

        return isValidCommand;
    }

    // Singleton Pattern:  Return instance of Shell Mode Driver
    public static Driver getShellModeInstance() {

        if(sDriver == null) {
            sDriver = new Driver();
            return sDriver;
        }
        else
            return sDriver;

    }

    // Singleton Pattern : Return instance of Automated Mode Driver
    public static Driver getAutomatedModeInstance(String inputFileName, String outputFileName) {
        if(sDriver == null) {
            sDriver = new Driver(inputFileName, outputFileName);
            return sDriver;
        }
        else
            return sDriver;
    }

    // Main : Start point of this program
    public static void main(String[] args) {

        if(args.length == 0)
            Driver.getShellModeInstance();

        if(args.length == 2)
            Driver.getAutomatedModeInstance(args[0], args[1]);

    }

}
