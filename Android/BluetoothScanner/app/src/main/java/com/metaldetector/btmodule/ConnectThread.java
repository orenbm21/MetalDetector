package com.metaldetector.btmodule;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by User on 6/3/2015.
 */
public class ConnectThread extends Thread{

    private final BluetoothDevice bTDevice;

    public BluetoothSocket getbTSocket() {
        return bTSocket;
    }

    public BluetoothDevice getbTDevice() {
        return bTDevice;
    }

    private BluetoothSocket bTSocket;

    public ConnectThread(BluetoothDevice bTDevice, UUID UUID) {
        BluetoothSocket tmp = null;
        this.bTDevice = bTDevice;

        try {
            tmp = this.bTDevice.createRfcommSocketToServiceRecord(UUID);
        }
        catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not start listening for RFCOMM");
        }
        bTSocket = tmp;
    }

    public boolean connect() {

        try {
            bTSocket.connect();
        } catch(IOException e) {
            Log.d("CONNECTTHREAD","Could not connect: " + e.toString());
            try {
                bTSocket.close();
            } catch(IOException close) {
                Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }

    public boolean cancel() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    public void closeSocket() {
        if (bTSocket != null) {
            try {bTSocket.close();} catch (Exception e) {}
            bTSocket = null;
        }

    }

}
