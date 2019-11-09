package dev.ujjwal.fluffytelegram;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String DEVICE = getIntent().getStringExtra("DEVICE");
        if (DEVICE.equalsIgnoreCase("teacher")) {
            Constants.IS_TEACHER = true;
        } else if (DEVICE.equalsIgnoreCase("student")) {
            Constants.IS_STUDENT = true;
        }
    }
}
