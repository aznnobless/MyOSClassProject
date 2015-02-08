/**
 * Author       : Byung Ho Lee
 * Student ID#  : 60626811
 *
 * Created by Byung Ho Lee on 1/6/15.
 */

public class IOSystem implements IOInterface {

    private static IOSystem disk = null;
    // LDisk
    public static final int NUMBER_OF_LDISK_BLOCKS = 64; // ldisk : 64 blocks
    public static final int LENGTH_OF_LDISK_BLOCK = 64; // block = 64bytes = 16 integer (In Java int is 32bit (4bytes))

    private byte[][] lDisk;


    /**
     *  Default Constructor
     */
    private IOSystem() {

       lDisk = new byte[NUMBER_OF_LDISK_BLOCKS][LENGTH_OF_LDISK_BLOCK];
    }

    /**
     *  Write i-th block with passed array
     */
    @Override
    public void write_block(int i, byte[] p) {

        if(!isValidIndex(i)) {

            System.err.println("ERROR@IOSystem.write_block() : Invalid index");

        } else {

            // System.arraycopy(src, srcPos, dest, destPos, length)
            System.arraycopy(p, 0, lDisk[i], 0, LENGTH_OF_LDISK_BLOCK);

        }
    }

    /**
     *  Read i-th block in the lDisk
     */
    @Override
    public byte[] read_block(int i){
        if(!isValidIndex(i)) {
            System.err.println("ERROR@IOSystem.read_block() : Invalid index");

        }

        return this.lDisk[i];

    }

    /**
     *  Check index is valid or not
     */
    private boolean isValidIndex(int index) {
        if( (index < 0) || (NUMBER_OF_LDISK_BLOCKS <= index)){
            System.out.println(index);
            displayIOErrorMessage("isValidIndex()");
            return false;
        }

        return true;

    }

    /**
     *  print out the invalid block index error
     */
    private void displayIOErrorMessage(String from) {
        System.err.println("Error@" + from + ": Invalid block index");
    }

    /**
     * Debugging Purpose:
     * print out the contents of lDisk
     */
    public void showDisk() {

    }

    // Singleton pattern is applied.
    public static IOSystem getInstance() {

        if(disk==null)
            disk = new IOSystem();

        return disk;
    }

    /**
     * Testing purpose
     */
    public static void main(String[] args) {
        IOSystem myIO = new IOSystem();

        byte[] temp = { 'z', 'x', 'q', 'w', 'e', 't'};

        myIO.showDisk();

        System.out.println("After write operation");
        try {
            myIO.write_block(7, temp);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        myIO.showDisk();
    }


}
