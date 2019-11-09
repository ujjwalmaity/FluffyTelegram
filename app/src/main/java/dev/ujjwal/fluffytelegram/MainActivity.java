package dev.ujjwal.fluffytelegram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.ujjwal.fluffytelegram.events.Broker;

public class MainActivity extends AppCompatActivity {

    NSDHelperRegister nsdHelperRegister;
    NSDHelperDisccover nsdHelperDisccover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.nsdStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nsdHelperRegister == null)
                    nsdHelperRegister = new NSDHelperRegister(getApplicationContext());
                else
                    Toast.makeText(getApplicationContext(), "NSD already running", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.nsdStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nsdHelperRegister != null) {
                    nsdHelperRegister.tearDown();
                    nsdHelperRegister = null;
                    Toast.makeText(getApplicationContext(), "NSD stopped", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "NSD not started", Toast.LENGTH_SHORT).show();
            }
        });

        nsdHelperDisccover = new NSDHelperDisccover(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        if (nsdHelperRegister != null) {
            nsdHelperRegister.tearDown();
        }
        if (nsdHelperDisccover != null) {
            nsdHelperDisccover.tearDown();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(Broker broker) {
        Toast.makeText(getApplicationContext(), broker.getBROKER_URL(), Toast.LENGTH_SHORT).show();
    }
}
