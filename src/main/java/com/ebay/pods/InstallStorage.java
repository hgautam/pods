package com.ebay.pods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


import org.json.simple.JSONArray;
//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InstallStorage {
	/**
	 * InstallStorgae command will interact with ESIaaS API to connect to a storage target
	 * provided by the API.
	 */
	//class variables
	//private static String stagingURL = "https://crocus.vip.qa.ebay.com/service/1.0/storage";
	private static String prodURL = "https://esiaas.vip.qa.ebay.com/service/1.0/storage";
	//private static final String authStr = ("esadmin:estest");
	private static String hostName;
	private static String iqn;
	private static int status = -2;
	
	
	public static void main (String [] args) throws Exception{
		ConnectionManager.createTrustedConnection();
		//setup();
		//read hostname
		hostName = getHostName();
		//hostName = "phx5qa01c-a9dc.stratus.phx.qa.ebay.com";
		if (hostName == null) {
			System.out.println("host name cannot be null");
			postMsg(false, "host name cannot be null");
			System.exit(1);
		}
		
		System.out.println(hostName);
		
		//get iqn from initiator.iscsi file
		//iqn = "iqn.1993-08.org.debian:01:d0a67c68558";
		iqn = getIqn();
		if (iqn == null) {
			System.out.println("iqn cannot be null");
			postMsg(false, "iqn cannot be null");
			System.exit(1);
		}
		System.out.println(iqn);
		//call ESIaaS service to export a block device
		exportVolume();
		//We need to sleep to make sure block device is exported to VM 
		Thread.sleep(30000);
		//check api call status and connect to one of the portals
		int counter = 1;
		while (true) {
		  status = checkApiStatus();
		  if (counter > 5) {
		     System.out.println("Vol export unsuccessful in 5 tries. Exiting.");
		     postMsg(false, "Vol export unsuccessful in 5 tries. Exiting.");
		     System.exit(1);
		  }
		  if (status == -1) {
		     Thread.sleep(10000);
		     System.out.println("Retrying export status check...");
		     counter++;

		  }else if (status == -2) {
		     System.out.println("Vol export failed!");
		     postMsg(false, "Vol export failed!");
		     System.exit(1);
		  } else {
		     System.out.println("Vol export successful");
		     System.exit(0);
		  }
		}

	}

	public static int checkApiStatus() {
		String output;
		StringBuilder sb = new StringBuilder();
		URL url;
		HttpsURLConnection conn = null;
		try {
			url = new URL(prodURL + "/" + hostName + "/" + "volumeInfo");
			conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			// conn.setRequestProperty ("Authorization", "Basic " + encoding);
			conn.setRequestProperty("Content-Type", "application/json");

			if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
				System.out.println("status code is:" + conn.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			// System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				// System.out.println(output);
				sb.append(output);
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		conn.disconnect();
		JSONParser parser = new JSONParser();
		// Object obj1 = parser.parse(output);
		Object obj = null;
		try {
			obj = parser.parse(sb.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		JSONObject jsonObject = (JSONObject) obj;
		JSONObject innerObj = (JSONObject) jsonObject
				.get("getVolumeInfoResponse");
		boolean success = (Boolean) innerObj.get("success");
		System.out.println(success);

		if (!success) {
			System.out.println("Api call unsuccessful");
			postMsg(false, "Api call unsuccessful");
			System.exit(1);
		}
		JSONObject insideObj = (JSONObject) innerObj.get("object");
		long status = (Long) insideObj.get("status");
		System.out.println(status);

		if (status != 4) {
			return -1;
		}
		JSONArray portals = (JSONArray) insideObj.get("portals");
		int outcome = -1;
		for (int i = 0; i < portals.size(); i++) {
			System.out.println("Portal is " + portals.get(i));
			outcome = runCommand((String) (portals.get(i)));
			if (outcome != 0) {
				continue;
			} else {
				//outcome = 0;
				break;
			}
		}
		if (outcome == 0) {
			return 0;
		}
		return -2;
	}
	
	public static int runCommand(String portal) {
		  ArrayList<String>data = new ArrayList<String>();
		  //data = runCmd("C:\\Windows\\System32\\cmd.exe /c dir");
		  data = ExecCommand.runCmd("/sbin/iscsiadm -m discovery -t st -p " + portal);
		  //data.add("0");
		  //data.add("Logging in to [iface: default, target: iqn.2004-02.com.vmem:phxmvmem-02-gwa:tp4, portal: 10.81.96.18,3260]iscsiadm: Could not login to [iface: default, target: iqn.2004-02.com.vmem:phxmvmem-02-gwa:tp4, portal: 10.81.96.18,3260]: iscsiadm: initiator reported error (15 - already exists)");
		  //data.add("10.81.96.18:3260,1 iqn.2004-02.com.vmem:phxmvmem-02-gwa:tp4");
          
		  
		  //String val = data.get(0);
		  int status = Integer.parseInt(data.get(0));
		  System.out.println(status);
		  if (status == 0) {
			  String val = data.get(1);
			  System.out.println(val);
			  String [] commandData = val.split(" ");
			  System.out.println(commandData[1]);
			  ArrayList<String>al = new ArrayList<String>();
			  String command = "/sbin/iscsiadm -m node --targetname " + commandData[1] +" --portal " + portal +":3260 --login";
			  //String command = "/sbin/iscsiadm -m node --targetname iqn.2004-02.com.vmem:phxmvmem-02-gwa:tp4 --portal 10.81.96.18:3260 --login";
			  //System.out.println(command);
			  al = ExecCommand.runCmd(command);
			  //al.add("0");
			  //al.add("test");
			  int returnCode = Integer.parseInt(al.get(0));
			  if (returnCode == 0) {
				  System.out.println("Connected successfully");
				  System.out.println(al.get(1));
				  return 0;
			  }
			  if (returnCode == 255) {
				  int datasize = al.size();
				  //System.out.println("data size is "+ datasize);
				  int outputLen = al.get(datasize -1).length();
				  //System.out.println("command output is "+ al.get(datasize -1));
				  //System.out.println("output length is "+ outputLen);
				  if (outputLen != 285) {
					  System.out.println("iscsiadm command is not returning expected data");
					  System.exit(1);
				  }
				  String output = al.get(datasize - 1);
				  int endPos = output.lastIndexOf(")");
				  int offSet = endPos - 14;
				  String errorString = output.substring(offSet, endPos);
				  System.out.println(errorString);
				  if (errorString.equals("already exists")) {
					 System.out.println("We are good");
					 return 0;
				  } else {
					  //make call to api with error
					  System.out.println("Error Encounterd");
					  return -1;
					  //System.exit(1);
				  }
			  }
			  //return 0;
		  }

		return -1;
	}
	public static String getIqn() {
		String iqn = null;
	    String lineString;
	    BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/etc/iscsi/initiatorname.iscsi"));
			//br = new BufferedReader(new FileReader("c:\\initiatorname.iscsi"));
			while ((lineString = br.readLine()) != null) {
				if (lineString.startsWith("#")) {
					continue;
				} else {
					// System.out.println(lineString);
					String[] data = lineString.split("=");
					iqn = data[1];
					// System.out.println(iqn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	  }
	  return iqn;
	}
	// Runs a command to get the hostName
	public static String getHostName() {
		String name = null;
		ArrayList <String>data = new ArrayList<String>();
		data = ExecCommand.runCmd("/bin/hostname -f");
		if (data == null) {
			System.out.println("Invalid hostname");
			System.exit(1);
		}
		int status = Integer.parseInt(data.get(0));
		if (status == 0) {
			name = data.get(1);
		}
		return name;
	}
	
	@SuppressWarnings("unchecked")
	public static void exportVolume() {
		String output;
		URL url;
		OutputStream os;
		HttpsURLConnection urlCon;
		StringBuilder sb = new StringBuilder();
		JSONParser parser = new JSONParser();
		JSONObject obj = new JSONObject();
		obj.put("iqn", iqn);
		String input = obj.toJSONString();
		try {
			url = new URL(prodURL + "/" + hostName + "/" + "exportVolume");
			urlCon = (HttpsURLConnection) url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setRequestProperty("Content-Type", "application/json");
			os = urlCon.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (urlCon.getResponseCode() != HttpsURLConnection.HTTP_OK) {
				System.out
						.println("status code is:" + urlCon.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ urlCon.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(urlCon.getInputStream())));

			// System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				//System.out.println(output);
				sb.append(output);
			}
			urlCon.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		Object obj1 = null;
		try {
			obj1 = parser.parse(sb.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		JSONObject jsonObject = (JSONObject) obj1;
		// get the inside object and store here
		JSONObject innerObj1 = (JSONObject) jsonObject.get("exportResponse");
		boolean success = (Boolean) innerObj1.get("success");
		System.out.println(success);

		String msg = (String) innerObj1.get("msg");
		System.out.println(msg);

		if (!success) {
			System.out.println("Api call unsuccessful");
			postMsg(false, "Api call unsuccessful");
			System.exit(1);
		}

	}
	@SuppressWarnings("unchecked")
	public static void postMsg(boolean isSuccessful, String msg) {
		URL url;
		OutputStream os;
		HttpsURLConnection urlCon;
		hostName = getHostName();
		JSONObject obj = new JSONObject();
		obj.put("success", isSuccessful);
		if (!isSuccessful) {
			obj.put("msg", msg);
		} else {
			obj.put("mountPath", msg);
		}
		//obj.put("msg", msg);
		String input = obj.toJSONString();
		try {
			url = new URL(prodURL + "/" + hostName + "/" + "extraInfo");
			urlCon = (HttpsURLConnection) url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setRequestProperty("Content-Type", "application/json");
			os = urlCon.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			if (urlCon.getResponseCode() != HttpsURLConnection.HTTP_OK) {
				System.out
						.println("status code is:" + urlCon.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ urlCon.getResponseCode());
			}
		 } catch (IOException e) {
		   	e.printStackTrace();
			//System.exit(1);
         }
   }	
	
	public static void postMsg() {
		URL url;
		//OutputStream os;
		HttpsURLConnection urlCon;
		hostName = getHostName();
		System.out.println("Host name is "+ hostName);
		try {
			url = new URL(prodURL + "/" + hostName + "/" + "unmounted");
			urlCon = (HttpsURLConnection) url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setRequestProperty("Content-Type", "application/json");
			//os = urlCon.getOutputStream();
			//os.write(input.getBytes());
			//os.flush();
			if (urlCon.getResponseCode() != HttpsURLConnection.HTTP_OK) {
				System.out
						.println("status code is:" + urlCon.getResponseCode());
				throw new RuntimeException("Failed : HTTP error code : "
						+ urlCon.getResponseCode());
			}
		 } catch (IOException e) {
		   	e.printStackTrace();
			//System.exit(1);
         }
}
/*
	public static void setup() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
*/
}
