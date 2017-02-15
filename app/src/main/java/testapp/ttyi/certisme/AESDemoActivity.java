package testapp.ttyi.certisme;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

public class AESDemoActivity extends Activity {
	static final String TAG = "SymmetricAlgorithmAES";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_aesdemo);

		// Original text
		String theTestText = "G1313774P";
		TextView tvorig = (TextView)findViewById(R.id.tvorig);
		tvorig.setText("\n[ORIGINAL]:\n" + theTestText + "\n");

		SecretKeySpec sks = null;

		try {
			sks = new SecretKeySpec(generateKey("Certis".toCharArray(), "Cisco".getBytes()).getEncoded(),"AES");
		} catch (Exception e) {
			Log.e(TAG, "AES key generation error");
		}

		// Set up secret key spec for 128-bit AES encryption and decryption
		//SecretKeySpec sks = null;
        /* ORIGINAL CODE
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed("any data used as random seed".getBytes());
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128, sr);
			sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
		} catch (Exception e) {
			Log.e(TAG, "AES secret key spec error");
		}
		*/

        // Works, generate AES key from secret. (Not secure though because it uses hash only)
		/*
        try {
            sks = buildKey("Certis Cisco");
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        */


		// Encode the original data with AES
		byte[] encodedBytes = null;
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, sks);
			encodedBytes = c.doFinal(theTestText.getBytes());
		} catch (Exception e) {
			Log.e(TAG, "AES encryption error");
		}		
		TextView tvencoded = (TextView)findViewById(R.id.tvencoded);
		tvencoded.setText("[ENCODED]:\n" + 
				Base64.encodeToString(encodedBytes, Base64.DEFAULT) + "\n");

		// How many bytes is AES encrypted by 128 bit key?
		Log.e("ENCODED BYTES", "Length of encoded bytes = " + Integer.toString(encodedBytes.length));

		// Decode the encoded data with AES
		byte[] decodedBytes = null;
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, sks);
			decodedBytes = c.doFinal(encodedBytes);
		} catch (Exception e) {
			Log.e(TAG, "AES decryption error");
		}		
		TextView tvdecoded = (TextView)findViewById(R.id.tvdecoded);
		tvdecoded.setText("[DECODED]:\n" + new String(decodedBytes) + "\n");

        TextView seckey_text = (TextView)findViewById(R.id.seckey_textview);
        seckey_text.setText("[SECRET KEY]:\n" + Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT) + "\n");

	}


    // -----Added Codes-----

    // Add in this part to build private key from hardcoded master key (original code is hashing password)
    // This one uses hash only
    private SecretKeySpec buildKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digester = MessageDigest.getInstance("MD5");
        digester.update(password.getBytes("UTF-8"));
        byte[] key = digester.digest();
        SecretKeySpec spec = new SecretKeySpec(key, "AES");
        return spec;
    }

	/**
	 * Build private key from a passpharase/PIN (incl. key derivation (Uses PBKDF2))
	 * @param passphraseOrPin
	 * @param salt
	 * @return The generated SecretKey (Used for AES-encryption, key size specified in outputKeyLength)
	 */
    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 1000;

        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

}

/*
References:
AES example: http://www.developer.com/ws/android/encrypting-with-android-cryptography-api.html

Create an AES key from "password": http://stackoverflow.com/questions/8397047/what-secretkeyfactory-not-available-does-mean

The second generateKey (passphrase, salt) is from https://android-developers.googleblog.com/2013/02/using-cryptography-to-store-credentials.html
 */