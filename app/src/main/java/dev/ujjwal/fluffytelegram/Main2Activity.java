package dev.ujjwal.fluffytelegram;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.ujjwal.fluffytelegram.events.NsdDiscoverInfo;
import dev.ujjwal.fluffytelegram.events.NsdRegisterInfo;
import dev.ujjwal.fluffytelegram.services.NsdDiscover;
import dev.ujjwal.fluffytelegram.services.NsdRegister;

public class Main2Activity extends AppCompatActivity {

    TextView tv_nsdInfo;

    NsdRegister nsdRegister;
    NsdDiscover nsdDiscover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String DEVICE = getIntent().getStringExtra("DEVICE");
        if (DEVICE.equalsIgnoreCase("teacher")) {
            Constants.IS_TEACHER = true;
            Constants.IS_STUDENT = false;
        } else if (DEVICE.equalsIgnoreCase("student")) {
            Constants.IS_TEACHER = false;
            Constants.IS_STUDENT = true;
        }

        init();
        if (Constants.IS_TEACHER) {
            initTeacher();
        }
        if (Constants.IS_STUDENT) {
            initStudent();
        }
    }

    private void init() {
        tv_nsdInfo = findViewById(R.id.tv_nsdInfo);
    }

    private void initTeacher() {
        if (nsdRegister == null)
            nsdRegister = new NsdRegister(getApplicationContext());
        if (nsdDiscover == null)
            nsdDiscover = new NsdDiscover(getApplicationContext());
    }

    private void initStudent() {
        if (nsdDiscover == null)
            nsdDiscover = new NsdDiscover(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        if (nsdRegister != null) {
            nsdRegister.tearDown();
        }
        if (nsdDiscover != null) {
            nsdDiscover.tearDown();
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
    public void onNsdRegisterEvent(NsdRegisterInfo nsdRegisterInfo) {
        String name = nsdRegisterInfo.getName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNsdDiscoverEvent(NsdDiscoverInfo nsdDiscoverInfo) {
        String name = nsdDiscoverInfo.getName();
        String type = nsdDiscoverInfo.getType();
        String host = nsdDiscoverInfo.getHost();
        int port = nsdDiscoverInfo.getPort();
        tv_nsdInfo.setText("Name: " + name + "  Type: " + type + "\nHost: " + host + "  Port: " + port);
    }
}
