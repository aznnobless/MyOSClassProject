import java.io.*;

/**
 * Author       : Byung Ho Lee
 * Student ID#  : 60626811
 *
 * Created by Byung Ho Lee on 1/6/15.
 */

public class FileSystem implements UserInterface {

    // Constants
    public static final String ERROR_MESSAGE = "error";
    private static final int NUMBER_OF_BITMAP_BLOCK = 1;
    private static final int NUMBER_OF_DESCRIPTOR_BLOCK = 6;
    public static final int DESCRIPTOR_SIZE_IN_BYTES = 16; // 4 int size * 4 bytes;

    private static final int MAX_FILE_NUMBER = 24; // max number of files which can be stored in this file system.
    private static final int MAX_FILENAME_LENGTH = 4;
    // Max File Length is 192, but count from 9, so 191.
    private static final int MAX_FILE_LENGTH = (IOSystem.LENGTH_OF_LDISK_BLOCK * 3)-1;

    private static final int ROOT_DIRECTORY_INDEX = 0;
    private static final int ROOT_DIRECTORY_DATA_INDEX = NUMBER_OF_BITMAP_BLOCK + NUMBER_OF_DESCRIPTOR_BLOCK;
    private static final int NUMBER_OF_DESCRIPTOR_IN_A_BLOCK = 4;


    // FLAG Constants
    private static final int BITMAP_FLAG_EMPTY = 0; // bit is binary number so only 0 and 1 are allowed
    public static final int FLAG_EMPTY = -1;
    private static final int FLAG_OCCUPIED = 1;
    private static final int FLAG_NOT_FOUND = FLAG_EMPTY;
    private static final boolean TRUE_FLAG = true;
    private static final boolean FALSE_FLAG = false;

    // Components
    private IOSystem disk;
    private OpenFileTable oft;
    private BufferedWriter bufferedWriter;

    // Member variables
    private boolean isInitialized;
    private boolean isAutomaticMode; // true : automated mode, false: shell mode
    private int totalNumberOfFilesInDisk;



    // Constructor for Shell Mode
    public FileSystem(boolean isAutomaticMode) {

        setInitialized(false);
        setAutomaticMode(isAutomaticMode);
        this.bufferedWriter = null;
    }

    // Constructor for Automated mode
    public FileSystem(boolean isAutomaticMode, BufferedWriter bufferedWriter) {

        setInitialized(false);
        setAutomaticMode(isAutomaticMode);
        this.bufferedWriter = bufferedWriter;
    }

    /**
     * Restore lDisk from file.txt or create new (if no file)
     */
    @Override
    public void init(String fileName) throws IOException {

        // Instantiate components
        this.disk = IOSystem.getInstance();
        this.oft = new OpenFileTable(disk);
        setInitialized(true);
        // if saved file not exist, fileName length == 0;
        if(fileName.length() == 0) {
            format();

        }
        // Restore disk from the file
        else {
            restoreDiskFromFile(fileName);
            mountRestoredRootDirectoryInformation();
        }

    }

    private void printErrorMessage() throws IOException {
        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, ERROR_MESSAGE);
        } else {
            System.err.println(ERROR_MESSAGE);
        }


    }

    @Override
    public void create(String symbolicFileName) throws IOException {

        // get empty position
        int availableDescriptorSlot = findEmptyFileDescriptor();

        if(totalNumberOfFilesInDisk > MAX_FILE_NUMBER) {
            printErrorMessage();
            return;
        }

        // Check available space
        if(availableDescriptorSlot == -1) {
            printErrorMessage();
            return;
        }

        // check file exists or not
        if(checkFileExist(symbolicFileName)) {
            printErrorMessage();
            return;
        }

        // check valid file name
        if(symbolicFileName.length() > MAX_FILENAME_LENGTH || symbolicFileName.length() == 0 ) {

            printErrorMessage();
            return;
        }

        int emptyDirectorySlot = oft.getFreeDirectoryEntry();

        FileDescriptor rootDescriptor;

        // if oft buffer is full, need to save it to disk and allocate new buffer
        if(emptyDirectorySlot == -1) {

            rootDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);

            int emptySpotFromRootDescriptor = rootDescriptor.getEmptyBlockIndex();

            // ERROR : All buffer is full!
            if(emptySpotFromRootDescriptor == -1) {
                printErrorMessage();
                return;
            }

            int emptySpotFromDisk = findEmptyDataRegion();

            rootDescriptor.setBlockIndex(emptySpotFromRootDescriptor, emptySpotFromDisk);
            updateBitmap(emptySpotFromDisk, FLAG_OCCUPIED);

            this.write_descriptor(0, rootDescriptor);

            //update root oft here.
            oft.getOftEntry(0).setRwBuffer(HelperUtility.getInitialBlockData());
            oft.getOftEntry(0).setCurrentBlockNumber(emptySpotFromDisk);
            emptyDirectorySlot = 0;
        }

        int emptyBitmapSlot = findEmptyBitmap(); //use it has block #
        updateBitmap(emptyBitmapSlot, FLAG_OCCUPIED); // ? Can be eliminated if I use write_descriptor, no keep here

        // Each directory entry 2 int long
        byte[] directoryEntry = new byte[8];

        for(int i = 0; i < symbolicFileName.length(); i++) {
            directoryEntry[i] = (byte)symbolicFileName.charAt(i);
        }

        /** PackUtility.pack(byteArray, available DecriptorSlot, location*/
        PackUtiltiy.pack(directoryEntry, availableDescriptorSlot, 4);
        oft.getOftEntry(0).updateParitalBuffer(directoryEntry, emptyDirectorySlot);

        byte[] mem = disk.read_block(1 + (availableDescriptorSlot / NUMBER_OF_DESCRIPTOR_IN_A_BLOCK));
        PackUtiltiy.pack(mem, 0, (availableDescriptorSlot * DESCRIPTOR_SIZE_IN_BYTES) % IOSystem.LENGTH_OF_LDISK_BLOCK); //LENGTH
        PackUtiltiy.pack(mem, emptyBitmapSlot,(4 + (availableDescriptorSlot * DESCRIPTOR_SIZE_IN_BYTES)) % IOSystem.LENGTH_OF_LDISK_BLOCK); //INDEX BLOCK 0
        disk.write_block(1 + (availableDescriptorSlot / NUMBER_OF_DESCRIPTOR_IN_A_BLOCK), mem);

        // update oft info to descriptor
        rootDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);
        disk.write_block(rootDescriptor.getBlockIndex(rootDescriptor.getLastNonEmptyBlockIndex()), copyBuffer(oft.getRwBuffer(0)));

        incrementFileNumber();

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, symbolicFileName + " created");
        } else {
            System.out.println(symbolicFileName + " created");
        }

    }

    @Override
    public void destroy(String symbolicFileName) throws IOException {

        // Retrieve descriptor number
        int descriptorNumber = search_directory(symbolicFileName);

        // Check file exist or not
        if(descriptorNumber == -1 ) {
            if(isAutomaticMode()) {
                HelperUtility.writeMessageTofile(bufferedWriter, ERROR_MESSAGE);
            } else {
                System.out.println(ERROR_MESSAGE);
            }
            return;
        }

        // if target file is allocated in buffer, show error message
        for(int i = 1; i < 4; i++) {
            if (oft.getDescriptorIndex(i) == descriptorNumber) {

                if(isAutomaticMode()) {
                    HelperUtility.writeMessageTofile(bufferedWriter, ERROR_MESSAGE);
                } else {
                    System.out.println(ERROR_MESSAGE);
                }
                return;

            }
        }

        // Retrieve target file descriptor information
        FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(descriptorNumber);

        // update bitmap and
        for(int i = 0; i < 3; i++) {
            if(fd.getBlockIndex(i) != -1) {
                updateBitmap(fd.getBlockIndex(i), 0);
                fd.setBlockIndex(i,FLAG_EMPTY);
            }
        }

        fd.setFileLength(-1);

        // update descriptor.
        write_descriptor(descriptorNumber, fd);

        /// FIX POINT
        FileDescriptor rootDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);
        int targetRootDirectoryBufferIndex = (descriptorNumber-1) / 8;
        int rootDirectoryDataBlockIndex = rootDescriptor.getBlockIndex(targetRootDirectoryBufferIndex);

        byte[] directoryBlock = disk.read_block(rootDirectoryDataBlockIndex);

        int i = ((descriptorNumber-1) * 8) % 64;
        int endOfDescriptorIndex = i + 8;
        for(; i < endOfDescriptorIndex ; i++) {

            directoryBlock[i] = FLAG_EMPTY;
        }
        // update disk
        disk.write_block(rootDirectoryDataBlockIndex, directoryBlock);

        // update oft
        oft.getOftEntry(0).setRwBuffer(disk.read_block(ROOT_DIRECTORY_DATA_INDEX));

        decrementFileNumber();

        boolean isBufferEmpty = true;
        for(int k = 0 ; k < 64; k ++) {
            if(directoryBlock[k] != -1)
                isBufferEmpty = false;
        }

        if(isBufferEmpty) {
            if(targetRootDirectoryBufferIndex != 0) {
                rootDescriptor.setBlockIndex(targetRootDirectoryBufferIndex, FLAG_EMPTY);
                oft.getOftEntry(ROOT_DIRECTORY_INDEX).setCurrentBlockNumber(rootDescriptor.getBlockIndex(0));
                updateBitmap(rootDirectoryDataBlockIndex, 0); // update directory bitmap
                write_descriptor(ROOT_DIRECTORY_INDEX, rootDescriptor);
            }
        }


        if(isAutomaticMode()) {
           HelperUtility.writeMessageTofile(bufferedWriter, symbolicFileName + " destroyed");
        } else {
            System.out.println(symbolicFileName + " destroyed");
        }
    }

    /**
     * Search given file is exist in disk or not. If the file exist, it will return index of its descriptor #, else -1
     */
    public int search_directory(String symbolicFileName) {

        // Retrieve root directory file descriptor
        FileDescriptor rootDirectoryFileDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);

        for(int i = 0; i < 3; i++) {
            // Retrieve block index from descriptor
            int blockIndex = rootDirectoryFileDescriptor.getBlockIndex(i);

            byte[] buffer;
            // if non-empty block is found
            if(blockIndex != FLAG_EMPTY) {

                // read the block from the disk
                buffer = disk.read_block( blockIndex );

                for(int k = 0; k < IOSystem.LENGTH_OF_LDISK_BLOCK; k+=8) {
                    StringBuilder strBuilder = new StringBuilder();
                    if(buffer[k] != FLAG_EMPTY) {
                        for(int j = k; j < 4+k; j++) {
                            if(buffer[j] != FLAG_EMPTY) {
                                char ch = (char) buffer[j];
                                strBuilder.append(ch);
                            }
                        }

                        if(strBuilder.toString().trim().equals(symbolicFileName)) {
                            //System.out.println("Found");
                            return PackUtiltiy.unpack(buffer, k+4);

                        }
                    }
                }
            }
        }

        return FLAG_NOT_FOUND;

    }


    // TODO : Test this method, when open file
    @Override
    public void open(String symbolicFileName) throws IOException{

        // If the file exist, this value is index of the file's descriptor #; otherwise, FLAG_NOT_FOUND
        int resultOfSearching = search_directory( symbolicFileName );

        //System.out.println("Descriptor number : " + resultOfSearching);

        if(search_directory(symbolicFileName) == -1) {
           printErrorMessage();
           return;
        }

        // Check file is already opened or not
        if(isFileOpened(symbolicFileName)) {
           printErrorMessage();
           return;
        }

        if(oft.getFreeBuffer() == - 1) {
            printErrorMessage();
            return;
        }

        // If file exist, retrieve its information and update to OFT
        if(resultOfSearching != FLAG_NOT_FOUND) {

            // Retrieve descriptor information
            FileDescriptor targetFileDescriptor = FileDescriptor.retrieveFileDescriptor(resultOfSearching);

            int indexOfFirstOccupiedBlockNumber = targetFileDescriptor.getFirstOccupiedBlockIndex(); // 8

            // Retrieve available buffer from OFT table
            int freeOftBufferIndex = oft.getFreeBuffer();

            // TODO : Make sure the mechanism
            if(freeOftBufferIndex == FLAG_NOT_FOUND) {
                printErrorMessage();
            }

            OpenFileTableEntry oftEntry = oft.getOftEntry(freeOftBufferIndex);
            oftEntry.setFileDescriptorIndex(resultOfSearching);
            oftEntry.setCurrentBlockNumber(indexOfFirstOccupiedBlockNumber);
            oftEntry.setLengthOfFile(targetFileDescriptor.getFileLength());
            oftEntry.setEndOfFileIndex(targetFileDescriptor.getFileLength());
            oftEntry.setHasBeenWritten((oftEntry.getEndOfFileIndex() > 0));

            // update oft buffer
            oftEntry.setRwBuffer(copyBuffer(disk.read_block(targetFileDescriptor.getBlockIndex(indexOfFirstOccupiedBlockNumber))));

            oftEntry.setCurrentPosition(0);

            if(isAutomaticMode()) {
                HelperUtility.writeMessageTofile(bufferedWriter, symbolicFileName + " opened " + freeOftBufferIndex);
            } else {
                System.out.printf("%s opened %d\n", symbolicFileName, freeOftBufferIndex);
            }

        } else {

            printErrorMessage();

        }

    }

    /**
     *
     */

    // TODO
    @Override
    public void read(int index, int count) throws IOException {

        // Step 1 : Retrieve OpenFileTable Entry
        OpenFileTableEntry oftEntry = oft.getOftEntry(index);

       // Check oftEntry is empty or not;
       if(oftEntry.getFileDescriptorIndex() == -1) {
           printErrorMessage();
           return;
       }

        if(count > oftEntry.getLengthOfFile()) {
            count = oftEntry.getLengthOfFile() - oftEntry.getCurrentPosition();
        } else if(oftEntry.getCurrentPosition() + count > oftEntry.getLengthOfFile()) {
            count = oftEntry.getLengthOfFile() - oftEntry.getCurrentPosition();
        }

        int currentBlockIndex = oftEntry.getCurrentBlockNumber();

        int currentPosition = oftEntry.getCurrentPosition();



        StringBuilder stringBuilder = new StringBuilder();

        int counter = 0 ;
        if(currentPosition < oftEntry.getLengthOfFile()) {

            FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(oftEntry.getFileDescriptorIndex());

            do {
                //System.out.println("Current Position : " + oftEntry.getCurrentPosition());
                //System.out.println(oftEntry.getBufferAt(oftEntry.getCurrentPosition()%64));
                if(oftEntry.getCurrentPosition() >= oftEntry.getLengthOfFile()) {
                    break;
                }


                if ( isTimeToReadNextBlock(oftEntry.getCurrentPosition(), oftEntry.getCurrentBlockNumber())&& (currentBlockIndex < 2)) {
                    currentBlockIndex +=1;

                    oftEntry.setRwBuffer(disk.read_block(fd.getBlockIndex(currentBlockIndex)));
                    oftEntry.setCurrentBlockNumber(currentBlockIndex);

                }
                //System.out.println(oftEntry.getCurrentPosition()%64);
                //System.out.println(oftEntry.getBufferAt(oftEntry.getCurrentPosition()%64));
                stringBuilder.append(oftEntry.getBufferAt(oftEntry.getCurrentPosition()%64));

                oftEntry.increaseCurrentPosition();
                ++currentPosition;
                counter++;

            } while (count > counter);
        } else if (count == 0) {

            stringBuilder.append("");

        } else {

           printErrorMessage();
           return;

        }

        oftEntry.setCurrentPosition(currentPosition);

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, stringBuilder.toString().trim());
        } else {
            System.out.println(stringBuilder.toString().trim());
        }

    }

    /* *
     * Write characters to FileSystem
     */

    @Override
    public void write(int index, char character, int quantity) throws IOException {

        // Retrieve Open File Table entry
        OpenFileTableEntry oftEntry = oft.getOftEntry(index);

        // Retrieve descriptor number from the oft entry
        int descriptorNumber = oftEntry.getFileDescriptorIndex();

        // Retrieve file descriptor
        FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(descriptorNumber);

        // check exceeding MAX file size or not, it exceeding trim it.
        if(oftEntry.getCurrentPosition() + 1 + quantity > (IOSystem.LENGTH_OF_LDISK_BLOCK * 3)) {
            quantity = (IOSystem.LENGTH_OF_LDISK_BLOCK * 3) - (oftEntry.getCurrentPosition());
        }

        // ******* New logic : Feel 0s in the gap *******
        int eofIndex = oftEntry.getEndOfFileIndex(); //

        int actualCurrentPosition = oftEntry.getCurrentPosition();

        if( actualCurrentPosition > eofIndex) {

            int spaceSize = actualCurrentPosition - eofIndex;

            int startDesBlock = (eofIndex == 0) ? 0 : (eofIndex) / 64;
            int actualDesBlock = (actualCurrentPosition == 0) ? 0 : (actualCurrentPosition) / 64;

            OpenFileTableEntry tempEntry = new OpenFileTableEntry();
            if(startDesBlock == actualDesBlock) {
                //tempEntry.setRwBuffer(copyBuffer(oftEntry.getRwBuffer()));
                int testing = oftEntry.findFirstEmptySlotFromBuffer();
               // System.out.println("EOF : " + testing);
                for(int i = testing; i < (testing+spaceSize); i++) {
                    oftEntry.setBufferAtByte(i, (byte)0);
                }
            } else {
                tempEntry.setRwBuffer( copyBuffer(disk.read_block(fd.getBlockIndex(startDesBlock))) );

                tempEntry.setCurrentPosition(tempEntry.findFirstEmptySlotFromBuffer());
                tempEntry.setLengthOfFile(fd.getFileLength());

//                System.out.println("temp BLock Number ; " + tempEntry.getCurrentBlockNumber()); // -1
//                System.out.println("temp current : " + tempEntry.getCurrentPosition());
//                System.out.println("fd's filelength : " + fd.getFileLength());
//                System.out.println("des non-empty-block : " + fd.getLastNonEmptyBlockIndex());
//                System.out.println("get number of free block : " + fd.getNumberOfFreeBlocks());

                int counter = 0;
                while(counter < spaceSize) {
                    //System.out.println("Counter " + counter );
                    tempEntry.setBufferAtByte(tempEntry.getCurrentPosition() % 64, (byte)0);
                    tempEntry.increaseCurrentPosition(); //fine
                    tempEntry.increaseLengthOfFile();
                    counter++; // fine

                    if( (tempEntry.getCurrentPosition() % 64) == 0 && tempEntry.getCurrentPosition() != 0 ) {

                        // update length of file
                        fd.setFileLength(tempEntry.getLengthOfFile());

//                        System.out.println("LastNonEmptyBlockIndexFromDes : " + fd.getLastNonEmptyBlockIndex());
//                        System.out.println("Wrapped : " + fd.getBlockIndex(fd.getLastNonEmptyBlockIndex()));

                        // copy oft buffer to disk
                        disk.write_block(fd.getBlockIndex(fd.getLastNonEmptyBlockIndex()), copyBuffer(tempEntry.getRwBuffer()));


                        // find an empty data block in a disk
                        int indexOfFreeBlockFromDisk = findEmptyBitmap();
                        fd.setBlockIndex(fd.getEmptyBlockIndex(), indexOfFreeBlockFromDisk);

                        // update Bitamp
                        updateBitmap(indexOfFreeBlockFromDisk, FLAG_OCCUPIED);

                        // update current block number to OFT
                        tempEntry.setCurrentBlockNumber((fd.getFileLength() / 64));

                        // update Descriptor information
                        write_descriptor(oftEntry.getFileDescriptorIndex(), fd);

                        tempEntry.setRwBuffer(HelperUtility.getInitialBlockData());
                    }

                }

                oftEntry.setRwBuffer(copyBuffer(tempEntry.getRwBuffer()));
                oftEntry.setCurrentBlockNumber(actualDesBlock); //ok
                oftEntry.setCurrentPosition(tempEntry.getLengthOfFile());
                oftEntry.setLengthOfFile(tempEntry.getLengthOfFile());
                // REPORT
               // System.out.println("BLOCK INDEX: " + fd.getBlockIndex(startDesBlock));
               // System.out.println("FILE LENGTH @ DES : " + fd.getFileLength());
//                for(int i = 0;i < 3; i++)
//                {
//                    System.out.println(fd.getBlockIndex(i));
//                }

            }

        }

        // **** End of New Logic ****

        int counter = 0;
        boolean overWritingMode = false;

        while(quantity > counter) {

            overWritingMode = false;

            int availableBlockBufferSpace = fd.getNumberOfFreeBlocks(); // number of empty buffer slot in DES

            int targetSlot = oftEntry.findFirstEmptySlotFromCurrentPosition();

            //System.out.println("T@" + targetSlot+ " :::" + (oftEntry.getCurrentPosition() % 64));
            //System.out.println("POSSIBLE TO EXPAND : ? " + possibleToExpand(oftEntry.getCurrentBlockNumber(), availableBlockBufferSpace));
            if((targetSlot == -1) && ((oftEntry.getCurrentPosition() % 64) == 0)
                    //&& (oftEntry.getCurrentPosition() != 0)
                    && possibleToExpand(oftEntry.getCurrentBlockNumber(), availableBlockBufferSpace)    ){

//                System.out.println("EXPANDING");

                /** Expand buffer **/

                // update length of file
                fd.setFileLength(oftEntry.getLengthOfFile());
                // copy OFT buffer to disk
                disk.write_block(fd.getBlockIndex(fd.getLastNonEmptyBlockIndex()), copyBuffer(oftEntry.getRwBuffer()));

                // find an empty data block in a disk
                int indexOfFreeBlock = findEmptyBitmap();
                fd.setBlockIndex(fd.getEmptyBlockIndex(), indexOfFreeBlock);

                // update bitmap
                updateBitmap(indexOfFreeBlock, FLAG_OCCUPIED);

                // update current Block number to OFT
                oftEntry.setCurrentBlockNumber( oftEntry.getCurrentBlockNumber() + 1  );

                // update descriptor information
                write_descriptor(oftEntry.getFileDescriptorIndex(), fd);

                // Initialize oft buffer
                oftEntry.setRwBuffer(HelperUtility.getInitialBlockData());


            } else if((targetSlot == -1) && ((oftEntry.getCurrentPosition() % 64) == 0)
                    && (oftEntry.getCurrentPosition() != 0)
                    && (oftEntry.getCurrentPosition() != oftEntry.getLengthOfFile()) ) {

                /** Logic : Move to next buffer **/
                //System.out.println("JUST MOVE BUFFER");
                // copy oft buffer to disk
                disk.write_block(fd.getBlockIndex(oftEntry.getCurrentBlockNumber()), copyBuffer(oftEntry.getRwBuffer()));
                oftEntry.setCurrentBlockNumber(oftEntry.getCurrentPosition()/64);
                oftEntry.setRwBuffer(copyBuffer(disk.read_block(fd.getBlockIndex(oftEntry.getCurrentBlockNumber()))));

            }

            // If current slot is occupied with value,
            if((byte)oftEntry.getBufferAt(oftEntry.getCurrentPosition() % 64) != FLAG_EMPTY) {

                targetSlot = oftEntry.getCurrentPosition();
                overWritingMode = true;
            } else {
                targetSlot = oftEntry.findFirstEmptySlotFromCurrentPosition();
                //System.out.println("TargetSlot@" + targetSlot);
            }

            if(overWritingMode) {

                oftEntry.setBufferAt(oftEntry.getCurrentPosition() % 64, character);


            } else {
                /** Write empty slot **/
                oftEntry.setBufferAt(targetSlot, character);
                oftEntry.increaseLengthOfFile();

                fd.setFileLength(oftEntry.getLengthOfFile());
            }

            oftEntry.increaseCurrentPosition(); // increase cursor.
            oftEntry.setEndOfFileIndex(oftEntry.getLengthOfFile());
            counter++;
        } // End of while loop

//        System.out.println("current position :" + oftEntry.getCurrentPosition() );

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, quantity + " bytes written");
            oftEntry.setHasBeenWritten(TRUE_FLAG);
        } else {

            System.out.println(quantity + " bytes written");
            oftEntry.setHasBeenWritten(TRUE_FLAG);
        }
    }


    /* *
     * Move cursor to given position
     */
    // NEED TO CHECK
    // lseek is not dependent to current position.
    @Override
    public void lseek(int index, int position) throws IOException {

        // required component
        OpenFileTableEntry oftEntry = oft.getOftEntry(index);

        // Check file is opened
        if(oftEntry.getFileDescriptorIndex() == -1) {
            printErrorMessage();
            return;
        }


        FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(oftEntry.getFileDescriptorIndex());
        //System.out.println("oft.getLength() : " + oftEntry.getLengthOfFile() );
        //System.out.println("fd.getLength() : " + fd.getFileLength());

        // check position is valid or not
        if(position > MAX_FILE_LENGTH || position < 0) {
            printErrorMessage();
            return;
        }

        // Calculate target index
        int targetDescriptorBlockIndex = (position == 0 ) ? 0 : (position) / 64; // WARNING!!!!
      // System.out.println("Target Index :" + targetDescriptorBlockIndex);
        int currentIndex = oftEntry.getCurrentBlockNumber();
      // System.out.println("Current Index : " + currentIndex); // descriptor dependent

        // If targetBlockIndex is differ to current one,
        if(targetDescriptorBlockIndex != oftEntry.getCurrentBlockNumber() ) {



            // if unsaved data exist in buffer , save it to disk.
            if(oftEntry.getHasBeenWritten() ) {


                fd.setFileLength(oftEntry.getLengthOfFile());

                disk.write_block(fd.getBlockIndex(oftEntry.getCurrentBlockNumber()), copyBuffer(oftEntry.getRwBuffer()));

                write_descriptor(oftEntry.getFileDescriptorIndex(), fd);

                // if position is greater than current file length generate temp buffer and update it to oft.
                if( position >= oftEntry.getLengthOfFile()  ) {
                    OpenFileTableEntry newOftEntry = new OpenFileTableEntry();
                    newOftEntry.setCurrentBlockNumber(targetDescriptorBlockIndex);
                    newOftEntry.setCurrentPosition(position);
                    newOftEntry.setFileDescriptorIndex(oftEntry.getFileDescriptorIndex());
                    newOftEntry.setLengthOfFile(oftEntry.getLengthOfFile());
                    newOftEntry.setEndOfFileIndex(oftEntry.getLengthOfFile());
                    oft.setOftEntry(newOftEntry, index);// Be aware of shallow copy. TODO: Check its valid or not
                }
                else {
                   byte[] buffer = copyBuffer(disk.read_block(fd.getBlockIndex(targetDescriptorBlockIndex)));
                   oftEntry.setRwBuffer(buffer);
                   oftEntry.setCurrentPosition(position);
                   oftEntry.setCurrentBlockNumber(targetDescriptorBlockIndex);

                }

            } else { // within same block
                int actualDiskBlockIndex = fd.getBlockIndex(targetDescriptorBlockIndex);

                // Case : file never written, but changed the cursor point(increasing order)
                if(actualDiskBlockIndex == -1) {

                    OpenFileTableEntry newOftEntry = new OpenFileTableEntry();
                    newOftEntry.setCurrentBlockNumber(targetDescriptorBlockIndex);
                    newOftEntry.setCurrentPosition(position);
                    newOftEntry.setFileDescriptorIndex(oftEntry.getFileDescriptorIndex());
                    newOftEntry.setLengthOfFile(oftEntry.getLengthOfFile());
                    oft.setOftEntry(newOftEntry, index);

                }
                else {
                    byte[] buffer = copyBuffer(disk.read_block(actualDiskBlockIndex));
                    oftEntry.setRwBuffer(buffer);

                }
            }
        }


        oftEntry.setCurrentPosition(position);
        oftEntry.setCurrentBlockNumber(targetDescriptorBlockIndex);

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, "position is " + position);
        } else {
            System.out.println("position is " + position);
        }

    }

    /* *
     *  Close opened file
     */

    @Override
    public void close(int oftIndex) throws IOException {

        // Retrieve descriptor number from OFT
        OpenFileTableEntry oftEntry = oft.getOftEntry(oftIndex);
        // If current oftEntry is empty, display error message
        if(oftEntry.getFileDescriptorIndex() == -1) {

            if(isAutomaticMode()) {
                HelperUtility.writeMessageTofile(bufferedWriter, ERROR_MESSAGE);
            } else {
                System.err.println(ERROR_MESSAGE);
            }
            return;
        }

        FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(oftEntry.getFileDescriptorIndex());

        // cursor has not been moved
        if(oftEntry.getCurrentBlockNumber() == ((oftEntry.getLengthOfFile()-1) / IOSystem.LENGTH_OF_LDISK_BLOCK) ) {

            // write buffer data to disk
            disk.write_block(fd.getBlockIndex( fd.getLastOccupiedBlockIndex() ), copyBuffer(oftEntry.getRwBuffer()) );

        }

        // Update information to descriptor
        fd.setFileLength( oftEntry.getLengthOfFile() );
        write_descriptor(oftEntry.getFileDescriptorIndex(), fd);

        // Intialize current oft buffer
        oftEntry.init();

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, oftIndex + " closed");
        } else {
            System.out.printf("%d closed\n", oftIndex);
        }
    }

    /**
     *  Display name of all files in root directory (Done)
     */
    @Override
    public void directory() throws IOException {
        StringBuilder directoryList = new StringBuilder();

        FileDescriptor rootDirectoryDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);

        byte[] buffer = null;
        for(int i = 0; i < 3; i ++) {
            // ok
            if(rootDirectoryDescriptor.getBlockIndex(i) != -1) {

                buffer = disk.read_block( rootDirectoryDescriptor.getBlockIndex(i) );

                for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j = j + 8) {
                    StringBuilder fileName = new StringBuilder();
                    for(int k = 0; k < 4; k++) {
                        if(buffer[j+k] != -1 && buffer[j+k] !=0 )
                            fileName.append((char)buffer[j+k]);
                    }
                    if(fileName.length() != 0)
                        directoryList.append(fileName.toString() + " " );
                }

            }

        }

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, directoryList.toString().trim());
        } else {
            System.out.println(directoryList.toString().trim());
        }

    }

    /**
     * Getter
     * @return isAutomaticMode
     */
    public boolean isAutomaticMode() {
        return isAutomaticMode;
    }

    /**
     * Setter
     * @param isAutomaticMode
     */

    public void setAutomaticMode(boolean isAutomaticMode) {
        this.isAutomaticMode = isAutomaticMode;
    }

    /**
     * check filename exists in FileSystem
     */
    private boolean checkFileExist(String symbolicFileName) {

        // Retrieve root directory descriptor
        FileDescriptor fd = FileDescriptor.generateDescriptor(0);

        // check saved data is in the disk or not
        for(int i = 0; i < 3; i++) {
            int bufferIndex = fd.getBlockIndex(i);
            // if saved data exist, perform algorithm
            if(bufferIndex != -1) {
                byte[] buffer = disk.read_block(bufferIndex);
                for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j+=4) {
                    if(buffer[j] == symbolicFileName.charAt(0)) {

                        StringBuilder sb = new StringBuilder();

                        for(int k = j; k < j+4; k++) {
                            if(buffer[k] != -1) {
                                sb.append((char)buffer[k]);
                            }

                        }

                        if(symbolicFileName.equals(sb.toString().trim())) {
                            return true;
                        }
                    }
                } // End of Inner For loop with index j

            } // end of if
        } // End of outer for loop with index i

        return false;
    }



    /**
     *  Initialize disk
     */
    private void format() throws IOException {

        // basic disk initialization
        initBitmap();
        initDescriptorRegion();
        initDataRegion();
        totalNumberOfFilesInDisk = BITMAP_FLAG_EMPTY ;

        // Create root directory descriptor
        FileDescriptor rootDirectoryDescriptor = new FileDescriptor(ROOT_DIRECTORY_INDEX);
        rootDirectoryDescriptor.setBlockIndex(0, ROOT_DIRECTORY_DATA_INDEX);

        // Update root directory descriptor to disk
        write_descriptor(ROOT_DIRECTORY_INDEX, rootDirectoryDescriptor);

        // Mount root directory to oft
        OpenFileTableEntry rootEntry = oft.getOftEntry(ROOT_DIRECTORY_INDEX);
        rootEntry.setFileDescriptorIndex(ROOT_DIRECTORY_INDEX);
        rootEntry.setCurrentBlockNumber(7);
        rootEntry.setCurrentPosition(ROOT_DIRECTORY_INDEX);

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, "disk initialized");
        } else {
            System.out.println( "﻿disk initialized" );
        }
    }

    private void restoreDiskFromFile(String fileName) throws IOException {

        File file = new File("./", fileName);
        if(file.exists()) {

            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;

            try {
                fileInputStream = new FileInputStream(file.getAbsoluteFile());
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                for(int i = 0; i < IOSystem.NUMBER_OF_LDISK_BLOCKS;i++) {
                    byte[] buffer = disk.read_block(i);
                    fileInputStream.read(buffer);
                }

            }catch(Exception ex) {
                ex.printStackTrace();
            } finally {

                if(bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    }catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }

                if(fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    }catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            countFileNumberForRestoringDisk();
            if(isAutomaticMode()) {
                HelperUtility.writeMessageTofile(bufferedWriter, "disk restored");
            } else {
                System.out.println("disk restored");
            }

        } else {
            // STEP 1 : Create File
            file.createNewFile();

            // STEP 2 : DISK must be initialize.
            format();



        }
    }



    /**
     * Save the contents of the lDisk to "fileName".txt
     */
    @Override
    public void save(String fileName) throws IOException {

        saveAllBufferDataToDisk();

        File file = new File("./", fileName);

        if(!file.exists()){

            file.createNewFile();

        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        fos = new FileOutputStream(file.getAbsoluteFile());
        bos = new BufferedOutputStream(fos);

            for(int i = 0 ; i < IOSystem.NUMBER_OF_LDISK_BLOCKS; i++) {
                byte[] srcBuffer = disk.read_block(i);
                bos.write(srcBuffer);
            }

        bos.close();

        fos.close();

        if(isAutomaticMode()) {
            HelperUtility.writeMessageTofile(bufferedWriter, "disk saved");
        } else {
            System.out.println("disk saved");
        }

    }

    /* *
    * Before save, store all the data in the buffer to disk
    */
    private void saveAllBufferDataToDisk() {

        for(int i = 1; i < 4; i++ ) {
            OpenFileTableEntry oftEntry = oft.getOftEntry(i);
            if(oftEntry.getFileDescriptorIndex() == -1) {
                continue;
            }

            FileDescriptor fd = FileDescriptor.retrieveFileDescriptor(oftEntry.getFileDescriptorIndex());

            // cursor has not been moved
            if(oftEntry.getCurrentBlockNumber() == ((oftEntry.getLengthOfFile()-1) / IOSystem.LENGTH_OF_LDISK_BLOCK) ) {

                // write buffer data to disk
                disk.write_block(fd.getBlockIndex( fd.getLastOccupiedBlockIndex() ), copyBuffer(oftEntry.getRwBuffer()) );

            }

            // Update information to descriptor
            fd.setFileLength( oftEntry.getLengthOfFile() );
            write_descriptor(oftEntry.getFileDescriptorIndex(), fd);

        }

    }

    /* *
     * To deep copy the block
     */
    private byte[] copyBuffer(byte[] original) {
        byte[] temp = new byte[IOSystem.LENGTH_OF_LDISK_BLOCK];
        System.arraycopy(
                original,
                0,
                temp,
                0,
                temp.length
        );

        return temp;
    }

    // get blockNumber
    private int getBlockNumber(int blockIndex) {
        return (blockIndex / NUMBER_OF_DESCRIPTOR_IN_A_BLOCK) + 1;
    }
    // get descriptor Number
    private int getDescriptorLocation(int blockIndex) {
        return(blockIndex % NUMBER_OF_DESCRIPTOR_IN_A_BLOCK) * DESCRIPTOR_SIZE_IN_BYTES;
    }


    /* *
     * Update descriptor to disk
     */
    private void write_descriptor(int blockIndex, FileDescriptor fileDescriptor) {

        // update bitmap here.
        //updateBitmap(blockIndex, 1);

        int blockNumber = getBlockNumber(blockIndex);
        int locationOnBlock = getDescriptorLocation(blockIndex);

        byte[] blockFromDisk = disk.read_block(blockNumber);

        PackUtiltiy.pack(blockFromDisk, fileDescriptor.getFileLength(), locationOnBlock);
        for(int i =0; i < 3; i++) {
            locationOnBlock+=4;
            PackUtiltiy.pack(blockFromDisk, fileDescriptor.getBlockIndex(i),locationOnBlock);

        }

        disk.write_block(blockNumber, blockFromDisk);

    }

    /**
     *  Update root directory information to OFT buffer in restore mode.
     */
    private void mountRestoredRootDirectoryInformation() {

        // Retrieve root descriptor from the disk
        FileDescriptor rootDirectoryDescriptor = FileDescriptor.retrieveFileDescriptor(ROOT_DIRECTORY_INDEX);

        // update oft using the lastNonEmptyBlockIndex
        oft.getOftEntry(ROOT_DIRECTORY_INDEX).setRwBuffer(disk.read_block(7));

    }

    /**
     * Increase number of files in a disk;
     */
    private void incrementFileNumber() {
        this.totalNumberOfFilesInDisk+=1;
    }

    /**
     * Decrease number of files in a disk;
     */
    private void decrementFileNumber() {
        this.totalNumberOfFilesInDisk-=1;
    }

    /* Initialize Bitmap in a disk */
    private void initBitmap() {
        // Initialize bitmap and descriptors using the initial block (UP TO 7)
        for(int i =0; i <= NUMBER_OF_BITMAP_BLOCK + NUMBER_OF_DESCRIPTOR_BLOCK; i++ ) {
            updateBitmap(i, FLAG_OCCUPIED);
        }

        for(int i =8; i < IOSystem.LENGTH_OF_LDISK_BLOCK; i++) {
            updateBitmap(i, BITMAP_FLAG_EMPTY);
        }
    }

    /* Initialize Descriptor Region in a disk */
    private void initDescriptorRegion() {

        // Update to disk
        for(int i = 1; i <= NUMBER_OF_DESCRIPTOR_BLOCK; i++) {
            disk.write_block(i, HelperUtility.getInitialBlockData());
        }
    }

    /* Initialize data region in a disk */
    private void initDataRegion() {

        // Initialize data region
        for(int i = 7 ; i < IOSystem.NUMBER_OF_LDISK_BLOCKS; i++) {
            disk.write_block(i, HelperUtility.getInitialBlockData());
        }

    }

    /**
     *  Counter number of files in a disk checking the descriptor block for the restoring mode.
     */
    private void countFileNumberForRestoringDisk() {
        for(int i = 1; i < 7;i++) {
            byte[] buffer = disk.read_block(i);
            int position;
            if(i == 1)
                position = 23;
            else
                position = 7;

            for(; position < buffer.length; position+=16) {

                if(buffer[position] != -1) {
                    incrementFileNumber();
                }
            }
        }
    }

    /**
     * Update bitmap using given index and a binary flag
     */
    private void updateBitmap(int index, int flag) {

        if(index < 0 || 64 <= index)
        {
            System.out.println("current index " + index);
            System.err.println("@updateBitmap(int index, int flag) : Invalid flag index is used");
            return;
        }
        // Retrieve a target block from the disk
        byte[] bitmap = disk.read_block(0);

        if(flag == 1)
            bitmap[index] = 1;
        else if (flag == 0)
            bitmap[index] = 0;
        else // FOR DEBUGGING COMFORT
            System.err.println("@updateBitmap(int index, int flag) : Invalid flag value is used");

        // Update target block to the disk
        disk.write_block(0, bitmap);
    }

    /**
     *   Check given file is already opened or not
     */
    private boolean isFileOpened(String symbolicFileName) {

        int resultOfSearching = search_directory( symbolicFileName );

        for(int i = 1 ; i < 4; i++) {
            if(oft.getDescriptorIndex(i) == resultOfSearching) {
                return true;
            }
        }

        return false;

    }

    /**** Utility Methods ****/

    /**
     *  Looking for a empty slot
     */

    public int findEmptyBitmap() {

        byte[] bitmapBlock = disk.read_block(0); // 0th block is bitmap information;

        for(int i = 1; i <= bitmapBlock.length ; i++) {
            if(bitmapBlock[i] == BITMAP_FLAG_EMPTY)
                return i;
        }

        return FLAG_NOT_FOUND;
    }

    private int findEmptyDataRegion() {
        byte[] bitmapBlock = disk.read_block(0);

        for(int i = 8; i <= bitmapBlock.length; i++) {
            if(bitmapBlock[i] == BITMAP_FLAG_EMPTY)
                return i;
        }

        return FLAG_NOT_FOUND;
    }

    public int findEmptyFileDescriptor(){

        for(int i = 1; i < 7; i++){
            byte[] block = disk.read_block(i);
            for(int j = 0; j < IOSystem.LENGTH_OF_LDISK_BLOCK; j = j + DESCRIPTOR_SIZE_IN_BYTES) {
                if(block[j] == -1) {
                    int blockOffset = (i - 1) * NUMBER_OF_DESCRIPTOR_IN_A_BLOCK;
                    return j / DESCRIPTOR_SIZE_IN_BYTES + blockOffset;
                }
            }
        }

        return FLAG_NOT_FOUND;

    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /* *
     * Check its fine to read next block.
     */
    private boolean isTimeToReadNextBlock(int currentIndex, int currentBufferIndex) {

        /* This ternary operator is logically wrong.
         * Cannot be generalized if size of buffer is changed
         * , but its fine just cover in this case. */

        int bufferIndexBasedOnCurrentIndex = (currentIndex == 0) ? 0 : ( (currentIndex) / 64 );

        if(currentBufferIndex == bufferIndexBasedOnCurrentIndex)
            return false;

        return true;
    }

    /* *
     * Check buffer is expandable in file descriptor or not.
     */
    private boolean possibleToExpand(int currentIndex, int available) {
        switch(currentIndex) {
            case 0:
                if(available == 2)
                    return true;
                else
                    return false;

            case 1:
                if(available == 1)
                    return true;
                else
                    return false;

            case 2:
                return false;

        }

        return false;
    }

    /****** DEBUG PURPOSE METHODS ******/
    public void displayTotalNumberOfFile() {
        System.out.println("Total number of file in a disk: " + totalNumberOfFilesInDisk);
    }

    public void displayRootDirectoryBuffer() { HelperUtility.showRootOFT(oft);}

    public void displayOpenFileTable() {
        HelperUtility.showOpenFileTable(oft);
    }

    public void displayLdisk(){
        HelperUtility.showDisk(disk);
    }

    public void displayDescriptorTable() {
        HelperUtility.showDescriptor(disk);
    }

    public void displayDataTable() {
        HelperUtility.showData(disk);
    }

    public void displayPartialDataTable() {
        HelperUtility.showData2(disk);
    }
    public void displayOFT1() { HelperUtility.showOpenFileTable1(oft);}

} // End of Class