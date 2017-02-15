package testapp.ttyi.certisme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static testapp.ttyi.certisme.AESDemoActivity.generateKey;

public class DisplayQRActivity extends AppCompatActivity {

    // Added for QR code
    ImageView qrCodeImageview;
    String QRcode;
    byte QRcode_byte[];
    public final static int WIDTH=500;
    public final static char[] PASSPHRASE="Certis".toCharArray(); // Used in generateKey()
    public final static String SALT="Cisco"; // Used in generateKey()
    boolean nricList_loaded = false;
    boolean loginCheckComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr);

        // This section for QR code

        final String fin = getIntent().getStringExtra("EXTRA_FIN"); // Get the fin String passed in from the previous page (MainActivity)

        Button moodleHP_btn = (Button) findViewById(R.id.moodleHP_btn);

        getID();
        // create thread to avoid ANR Exception
        Thread t = new Thread(new Runnable() {
            public void run() {
                // this is the msg which will be encode in QRcode
                //QRcode=fin;

                QRcode_byte = encryptFIN128AES(fin + SHA1(fin));
                Log.e("QRcode_byte", "Message to be encrypted: " + fin + SHA1(fin));
                Log.e("QRcode_byte", "Encrypted QRcode_byte: " + Base64.encodeToString(QRcode_byte, Base64.DEFAULT));


                // Due to certain issues we are going to encrypt a shorter string and hopefully the RPi can decode it fine.
                byte[] pls_byte = encryptFIN128AES("pls");
                String FILENAME = "QRcode_byte";

                /* // Testing method - can be deleted (Used to test QR code decryption on RPi before webcam is available
                try {
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(QRcode_byte);
                    fos.close();
                    Log.e("QRcode_byte", "Write to file success! Check for QRcode_byte at /mnt/sdcard/ or /data/data/Your Package Name/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */

                Log.e("QRcode_byte", "Length of encoded bytes = " + Integer.toString(QRcode_byte.length));

                // added to check decoded + verify hash is correct
                String decoded = decryptFIN128AES(QRcode_byte);
                Log.e("decoded", decoded);

                // This part to check if the decoded FIN matches the hash (signature checking?)
                if (verifyFIN(decoded)) {
                    Log.e("onCreate", "FIN verified and decoded, FIN = " + decoded.substring(0,9));
                    Log.e("onCreate", "Hash = " + decoded.substring(9));
                    /*
                    TextView decrypt_cyphertext = (TextView) findViewById(R.id.decrypted_fin_textview);
                    decrypt_cyphertext.setText("[DECODED]:\n" + decoded.substring(0, 9) + "\n");
                    */
                }
                else
                    Log.e("onCreate", "FIN verification failed. Something went wrong.");

                // Here's another check to test if the STRING (QR Code) decode to byte is correct
                Log.e("onCreate", "Here's another check to test if the STRING (QR Code) decode to byte is correct.");
                byte[] decodeQRString_byte = Base64.decode(Base64.encodeToString(QRcode_byte, Base64.DEFAULT), Base64.DEFAULT);
                String decoded2 = decryptFIN128AES(decodeQRString_byte);
                Log.e("decoded2", decoded2);
                // This part to check if the decoded FIN matches the hash (signature checking?)
                if (verifyFIN(decoded2)) {
                    Log.e("onCreate", "FIN verified and decoded, FIN = " + decoded2.substring(0,9));
                    Log.e("onCreate", "Hash = " + decoded2.substring(9));
                }
                else
                    Log.e("onCreate", "FIN verification failed. Something went wrong.");


                /*
                Log.e("AES_Test", "Begin AES test...");
                // AES is going wrong... here's a test case
                byte[] a = encryptFIN128AES("pls");
                String b = decryptFIN128AES(a);
                Log.e("AES_Test", "b = " + b);
                */


                try {
                    synchronized (this) {
                        //wait(5000);
                        wait(5);
                        // runOnUiThread method used to do UI task in main thread.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap bitmap = null;

                                    bitmap = encodeAsBitmap(Base64.encodeToString(QRcode_byte, Base64.DEFAULT));
                                    qrCodeImageview.setImageBitmap(bitmap);

                                } catch (WriterException e) {
                                    e.printStackTrace();
                                } // end of catch block
                            } // end of run method
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Read from the Firebase --------------- [WORKING!]
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("nric");
                final List<String> nricList = new ArrayList<String>();

                final Query query = myRef.child("nric").orderByChild("value").equalTo(fin); // TEST1

                // ***ORIGINAL "VERIFY LOGIN" FUNCTION [WORKING!]
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot data) {
                        // Read an instance of the Firebase when something changes (onDataChange)
                        // meaning every time we update the Firebase (Raspberry Pi read a QR code it will auto update Firebase)
                        // this app will load the updated Firebase, find that the NRIC/FIN exists --> go to the next page
                        for (DataSnapshot userSnap: data.getChildren()) {
                            Log.i("SINGLE VALUE EVENT", userSnap.child("value").getValue(String.class));
                            nricList.add(userSnap.child("value").getValue(String.class));
                        }
                        nricList_loaded = true;

                        if (nricList_loaded == true) {
                            for (String str : nricList) {
                                if (str.trim().contains(fin)) {
                                    // Firebase-related stuff
                                    // For log purposes
                                    Log.w("onCreate", "Login successfuly. Now can skip to next page or sth... (Cause this means the NRIC/FIN is in the database)");
                                    // Move to next page

                                    /*
                                    query.addListenerForSingleValueEvent(new ValueEventListener() { // TEST1
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.w("onCreate", "Reach here. (0)");
                                            if (dataSnapshot.exists()) {
                                                // dataSnapshot is the "issue" node with all children with id 0
                                                Log.w("onCreate", "Reach here. (1)");
                                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                                    // do something with the individual "issues"
                                                    Log.w("onCreate", "Reach here. (2)");
                                                }
                                            }
                                            else
                                                Log.w("onCreate", "Reach here. (3)");
                                            Log.w("onCreate", "Reach here. (4)");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    */

                                    // The date works, time working now [WORKING!]
                                    DatabaseReference testRef = FirebaseDatabase.getInstance().getReference().child("nric");
                                    testRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot testSnapshot) {
                                            for (DataSnapshot dataSnapshot: testSnapshot.getChildren()) {
                                                String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                                                String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                                                try {
                                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                                    SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
                                                    Date dbDate = formatter.parse(dataSnapshot.child("date").getValue(String.class));
                                                    Date curDate = formatter.parse(currentDate);
                                                    Date dbTime = formatter2.parse(dataSnapshot.child("time").getValue(String.class));
                                                    Date curTime = formatter2.parse(currentTime);
                                                    long diffInMs = (curTime.getTime() + 28800000) - dbTime.getTime(); // 28800000 is the 8 hour offset for timezone (GMT+8)
                                                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                                                    if ((curDate.compareTo(dbDate)==0) && (diffInSec < 30))
                                                    {
                                                        Log.w("onCreate", "Pass! Welcome :) (Same date + Time < 30 seconds), diffInSec = " + diffInSec);
                                                        Log.w("onCreate", "Time: "+ dataSnapshot.child("time").getValue(String.class) + " Date: "+ dataSnapshot.child("date").getValue(String.class) + " Value: "+ dataSnapshot.child("value").getValue(String.class));
                                                        Intent intent = new Intent(DisplayQRActivity.this, LoginConfirmActivity.class);
                                                        intent.putExtra("EXTRA_FIN", fin.toUpperCase()); // Pas
                                                        startActivity(intent);
                                                    }
                                                    //else
                                                    //Log.w("onCreate", "Failed (Date & Time check failed), currentTime = " + currentTime + ", dbTime = " + formatter2.format(dbTime) + ", curTime = " + curTime.getTime() + ", dbTime = " + dbTime.getTime() + ", diffInMs = " + diffInMs);

                                                }catch (ParseException e1){
                                                    e1.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            throw databaseError.toException();
                                        }
                                    });

                                    //Intent intent = new Intent(DisplayQRActivity.this, LoginConfirmActivity.class);
                                    //intent.putExtra("EXTRA_FIN", fin.toUpperCase()); // Pas
                                    //startActivity(intent);
                                } //else
                                    //Log.w("onCreate", "Login Failed. (2)");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("onCreate", "Failed to read value.", error.toException());
                    }
                });

            }
        });

        t.start();

        moodleHP_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.moodle.moodlemobile");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }

            }
        });
    }

    // QR Code method
    private void getID() {
        qrCodeImageview=(ImageView) findViewById(R.id.img_qr_code_image);
    }

    // QR Code method - this is method call from on create and return bitmap image of QRCode.
    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    } /// end of this method


    /**
     * Encrypts a string with RSA-1024 (Using RSA PRIVATE Key)
     * @param fin (User's FIN/NRIC number)
     * @return the RSA-1024 encrypted String
     */
    private String encryptFIN1024RSA(String fin){

        // -----Section copied from RSADemoActivity-----

        // Generate key pair for 1024-bit RSA encryption and decryption
        Key publicKey = null;
        Key privateKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.e("RSAException", "RSA key pair error");
        }

        // Encode the original data with RSA private key
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = c.doFinal(fin.getBytes());
        } catch (Exception e) {
            Log.e("RSAException", "RSA encryption error");
        }

        // Decode the encoded data with RSA public key
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, publicKey);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Log.e("RSAException", "RSA decryption error");
        }
        TextView decrypt_cyphertext = (TextView)findViewById(R.id.decrypted_fin_textview);
        decrypt_cyphertext.setText("[DECODED]:\n" + new String(decodedBytes) + "\n");

        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);

        // -----End of section copied from RSADemoActivity-----
    }


    /**
     * Encrypts a string with AES (128 bit key)
     * @param fin (User's FIN/NRIC number) + hash(?)
     * @return the AES encrypted string
     */
    private byte[] encryptFIN128AES(String fin) {

        // -----Section copied from AESDemoActivity-----
        //StandardCharsets.UTF_8 x2

        SecretKeySpec sks_en = null;

        try {
            sks_en = new SecretKeySpec(generateKey(PASSPHRASE, SALT.getBytes(StandardCharsets.UTF_8)).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e("encryptFIN128AES", "AES key generation error");
        }

        // Get a random IV
        SecureRandom r = new SecureRandom();
        byte[] ivBytes = new byte[16];
        r.nextBytes(ivBytes);

        // Print out the IV in Hexadecimal (for online testing purposes only - can be deleted)
        StringBuilder sb = new StringBuilder();
        for (byte b : ivBytes) {
            sb.append(String.format("%02X ", b));
        }
        Log.e("encryptFIN128AES", "Random IV Generated (HEX): " + sb.toString());

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, sks_en, new IvParameterSpec(ivBytes));
            encodedBytes = c.doFinal(fin.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("encryptFIN128AES", "AES encryption error");
        }

        // Append ivBytes to the start of result
        byte[] result = new byte[ivBytes.length + encodedBytes.length];
        System.arraycopy(ivBytes, 0, result, 0, ivBytes.length);
        System.arraycopy(encodedBytes, 0, result, ivBytes.length, encodedBytes.length);

        return result;

    }


    /**
     * Decrypts a string with AES (128 bit key)
     * @param encodedBytes
     * @return the decrypted String (in the format of FIN/NRIC + Hash)
     */
    private String decryptFIN128AES(byte[] encodedBytes) {

        // -----Section copied from AESDemoActivity-----

        // Know that the FIRST 16 BYTES is the UNENCRYPTED IV, which is used to decrypt the rest

        SecretKeySpec sks_de = null;

        try {
            sks_de = new SecretKeySpec(generateKey(PASSPHRASE, SALT.getBytes(StandardCharsets.UTF_8)).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e("decryptFIN128AES", "AES key generation error");
        }

        // FOR EXPORTING TO RASPBERRY PI PURPOSES
        try {
            byte[] keyBytes = generateKey(PASSPHRASE, SALT.getBytes(StandardCharsets.UTF_8)).getEncoded();
            StringBuilder sb = new StringBuilder();
            for (byte b : keyBytes) {
                sb.append(String.format("%02X ", b));
            }
            Log.e("decryptFIN128AES", "AES Key Generated (HEX): " + sb.toString());
        } catch (Exception e) {
            Log.e("decryptFIN128AES", "AES key generation error");
        }


        // Decode the encoded data with AES
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, sks_de, new IvParameterSpec(Arrays.copyOfRange(encodedBytes,0,16)));
            decodedBytes = c.doFinal(Arrays.copyOfRange(encodedBytes,16, encodedBytes.length));
        } catch (Exception e) {
            Log.e("MYAPP", "exception", e);
        }

        Log.e("decryptFIN128AES", "Length of decoded bytes = " + Integer.toString(decodedBytes.length));

        //return Base64.encodeToString(decodedBytes, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Verifies the SHA-1 encoded FIN
     * @param decodedString
     * @return the decrypted String (in the format of FIN/NRIC + Hash)
     */
    private boolean verifyFIN(String decodedString) {

        String fin = decodedString.substring(0,9);
        String hash = decodedString.substring(9);

        return (SHA1(fin).equals(hash));

    }


    /**
     * Returns the SHA1 hash for the provided String
     * @param text
     * @return the SHA1 hash or null if an error occurs
     */
    public static String SHA1(String text) {

        try {

            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"),
                    0, text.length());
            byte[] sha1hash = md.digest();

            return toHex(sha1hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toHex(byte[] buf) {

        if (buf == null) return "";

        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);

        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }

        return result.toString();

    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {

        sb.append(HEX.charAt((b >> 4) & 0x0f))
                .append(HEX.charAt(b & 0x0f));

    }


    public void writeFile(byte[] data, String fileName) throws IOException{
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }

    public static class EntryRecord {

        OutsideDimension outsideDimension;

        public OutsideDimension getOutsideDimension() {
            return outsideDimension;
        }

        public class OutsideDimension {

            Dimensions dimensions;

            public Dimensions getDimensions() {
                return dimensions;
            }

            public class Dimensions {
                String date;
                String time;
                String nric_no;

                public String getDate() {
                    return date;
                }

                public String getTime() {
                    return time;
                }

                public String getNric_no() {
                    return nric_no;
                }
            }
        }
    }


}

/* References:
QR code using ZXing Library:
http://smartandroiddeveloper.com/2016/01/29/how-to-generate-qrcode-in-10-minutes-using-zxing-library-in-android-studio/

The length of encodedBytes = 128 bytes, which is 1024 bits / 8 (RSA key size / 8)
That is far less than the maximum size a QR can carry 2953 bytes of 8-bits each
What can we do to "secure" the QR? http://stackoverflow.com/questions/6249442/secure-encrypted-qr-codes
SHA-1 hashing algorithm https://codebutchery.wordpress.com/2014/08/27/how-to-get-the-sha1-hash-sum-of-a-string-in-android/

If you put Cipher c = Cipher.getInstance("AES");
Java will default to AES/ECB/PKCS5Padding --> Using ECB
Now if I use AES/CBC/PKCS5Padding --> Crash due to decrypt failure...? Why?????
FIXED: Because CBC mode requires an IV parameter else it will crash

How to generate an IV:
http://stackoverflow.com/questions/31036780/android-cryptography-api-not-generating-safe-iv-for-aes

How to merge two byte[] arrays: (IV appended before the encrypted string before encoded to QR code/transmitted)
http://stackoverflow.com/questions/5683486/how-to-combine-two-byte-arrays

How to split two byte[] arrays: (Extract IV from recieved QR code/demo transmission)
http://stackoverflow.com/questions/2253912/splitting-a-byte-array

How To Read/Write String From A File In Android
http://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android

Write byte[] to file in Java(Android)
http://stackoverflow.com/questions/6828634/write-byte-to-file-in-java

Regarding Firebase "onDataChange" method:
No, this is a value event listener, and will grab the intended data immediately. The onDataChange method is just grabbing the dataSnapshot, but it is not waiting for anything to be changed
http://stackoverflow.com/questions/39023945/how-to-get-data-from-real-time-database-in-firebase/39024068#39024068

Comparing date and time in Java + Convert String to date
http://stackoverflow.com/questions/18718743/compare-two-string-date-and-time-in-android
http://stackoverflow.com/questions/5369682/get-current-time-and-date-on-android
 */