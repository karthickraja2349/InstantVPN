package com.example.instantvpn;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private static final int VPN_REQUEST_CODE = 0x0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startVpnButton = findViewById(R.id.startVpnButton);
        Button stopVpnButton = findViewById(R.id.stopVpnButton);

        startVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = VpnService.prepare(MainActivity.this);
                if (intent != null) {
                    // Request permission for VPN service
                    startActivityForResult(intent, VPN_REQUEST_CODE);
                } else {
                    // If no preparation is needed, directly start the VPN service
                    onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
                }
            }
        });

        stopVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyVpnService.class);
                stopService(intent);
            }
        });
    }



    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == VPN_REQUEST_CODE && result == RESULT_OK) {
            // Start the VPN service if permission is granted
            Intent intent = new Intent(this, MyVpnService.class);
            startService(intent);
        }
        super.onActivityResult(request, result, data);
    }
}
