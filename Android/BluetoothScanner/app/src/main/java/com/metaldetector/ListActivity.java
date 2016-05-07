package com.metaldetector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.metaldetector.Connecting.ManageConnectThread;

import java.io.IOException;
import java.util.UUID;


public class ListActivity extends ActionBarActivity implements DeviceListFragment.OnFragmentInteractionListener  {

    private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;
    private BluetoothDevice detector;

    public ConnectThread getConnectThread() {
        return connectThread;
    }

    private ConnectThread connectThread;

    public static int REQUEST_BLUETOOTH = 1;
    public static final String BLUETOOTH_SOCKET = "BlueToothSocketExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

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
        ManageConnectThread manageConnectThread = null;
        try {
            manageConnectThread = new ManageConnectThread(connectThread.getbTSocket(), mDeviceListFragment.scanResult);
        } catch (IOException e) {
            Log.d("ListActivity", "Could not get socket");
        }
            manageConnectThread.beginListenForData();
//            while (!manageConnectThread.isStopWorker()) {
//
//            }
//            ArrayList<Integer> inputs = manageConnectThread.getInputs();
//            devicesLabel.setText("Start Analyzing");
    }

    @Override
    public void onFragmentInteraction(int position) {
        if (position == DeviceListFragment.MANAGE_CONNECTION_POSITION) {
            manageConnection();
            return;
        }
        detector = mDeviceListFragment.getBluetoothDeviceList().get(position);
        connectThread = new ConnectThread(detector, UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        boolean connectionSuccess = connectThread.connect();

        if (connectionSuccess) {

//            Intent connectionManagerIntent = new Intent(this, ConnectionActivity.class);
//            startActivity(connectionManagerIntent);
            mDeviceListFragment.setDeviceNameTitle(mDeviceListFragment.getDeviceNameTitle().getText().toString() + connectThread.getbTDevice().getName());
            mDeviceListFragment.toggleScreen();
        }
    }
}