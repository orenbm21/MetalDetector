package com.metaldetector;

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

import com.metaldetector.Connecting.ManageConnectThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class ListActivity extends ActionBarActivity implements DeviceListFragment.OnFragmentInteractionListener  {

    private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;
    private BluetoothDevice detector;
    private ManageConnectThread manageConnectThread;
    private Algorithm alg;
    private ConnectThread connectThread;

    public static int REQUEST_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

//         Hide title bar
        getSupportActionBar().hide();

//        // Go to full screen
//        this.getWindow().setFlags(WindowManager.LayoutParams.
//                FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        alg = new Algorithm();
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
        manageConnectThread = null;
        try {
             manageConnectThread = new ManageConnectThread(connectThread.getbTSocket(), mDeviceListFragment);
        } catch (IOException e) {
            Log.d("ListActivity", "Could not get socket");
        }
        try {
            manageConnectThread.sendData("7");
            Log.d("ManageConnectThread", "send data - begin reading");
        } catch (IOException e) {
            Log.d("ManageConnectThread", "Could not send data");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manageConnectThread.beginListenForData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manageConnectThread.setStopWorker(true);
                        getResults();
                    }
                }, 5000);
            }
        }, 5000);
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

        ArrayList<byte[]> packets = manageConnectThread.getPackets();
        alg.analyzePackets(packets);

        // calc by frequency
        mDeviceListFragment.changeColor(alg.calcHasMetalByFrequency(1, alg.getSensor1Frequency()), 1);
        mDeviceListFragment.changeColor(alg.calcHasMetalByFrequency(2, alg.getSensor2Frequency()), 2);

//        // calc by amplitude
//        mDeviceListFragment.changeColor(alg.calcHasMetalByAmplitude(1, alg.getSensor1Amplitude()), 1);
//        mDeviceListFragment.changeColor(alg.calcHasMetalByAmplitude(2, alg.getSensor2Amplitude()), 2);

        connectThread.closeSocket();
    }

}
