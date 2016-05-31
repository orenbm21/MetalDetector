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
    private double sensor1Frequency;
    private double sensor2Frequency;
    int currentSensor;
    double sensor1CalibratedFrequency;
    double sensor2CalibratedFrequency;

    public double getSensorFrequency(int sensor) {
        return sensor == 1 ? getSensor1Frequency() : getSensor2Frequency();
    }

    public void setFrequency(int sensor, double frequency) {
        if (sensor == 1) {
            if (sensor1CalibratedFrequency == 0) {
                sensor1CalibratedFrequency = frequency;
            }
            setSensor1Frequency(frequency);
        }
        else {
            if (sensor2CalibratedFrequency == 0) {
                sensor2CalibratedFrequency = frequency;
            }
            setSensor2Frequency(frequency);
        }
    }

    public double getSensor2Frequency() {
        return sensor2Frequency;
    }

    public void setSensor1Frequency(double sensor1Frequency) {
        this.sensor1Frequency = sensor1Frequency;
    }

    public void setSensor2Frequency(double sensor2Frequency) {
        this.sensor2Frequency = sensor2Frequency;
    }

    public double getSensor1Frequency() {
        return sensor1Frequency;
    }

    public Algorithm() {
        sensor1Inputs = new ArrayList<>();
        sensor2Inputs = new ArrayList<>();
        sensor1CalibratedFrequency = 0;
        sensor2CalibratedFrequency = 0;
    }

    public void analyzePackets(ArrayList<byte[]> packets) {

        currentSensor = 1;
        int numOfReadInputs = 0;
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
                String data;
                try {
                    data = new String(encodedBytes, "US-ASCII");
                    data = data.replaceAll("(\\r|\\n)", "");
                    try {
                        int dataNum = Integer.parseInt(data);
                        if (numOfReadInputs == NUM_OF_INPUTS_PER_SENSOR) {
                            currentSensor = 2;
                        }
                        addInputToArray(dataNum);
                        numOfReadInputs++;
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
        int numOfCycles = getNumOfCycles(currentSensorInputs);
        double freq = calcFrequency(currentSensorInputs, numOfCycles);
        setFrequency(currentSensor, freq);
        String preFix = "Sensor " + currentSensor;
        Log.d("Algorithm", preFix +  " num of peaks: " + numOfCycles);
        Log.d("Algorithm", preFix + " num of inputs: " + currentSensorInputs.size());
        Log.d("Algorithm", preFix + " frequency: " + getSensorFrequency(currentSensor));

        currentSensorInputs.clear();
    }

    private double calcFrequency(ArrayList<Integer> currentSensorInputs, int numOfPeaks) {
        if (currentSensorInputs.size() == 0) {
            return 0;
        }
        double freq = (double) (numOfPeaks * 10000) / (double) currentSensorInputs.size();
        return (freq * 100) / (double) 110;
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

    public int getNumOfCycles(ArrayList<Integer> inputs) {
        if (inputs.size() == 0 || inputs.size() == 1) {
            Log.d("Algorithm", "No inputs received");
            return 0;
        }

        int numOfPeaks = 0;

        boolean isUpwards = inputs.get(0) < inputs.get(1);
        boolean startUpwards = isUpwards;
        for (int cur = 1; cur < inputs.size(); cur++) {
            int curInput = inputs.get(cur);
            int prevInput = inputs.get(cur - 1);

            if (isUpwards) {
                if (curInput < prevInput) {
                    numOfPeaks++;
                }
            }
            isUpwards = prevInput <= curInput;
        }

        int numOfCycles = numOfPeaks;
        numOfCycles = adjustLastCycle(inputs, isUpwards, startUpwards, numOfCycles);

        return numOfCycles;
    }

    private int adjustLastCycle(ArrayList<Integer> inputs, boolean isUpwards, boolean startUpwards, int numOfCycles) {
        int firstInput = inputs.get(0);
        int lastInput = inputs.get(inputs.size()-1);
        if ((startUpwards && (!isUpwards || lastInput < firstInput)) || (!startUpwards && !isUpwards && lastInput > firstInput)) {
            numOfCycles--;
        }
        return numOfCycles;
    }

    private void addInputToArray(int dataNum) {
        if (currentSensor == 1) {
            sensor1Inputs.add(dataNum);
        }
        else if (currentSensor == 2) {
            sensor2Inputs.add(dataNum);
        }
    }

    public boolean calcHasMetal(int curSensor, double sensorFrequency) {
        double calibratedFreq = curSensor == 1 ? sensor1CalibratedFrequency : sensor2CalibratedFrequency;
        return Math.abs(sensorFrequency - calibratedFreq) >  30;
    }

}
