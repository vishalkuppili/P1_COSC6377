/*Source Code referenced from Stack Overflow, CodeRanch, Rgagnon.com, java2s.com*/

import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NServer extends Thread {
   private ServerSocket serverSocket;
   
   public NServer(int port) throws IOException {
      serverSocket = new ServerSocket(port);
   }

   public void run() {
      while(true) {
         try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            
            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(server.getInputStream());
   
			String file = (String)in.readUTF();
			String filename="";
			
			JSONParser parser1 = new JSONParser();
		    Object obj1 = parser1.parse(new FileReader("config.json"));
		    JSONObject jsonObject1 = (JSONObject) obj1;
		    
			if(file.startsWith("~download~"))
			{
      
				int lastSlash = file.lastIndexOf("~");
        filename = file.substring(lastSlash+1, file.length());
        downloadReply(filename,server,jsonObject1);
			}
			else{
				filename=file;
				uploadReply(filename,server,jsonObject1);
			}
      System.out.println("Done on server's end");
            server.close();
            
         }catch(SocketTimeoutException s) {
            System.out.println("Socket timed out!");
            break;
         }catch(Exception e) {
            e.printStackTrace();
            break;
         }
      }
   }
   public void uploadReply (String filename, Socket server, JSONObject jsonObject1) throws Exception{
	   DataOutputStream out = null;
	   InputStream in1 = null;
	   OutputStream out1 = null;
	   try{
		   out = new DataOutputStream(server.getOutputStream());
	       File file =new File(filename);
	       if(file.exists()&&!file.isDirectory()){
           int Slash = filename.lastIndexOf("/");
           String up = filename.substring(Slash+1, filename.length());
	    	   out.writeUTF("Server ready to accept " + up + " for upload");
	       }
	       
		   else
			   out.writeUTF("Sorry! But either the specified file doesn't exist or it is a directory");
	       
	       out.flush();
	       
	       if(file.exists()&&!file.isDirectory()){
           int lastSlash = filename.lastIndexOf("/");
               String upfile = filename.substring(lastSlash+1, filename.length());
           System.out.println("received upload request of "+file.length()+" bytes for "+upfile);
           long lastbyte=file.length()-1;
           System.out.println("received primary bytes [0,"+lastbyte+"] bytes for "+upfile);
	    	   String filepath  = (String) jsonObject1.get("homedir")+"/"+upfile;
	    	   in1 = server.getInputStream();
	    	   out1 = new FileOutputStream(filepath);
	    	   byte[] bytes = new byte[6022386];
	    	   int count;
	           while ((count = in1.read(bytes)) > 0) {
	        	  
	               out1.write(bytes, 0, count);
	           }
			   out1.close();           
			   in1.close();
	       }
	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
	   finally{
		   out.close();
	   }
       return;
   }
   public void downloadReply (String filename, Socket server, JSONObject jsonObject1) throws IOException{
     System.out.println("Received download request for "+filename);
	   FileInputStream fis = null;
	   BufferedInputStream bis = null;
       OutputStream os = null;
	   try{
	       String filepath  = (String) jsonObject1.get("homedir")+"/"+filename;
	       File file = new File(filepath);
	       if(file.exists()){
               long lastbyte=file.length()-1;
               System.out.println("We have primary bytes [0,"+lastbyte+"] bytes of "+filename);
			   byte [] mybytearray  = new byte [(int)file.length()];
			   fis = new FileInputStream(file);
			   bis = new BufferedInputStream(fis);
               bis.read(mybytearray,0,mybytearray.length);
               os = server.getOutputStream();
               System.out.println("sending primary bytes [0,"+lastbyte+"] to the client");
			   os.write(mybytearray,0,mybytearray.length);
               os.flush();
               bis.close();
               os.close();
		   }
	       else{
			   System.out.println(filename+" doesn't exist on the shard. Try another file.\nGoodbye");
		   }

	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
       return;
   }
   public static void main(String [] args) {
      
      try {
    	  JSONParser parser1 = new JSONParser();
          Object obj1 = parser1.parse(new FileReader("config.json"));
          JSONObject jsonObject1 = (JSONObject) obj1;
          int port  = Integer.parseInt((String) jsonObject1.get("listenport")); 
         Thread t = new NServer(port);
         t.start();
      }catch(Exception e) {
         e.printStackTrace();
      }
   }
}