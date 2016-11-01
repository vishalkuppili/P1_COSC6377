/*Source Code referenced from Stack Overflow, CodeRanch, Rgagnon.com, java2s.com*/

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NClient {

   private static DataInputStream in;
public static void main(String [] args) {
    try {
    	  Scanner sc = new Scanner(System.in);
    	  System.out.println("To Upload a file to the shard: u <Absolute file path>");
    	  System.out.println("To Download a file from the shard: d File name");
          String file1=sc.nextLine();
          String file=file1.substring(2, file1.length());
    	  JSONParser parser = new JSONParser();
          Object obj = parser.parse(new FileReader("config.json"));
          JSONObject jsonObject = (JSONObject) obj;
          String ipAddr = (String) jsonObject.get("shard1ip");
          int port = Integer.parseInt((String) jsonObject.get("shard1port")); 
    	  InetAddress serverName = InetAddress.getByName(ipAddr);
    	  String home= (String)jsonObject.get("homedir");
          System.out.println("Connecting to " + serverName + " on port " + port);
          Socket client = new Socket(serverName, port);
          System.out.println("Just connected to " + client.getRemoteSocketAddress());
          	if(file1.charAt(0)==('d'))
          		downloadReply(file,client,home);
          	if(file1.charAt(0)==('u'))
          		uploadReply(file,client);
 	  	  	System.out.println("Done on client's end");
          System.out.println("Closing Connection to shard1"); 
      		sc.close();
          System.out.println("Closed Connection to shard1");
          client.close();
      }catch(Exception e) {
         e.printStackTrace();
      }
   }
   public static void downloadReply(String file,Socket client,String home ) throws IOException{
	   int bytesRead;
	   int current = 0;
       FileOutputStream fos = null;
       BufferedOutputStream bos = null;
	   try{
		   String file2 = "~download~"+file;
		   OutputStream outToServer = client.getOutputStream();
		   DataOutputStream out = new DataOutputStream(outToServer);
	       out.writeUTF(file2);
	       home=home+"/"+file;
	       File dest=new File(home);
         System.out.println("asking if shard1 has "+file+"\nReply was:");
	       if(dest.exists()&& !dest.isDirectory()){
               long lastbyte = dest.length()-1;
         System.out.println("[0,"+lastbyte+"] bytes of "+file);
         System.out.println("Downloading [0,"+lastbyte+"] bytes from shard1");
	    	   byte [] mybytearray  = new byte [6022386];
	           InputStream is = client.getInputStream();
			   fos = new FileOutputStream(new File(file),false);
			   bos = new BufferedOutputStream(fos);
	           bytesRead = is.read(mybytearray,0,mybytearray.length);
	           current = bytesRead;
			   do {
				bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
				if(bytesRead >= 0) current += bytesRead;
			   } while(bytesRead > -1);
			   bos.write(mybytearray, 0 , current);
			   bos.flush();
			   fos.close();
			   bos.close();
	       }
	       else{
	    	   System.out.println("No such file available for download!");
	    	   return;
	       }
	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
	   return;
   }
   public static void uploadReply(String file, Socket client) throws Exception{
	   OutputStream outToServer = null;
	   DataOutputStream out = null;
	   OutputStream toServer = null;
	   InputStream inFromServer = null;
	   try{
           File upfile=new File(file);
           System.out.println("Size of upload file: "+upfile.length()+" bytes");
           outToServer = client.getOutputStream();
  		   out = new DataOutputStream(outToServer);
           out.writeUTF(file);
           inFromServer = client.getInputStream();
           in = new DataInputStream(inFromServer);
           String decision = in.readUTF();
           System.out.println("Server says: " + decision);
           out.flush();
           if(decision.startsWith("Sorry!"))
        	   return;
           else{
        	   byte[] bytes = new byte[6022386];
        	   File upload = new File(file);
    		   inFromServer = new FileInputStream(upload);
    		   toServer =  client.getOutputStream();
    		   int count;
    		   
    	        while ((count = inFromServer.read(bytes)) > 0) {
    	        
    	        	toServer.write(bytes, 0, count);
    	        }
    	        
    	        out.flush();
           }  
	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
	   finally{
		   outToServer.close();
           out.close();
	       inFromServer.close();
	   }
	   return;
   }
}