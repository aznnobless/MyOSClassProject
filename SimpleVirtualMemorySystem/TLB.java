import java.util.ArrayList;

public class TLB {

    ArrayList<TLBEntry> buffer;
    int size;

    public TLB() {

        size = 0;
        buffer = new ArrayList<TLBEntry>(size);

    }

    public TLBEntry createTLBEntry(VirtualMemory vm, int physicalAddress) {

        TLBEntry tlbEntry = new TLBEntry(vm, physicalAddress);
        tlbEntry.setReplacementInformation(3);

        return tlbEntry;
    }

    public int search(TLBEntry entry) {

        int result = -1;
        for(int i = 0 ; i < buffer.size(); i++) {

            if(buffer.get(i).getSegmentPageNumber() == entry.getSegmentPageNumber()) {
                result = i;
                break;
            }

        }

        return result;

    }

    public int search(VirtualMemory vm) {

        TLBEntry entry = new TLBEntry(vm);

        int result = -1;
        for(int i = 0 ; i < buffer.size(); i++) {

            if(buffer.get(i).getSegmentPageNumber() == entry.getSegmentPageNumber()) {
                result = i;
                break;
            }

        }

        return result;
    }

    public TLBEntry findTagetEntry(TLBEntry src) {

        TLBEntry entry = buffer.get(0);

        for(int i = 1; i < buffer.size(); i++) {
            if(src.getSegmentPageNumber() == buffer.get(i).getSegmentPageNumber() ) {
                entry = buffer.get(i);
                break;
            }
        }

        return entry;

    }
    // This method can be refactored. Due to lack of time, just keep the logic.
    public void add(TLBEntry entry, boolean hit) {

        if(size == 0) {

            buffer.add(entry);

        }
        else if(size < 4) {

            if(hit) {

                // STEP 1: Find target entry (When hit find the entry that has same value)
                TLBEntry replaceCandidate = findTagetEntry(entry);
                int indexOfReplaceCandidate = buffer.indexOf(replaceCandidate);

                // STEP 2: Decrement all LRU values greater than target by 1
                for(TLBEntry tlbEntry : buffer) {
                    if(tlbEntry.getReplacementInformation() > replaceCandidate.getReplacementInformation())
                        tlbEntry.decreaseReplacementInformation();
                }
                // STEP 3: set target entry's LRU = 3
                buffer.get(indexOfReplaceCandidate).setReplacementInformation(3);


            } else {

                // STEP 1 : Decrement all entries LRU values by 1
                for(TLBEntry tlbEntry : buffer) {
                    tlbEntry.decreaseReplacementInformation();
                }

                // STEP 2 : Add new entry with LRU value 3
                buffer.add(entry);
            }

        }
        // When buffer is full
        else {

            if(hit) {
                // STEP 1: Find target entry
                TLBEntry replaceCandidate = findTagetEntry(entry);
                int indexOfReplaceCandidate = buffer.indexOf(replaceCandidate);

                // STEP 2: Decrement all LRU values greater than target by 1
                for(TLBEntry tlbEntry : buffer) {
                    if(tlbEntry.getReplacementInformation() > replaceCandidate.getReplacementInformation())
                        tlbEntry.decreaseReplacementInformation();
                }
                // STEP 3: replace the target entry's LRU = 3
                buffer.get(indexOfReplaceCandidate).setReplacementInformation(entry.getReplacementInformation());

            } else {
                // STEP 1 : Find the entry with the lowest LRU
                TLBEntry replaceCandidate = findLowestPriorityEntry();
                int indexOfReplaceCandidate = buffer.indexOf(replaceCandidate);

                // STEP 2 : Decrement all element's LRU by 1
                for(TLBEntry tlbEntry : buffer) {
                    tlbEntry.decreaseReplacementInformation();
                }

                // STEP 3: replace the value
                buffer.set(indexOfReplaceCandidate, entry);

            }


        }

        size = buffer.size();

    }

    public TLBEntry getTLBEntryFromBuffer(int index) {
        if(index < 0 || index >= 4) {
            System.err.println("ERROR @ TLB.getTLBEntryFromBuffer : Invalid Index");
            return null;
        }
        return buffer.get(index);
    }

    private TLBEntry findLowestPriorityEntry() {

        TLBEntry entry = buffer.get(0);

        for(int i = 1; i < buffer.size(); i++) {
            if(entry.getReplacementInformation() > buffer.get(i).getReplacementInformation() )
                entry = buffer.get(i);
        }

        return entry;
    }

    public void showTLB() {

        System.out.println();
        System.out.println("------- TLB Contents -------");
        for(int i = 0 ; i < buffer.size(); i++) {
            System.out.println("Buffer[" + i + "] : " + buffer.get(i).getReplacementInformation()
                    + " " + buffer.get(i).getSegmentPageNumber()
                    + " " + buffer.get(i).getStartFrameAddress() );
        }
        System.out.println("------- END OF TLB -------");
        System.out.println();
    }

}
