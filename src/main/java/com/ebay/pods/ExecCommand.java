package com.ebay.pods;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ExecCommand {
	/**
	 * ExecCommand shows how to execute an external program
	 * read its output, and print its exit status.
	 */
	  public static ArrayList<String> runCmd(String cmd) {
		    ArrayList<String> al = new ArrayList<String>();
	        StringBuilder sb = new StringBuilder();
	        String s;
	        Process p;
	        System.out.println("Executing cmd "+ cmd);
	        try {
	            p = Runtime.getRuntime().exec(cmd);
	            //Reader for stdOutput
	            BufferedReader stdOutput = new BufferedReader(new 
	                    InputStreamReader(p.getInputStream()));
	            //Reader for stdError
	            BufferedReader stdError = new BufferedReader(new 
	                    InputStreamReader(p.getErrorStream()));
	            //Reading std output
	            while ((s = stdOutput.readLine()) != null) {
	                //System.out.println("line: " + s);
	                sb.append(s);
	            }
	            //Reading Std error
	            while ((s = stdError.readLine()) != null) {
	                //System.out.println("line: " + s);
	                sb.append(s);
	            }
	            p.waitFor();
	            //System.out.println ("exit: " + p.exitValue());
	            p.destroy();
	            al.add(Integer.valueOf(p.exitValue()).toString());
	            al.add(sb.toString());
	            
	            //return sb.toString();
	            return al;
	        } catch (Exception e) {
	          e.printStackTrace();
	          //return sb.toString();
	          return null;
	        }
	    }
	  public static ArrayList<String> runCmd(String [] cmd) {
		    ArrayList<String> al = new ArrayList<String>();
	        StringBuilder sb = new StringBuilder();
	        String s;
	        Process p;
	        System.out.println("Executing cmd");
	        for (int i =0; i < cmd.length; i++) {
	        	System.out.print(cmd[i]);
	        }
	        try {
	            p = Runtime.getRuntime().exec(cmd);
	            //Reader for stdOutput
	            BufferedReader stdOutput = new BufferedReader(new 
	                    InputStreamReader(p.getInputStream()));
	            //Reader for stdError
	            BufferedReader stdError = new BufferedReader(new 
	                    InputStreamReader(p.getErrorStream()));
	            //Reading std output
	            while ((s = stdOutput.readLine()) != null) {
	                //System.out.println("line: " + s);
	                sb.append(s);
	            }
	            //Reading Std error
	            while ((s = stdError.readLine()) != null) {
	                //System.out.println("line: " + s);
	                sb.append(s);
	            }
	            p.waitFor();
	            //System.out.println ("exit: " + p.exitValue());
	            p.destroy();
	            al.add(Integer.valueOf(p.exitValue()).toString());
	            al.add(sb.toString());
	            
	            //return sb.toString();
	            return al;
	        } catch (Exception e) {
	          e.printStackTrace();
	          //return sb.toString();
	          return null;
	        }
	    }

}
