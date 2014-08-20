package com.ebay.pods;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class EditFStab {
	private static String diskName;
	public static void main(String[] args) {
		//call exec command to find the disk name
		try {
			ConnectionManager.createTrustedConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList <String> data = new ArrayList<String>();
	    //String [] cmd = {"/bin/df", "-h", "|", "grep", "esEbay", "|", "/usr/bin/awk", "'{print $1}'"};
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
		
		RandomAccessFile raf;
		try {
			//raf = new RandomAccessFile("c:\\fstab.txt", "rw");
			raf = new RandomAccessFile("/etc/fstab", "rw");
			//raf.seek(raf.length() -1) ;
			raf.seek(raf.length()) ;
			raf.writeBytes(diskName + "       /esEbay        ext4    defaults,auto,_netdev 0 0\n");
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("mount info added to fstab");
        InstallStorage.postMsg(true, "/esEbay");
        System.exit(0);
		
	}
}
