/**
 * Created by aznnobless on 1/9/15.
 */

/**
 *  Spec:
 *      Descriptor: 4 integers long
 *      (file length + 3 block numbers)
 */

public class FileDescriptor {

    public static final int EMPTY_FLAG = -1;

    private int fileLength;
    private int[] blocks;


    public FileDescriptor(int length) {
        this.fileLength = length;
        blocks = new int[3];
        for(int i =0; i < blocks.length; i++) {
            setBlockIndex(i, EMPTY_FLAG);
        }
    }

    public int getNumberOfFreeBlocks() {
        int counter = 0;
        for(int i =0; i < 3; i++) {
            if(blocks[i] == -1) {
                counter++;
            }
        }

        return counter;
    }

    public static FileDescriptor generateDescriptor(int indexOfEmptyDescriptor) {

        int descriptorBlockIndex = indexOfEmptyDescriptor / 4 + 1;

        byte[] descriptorBlock = IOSystem.getInstance().read_block(descriptorBlockIndex);

        //System.out.println(descriptorBlockIndex);

        FileDescriptor descriptor = new FileDescriptor(0);

        int startIndex = indexOfEmptyDescriptor * 16;
        int counter = 0;
        for(int i = startIndex; i < startIndex+16; i+=4) {
            int data = PackUtiltiy.unpack(descriptorBlock, i);
            if(counter == 0) {
                descriptor.setFileLength(data);
            }
            else {
                descriptor.setBlockIndex((counter - 1), data);
            }

            counter++;
        }

        return descriptor;

    }

    public static FileDescriptor retrieveFileDescriptor(int index) {


        // create new instance of descriptor
        FileDescriptor fd = new FileDescriptor(0);
        //     1            2       3           4           5           6
        //1~6 0 1 2 3 / 4 5 6 7/ 8 9 10 11 / 12 13 14 15 / 16 17 18 19 /20 21 22 23
        int blockNumber = index / 4 + 1;
        int nThDescriptor = index % 4;

        byte[] buffer = IOSystem.getInstance().read_block(blockNumber);

        // determine position
        int position = nThDescriptor*16;

        // update descriptor
        fd.setFileLength(PackUtiltiy.unpack(buffer, position));

        // fill up 3 block;
        for(int i = 0; i < 3; i++) {
            fd.setBlockIndex(i, PackUtiltiy.unpack(buffer, position += 4));
        }


        return fd;
    }

    public int getFirstOccupiedBlockIndex() {
        if(blocks[0] != -1)
            return 0;
        else
            return -1;
    }

    public int getLastOccupiedBlockIndex() {
        int lastIndex = -1;
        for(int i = 0; i < blocks.length; i++) {
            if(blocks[i] != -1) {
                lastIndex = i;
            }
        }

        return lastIndex;
    }

    public int getLastNonEmptyBlockIndex() {

        for(int i =0; i < 3; i++) {
            if(blocks[i] == -1) {
                return (i % 3) - 1;
            }
        }

        return 2;
    }

    public void incrementFileLength() {
        this.fileLength++;
    }

    public int getFileLength() {
        return this.fileLength;
    }

    public void setFileLength(int fileLength ) {
        this.fileLength = fileLength;
    }

    public int getBlockIndex(int i) {
        return blocks[i];
    }

    public void setBlockIndex(int index, int data) {
        blocks[index] = data;

    }

    public int getEmptyBlockIndex() {
        for(int i =0 ; i < blocks.length; i++) {
            if(getBlockIndex(i) == -1)
                return i;
        }

        return -1;
    }

}
