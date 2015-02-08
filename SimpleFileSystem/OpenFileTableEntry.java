/**
 * Author       : Byung Ho Lee
 * Student ID#  : 60626811
 *
 * Created by Byung Ho Lee on 1/6/15.
 */

public class OpenFileTableEntry {

    // OFT has 4 entries:  1 directory + 3 other open files
    public static final int OFT_MAX_SIZE = 4;

    public boolean hasBeenWritten;

    private byte[] rwBuffer;
    private int currentPosition;
    private int fileDescriptorIndex;
    private int currentBlockNumber;
    private int lengthOfFile;
    private int endOfFileIndex;

    /**
     * Constructor
     */

    public OpenFileTableEntry() {
        this.rwBuffer = new byte[IOSystem.LENGTH_OF_LDISK_BLOCK];

        init();

    }

    /* *
     * Initialize member variables
     */
    public void init() {
        for(int i = 0; i < 64; i++) {
            rwBuffer[i] = -1;
        }
        hasBeenWritten = false;
        currentPosition = 0;
        fileDescriptorIndex = -1;
        currentBlockNumber = -1;
        lengthOfFile = 0;
        endOfFileIndex = 0;
    }

    // update Partial buffer
    public void updateParitalBuffer(byte[] partial, int position) {
        for( int i = position; i < position + partial.length; i++) {
            rwBuffer[i] = partial[i-position];
        }
    }

    public int getLengthOfFile() {
        return lengthOfFile;
    }

    public void setLengthOfFile(int lengthOfFile) {
        this.lengthOfFile = lengthOfFile;
    }

    public void increaseLengthOfFile() {
        this.lengthOfFile++;
    }


    public void setCurrentBlockNumber(int currentBlockNumber) {
        this.currentBlockNumber = currentBlockNumber;
    }

    public int getCurrentBlockNumber() {
        return currentBlockNumber ;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void increaseCurrentPosition() {
        this.currentPosition++;
    }


    /****
     * NOTE:
     * Below two methods are functionally same.
     * But I will get rid of redundant later.
     * ****/
    public int findFirstEmptySlotFromCurrentPosition() {

        for(int i = (currentPosition % IOSystem.LENGTH_OF_LDISK_BLOCK); i < IOSystem.LENGTH_OF_LDISK_BLOCK ; i++) {
            if(rwBuffer[i] == -1)
                return i;
        }

        return -1;
    }

    public int findFirstEmptySlotFromBuffer() {

        for(int i = 0; i < 64; i++) {
            if(rwBuffer[i] == -1) {
                return i;
            }
        }

        return -1;
    }

    public int getFileDescriptorIndex() {
        return fileDescriptorIndex;
    }

    public void setFileDescriptorIndex(int fileDescriptorIndex) {
        this.fileDescriptorIndex = fileDescriptorIndex;
    }

    public byte[] getRwBuffer() {
        return rwBuffer;
    }

    public void setRwBuffer(byte[] rwBuffer) {
        this.rwBuffer = rwBuffer;
    }

    public char getBufferAt(int index) {
        return (char)rwBuffer[index];
    }

    public void setBufferAt(int index, char value) {
        rwBuffer[index]= (byte)value;
    }

    public void setBufferAtByte(int index, byte value) { rwBuffer[index] = value;}

    public void setHasBeenWritten(boolean flag) {
        hasBeenWritten = flag;
    }

    public boolean getHasBeenWritten() {
        return hasBeenWritten;
    }

    public int getEndOfFileIndex() {
        return endOfFileIndex;
    }

    public void setEndOfFileIndex(int endOfFileIndex) {
        this.endOfFileIndex = endOfFileIndex;
    }
}
