package dev.ujjwal.fluffytelegram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import dev.ujjwal.fluffytelegram.events.Broker;

public class MainActivity extends AppCompatActivity {

    NSDHelperRegister nsdHelperRegister;
    NSDHelperDisccover nsdHelperDisccover;

    MqttAndroidClient client;

    String topic;
    String payload;

    EditText editTextPayload;

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
        enableMQTT();
    }

    private void enableMQTT() {
        findViewById(R.id.linear_layout_2).setVisibility(View.VISIBLE);
        findViewById(R.id.linear_layout_3).setVisibility(View.VISIBLE);
        findViewById(R.id.linear_layout_4).setVisibility(View.VISIBLE);

        editTextPayload = findViewById(R.id.payload);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", clientId);
        topic = Constants.TOPIC;


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
