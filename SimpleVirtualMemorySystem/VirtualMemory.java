/**
 * Created by aznnobless on 3/6/15.
 */
public class VirtualMemory {

    private int virtualAddress;
    private int segmentNumber;
    private int pageNumber;
    private int offsetWithPage;

    // Default Constructor
    public VirtualMemory() {
        // intentionally empty;
    }

    // Constructor with 3 parameters
    public VirtualMemory(int segSize, int ptSize, int pgSize) {

        segmentNumber = segSize;
        pageNumber = ptSize;
        offsetWithPage = pgSize;

        generateVA();

    }

    // generate VirtualAddress
    public void generateVA() {
        genSegmentNumber(segmentNumber);
        genPageNumber(pageNumber);
        genOffsetWithPage(offsetWithPage);

    }

    // Generate segment number part of virtual address, then store it to virtualAddress
    private void genSegmentNumber(int size) {

        int temp = size << 19;
        virtualAddress = virtualAddress | temp;
    }

    // Generate page number part of virtual address, then store it to virtualAddress
    private void genPageNumber(int size) {
        int temp = size << 9;
        virtualAddress = virtualAddress | temp;
    }

    // Generate offset part of virtual address, then store it to virtualAddress
    private void genOffsetWithPage(int size) {

        virtualAddress = virtualAddress | size;
    }

    /** Getters **/
    public int getSegmentNumber() {
        return segmentNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getOffsetWithPage() {
        return offsetWithPage;
    }

    public int getVirtualAddress() {
        return virtualAddress;
    }
    /** End of Getters **/

}
