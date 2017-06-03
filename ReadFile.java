package bbtrial.nl.logicgate.ace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple file reader which reads a text file and returns
 * its contents as a list.  Each line of the file is a list
 * item.
 * @author smedlock
 */

public class ReadFile {
	
	public ReadFile(){
	}
	
	/**
	 * Reads a text file and returns its contents as a list.
	 * @param file
	 * @return List<String>
	 */
	public List<String> readLines(File file){
		FileReader fileReader = null;
		BufferedReader in = null;
		List<String> lines = new LinkedList<String>();
        	try {
            	fileReader = new FileReader(file);
            	in = new BufferedReader(fileReader);

            	String line;
            	while (true) {
            		line = in.readLine();
            		if (line == null) {
            			break;
            		} else {
	                	lines.add(line);
            		}
            	}
			return lines;
		} catch (IOException e) {
			//if the file does not exist, return an empty list and log the error.
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "IOException reading file" + file + "\n");
			return lines;
        	} finally {
        		close(in);
        		close(fileReader);
        	}
	}

	/**
	 * Reads a file and returns its contents as a string.
	 * @param file
	 * @return
	 */
	public String readFileAsString(File file){
		FileReader fileReader = null;
		BufferedReader in = null;
		try {
			fileReader = new FileReader(file);
			in = new BufferedReader(fileReader);
			StringBuffer fileData = new StringBuffer(1000);
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=in.read(buf)) != -1){
				fileData.append(buf, 0, numRead);
			}
			return fileData.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(in);
			close(fileReader);
		}
}

	
	/**
	 * Closes the reader.
	 * @param reader
	 */
	private void close(Reader reader) {
		try {
            		if (reader != null) {
				reader.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
