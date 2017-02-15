package testapp.ttyi.certisme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class QRTutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrtut);

        final String fin = getIntent().getStringExtra("EXTRA_FIN");

        Button moodle_btn = (Button) findViewById(R.id.launchMoodle_btn);
        Button showQR_btn = (Button) findViewById(R.id.toShowQR_btn);

        moodle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                /* (This part for linking to URL directly
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.moodle.moodlemobile");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
                */

                String url = "https://moodle.certislearning.net/moodle/pluginfile.php/106/mod_resource/content/1/sample.gif";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);


            }
        });

        showQR_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(QRTutActivity.this, DisplayQRActivity.class);
                intent.putExtra("EXTRA_FIN", fin.toUpperCase());
                startActivity(intent);

            }
        });
    }

}