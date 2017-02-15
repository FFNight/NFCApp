package testapp.ttyi.certisme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        Button visitor_btn = (Button) findViewById(R.id.visitor_btn);
        Button officer_btn = (Button) findViewById(R.id.next_btn);

        // If Visitor button is pressed
        visitor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                    Intent intent = new Intent(ChooseRoleActivity.this, CheckLocationActivity.class);
                    intent.putExtra("EXTRA_FIN", "A1234567A"); // Hardcoded FIN just for the demo XD
                    startActivity(intent);
                }
        });

        // If APO button is pressed
        officer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(ChooseRoleActivity.this, QRTutActivity.class);
                intent.putExtra("EXTRA_FIN", "A1234567A"); // Hardcoded FIN just for the demo XD
                startActivity(intent);
            }
        });

    }
}
