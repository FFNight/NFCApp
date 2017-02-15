package testapp.ttyi.certisme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.TreeSet;

import javax.crypto.Cipher;

public class RSADemoActivity extends AppCompatActivity {
    static final String TAG = "AsymmetricAlgorithmRSA";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rsademo);

        // Original text
        String theTestText = "G1313774P";
        TextView plaintext = (TextView) findViewById(R.id.plaintext_textview);
        plaintext.setText("\n[ORIGINAL]:\n" + theTestText + "\n");

        // Generate key pair for 1024-bit RSA encryption and decryption
        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
        }

        // Encode the original data with RSA private key
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = c.doFinal(theTestText.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error");
        }
        TextView cyphertext = (TextView) findViewById(R.id.cyphertext_textview);
        cyphertext.setText("[ENCODED]:\n" +
                Base64.encodeToString(encodedBytes, Base64.DEFAULT) + "\n");

        // Decode the encoded data with RSA public key
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, publicKey);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error");
        }
        TextView decrypt_cyphertext = (TextView)findViewById(R.id.decrypt_cyphertext_textview);
        decrypt_cyphertext.setText("[DECODED]:\n" + new String(decodedBytes) + "\n");


        // Print out the private/public key (involves converting Key --> byte --> String)
        TextView privatekey_text = (TextView)findViewById(R.id.privatekey_textview);
        privatekey_text.setText("[PUBLIC KEY]:\n" + Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT) + "\n");


        TreeSet<String> algorithms = new TreeSet<>();
        for (Provider provider : Security.getProviders())
            for (Provider.Service service : provider.getServices())
                if (service.getType().equals("Signature"))
                    algorithms.add(service.getAlgorithm());
        for (String algorithm : algorithms)
            Log.e("KeyFactory", algorithm);


        /*
        // Test codes
        String publicKeyInStr = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        Log.e("PublicKeyString", publicKeyInStr);
        byte[] publicKeyInByte = Base64.decode(publicKeyInStr, Base64.DEFAULT);
        PublicKey publicKeyInSecretKey = KeyFactory.getInstance("RSA",).generatePublic(new X509EncodedKeySpec(publicKeyInByte));
        String publicKeyBackInStr = Base64.encodeToString(publicKeyInSecretKey.getEncoded(), Base64.DEFAULT);
        Log.e("PublicKeyBackInStr", publicKeyBackInStr);

        byte[] encodedBytes2 = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes2 = c.doFinal(theTestText.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error");
        }

        byte[] decodedBytes2 = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, publicKeyInSecretKey);
            decodedBytes2 = c.doFinal(encodedBytes2);
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error");
        }
        Log.e("Decoded String", Base64.encodeToString(decodedBytes2, Base64.DEFAULT));
        */

        /*
        // Print out private/public key ***length*** (can adjust the field before ".getEncoded")
        TextView privatekey_text = (TextView)findViewById(R.id.privatekey_textview);
        privatekey_text.setText("[PUBLIC KEY LENGTH]:\n" + Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT).length() + "\n");
        */



    }

}

/*
References:
RSA example from http://www.developer.com/ws/android/encrypting-with-android-cryptography-api.html
The complete file and codes available at http://www.developer.com/imagesvr_ce/3421/TutorialOnCrypto.zip

Converting "Key" type into "String" (to print out the private key for reference at
http://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
 */