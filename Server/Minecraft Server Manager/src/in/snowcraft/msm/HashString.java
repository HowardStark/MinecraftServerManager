package in.snowcraft.msm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashString {
	
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String sha256(byte[] convert) {
	    MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-256");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    
	    byte[] bytes = md.digest(convert);
	    
	    char[] hexChars = new char[bytes.length * 2];
	    for(int j = 0; j < bytes.length; j++){
	    	int v = bytes[j] & 0xFF;
	    	hexChars[j * 2] = hexArray[v >>> 4];
	    	hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    
	    return new String(hexChars);
	}

}
