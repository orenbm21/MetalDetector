package com.metaldetector.Connecting;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by User on 6/5/2015.
 */
public class ManageConnectThread extends Thread {

    TextView myLabel;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    public boolean isStopWorker() {
        return stopWorker;
    }

    public ArrayList<Integer> getInputs() {
        return inputs;
    }

    volatile boolean stopWorker;
    private ArrayList<Integer> inputs;

    public ManageConnectThread(BluetoothSocket socket, TextView label) throws IOException {
        mmInputStream = socket.getInputStream();
        myLabel = label;
        inputs = new ArrayList<>();
    }

    public void beginListenForData()
    {
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
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    data = data.replaceAll("(\\r|\\n)", "");
                                    try {
                                        int dataNum = Integer.parseInt(data);
                                        inputs.add(dataNum);
                                    } catch (Exception e) {
                                        // do nothing, just move to next input
                                    }
                                    readBufferPosition = 0;
                                    if (inputs.size() >= 10000) {
                                        stopWorker = true;

                                        handler.post(new Runnable()
                                        {
                                            public void run()
                                            {
                                                myLabel.setText(String.valueOf((new Random()).nextInt(100)));
                                            }
                                        });
                                        closeStream();
                                        break;
                                    }
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
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
        if (mmInputStream != null) {
            try {mmInputStream.close();} catch (Exception e) {}
            mmInputStream = null;
        }
    }
}
