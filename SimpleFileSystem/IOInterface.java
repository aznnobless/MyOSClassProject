/**
 * Author       : Byung Ho Lee
 * Student ID#  : 60626811
 *
 * Created by Byung Ho Lee on 1/6/15.
 *
 * I/O System Interface:
 *     - read_block(int i, char* p)
 *     - write_block(int i, char* p)
 *
 *  - each command reads or writes on entire block. (B bytes)
 *  - memory area(*p) is also a byte array.
 *
 * NOTE: File System can access IOSystem using only these functions ( no direct access to ldisk is allowed).
 */

public interface IOInterface {

    public abstract byte[] read_block(int i) throws Exception;

    public abstract void write_block(int i, byte[] p) throws Exception;

}
