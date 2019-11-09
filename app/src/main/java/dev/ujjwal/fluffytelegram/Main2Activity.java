package dev.ujjwal.fluffytelegram;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;

import dev.ujjwal.fluffytelegram.events.MqttBrokerInfo;
import dev.ujjwal.fluffytelegram.events.NsdDiscoverInfo;
import dev.ujjwal.fluffytelegram.events.NsdRegisterInfo;
import dev.ujjwal.fluffytelegram.services.MqttBroker;
import dev.ujjwal.fluffytelegram.services.NsdDiscover;
import dev.ujjwal.fluffytelegram.services.NsdRegister;

public class Main2Activity extends AppCompatActivity {

    TextView tv_nsdInfo;
    EditText editTextPayload;

    NsdRegister nsdRegister;
    NsdDiscover nsdDiscover;

    MqttAndroidClient client;

    String topic;
    String payload;

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
        editTextPayload = findViewById(R.id.payload);
    }

    private void initTeacher() {
        if (nsdRegister == null)
            nsdRegister = new NsdRegister(getApplicationContext());
        if (nsdDiscover == null)
            nsdDiscover = new NsdDiscover(getApplicationContext());

        findViewById(R.id.nsdStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nsdRegister == null)
                    nsdRegister = new NsdRegister(getApplicationContext());
                else
                    Toast.makeText(getApplicationContext(), "NSD already running", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.nsdStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nsdRegister != null) {
                    nsdRegister.tearDown();
                    nsdRegister = null;
                    Toast.makeText(getApplicationContext(), "NSD stopped", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "NSD isn't started", Toast.LENGTH_SHORT).show();
            }
        });
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
        Constants.BROKER_URL = host;

        if (Constants.IS_TEACHER && !Constants.BROKER_STARTED) {
            Intent mqttIntent = new Intent(getApplicationContext(), MqttBroker.class);
            startService(mqttIntent);
        }

        if (Constants.IS_TEACHER) {
            findViewById(R.id.linear_layout_1).setVisibility(View.VISIBLE);
        }
        enableMQTT();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttBrokerEvent(MqttBrokerInfo mqttBrokerInfo) {
        Constants.BROKER_STARTED = mqttBrokerInfo.isBROKER_STARTED();
    }

    private void enableMQTT() {
        findViewById(R.id.linear_layout_2).setVisibility(View.VISIBLE);
        findViewById(R.id.linear_layout_3).setVisibility(View.VISIBLE);
        findViewById(R.id.linear_layout_4).setVisibility(View.VISIBLE);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + Constants.BROKER_URL + ":1883", clientId);
        topic = Constants.MQTT_TOPIC;


        /*******************************************************************************************
         * Connect
         ******************************************************************************************/
        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();

                findViewById(R.id.disconnect).setEnabled(true);
                findViewById(R.id.subscribe).setEnabled(true);
                findViewById(R.id.unsubscribe).setEnabled(true);
                findViewById(R.id.publish).setEnabled(true);
            }
        });


        /*******************************************************************************************
         * Disconnect
         ******************************************************************************************/
        findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.isConnected()) {
                    disconnect();

                    findViewById(R.id.disconnect).setEnabled(false);
                    findViewById(R.id.subscribe).setEnabled(false);
                    findViewById(R.id.unsubscribe).setEnabled(false);
                    findViewById(R.id.publish).setEnabled(false);
                } else {
                    Toast.makeText(getApplicationContext(), "Client is offline", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*******************************************************************************************
         * Subscribe
         ******************************************************************************************/
        findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.isConnected()) {
                    subscribe();
                } else {
                    Toast.makeText(getApplicationContext(), "Client is offline", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*******************************************************************************************
         * Unsubscribe
         ******************************************************************************************/
        findViewById(R.id.unsubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.isConnected()) {
                    unsubscribe();
                } else {
                    Toast.makeText(getApplicationContext(), "Client is offline", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*******************************************************************************************
         * Publish Message
         ******************************************************************************************/
        findViewById(R.id.publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.isConnected()) {
                    publish();
                } else {
                    Toast.makeText(getApplicationContext(), "Client is offline", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*******************************************************************************************
         * Receive Message
         ******************************************************************************************/
        receive();
    }

    private void connect() {
        try {
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
//            options.setUserName("USERNAME");
//            options.setPassword("PASSWORD".toCharArray());
//            IMqttToken token = client.connect(options);

            IMqttToken token = client.connect();

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(getApplicationContext(), "Connect success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(getApplicationContext(), "Connect failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    Toast.makeText(getApplicationContext(), "Disconnect success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                    Toast.makeText(getApplicationContext(), "Disconnect failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        try {
            IMqttToken subToken = client.subscribe(topic, Constants.MQTT_QOS);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText(getApplicationContext(), "Subscribe success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(getApplicationContext(), "Subscribe failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe() {
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Toast.makeText(getApplicationContext(), "Unsubscribe success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Toast.makeText(getApplicationContext(), "Unsubscribe failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish() {
        byte[] encodedPayload;
        try {
            payload = editTextPayload.getText().toString().trim();
            if (payload.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter payload", Toast.LENGTH_SHORT).show();
                return;
            }
            editTextPayload.setText("");
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void receive() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast.makeText(getApplicationContext(), new String(message.getPayload()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }
}
