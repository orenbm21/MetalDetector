package com.metaldetector.UI;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.metaldetector.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener{

    public static final int MANAGE_CONNECTION_POSITION = 1010;
    public static final int GET_SCAN_RESULTS = 1111;
    public static final int CALIBRATE_DEVICE = 2222;

    private OnFragmentInteractionListener mListener;

    private Button scanWallButton;
    private Button restartButton;
    private Button calibrateButton;

    private ProgressBar scanningBar;

    private TextView calibrationResult;
    private TextView deviceNameTitle;
    private TextView devicesLabel;

    private ImageView sensor1ResultImageView;
    private ImageView sensor2ResultImageView;
    private ImageView sensor3ResultImageView;
    private ImageView sensor4ResultImageView;

    private AbsListView mListView;

    private LinearLayout screen1;
    private LinearLayout screen2;
    private LinearLayout screen3;
    private LinearLayout sensorResults;
    private LinearLayout endCalibration;

    private ArrayList <BluetoothDevice> bluetoothDeviceList;
    private static BluetoothAdapter bTAdapter;
    private ArrayAdapter<DeviceItem> mAdapter;
    private ArrayList <DeviceItem> deviceItemList;

    private String connectedTo;
    private boolean isBeforeCalibration;

    public ArrayList<BluetoothDevice> getBluetoothDeviceList() {
        return bluetoothDeviceList;
    }

    public TextView getDeviceNameTitle() {
        return deviceNameTitle;
    }

    public void setDeviceNameTitle(String deviceNameTitle) {
        this.deviceNameTitle.setText(deviceNameTitle);
    }

    public void changeColor(boolean success, int sensorIndex) {
        GradientDrawable drawable;
        drawable = getGradientDrawable(sensorIndex);
        if (drawable != null) {
            colorSensor(success, drawable);
        }
    }

    private void colorSensor(boolean success, GradientDrawable drawable) {
        if (success) {
            drawable.setColor(Color.WHITE);
            return;
        }
        drawable.setColor(Color.BLACK);
    }

    @Nullable
    private GradientDrawable getGradientDrawable(int sensorIndex) {
        GradientDrawable drawable;
        switch (sensorIndex) {
            case 0:
                drawable = (GradientDrawable) sensor1ResultImageView.getDrawable();
                break;
            case 1:
                drawable = (GradientDrawable) sensor2ResultImageView.getDrawable();
                break;
            case 2:
                drawable = (GradientDrawable) sensor3ResultImageView.getDrawable();
                break;
            case 3:
                drawable = (GradientDrawable) sensor4ResultImageView.getDrawable();
                break;
            default:
                drawable = null;
        }
        return drawable;
    }

    public void toggleScreen(String state) {
        switch (state) {
            case "finishedScan":
                // closing screen 2
                scanningBar.setVisibility(View.INVISIBLE);
                screen2.setVisibility(View.GONE);

                // opening screen 3
                screen3.setVisibility(View.VISIBLE);

                // modifying screen 3
                if (isBeforeCalibration) {
                    endCalibration.setVisibility(View.VISIBLE);
                    sensorResults.setVisibility(View.GONE);
                    isBeforeCalibration = false;
                }
                else {
                    endCalibration.setVisibility(View.GONE);
                    sensorResults.setVisibility(View.VISIBLE);
                }
                break;
            case "scanAgain":
                // closing screen 3
                screen3.setVisibility(View.GONE);

                // opening screen 2
                screen2.setVisibility(View.VISIBLE);

                // modifying screen 2
                deviceNameTitle.setText("Scanning...");
                scanningBar.setVisibility(View.VISIBLE);
                break;
            case "firstScan":
                // modifying screen 2
                scanningBar.setVisibility(View.VISIBLE);
                scanWallButton.setVisibility(View.GONE);
                deviceNameTitle.setText("Calibrating...");
                break;
            case "calibrate":
                // closing screen 3
                screen3.setVisibility(View.GONE);

                // opening screen 2
                screen2.setVisibility(View.VISIBLE);

                // modifying screen 2
                deviceNameTitle.setText("Calibrating...");
                scanningBar.setVisibility(View.VISIBLE);
                break;
            case "preScan":
                // closing screen 1
                screen1.setVisibility(View.GONE);

                // opening screen 2
                screen2.setVisibility(View.VISIBLE);

                // modifying screen 2
                scanWallButton.setVisibility(View.VISIBLE);
                scanningBar.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                mAdapter.add(newDevice);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    // TODO: Rename and change types of parameters
    public static DeviceListFragment newInstance(BluetoothAdapter adapter) {
        DeviceListFragment fragment = new DeviceListFragment();
        bTAdapter = adapter;
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");
        deviceItemList = new ArrayList<DeviceItem>();
        bluetoothDeviceList = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);
                bluetoothDeviceList.add(device);
            }
        }

        // If there are no devices, add an item that states so. It will be handled in the view.
        if(deviceItemList.size() == 0) {
            deviceItemList.add(new DeviceItem("No Devices", "", "false"));
        }

        Log.d("DEVICELIST", "DeviceList populated\n");

        mAdapter = new DeviceListAdapter(getActivity(), deviceItemList, bTAdapter);

        Log.d("DEVICELIST", "Adapter created\n");

        isBeforeCalibration = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceitem_list, container, false);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        devicesLabel = (TextView) view.findViewById(R.id.devicesTitle);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        deviceNameTitle = (TextView) view.findViewById(R.id.deviceNameTitle);
        connectedTo = deviceNameTitle.getText().toString();

        calibrationResult = (TextView) view.findViewById(R.id.calibrationEnd);
        scanWallButton = (Button) view.findViewById(R.id.scanWall);
        scanWallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleScreen("firstScan");
                mListener.onFragmentInteraction(MANAGE_CONNECTION_POSITION);
            }
        });

        scanningBar = (ProgressBar) view.findViewById(R.id.loadingBar);

        restartButton = (Button) view.findViewById(R.id.scanAgain);
        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleScreen("scanAgain");
                mListener.onFragmentInteraction(MANAGE_CONNECTION_POSITION);
            }
        });

        calibrateButton = (Button) view.findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isBeforeCalibration = true;
                toggleScreen("calibrate");
                mListener.onFragmentInteraction(CALIBRATE_DEVICE);
            }
        });

        screen1 = (LinearLayout) view.findViewById(R.id.screen1);
        screen2 = (LinearLayout) view.findViewById(R.id.screen2);
        screen3 = (LinearLayout) view.findViewById(R.id.screen3);
        sensorResults = (LinearLayout) view.findViewById(R.id.sensorResults);
        endCalibration = (LinearLayout) view.findViewById(R.id.endCalibrateIndication);

        sensor1ResultImageView = (ImageView) view.findViewById(R.id.sensor1Result);
        sensor2ResultImageView = (ImageView) view.findViewById(R.id.sensor2Result);
        sensor3ResultImageView = (ImageView) view.findViewById(R.id.sensor3Result);
        sensor4ResultImageView = (ImageView) view.findViewById(R.id.sensor4Result);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d("DEVICELIST", "onItemClick position: " + position +
                " id: " + id + " name: " + deviceItemList.get(position).getDeviceName() + "\n");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(position);
        }

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int position);
    }

}
