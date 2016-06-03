package com.metaldetector.UI;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.metaldetector.R;
import com.metaldetector.btmodule.Communicator;
import com.metaldetector.btmodule.ConnectThread;
import com.metaldetector.scanningdevice.ScanningDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class ListActivity extends ActionBarActivity implements DeviceListFragment.OnFragmentInteractionListener  {

    public static final String START_SENDING = "7";
    public static final int DELAY_BEFORE_LISTENING = 5000;
    public static final int DELAY_BEFORE_STOP_LISTENING = 5000;
    private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;
    private BluetoothDevice detector;
    private Communicator communicator;
    private ScanningDevice scanningDevice;
    private ConnectThread connectThread;

    public static int REQUEST_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

//         Hide title bar
        getSupportActionBar().hide();

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        mDeviceListFragment = DeviceListFragment.newInstance(BTAdapter);
        fragmentManager.beginTransaction().replace(R.id.container, mDeviceListFragment).commit();
        scanningDevice = new ScanningDevice(2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void manageConnection() {
        communicator = null;
        try {
             communicator = new Communicator(connectThread.getbTSocket(), mDeviceListFragment);
        } catch (IOException e) {
            Log.d("ListActivity", "Could not get socket");
        }
        try {
            communicator.sendData(START_SENDING);
            Log.d("Communicator", "send data - begin reading");
        } catch (IOException e) {
            Log.d("Communicator", "Could not send data");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                communicator.beginListenForData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        communicator.setStopWorker(true);
                        getResults();
                    }
                }, DELAY_BEFORE_STOP_LISTENING);
            }
        }, DELAY_BEFORE_LISTENING);
    }

    @Override
    public void onFragmentInteraction(int position) {

        if (position == DeviceListFragment.MANAGE_CONNECTION_POSITION) {
            mDeviceListFragment.toggleScreen("scan");
            manageConnection();
            return;
        }
        if (position == DeviceListFragment.GET_SCAN_RESULTS) {
            getResults();
            return;
        }
        detector = mDeviceListFragment.getBluetoothDeviceList().get(position);
        connectThread = new ConnectThread(detector, UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        boolean connectionSuccess = connectThread.connect();

        if (connectionSuccess) {
            mDeviceListFragment.setDeviceNameTitle(mDeviceListFragment.getDeviceNameTitle().getText().toString() + connectThread.getbTDevice().getName());
            mDeviceListFragment.toggleScreen("preScan");
        }
    }

    public void getResults() {

        ArrayList<byte[]> packets = communicator.getPackets();
        scanningDevice.analyzePackets(packets);

        ArrayList<Boolean> hasMetal = scanningDevice.calcHasMetal();

        for (int i = 0; i < scanningDevice.getNumOfSensors(); i++) {
            mDeviceListFragment.changeColor(hasMetal.get(i), i);
        }

        connectThread.closeSocket();
    }

}
