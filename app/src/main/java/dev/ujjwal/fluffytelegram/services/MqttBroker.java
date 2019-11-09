package dev.ujjwal.fluffytelegram.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import dev.ujjwal.fluffytelegram.Constants;
import dev.ujjwal.fluffytelegram.events.MqttBrokerInfo;
import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;

public class MqttBroker extends Service {

    private static final String TAG = MqttBroker.class.getName();

    Server server;
    MemoryConfig memoryConfig;
    Thread mqttServiceThread;

    public MqttBroker() {
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.i(TAG, "onStartCommand called...");

        //starting mqtt service
        server = new Server();
        //String hostname = intent.getStringExtra(Constants.BROKER_HOSTNAME);

        memoryConfig = new MemoryConfig(new Properties());

        File path = getApplicationContext().getExternalFilesDir(null);
        if (!path.exists())
            path.mkdirs();

        String dirPath = path.getAbsolutePath();
        dirPath += "/.SVEData";

        //memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME);
        memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,
                dirPath + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME);

        //delete the existing files
        File tempFile = new File(dirPath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        File[] files = tempFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith(BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME)) {
                    Log.i(TAG, "deleting: " + file.getName());
                    file.delete();
                }
            }
        }

        //Run service in foreground so it is less likely to be killed by system
        //startInForeground();
        mqttServiceThread = new Thread(new MqttThread());
        mqttServiceThread.start();
        Log.i(TAG, "starting MQTT service on IP: " + Constants.BROKER_URL);
        EventBus.getDefault().post(new MqttBrokerInfo(true));

        return START_STICKY;
    }

    /*
    private void startInForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText("Running")
                //.setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(9999,notification);
    }
    */

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onDestroy() {
        mqttServiceThread.interrupt();
        /*
        try {
            mqttServiceThread.join();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(TAG,"thread is interrupted and joined? isAlive: "+ mqttServiceThread.isAlive());
        server.stopServer();
        */

        super.onDestroy();
    }

    private class MqttThread extends Thread {
        public void run() {
            try {
                server.startServer(memoryConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
