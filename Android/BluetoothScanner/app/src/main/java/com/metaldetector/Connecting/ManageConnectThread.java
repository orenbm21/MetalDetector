package com.metaldetector.Connecting;

import android.bluetooth.BluetoothSocket;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.metaldetector.DeviceListFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by User on 6/5/2015.
 */
public class ManageConnectThread extends Thread {

    private final DeviceListFragment myListFragment;
    InputStream inputStream;
    OutputStream outputStream;
    Thread workerThread;

    public void setStopWorker(boolean stopWorker) {
        this.stopWorker = stopWorker;
    }

    volatile boolean stopWorker;
    private ArrayList<Integer> sensor1Inputs;

    public ArrayList<byte[]> getPackets() {
        return packets;
    }

    private ArrayList<byte[]> packets;

    public ManageConnectThread(BluetoothSocket socket, DeviceListFragment deviceListFragment) throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        packets = new ArrayList<>();

        myListFragment = deviceListFragment;
    }

    public void sendData(String data) throws IOException{
        byte[] msgBuffer = data.getBytes();
        outputStream.write(msgBuffer);
    }

    public void beginListenForData()
    {
        final Handler handler = new Handler();

        stopWorker = false;
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = inputStream.available();
                            if(bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);
                                packets.add(packetBytes);
                            }
                        }
                        catch (IOException ex) {
                            stopWorker = true;
                        }
                }
                handler.post(new Runnable() {
                    public void run()
                    {
                        myListFragment.toggleScreen("results");
                    }
                });
                closeStream();
            }
        });
        workerThread.start();
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    private void closeStream() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                Log.d("ManageConnectThread", "Failed to close stream");
            }
            inputStream = null;
        }
    }
}
