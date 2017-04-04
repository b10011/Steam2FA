package steamguard2fa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SteamGuard2FA {
    public static void main(String[] args) {
        String sSecret = "";
        
        try {
            // Read shared_secret from file
            BufferedReader br = new BufferedReader(new FileReader("sharedsecret.dat"));
            sSecret = br.readLine();
            br.close();
            
            // Write out the code
            PrintWriter writer = new PrintWriter("code.txt", "UTF-8");
            writer.print(generate(sSecret, System.currentTimeMillis()/1000L));
            writer.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("I didn\'t find sharedsecret.dat!");
            System.out.print("Give your shared_secret here: ");
            try {
                // Ask for shared_secret and write it to a file for later use
                Writer writer = new PrintWriter("sharedsecret.dat", "UTF-8");
                Scanner reader = new Scanner(System.in);
                sSecret = reader.nextLine();
                writer.write(sSecret);
                writer.close();
                
                // Write out the code
                writer = new PrintWriter("code.txt", "UTF-8");
                writer.write(generate(sSecret, System.currentTimeMillis()/1000L));
                writer.close();
            } catch (Exception e) {
                System.out.println("Writing error: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Funny error: " + e.getMessage());
        }
    }
    
    public static String generate(String secretcode, long seconds) {
        // Decode base64 representation of the secret
        byte[] mSecret = DatatypeConverter.parseBase64Binary(secretcode);

        // All possible characters used in code
        byte[] codeChars = "23456789BCDFGHJKMNPQRTVWXY".getBytes();
        
        // Code changes every 30 seconds
        seconds /= 30;

        // Calculate tmp-array from seconds
        byte[] tmp = new byte[8];
        for (int i = 0; i < 8; ++i) {
            tmp[7-i] = (byte)seconds;
            seconds >>>= 8;
        }

        // Initialize HmacSHA1
        SecretKeySpec localSecretKeySpec = new SecretKeySpec(mSecret, "HmacSHA1");
        int x = 0;
        int y = 0;

        try {
            Mac localMac = Mac.getInstance("HmacSHA1");
            localMac.init(localSecretKeySpec);
            
            // Get byte-array with decoded shared secret & tmp-array
            tmp = localMac.doFinal(tmp);
            
            // Byte shifting to mix up the code
            x = tmp[19] & 0xF;
            y = (tmp[x] & 0x7F) << 24 | (tmp[(x + 1)] & 0xFF) << 16 | (tmp[(x + 2)] & 0xFF) << 8 | tmp[(x + 3)] & 0xFF;
            
            // Generate the byte-array code
            byte[] code = new byte[5];
            for (int i = 0; i < 5; ++i) {
                code[i] = codeChars[y % codeChars.length];
                y /= codeChars.length;
            }
            
            // Make it String
            String sCode = "";
            for (byte b : code)
                sCode += (char) b;
            
            // Return the code
            return sCode;
            
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("No such algorithm (HmacSHA1): " + nsae.getMessage());
        } catch (InvalidKeyException ike) {
            System.out.println("Invalid key: " + ike.getMessage());
        }

        return null;
    }
}
