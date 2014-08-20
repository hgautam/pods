package com.ebay.pods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class UpdateFStab {
	    public static void main(String args[]){
	    	try {
				ConnectionManager.createTrustedConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	String diskName = null;
	    	String fsTab = "/etc/fstab";
	    	//call exec command to find the disk name
			ArrayList <String> data = new ArrayList<String>();
		    String cmd = "/etc/puppet/work/scripts/df.sh";
			data = ExecCommand.runCmd(cmd);
			//data.add("0");
			//data.add("/dev/sdb1");
			int status = Integer.parseInt(data.get(0));
			System.out.println("command status is "+ status);
			if (status == 0) {
				diskName = data.get(1);
				System.out.println("disk name is "+ diskName);
				if (diskName == null || diskName.length() < 3) {
					System.out.println("Incorrect disk name");
					InstallStorage.postMsg(false, "Incorrect disk name");
					System.exit(1);
				}
				
			}else {
				System.out.println("Command unsuccessful");
				InstallStorage.postMsg(false, "Command unsuccessful");
				System.exit(1);
			}
			
            //check if fstab exists
	        try {
	            File inputFile = new File(fsTab);
	            if (!inputFile.isFile()) {
	                System.out.println(fsTab + " is missing");
	                return;
	            }
	            //Construct the new file that will later be renamed to the original filename.
	            File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");
	            BufferedReader br = new BufferedReader(new FileReader(fsTab));
	            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
	            String line = null;
	            //Read from the original file and write to the new
	            //unless content matches data to be removed.
	            
	            while ((line = br.readLine()) != null) {
	            	if (!line.trim().equals(diskName +"       /esEbay        ext4    defaults,auto,_netdev 0 0")) {
	                    pw.println(line);
	                    pw.flush();
	                }
	            }
	            pw.close();
	            br.close();
	            //Delete the original file
	            if (!inputFile.delete()) {
	                System.out.println("Could not delete file");
	                return;
	            }
	            //Rename the new file to the filename the original file had.
	            if (!tempFile.renameTo(inputFile))
	                System.out.println("Could not rename file");
	            } catch (FileNotFoundException ex) {
	                ex.printStackTrace();
	            }catch (IOException ex) {
	                ex.printStackTrace(); 
	            }
	        System.out.println("mount info removed from fstab");
	        //InstallStorage.postMsg();
	        System.exit(0);
	    }

}
