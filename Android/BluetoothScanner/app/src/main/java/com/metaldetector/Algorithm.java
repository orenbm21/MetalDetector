package com.metaldetector;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by orenb on 5/14/2016.
 */
public class Algorithm {

    private static final int DELIMITER = 10;
    private ArrayList<Integer> sensor1Inputs;
    private int frequency;

    public int getFrequency() {
        return frequency;
    }

    public Algorithm() {
        sensor1Inputs = new ArrayList<>();
    }

    public void analyzePackets(ArrayList<byte[]> packets) {

        ArrayList<Byte> bytes = toOneByteArray(packets);
        byte[] readBuffer = new byte[10];
        int readBufferPosition = 0;
        int bytesAvailable = bytes.size();
        for(int i=0;i<bytesAvailable;i++)
        {
            byte b = bytes.get(i);
            if(b == DELIMITER) {
                byte[] encodedBytes = new byte[readBufferPosition];
                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                String data = null;
                try {
                    data = new String(encodedBytes, "US-ASCII");
                    data = data.replaceAll("(\\r|\\n)", "");
                    try {
                        int dataNum = Integer.parseInt(data);
                        sensor1Inputs.add(dataNum);
                    } catch (NumberFormatException e) {
                        Log.d("Algorithm", "Tried to parse a string: " + data);
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.d("Algorithm", "UnsupportedEncodingException");
                }
                readBufferPosition = 0;
            } else {
                readBuffer[readBufferPosition++] = b;
            }
        }
        int numOfPeaks = getNumOfPeaks(sensor1Inputs);
        setFrequency(numOfPeaks * 10000 / sensor1Inputs.size());
        Log.d("Algorithm", "num of peaks: " + numOfPeaks);
        Log.d("Algorithm", "num of inputs: " + sensor1Inputs.size());
        Log.d("Algorithm", "frequency: " + getFrequency());
    }

    private ArrayList<Byte> toOneByteArray(ArrayList<byte[]> packets) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (int j = 0; j < packets.size(); j++) {
            for (int i = 0; i < packets.get(j).length; i++) {
                bytes.add(packets.get(j)[i]);
            }
        }
        return bytes;
    }

    public int getNumOfPeaks(ArrayList<Integer> inputs) {
        if (inputs.size() == 0 || inputs.size() == 1) {
            Log.d("Algorithm", "No inputs received");
            return 0;
        }

        int numOfPeaks = 0;

        ArrayList<Integer> xPeaks = new ArrayList<>();
        int biasPoint = 0;
        ArrayList<Integer> peaks = new ArrayList<>();

        boolean isUpwards = inputs.get(0) < inputs.get(1);
        for (int cur = 1; cur < inputs.size(); cur++) {
            int curInput = inputs.get(cur);
            int prevInput = inputs.get(cur - 1);

            if (isUpwards) {
                if (curInput < prevInput) {
                    numOfPeaks++;
                    peaks.add(prevInput);
                    xPeaks.add(cur-1);
                }
            }
            isUpwards = prevInput <= curInput;
        }
        return numOfPeaks;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


//    private void addInputToArray(int dataNum) {
//        if (currentSensor == 1) {
//            sensor1Inputs.add(dataNum);
//        }
//        else if (currentSensor == 2) {
//            sensor2Inputs.add(dataNum);
//        }
//    }

}
