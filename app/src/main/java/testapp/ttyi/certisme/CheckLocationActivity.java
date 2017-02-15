package testapp.ttyi.certisme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CheckLocationActivity extends AppCompatActivity {

    GPSTracker gps;

    //Connect to the Firebase database
    final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    //Get a reference to certIsMeItems child items in the database
    final DatabaseReference myRef = mDatabase.getReference("certIsMeItems");

    //Get android ID as a unique identifier
    String android_id;
    TextView txtLatitudeVal, txtLongitudeVal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_location);

        //Create a new GPSTracker
        android_id= Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        gps = new GPSTracker(CheckLocationActivity.this);

        if(gps.canGetLocation()) {
            //Log.d("MainActivity"," canGetLocation = true");

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            txtLatitudeVal = (TextView)findViewById(R.id.latDisp_textView);
            txtLongitudeVal = (TextView)findViewById(R.id.longDisp_textView);

            txtLatitudeVal.setText(Double.toString(latitude));
            //childRef.setValue(txtLatitudeVal.getText().toString());
            txtLongitudeVal.setText(Double.toString(longitude));
            //childRef = myRef.push();
            //childRef.setValue(txtLongitudeVal.getText().toString());
            writeNewGPSLocation(latitude,longitude);
        }else{
            gps.showSettingsAlert();
        }

        Button next_btn = (Button) findViewById(R.id.next_btn);

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(getApplicationContext(), QRTutActivity.class);
                intent.putExtra("EXTRA_FIN", "A1234567A"); // This is hardcoded stuff
                startActivity(intent);

            }
        });


    }

    private void writeNewGPSLocation(double latitude, double longitude) {
        GPSLocation gpsLocation = new GPSLocation(latitude,longitude);

        myRef.child("GPSLocation").child(android_id).setValue(gpsLocation);
    }

}
