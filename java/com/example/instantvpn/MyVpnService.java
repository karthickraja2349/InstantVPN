package com.example.instantvpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.*;
import java.net.Socket;

public class MyVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private volatile boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Builder builder = new Builder();
        builder.setSession("InstantVPN")
                .addAddress("10.8.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0); // Full tunnel

        try {
            vpnInterface = builder.establish();
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf(); // Stop service if VPN fails
            return START_NOT_STICKY;
        }

        isRunning = true;

        vpnThread = new Thread(() -> runVpnTunnel());
        vpnThread.start();

        return START_STICKY;
    }

    private void runVpnTunnel() {
        try (
                Socket socket = new Socket("192.168.178.108", 5555);
                InputStream vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
                OutputStream vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());
                InputStream netInput = socket.getInputStream();
                OutputStream netOutput = socket.getOutputStream()
        ) {
            byte[] vpnToNet = new byte[32767];
            byte[] netToVpn = new byte[32767];

            while (isRunning) {
                if (vpnInput.available() > 0) {
                    int len = vpnInput.read(vpnToNet);
                    if (len > 0) {
                        netOutput.write(vpnToNet, 0, len);
                        netOutput.flush();
                    }
                }

                if (netInput.available() > 0) {
                    int len = netInput.read(netToVpn);
                    if (len > 0) {
                        vpnOutput.write(netToVpn, 0, len);
                        vpnOutput.flush();
                    }
                }

                Thread.sleep(10);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;

        if (vpnThread != null && vpnThread.isAlive()) {
            vpnThread.interrupt();
            try {
                vpnThread.join(); // Wait for clean shutdown
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            vpnInterface = null;
        }
        Toast.makeText(this, "VPN Disconnected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRevoke() {
        onDestroy();
        stopSelf();
    }
}
