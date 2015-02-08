import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * Author : Byung Ho Lee
 * #60626811
 *
 * HelperUtility class to minimize number of lines of code in FileSystem class
 * Since, FileSystem is quite complicated, I want to increase readability of the code.
 *
 * Created by aznnobless on 1/9/15.
 */
public class HelperUtility {

    public static void writeMessageTofile(BufferedWriter bufferedWriter, String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
    }

    /**
     * Initialization helper method to reduce redundant code
     * @return initialized block buffer
     */
    public static byte[] getInitialBlockData() {
        // Create initial block
        byte[] initialBlockData = new byte[64];

        // Init initialBlockData
        for(int i = 0; i < initialBlockData.length; i++) {
            initialBlockData[i] = FileSystem.FLAG_EMPTY;
        }

        return initialBlockData;

    }


    /**
     *  BELOW methods for DEBUGGING
     */

    public static void showRootOFT(OpenFileTable oft) {
        OpenFileTableEntry root = oft.getOftEntry(0);
        byte[] buffer = root.getRwBuffer();
        int counter = 1;
        for(int i = 0; i < 64;i++) {
            if(counter % 8 == 0)
                System.out.printf(" buffer[%d] : %d \n ", i, buffer[i]);
            else
                System.out.printf(" buffer[%d] : %d ", i, buffer[i]);
            counter++;
        };
        System.out.println();
    }

    /**
     *  Display current OpenFileTable Content
     */
    public static void showOpenFileTable(OpenFileTable oft) {

        for(int i =0;  i < OpenFileTable.OFT_MAX_SIZE; i++) {

            byte[] buffer = oft.getRwBuffer(i);

            System.out.printf("File Descriptor @ OFT[%d] : %d \n", i, oft.getDescriptorIndex(i));
            System.out.printf("Current Position @ OFT[%d] : %d \n", i, oft.getOftEntry(i).getCurrentPosition());
            System.out.printf("Current Block Number @ OFT[%d] : %d \n", i, oft.getOftEntry(i).getCurrentBlockNumber() );
            if(oft.getOftEntry(i).getCurrentBlockNumber() != -1)
                System.out.println("File Length : " + FileDescriptor.generateDescriptor(oft.getOftEntry(i).getFileDescriptorIndex()).getFileLength());
            System.out.printf("End of File Index @ OFT[%d] : %d\n", i , oft.getOftEntry(i).getEndOfFileIndex());
            for(int j = 0; j < OpenFileTable.RW_BUFFER_SIZE; j++) {
                System.out.printf("Buffer[%d] = %d\n", j, buffer[j]);
            }
        }
    }

    public static void showOpenFileTable1(OpenFileTable oft) {
        OpenFileTableEntry oftEntry = oft.getOftEntry(1);
        System.out.println("File Descriptor number : " + oftEntry.getFileDescriptorIndex());
        System.out.println("Current Position : " + oftEntry.getCurrentPosition() );
        System.out.println("Current Block# (DES) : " + oftEntry.getCurrentBlockNumber() );
        System.out.println("File Length in DES: " + FileDescriptor.generateDescriptor(oftEntry.getFileDescriptorIndex()).getFileLength());
        System.out.printf("End of File Index : %d\n", oftEntry.getEndOfFileIndex());

        byte[] buffer = oftEntry.getRwBuffer();
        for(int i = 0; i < 64; i++) {
           System.out.println("Buffer[" + i +"] = " + buffer[i] );
        }
    }

    /**
     *  Display descriptor region in a disk
     */
    public static void showDescriptor(IOSystem disk) {
        for(int i = 0; i < 7; i++) {
            byte[] tempBuffer = disk.read_block(i);
            for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK;j++) {
                System.out.printf("[%d][%d] = %d\t", i,j, tempBuffer[j] );
            }
            System.out.println();

        }
    }

    /**
     *  Display whole disk
     */
    public static void showDisk(IOSystem disk) {

        for(int i = 0; i < IOSystem.NUMBER_OF_LDISK_BLOCKS; i++) {
            byte[] tempBuffer = disk.read_block(i);
            for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j++) {

               System.out.printf("[%d][%d] = %d\t", i,j, tempBuffer[j] );

            }
            System.out.println();

        }
     }
    /**
     * Show data blocks only
     */
    public static void showData(IOSystem disk) {

        for(int i = 8; i < IOSystem.NUMBER_OF_LDISK_BLOCKS; i++) {
            byte[] tempBuffer = disk.read_block(i);
            for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j++) {

                System.out.printf("[%d][%d] = %d\t", i,j, tempBuffer[j] );

            }
            System.out.println();

        }
    }

    public static void showData2(IOSystem disk) {

        for(int i = 8; i < 15; i++) {
            byte[] tempBuffer = disk.read_block(i);
            for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j++) {

                System.out.printf("[%d][%d] = %d\t", i,j, tempBuffer[j] );

            }
            System.out.println();

        }
    }
}
