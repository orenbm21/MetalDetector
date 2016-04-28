package com.tutsplus.matt.bluetoothscanner.Connecting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by User on 6/5/2015.
 */
public class ManageConnectThread extends Thread {

    TextView myLabel;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;

    public boolean isStopWorker() {
        return stopWorker;
    }

    public ArrayList<Integer> getInputs() {
        return inputs;
    }

    volatile boolean stopWorker;
    ArrayList<Integer> inputs;

    public ManageConnectThread(BluetoothSocket socket, TextView label) throws IOException {
        mmInputStream = socket.getInputStream();
        myLabel = label;
        inputs = new ArrayList<>();
    }

//    public void sendData(BluetoothSocket socket, int data) throws IOException{
//        ByteArrayOutputStream output = new ByteArrayOutputStream(4);
//        output.write(data);
//        OutputStream outputStream = socket.getOutputStream();
//        outputStream.write(output.toByteArray());
//    }
//
//    public int receiveData(BluetoothSocket socket) throws IOException{
//
//        InputStream inputStream = socket.getInputStream();
//
//        int bytesAvailable = inputStream.available();
//        if(bytesAvailable > 0)
//        {
//            byte[] buffer = new byte[bytesAvailable];
//            inputStream.read(buffer);
//            ByteArrayInputStream input = new ByteArrayInputStream(buffer);
//
//            int readInput =  input.read();
//            return readInput;
//        }
//        return -1;
//    }

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
                                    int dataNum = Integer.parseInt(data);
                                    readBufferPosition = 0;
                                    inputs.add(dataNum);
                                    if (inputs.size() >= 1000) {
                                        stopWorker = true;
                                        break;
                                    }

//                                    handler.post(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//                                            myLabel.setText(data);
//                                        }
//                                    });
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
}
