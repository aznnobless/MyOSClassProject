/**
 * Created by aznnobless on 3/5/15.
 */
public class SegmentTable {

    private int[] segmentTable;

    public SegmentTable() {

        segmentTable = new int[PhysicalMemory.SEGMENT_TABLE_SIZE];

        for(int i = 0; i < segmentTable.length; i++) {
            segmentTable[i] = 0;
        }

    }

    public void readSegmentEntry(int segmentEntryIndex) {

        // Check segmentEntryIndex is valid
        if(segmentEntryIndex < 0 || segmentEntryIndex >= PhysicalMemory.SEGMENT_TABLE_SIZE) {
            System.err.println(
                    "ERROR @ SegmentTable.readSegmentEntry() : invalid segmentEntryIndex " + segmentEntryIndex);
            return;
        }

        int targetEntryValue = segmentTable[segmentEntryIndex];

        if(targetEntryValue == 0) {
            System.out.println("ERROR(requirement) @ PT does not exist ");
        }else if(targetEntryValue == -1) {
            System.out.println("ERROR(requirement) @ Page Fault");
        } else {
            // TODO : READ LOGIC
        }

    }

    public void setSegmentEntry(int segmentEntryIndex, int value) {

        if(segmentEntryIndex < 0 || segmentEntryIndex > 511) {
            System.err.println(
                    "ERROR @ SegmentTable.setSegmentEntry() : invalid segmentEntryIndex " + segmentEntryIndex);
            return;
        }


        segmentTable[segmentEntryIndex] = value;
    }

    public int getSegmentEntry(int segmentEntryIndex) {

        if(segmentEntryIndex < 0 || segmentEntryIndex >= 512) {
            System.err.println(
                    "ERROR @ SegmentTable.getSegmentEntry() : invalid segmentEntryIndex " + segmentEntryIndex );
            return -2;
        }

        return segmentTable[segmentEntryIndex];
    }

    public int[] getSegmentTable() {
        return segmentTable;
    }

}
