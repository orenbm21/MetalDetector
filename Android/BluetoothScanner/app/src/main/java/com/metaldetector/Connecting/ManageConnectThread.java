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

    private void version2(Handler handler) throws IOException {
        sensor1Inputs = new ArrayList<>();
        int readBufferPosition = 0;
        boolean arduinoIsReady = true;
        while(!Thread.currentThread().isInterrupted() && !stopWorker) {
            byte[] readBuffer = new byte[1024];
            int bytesAvailable = inputStream.available();
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                inputStream.read(packetBytes);

                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = packetBytes[i];
                    if (b == 10) {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        String data = null;
                        try {
                            data = new String(encodedBytes, "US-ASCII");
                            data = data.replaceAll("(\\r|\\n)", "");
//                            if (data.equals("$$$") || data.equals("$$$\r")) {
//                                sendData("8");
//                                Log.d("ManageConnectThread", "send data - start sending inputs");
//                                arduinoIsReady = true;
//                            } else if (arduinoIsReady) {
                                try {
                                    int dataNum = Integer.parseInt(data);
                                    sensor1Inputs.add(dataNum);
                                    if (sensor1Inputs.size() >= 500) {
                                        stopWorker = true;
                                        handler.post(new Runnable() {
                                            public void run()
                                            {
                                                myListFragment.toggleScreen("results");
                                            }
                                        });
                                        closeStream();
                                        break;
                                    }

                                } catch (NumberFormatException e) {
                                    Log.d("Algorithm", "Tried to parse a string: " + data);
                                }

//                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.d("Algorithm", "UnsupportedEncodingException");
                        }
                        sendData("8");
                        Log.d("ManageConnectThread", "send data - read input");
                        readBufferPosition = 0;
                    } else {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }
        }
    }

    public void beginListenForData()
    {
        final Handler handler = new Handler();

        stopWorker = false;
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                boolean started = false;
                boolean finished = false;
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    if (!started || !finished) {
                        try {
                            int bytesAvailable = inputStream.available();
                            if(bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);
                                packets.add(packetBytes);
                                started = true;
                            } else {
                                if (started) {
                                    finished = true;
                                }
                            }
                        }
                        catch (IOException ex) {
                            stopWorker = true;
                        }
                    } else {
                        stopWorker = true;
                        handler.post(new Runnable() {
                            public void run()
                            {
                                myListFragment.toggleScreen("results");
                            }
                        });
                        closeStream();
                        break;
                    }
                }
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
