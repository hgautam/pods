package com.ebay.pods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) throws Exception {
		RandomAccessFile raf = new RandomAccessFile("c:\\fstab.txt", "rw");
		raf.seek(raf.length() -1) ;
		
		//System.out.println(raf.readLine());
		raf.writeBytes("\n#new data added\n");
		raf.writeBytes("/dev/sdb1       /esEbay        ext4    defaults,auto,_netdev 0 0");
		raf.close();
		
	}
		
}	

