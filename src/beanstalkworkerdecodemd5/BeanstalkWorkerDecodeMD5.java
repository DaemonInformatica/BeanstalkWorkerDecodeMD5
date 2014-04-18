/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beanstalkworkerdecodemd5;

import dk.safl.beanstemc.Beanstemc;
import dk.safl.beanstemc.BeanstemcException;
import dk.safl.beanstemc.Job;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author matrix
 */
public class BeanstalkWorkerDecodeMD5 
{
    private String  m_host;
    private boolean m_bRun;
    private int     m_beanStalkPort;
    
    public BeanstalkWorkerDecodeMD5(String host)
    {
        m_host = host;
        m_bRun = true;
    }

    public String calcMD5(String plaintext)
    {
        String hashtext = "";
        
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            
            m.reset();
            m.update(plaintext.getBytes());
            
            byte[]      digest      = m.digest();
            BigInteger  bigInt      = new BigInteger(1,digest);
                        hashtext    = bigInt.toString(16);
            
            // Now we need to zero pad it if you actually want the full 32 chars.
            while(hashtext.length() < 32 )
                hashtext = "0"+hashtext;
            
        }
        catch(NoSuchAlgorithmException nsE) { System.err.println("No such algorithm"); }
        
        return hashtext;
    }
    
    private byte[] stringToByteArr(String s)
    {
        int len     = s.length();
        byte data[] = s.getBytes();
        
        System.out.println("BeanstalkClientTest1::stringToByteArr: data length: " + data.length);
        
        return data;
    }
    
    private void returnResult(long jobID, String strFound)
    {
        try
        {            
            // create JSON object to send as result. 
            JSONObject o = new JSONObject();

            o.put("id",     jobID);
            o.put("found",  strFound);
            o.put("status", "completed");
            
            String code = o.toJSONString();
            byte ba[]   = stringToByteArr(code);

            // fire job to the result queue. 
            Beanstemc stem = new Beanstemc("localhost", 9000);

            stem.use("result");
            stem.put(ba);
        }
        catch(UnknownHostException uhE) { System.err.println("Unknown host."); } 
        catch(IOException ioE)          { System.err.println("IOException: " + ioE.toString()); } 
        catch(BeanstemcException bsE)   { System.err.println("Beanstalk Exception: " + bsE.toString()); } 
    }
    
    private String getNextString(String currString)
    {
        // initialize a byte array the length of the string. 
        int     strLen  = currString.length();
        byte    arr[]   = new byte[strLen];
        String  result  = "";
        
        // for each charachter in the string: 
        int arrIndex = 0;
        
        for(int i = 0; i <= strLen - 1; i++)
        {
            // calculate byte value and add to the array. 
            byte b          = (byte)currString.charAt(i);
            arr[arrIndex]   = b;
            
            arrIndex++;
        }
        
        // update the last character by 1. 
        arr[strLen - 1]++;
        
        // Starting from the end: for each character: 
        for(int i = strLen - 1; i >= 0; i--)
        {        
            // if value > maxAsciivalue
            byte val = arr[i];
            if(val > 122)
            {
                // set to first ascii value. 
                arr[i] = 33;
                
                // increase the next value by 1, if there is one. 
                if(i > 0)
                    arr[i - 1] += 1;
            }
        }
        
        // build a new string out of the byte array. 
        try
        {
            result = new String(arr, "UTF-8");
        }
        catch(UnsupportedEncodingException ueE) { System.err.println("Unsupported encoding while figureing out a new String..."); }
        
        return result;
    }

    private void setProcessing(int databaseID)
    {
        JSONObject o = new JSONObject();

        o.put("id",     databaseID);
        o.put("found",  "");
        o.put("status", "processing");

        String code = o.toJSONString();
        byte ba[]   = stringToByteArr(code);
        
        try
        {
            // fire job to the result queue. 
            Beanstemc stem = new Beanstemc("localhost", 9000);

            stem.use("result");
            stem.put(ba);
        }
        catch(UnknownHostException uhE) { System.err.println("Unknown host."); } 
        catch(IOException ioE)          { System.err.println("IOException: " + ioE.toString()); } 
        catch(BeanstemcException bsE)   { System.err.println("Beanstalk Exception: " + bsE.toString()); } 

        
    }
    
    public String runJob(Job j)
    {
        String result = "";
        
        try
        {            
            // Decode json string. 
            byte        baData[]    = j.getData();
            String      data        = new String(baData, "UTF-8");
            JSONObject  o           = (JSONObject)JSONValue.parse(data);
            
            // get target md5, start string and end. 
            String  targetMD5   = (String)o.get("md5");
            String  start       = (String)o.get("start");
            String  end         = (String)o.get("end");
            boolean brun        = true;
            String  currString  = start;
            int     loopIndex   = 0;
            
            System.out.println("runJob: targetMD5: " + targetMD5 + " - start: " + start + " - end: " + end);
            
            // for each string between start and end: 
            while(brun == true)
            {
                // this is the final string to be checked? kill the loop. 
                if(currString.equals(end) == true)
                    brun = false;
                
                // convert to md5. 
                String currMD5 = calcMD5(currString);
                System.out.println(loopIndex + ": runJob: start: + " + start + " - currString: " + currString + " - end: " + end + " - currMD5: " + currMD5);

                // is it the same as the target md5? Report to server that it's found. 
                if(targetMD5.equals(currMD5) == true)
                {
                    System.out.println("runJob: Match found!");
                    result  = currString;
                    brun    = false;
                }
                
                // calculate new currString.
                currString = getNextString(currString);
                loopIndex++;
            }
        }
        catch(UnsupportedEncodingException usE) { System.err.println("Dude, you messed up the encoding..."); }
        
        System.out.println("runJob: Ending run. Sending back result: " + result);
        
        return result;
    }
    
    private int getDBIDFromJob(Job j) throws UnsupportedEncodingException
    {
        byte        baData[]    = j.getData();
        String      data        = new String(baData, "UTF-8");
        JSONObject  o           = (JSONObject)JSONValue.parse(data);

        // get database id, start string and end. 
        long id   = (Long)o.get("id");
        int nID   = (int)id;
        
        return nID;
    }
    
    public void runWorker(int port, String tubename)
    {
        System.out.println("Starting worker on tube: " + tubename);
        try
        {
            // Initialise beanstem
            Beanstemc stem = new Beanstemc(m_host, port);

            // watch correct tube. 
            stem.watch(tubename);

            while(m_bRun)
            {
                System.out.println("while loop.");
                // for each job: Execute 'runJob'. 
                Job j = stem.reserve();
                stem.delete(j);
                int nID         = getDBIDFromJob(j);
                setProcessing(nID);
                String  result  = runJob(j);
                
                
                returnResult(nID, result);
            }
        }
        catch(UnknownHostException uhE) { System.err.println("Unknown host!"); }
        catch(IOException uhE)          { System.err.println("Oh noes! IOException!"); }
        catch(BeanstemcException bsE)   { System.err.println("Beanstem keeled over!"); }
    }
    
    public static void usage()
    {
        System.out.println("Usage: java -jar BeanstalkWorkerDecodeMD5.jar <host> <port> <tube>");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        String host                     = "localhost";
        int port                        = 9000;
        String tube                     = "longtube";
        BeanstalkWorkerDecodeMD5 app    = new BeanstalkWorkerDecodeMD5(host);

        // TODO code application logic here
        if(args.length < 3)
        {
            usage();
            
            // return;
        }
        else
        {
            host    = args[0];
            port    = Integer.valueOf(args[1]);
            tube    = args[2];
            app     = new BeanstalkWorkerDecodeMD5(host);
        }
        
        app.runWorker(port, tube);
    }
}
