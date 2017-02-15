package testapp.ttyi.certisme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_confirm);

        Button btn_back = (Button) findViewById(R.id.back_button);

        final String fin = getIntent().getStringExtra("EXTRA_FIN"); // Get the fin String passed in from the previous page (MainActivity)

        TextView user_nric_no = (TextView) findViewById(R.id.fin_textView);
        user_nric_no.setText(fin);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });


    }
}
