package bbtrial.nl.logicgate.ace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A simple file writer.  
 * Allows writing of a new file/overwriting a file,
 * appending a file, and copying a file with a new name.
 * @author smedlock
 *
 */
public class WriteFile {

	public WriteFile(){
	}
	
	/**
	 * Writes a new file or overwrites the file with the
	 * specified file name.  Text of the file is provided
	 * as a single string.
	 * @param filename
	 * @param fileText
	 */
	public void overWriteFile(File file, String fileText){
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(file));
	        out.write(fileText);
	        out.close();
	    } catch (IOException e) {
	    }
	}
	
	/**
	 * Appends the existing file of the specified filename
	 * with the specified text, provided as a single string.
	 * If file does not exist, creates a new file with the
	 * specified filename.
	 * @param filename
	 * @param fileText
	 */
	public void appendFile(File file, String fileText){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.write(fileText);
			out.close();
		} catch (IOException e){
		}
	}

	/**
	 * Copies a file under a new name
	 * @param in : the file to be copied
	 * @param out : the new file path/name
	 * @throws IOException
	 */
	public void copyFile(File in, File out) 
    throws IOException 
{
    FileChannel inChannel = new
        FileInputStream(in).getChannel();
    FileChannel outChannel = new
        FileOutputStream(out).getChannel();
    try {
        inChannel.transferTo(0, inChannel.size(),
                outChannel);
    } 
    catch (IOException e) {
        throw e;
    }
    finally {
        if (inChannel != null) inChannel.close();
        if (outChannel != null) outChannel.close();
    }
}

}
