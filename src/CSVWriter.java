package com.benja;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/***
 * This class write the log file
 * Log file are saved in CSV format
 */
public class CSVWriter {
	String csvFile;
	
	public CSVWriter(String filename){
		this.csvFile = filename;
	}
	
	public void write(double data){
		BufferedWriter bw = null;		
		try {
			bw = new BufferedWriter(new FileWriter(csvFile,true));
			bw.write(String.valueOf(data));
			bw.write(";");
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
