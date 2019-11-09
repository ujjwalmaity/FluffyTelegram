package dev.ujjwal.fluffytelegram;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

class NSDHelperDisccover {

    private String TAG = MainActivity.class.getName();

    private Context context;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    NSDHelperDisccover(Context context) {
        this.context = context;

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        initializeDiscoveryListener();
        nsdManager.discoverServices(Constants.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    /***********************************************************************************************
     * Discover services on the network
     **********************************************************************************************/
    private void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(Constants.NSD_SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().contains(Constants.NSD_SERVICE_NAME)) {
                    initializeResolveListener();
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /***********************************************************************************************
     * Connect to services on the network
     **********************************************************************************************/
    private void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                Toast.makeText(context, "Service Info: " + serviceInfo.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    /***********************************************************************************************
     * Unregister your service on application close
     **********************************************************************************************/
    public void tearDown() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }
}
