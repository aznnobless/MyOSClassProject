/**
 * Created by aznnobless on 1/7/15.
 */


public class PackUtiltiy {

    public static void pack(byte[] arr, int val, int loc) {
        final int MASK = 0xff;
        for (int i = 3; i >= 0; i--) {
            arr[loc + i] = (byte) (val & MASK);
            val = val >> 8;
        }
    }

    public static int unpack(byte[] arr, int loc) {
        final int MASK = 0xff;
        int v = (int) arr[loc] & MASK;
        for (int i = 1; i < 4; i++) {
            v = v << 8;
            v = v | ((int) arr[loc + i] & MASK);
        }
        return v;
    }



}
