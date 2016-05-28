package com.metaldetector;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by orenb on 5/14/2016.
 */
public class Algorithm {

    private static final int NUM_OF_INPUTS_PER_SENSOR = 600;
    private static final int DELIMITER = 10;
    private ArrayList<Integer> sensor1Inputs;
    private ArrayList<Integer> sensor2Inputs;
    private int sensor1Frequency;
    private int sensor2Frequency;
    int currentSensor;

    public int getSensorFrequency(int sensor) {
        return sensor == 1 ? getSensor1Frequency() : getSensor2Frequency();
    }

    public void setFrequency(int sensor, int frequency) {
        if (sensor == 1) {
            setSensor1Frequency(frequency);
        }
        else {
            setSensor2Frequency(frequency);
        }
    }

    public int getSensor2Frequency() {
        return sensor2Frequency;
    }

    public void setSensor1Frequency(int sensor1Frequency) {
        this.sensor1Frequency = sensor1Frequency;
    }

    public void setSensor2Frequency(int sensor2Frequency) {
        this.sensor2Frequency = sensor2Frequency;
    }



    public int getSensor1Frequency() {
        return sensor1Frequency;
    }

    public Algorithm() {
        currentSensor = 1;
        sensor1Inputs = new ArrayList<>();
        sensor2Inputs = new ArrayList<>();
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
                        if (sensor1Inputs.size() == NUM_OF_INPUTS_PER_SENSOR) {
                            currentSensor = 2;
                        }
                        addInputToArray(dataNum);
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
        calculateSensorFrequency(1);
        calculateSensorFrequency(2);
    }

    private void calculateSensorFrequency(int currentSensor) {
        ArrayList<Integer> currentSensorInputs = currentSensor == 1 ? sensor1Inputs : sensor2Inputs;
        int numOfPeaks = getNumOfPeaks(currentSensorInputs);
        int freq = currentSensorInputs.size() == 0 ? 0 : numOfPeaks * 10000 / currentSensorInputs.size();
        setFrequency(currentSensor, freq);
        String preFix = "Sensor " + currentSensor;
        Log.d("Algorithm", preFix +  " num of peaks: " + numOfPeaks);
        Log.d("Algorithm", preFix + " num of inputs: " + currentSensorInputs.size());
        Log.d("Algorithm", preFix + " frequency: " + getSensorFrequency(currentSensor));
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

    private void addInputToArray(int dataNum) {
        if (currentSensor == 1) {
            sensor1Inputs.add(dataNum);
        }
        else if (currentSensor == 2) {
            sensor2Inputs.add(dataNum);
        }
    }

}
