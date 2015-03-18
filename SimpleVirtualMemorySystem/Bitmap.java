/**
 * NOTE: Bitmap manipulation is very tedious. Let's make my personal bit manipulation API.
 *
 * This Bitmap manipulation is Big Endian style.
 *
 * A bitmap is used to keep track of free/occupied frames.
 * The bit map consists of 1024 bits (one per frame).
 * There are total 1024 frames in physical memory.
 * It can be implemented as an array of 32 integers.
 *
 */
public class Bitmap {

    // Member variables
    private int[] bitmap;
    private int[] masks;
    private int size;

    // Default Constructor
    public Bitmap() {

        /* NOTE : The bitmap consists of 1024 bits. Thus, can be implemented as an array of 32 int */
        this(32);

    }

    // Constructor with one parameter
    public Bitmap(int sizeOfArray) {
        size = sizeOfArray;
        bitmap = new int[sizeOfArray];
        masks = new int[sizeOfArray];

        // Generat bitmask
        masks[sizeOfArray-1] = 1;
        for(int i = sizeOfArray-2; i >= 0; i--) {
            masks[i] = masks[i+1] << 1;
        }

        //Debug Purpose
                    //for(int i = 0 ; i < 32; i++) {
//                       showBit(masks[i]);
                    //}
        //End of Dubug
    }

    // Set given index flag up
    public void setTargetBitFlagUp(int indexOfTargetFrame) {

        int targetFrameIndex = getTargetFrameIndex(indexOfTargetFrame);

       // System.out.println("targetFrameIndex: " + targetFrameIndex);

        int targetBitIndex = getTargetBitIndex(indexOfTargetFrame);

        // System.out.println("targetBitIndex: " + targetBitIndex);

        int integerContainer = bitmap[targetFrameIndex];

        // System.out.println("integerContainer: " + integerContainer);

        int result = integerContainer | masks[targetBitIndex];

        // System.out.println("result: " + result);

        bitmap[targetFrameIndex] = result;

    }

    // Set given index flag down
    public void setTargetBitFlagDown(int indexOfTargetFrame) {

        int targetFrameIndex = getTargetFrameIndex(indexOfTargetFrame);

        int targetBitIndex = getTargetBitIndex(indexOfTargetFrame);

        int integerContainer = bitmap[targetFrameIndex];

        int result = integerContainer & ~ masks[targetBitIndex];

        bitmap[targetFrameIndex] = result;


    }

    public int findFirstFreeEmptyFrame(int startFrameIndex) {

        int targetFrameContainerIndex = getTargetFrameIndex(startFrameIndex); // 0 ~ 31
        int targetBitIndex = getTargetBitIndex(startFrameIndex);

        int result = -1;

        for(int i = targetFrameContainerIndex; i < bitmap.length; i++) {

            String temp = convertIntegerTo32BitBinaryString(bitmap[i]);

            for(int j = targetBitIndex; j < 32; j++ ) {
                if(temp.charAt(j) == '0') {
                    result = ( (i * 32) + j );

                    break;
                }
            }

            if(result != -1) {
                break;
            }
        }

        // System.out.println("result : " + result);

        return result;
    }




    public void showBit(int indexOfContainer) {
//        System.out.println("------------------------------");
//        System.out.println(Integer.toBinaryString(bitmap[indexOfContainer]).length() );
        System.out.println(convertIntegerTo32BitBinaryString(bitmap[indexOfContainer]));
    }

    public void showBitmap() {

        for(int i = 0 ; i < 32; i++) {

            showBit(i);

        }
     }


    public boolean isTargetFrameOccupied(int indexOfTargetFrame) {

        //System.out.println("indexOfTargetFrame :" + indexOfTargetFrame);

        int targetFrameIndex = getTargetFrameIndex(indexOfTargetFrame);

        //System.out.println("targetFrameIndex :" + targetFrameIndex);

        int targetBitIndex = getTargetBitIndex(indexOfTargetFrame);

        // System.out.println("targetBitIndex :" + targetBitIndex);

        int integerContainer = bitmap[targetFrameIndex];

        // System.out.println("integerContainer : " + integerContainer);

        String temp = convertIntegerTo32BitBinaryString(integerContainer) ;


        // System.out.println("temp : " + temp);

        // System.out.println("targetBit value : " + temp.charAt(targetBitIndex));

        return temp.charAt(targetBitIndex) == '1';

    }

    public String convertIntegerTo32BitBinaryString(int num) {

        String temp = Integer.toBinaryString(num);
        StringBuilder sb = new StringBuilder();

        if (temp.length() != 32) {
            int required = 32 - temp.length();

            for (int i = 0; i < required; i++) {
                sb.append('0');
            }

            for (int i = 0; i < temp.length(); i++) {
                sb.append(temp.charAt(i));
            }

            temp = sb.toString();
        }

        return temp;

    }

    public int getTargetBitIndex(int indexOfTargetFrame) {
        if(indexOfTargetFrame < 0 || indexOfTargetFrame > 1023) {
            System.err.println("ERROR @ Bitmap.getTargetBitIndex() : Invalid index" );
            return 0;
        }

        return indexOfTargetFrame % 32;
    }


    // paramer: count from 0; range 0 - 1023
    public int getTargetFrameIndex(int indexOfTargetFrame) {
        if(indexOfTargetFrame < 0 || indexOfTargetFrame > 1023) {
            System.err.println("ERROR @ Bitmap.getTargetFrame() : Invalid index" );
            return 0;
        }
        return indexOfTargetFrame / 32;
    }

    public static void main(String[] args) {
        Bitmap bitmap = new Bitmap();
        bitmap.showBitmap();

//        bitmap.setTargetBitFlagUp(0);
//        System.out.println("show bit:");
//        bitmap.showBit(0);
//
//        System.out.println(bitmap.isTargetFrameOccupied(0));
//        System.out.println(bitmap.isTargetFrameOccupied(1));


//        bitmap.setTargetBitFlagUp(0);
//        bitmap.showBit(0);
//
//        System.out.println(bitmap.isTargetFrameOccupied(0));
//
//
//        bitmap.setTargetBitFlagUp(27);
//        bitmap.showBit(0);
//        System.out.println(bitmap.isTargetFrameOccupied(26));

//
//        bitmap.setTargetBitFlagDown(0);
//        bitmap.showBit(0);
//
//
//        bitmap.setTargetBitFlagUp(2);
//        bitmap.showBit(0);
//
//        bitmap.setTargetBitFlagUp(32);
//        bitmap.showBit(1);
    }

}
