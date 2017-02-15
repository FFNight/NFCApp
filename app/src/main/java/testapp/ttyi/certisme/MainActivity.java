package testapp.ttyi.certisme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.Security;

public class MainActivity extends AppCompatActivity {

    // -----RSA/PKI Global Variables-----

    // -----RSA/PKI above-----

    // Intent stuff START
    private static final String LAUNCH_FROM_URL = "com.androidsrc.launchfrombrowser";
    // Intent Stuff END

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RSA/PKI Related
        // Intent stuff START
        TextView launchInfo = (TextView)findViewById(R.id.launch_info);

        Intent intent = getIntent();
        if(intent != null && intent.getAction().equals(LAUNCH_FROM_URL)){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String msgFromBrowserUrl = bundle.getString("msg_from_browser");
                launchInfo.setText(msgFromBrowserUrl);
            }
        }else{
            launchInfo.setText("Normal application launch");
        }
        // Intent stuff END

        // Below are non-RSA/PKI related

        Button btn = (Button) findViewById(R.id.submit_FIN_button);

        final EditText edt = (EditText)findViewById(R.id.FIN_textbox);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Log.d("MainActivity", "onClick");
                String fin = edt.getText().toString(); // Get the FIN/IC number entered by the user

                if (fin.equals(""))
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("NO NRIC/FIN")
                            .setMessage("Please enter your NRIC/FIN number in the text box.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else if (!(fin.length()==9) || !(fin.substring(1,8).matches("\\d+(?:\\.\\d+)?")) || (!fin.substring(0, 1).toUpperCase().equals("S") && !fin.substring(0, 1).toUpperCase().equals("T") && !fin.substring(0, 1).toUpperCase().equals("F") && !fin.substring(0, 1).toUpperCase().equals("G")))
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Invalid NRIC/FIN")
                            .setMessage("Please enter a valid NRIC/FIN number.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    Log.d("MyLog", "fin length = " + String.valueOf(fin.length()));
                    //TextView tv = (TextView)findViewById(R.id.enter_FIN_msg);
                    //tv.setText(fin);

                    Intent intent = new Intent(MainActivity.this, QRTutActivity.class);
                    intent.putExtra("EXTRA_FIN", fin.toUpperCase()); // Pas
                    startActivity(intent);
                }

            }

        });

    }



    public void generateQRCodeHandler(View view) {

        //Log.d("MainActivity", "generateQRCodeHandler");
    }

    // RSA/PKI Related


}

/* References:
Android QR Code - http://smartandroiddeveloper.com/2016/01/29/how-to-generate-qrcode-in-10-minutes-using-zxing-library-in-android-studio/

 */