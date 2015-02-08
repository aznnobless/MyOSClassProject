import java.io.IOException;

/**
 * Author       : Byung Ho Lee
 * Student ID#  : 60626811
 *
 * Created by Byung Ho Lee on 1/6/15.
 *
 * User interface requirement.
 * To pass, automatic tester, these methods must be implemented.
 */
public interface UserInterface {

    public abstract void create(String symbolicFileName) throws IOException;
    public abstract void destroy(String symbolicFileName) throws IOException;
    public abstract void open(String symbolicFileName) throws IOException;
    public abstract void close(int index) throws IOException;
    public abstract void read(int index, int count ) throws IOException;
    public abstract void write(int index, char chracter, int count ) throws IOException;
    public abstract void lseek(int index, int position) throws IOException;
    // return list of files
    public abstract void directory() throws IOException;

    /**
     * Restore lDisk from file.txt or create new (if no file)
     */
    public abstract void init(String fileName) throws IOException;

    /**
     * Save lDisk to file.txt
     */
    public abstract void save(String fileName) throws IOException;


}
