package com.metaldetector.Connecting;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by User on 6/5/2015.
 */
public class ManageConnectThread extends Thread {

    TextView myLabel;
    InputStream inputStream;
    OutputStream outputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int currentSensor = 1;

    public boolean isStopWorker() {
        return stopWorker;
    }

    public ArrayList<Integer> getSensor1Inputs() {
        return sensor1Inputs;
    }

    volatile boolean stopWorker;
    private ArrayList<Integer> sensor1Inputs;

    public ArrayList<Integer> getSensor2Inputs() {
        return sensor2Inputs;
    }

    private ArrayList<Integer> sensor2Inputs;

    public ManageConnectThread(BluetoothSocket socket, TextView label) throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        myLabel = label;
        sensor1Inputs = new ArrayList<>();
        sensor2Inputs = new ArrayList<>();
    }

    public void sendData(String data) throws IOException{
        byte[] msgBuffer = data.getBytes();

//        ByteArrayOutputStream output = new ByteArrayOutputStream(4);
//        output.write(data);
        outputStream.write(msgBuffer);
    }

    public void beginListenForData()
    {
        try {
            sendData("1212");
            Log.d("ManageConnectThread", "send data");
        } catch (IOException e) {
            Log.d("ManageConnectThread", "Could not send data");
        }

        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    data = data.replaceAll("(\\r|\\n)", "");
                                    if (data.equals("$$$")) {
                                        Log.d("ManageConnectThread", "dollar");
                                        if (currentSensor == 1) {
                                            currentSensor++;
                                            readBufferPosition = 0;
                                            continue;
                                        } else {
                                            stopWorker = true;

                                            handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    myLabel.setText("Hurray!");
                                                }
                                            });
                                            closeStream();
                                            break;
                                        }
                                    }
                                    try {
                                        int dataNum = Integer.parseInt(data);
                                        addInputToArray(dataNum);
                                    } catch (NumberFormatException e) {
                                        Log.d("ManageConnectThread", "Tried to parse a string: " + data);
                                    }
                                    readBufferPosition = 0;
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void addInputToArray(int dataNum) {
        if (currentSensor == 1) {
            sensor1Inputs.add(dataNum);
        }
        else if (currentSensor == 2) {
            sensor2Inputs.add(dataNum);
        }
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    private void closeStream() {
        if (inputStream != null) {
            try {
                inputStream.close();} catch (Exception e) {}
            inputStream = null;
        }
    }
}
