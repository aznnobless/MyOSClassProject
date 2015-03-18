import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by aznnobless on 3/5/15.
 */
public class Utility {

    private static Utility instance = new Utility(); // Singleton pattern instance.

    private StringBuilder withoutTlbOutput; // buffer to store output that will be written in file.
    private StringBuilder withTlbOutput; // buffer to store ouutput that will be written in file.

    // Default Constructor
    private Utility() {
        withoutTlbOutput = new StringBuilder();
        withTlbOutput = new StringBuilder();
    }

    // Append output to buffer (Utilizing Tranlation Look-aside Buffer)
    public void appendToTlbOutput(String message) {
        withTlbOutput.append(message).append(" ");
    }

    // Append output to buffer (NO cache. This means no TLB utilization)
    public void appendToWithoutTlbOutput(String message) {
        withoutTlbOutput.append(message).append(" ");
    }

    // Display the information in the buffers.
    public void printBuffers() {
        System.out.println("When not using the TLB : ");
        System.out.println(withoutTlbOutput.toString().trim());
        System.out.println("With TLB :");
        System.out.println(withTlbOutput.toString().trim());
    }

    // Write buffers to file
    public void writeToFile(BufferedWriter bw1, BufferedWriter bw2) throws IOException{

        bw1.write( withoutTlbOutput.toString().trim() );
        bw2.write( withTlbOutput.toString().trim() );
    }

    /* DEBUG TOOLS */
    public void showTargetFrameInPhysicalMemoryUsingAddress(int address) {
        PhysicalMemory.getPhysicalMemory().showTargetFrameUsingAddress(address);
    }

    /* END OF DEBUG TOOLS */

    static public Utility getInstance() {
        return instance;
    }
}
