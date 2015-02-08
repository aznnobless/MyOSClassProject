/**
 * Created by aznnobless on 1/8/15.
 */
public class OpenFileTable {

    public static final int FLAG_EMPTY = -1;
    public static final int ROOT_DIRECTORY_INDEX = 0;

    // OpenFileTable Buffer Size
    public static final int RW_BUFFER_SIZE = 64;
    // In Java, Integer is 32 bits which is 4 bytes
    public static final int INT_TO_BYTE = 4;
    // 1 for directory and 3 open files
    public static final int OFT_MAX_SIZE = 4;


    private IOSystem disk;
    private OpenFileTableEntry[] table;

    public OpenFileTable(IOSystem disk) {

        this.disk = disk;
        this.table = new OpenFileTableEntry[OFT_MAX_SIZE];
        for(int i = 0; i < 4; i++) {
            table[i] = new OpenFileTableEntry();
        }
    }


    /**
     *  returns index of free directory buffer entry (NOT PERFECT)
     */
    public int getFreeDirectoryEntry() {

        byte[] directoryBlock = getOftEntry(ROOT_DIRECTORY_INDEX).getRwBuffer();

        for(int i = 0; i < 64; i+=4) {
            if(directoryBlock[i] == FLAG_EMPTY) {

                return i;
            }

        }
        return -1;
    }

    /**
     *  returns index of free oft buffer
     */
    public int getFreeBuffer() {
        for(int i = 1; i < OFT_MAX_SIZE; i++) {
           if(getDescriptorIndex(i) == FLAG_EMPTY) {
               return i;
           }
        }
        return -1;
    }

    public void freeOftEntry(int i) {
        OpenFileTableEntry oftEntry = getOftEntry(i);
        oftEntry.setCurrentPosition(FLAG_EMPTY);
        oftEntry.setFileDescriptorIndex(FLAG_EMPTY);
        oftEntry.setRwBuffer(HelperUtility.getInitialBlockData());
    }


    public OpenFileTableEntry getOftEntry(int i) {
        return table[i];
    }

    public void setOftEntry(OpenFileTableEntry entry, int i) {
        table[i] = entry;
    };

    public byte[] getRwBuffer(int i) {
        return table[i].getRwBuffer();
    }

    public int getDescriptorIndex(int i) {
        return table[i].getFileDescriptorIndex();
    }

}


