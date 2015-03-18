/**
 * Created by aznnobless on 3/8/15.
 */
public class TLBEntry {

    private int replacementInformation; // LRU
    private int segmentPageNumber; // s + p of the Virtual address
    private int startFrameAddress; // the starting Physical Address of the frame corresponding to the sp value.

    public TLBEntry(VirtualMemory vm) {

        segmentPageNumber = extractSegmentPageNumberFromVirtualMemory(vm);

    }

    public TLBEntry(VirtualMemory vm , int physicalAddress) {
        this(vm);
        startFrameAddress = physicalAddress;
    }

    public TLBEntry(int lru, int sp, int f ) {
        this.replacementInformation = lru;
        this.segmentPageNumber = sp;
        this.startFrameAddress = f;
    }

    public int extractSegmentPageNumberFromVirtualMemory(VirtualMemory vm) {

        int temp = 0;

        int s = vm.getSegmentNumber();
        int p = vm.getPageNumber();

        temp = temp | (s << 19);
        temp = temp | (p << 9);

        return temp;
    }

    public void setReplacementInformation(int priority) {

        if(priority < 0 || priority > 3 ) {
            System.err.println("Error@ TLBEntry.setReplacementInformation : invalid priority value");
            return;
        }

        replacementInformation = priority;
    }

    public void setSegmentPageNumber(int segmentPageNumber) {
        this.segmentPageNumber = segmentPageNumber;
    }

    public void setStartFrameAddress(int startFrameAddress) {
        this.startFrameAddress = startFrameAddress;
    }

    public void decreaseReplacementInformation() {

        replacementInformation -= 1;

    }

    public int getReplacementInformation() {
        return replacementInformation;
    }

    public int getSegmentPageNumber() {
        return segmentPageNumber;
    }

    public int getStartFrameAddress() {
        return startFrameAddress;
    }

}
