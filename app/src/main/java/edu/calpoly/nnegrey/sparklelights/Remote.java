package edu.calpoly.nnegrey.sparklelights;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public final class Remote extends AppCompatActivity {

    private Button buttonOn;
    private Button buttonOff;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    public static BluetoothDevice bb_8;
    public static OutputStream outputStream = null;
    public static boolean isConnected = false;

    protected Menu m_vwMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    write("1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonOff = (Button) findViewById(R.id.buttonOff);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    write("0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        connectBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        m_vwMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                connectBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void connectBluetooth() {
        AsyncSetupBluetoothTask asyncSetupBluetoothTask = new AsyncSetupBluetoothTask();
        asyncSetupBluetoothTask.execute();
    }

    private void checkStatus() {
        if (isConnected) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    public static void write(String s) throws IOException {
        if (outputStream == null) {
//            Toast.makeText(Remote.this, "Disconnected", Toast.LENGTH_LONG).show();
        }
        else {
            s.replace("<", "");
            s.replace(">", "");
            String message;
            while (s.length() >= 78) {
                message = "<" + s.substring(0, 77) + ">";
                outputStream.write(message.getBytes());
                s = s.substring(77, s.length());
            }

            s = "<" + s + ">";
            outputStream.write(s.getBytes());

            // Signify EOF to board.
            s = "<!>";
            outputStream.write(s.getBytes());
        }
    }

    public class AsyncSetupBluetoothTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("HC05_SLV")) {
                        bb_8 = bt;
                    }
                }
            }

            try {
                ParcelUuid[] uuids = bb_8.getUuids();
                BluetoothSocket socket = bb_8.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                socket.connect();
                outputStream = socket.getOutputStream();
                isConnected = true;
            } catch (Exception e) {
                return false;
            }
            if (isConnected) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            checkStatus();
        }
    }
}
