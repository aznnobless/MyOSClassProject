/* * * * * * * *
 *
 *  The physical memory is represented as an array of integers, each corresponding to
 *  one addressable memory word. It is is implemented as an array of 524,288 integers ( = 2MB ).
 *
 *  There are divide into 1024 frames of size 512 word(integers);
 *
 *  The ST always resides in frame 0 of PM and is never paged ot.
 *
 * * * * * * */
public class PhysicalMemory {

    public final static int NUMBER_OF_FRAMES = 1024;
    public final static int FRAME_SIZE = 512;
    public final static int SEGMENT_TABLE_SIZE = FRAME_SIZE;
    public final static int PAGE_TABLE_SIZE = NUMBER_OF_FRAMES;
    public final static int SIZE_OF_PA = 19; // Size of Physical address is 19 bits
    public final static int STARTING_ADDRESS_OF_ST = 0; // start position of segmenttable
    public final static int SIZE_OF_PM = NUMBER_OF_FRAMES * FRAME_SIZE;

    public static PhysicalMemory sPM; // Singleton pattern instance

    private int[] pm;
    private Bitmap bitmap;
    private SegmentTable segmentTable;

    // Constructor
    private PhysicalMemory() {

        pm = new int[SIZE_OF_PM];
        bitmap = new Bitmap();
        segmentTable = new SegmentTable();

        // Frame 0 is always segment table;
        bitmap.setTargetBitFlagUp(STARTING_ADDRESS_OF_ST);

        for(int i = 0 ; i < SIZE_OF_PM; i++) {
            pm[i] = -1;
        }

        syncSegmentTable();
    }
    /*
     *  Bitmap manipulation
     */

    public void updateTargetFrameBitmapFlagUp(int targetFrameIndex) {
        bitmap.setTargetBitFlagUp(targetFrameIndex);
    }

    public void updateTargetFrameBitmapFlagDown(int targetFrameIndex) {
        bitmap.setTargetBitFlagDown(targetFrameIndex);
    }

    public int searchFirstFreeFrameUsingBitmap(int startIndex) {
        return bitmap.findFirstFreeEmptyFrame(startIndex);
    }

    /*
     *
     */

    public void updateSegmentTableEntry(int index, int startAddressOfPageTable) {

        if(index < 0 || index > 511) {
            System.err.println("Error @ PhysicalMemory.updateSegmentTableEntry() : invalid index");
            return ;
        }

        segmentTable.getSegmentTable()[index] = startAddressOfPageTable;

        if(startAddressOfPageTable > 0) {
            reservePageTable(startAddressOfPageTable);
        }

        syncSegmentTable();

    }

    public int getValueAtIndex(int index) {

        if(index == -1) {
            return -1;
        }
        //System.out.println("DEBUG : " + index);
        return pm[index];
    }

    public int getSegementTableEntry(int segmentNumber) {
        if(segmentNumber >= 0 || segmentNumber < 512 )
            return getValueAtIndex(segmentNumber);
        else
            return -1;
    }

    public int getPageTableEntry(int pageNumber) {
        if(pageNumber >= 512)
            return getValueAtIndex(pageNumber);
        else
            return -1;
    }


    public void updatePageTableEntryToPhysicalMemory(int pageIndex, int segmentEntryIndex, int startingAddressOfPage) {

        int pageTableIndex = pm[segmentEntryIndex];

        pm[pageTableIndex + pageIndex] = startingAddressOfPage;

        // if page table exist
        if(pageTableIndex > 0 && startingAddressOfPage > 0) {

            reservePage(startingAddressOfPage);

        }

    }

    public void reservePageTable(int startIndexOfPageTable) {

        int targetFrameNumber = getFrameNumber(startIndexOfPageTable);

        // initailize pagetable with 0
        for(int i = targetFrameNumber * FRAME_SIZE; i < ( (targetFrameNumber+2)*FRAME_SIZE); i++) {
            pm[i] = 0;
        }

        if(bitmap.isTargetFrameOccupied(targetFrameNumber) || bitmap.isTargetFrameOccupied(targetFrameNumber+1)) {
            System.err.println("Error @ PhysicalMemory.reservePageTable : target frames are not empty");
            return;
        }

        bitmap.setTargetBitFlagUp(targetFrameNumber);
        bitmap.setTargetBitFlagUp(targetFrameNumber+1);

    }

    public void reservePage(int startIndexOfPage) {

        int targetFrameNumber = getFrameNumber(startIndexOfPage);

        if(bitmap.isTargetFrameOccupied(targetFrameNumber)) {
            System.err.println("Error @ PhysicalMemory.reservePage : target frame is not empty");
            return;
        }

        bitmap.setTargetBitFlagUp(targetFrameNumber);
    }


    public int getFrameNumber(int index) {

        if(index < 0 || index >= SIZE_OF_PM) {
            System.err.println("Error @ PhysicalMemoery.getFrameNumber() : invalid index");
            return -2;
        }

        return index / FRAME_SIZE;

    }



    public boolean isTargetFrameEmpty(int indexOfTargetFrame) {

        if(indexOfTargetFrame < 0 || indexOfTargetFrame >= NUMBER_OF_FRAMES) {
            System.err.println("Error@ PhysicalMemory.isTargetGrameEmpty() : Invalid Index");
            return false;
        }
        return !bitmap.isTargetFrameOccupied(indexOfTargetFrame);
    }

    public boolean isTwoConsecutiveFrameEmpty(int indexOfStartFrame) {

        if(indexOfStartFrame < 0 || indexOfStartFrame >= NUMBER_OF_FRAMES) {
            System.err.println("Error@ PhysicalMemory.isTargetGrameEmpty() : Invalid Index");
            return false;
        }


        if(indexOfStartFrame == 1023) {
            return false;
        }

        boolean result = true;

        for(int i = 0; i < 2; i++ ) {
            if(!isTargetFrameEmpty(indexOfStartFrame + i)) {
                result = false;
            }

        }

        return result;
    }

    public boolean isSegmentEntryIndexEmpty(int indexOfTargetFrame) {
        return bitmap.isTargetFrameOccupied(indexOfTargetFrame);
    }




    public void showBitmap() { bitmap.showBitmap(); }

    public void showSegmentTableInPhysicalMemory() {
        for(int i = STARTING_ADDRESS_OF_ST ; i < FRAME_SIZE; i++ ) {
            System.out.println(i + " : " + pm[i]);
        }
    }

    // OK
    // Update segement table information to the Physical Memory
    private void syncSegmentTable() {

        for(int i = STARTING_ADDRESS_OF_ST; i < FRAME_SIZE; i++) {
            pm[i] = segmentTable.getSegmentTable()[i];
        }

    }

    // OK
    public void showTargetFrameUsingAddress(int startingAddress) {

        int targetFrameNumber = getFrameNumber(startingAddress);

        int startAddress = targetFrameNumber * FRAME_SIZE;

        System.out.println("Frame " + targetFrameNumber + " Info");
        for(int i = 0; i < NUMBER_OF_FRAMES;i++) {
            System.out.print(pm[startAddress + i] + " ");
        }

    }

    // OK
    public void showTargetFrameUsingFrameNumber(int targetFrameNumber) {

        int startAddress = targetFrameNumber * FRAME_SIZE;

        System.out.println("Frame " + targetFrameNumber + " Info");
        for(int i = STARTING_ADDRESS_OF_ST; i < NUMBER_OF_FRAMES;i++) {
            System.out.print(pm[startAddress + i] + " ");
        }

    }


    // Singleton
    public static PhysicalMemory getPhysicalMemory() {
        if(sPM == null) {
            sPM = new PhysicalMemory();
            return sPM;
        }
        else
            return sPM;
    }

}
