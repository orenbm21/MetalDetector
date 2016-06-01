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
    public static final int FREQUENCY_THRESHOLD = 30;
    public static final int AMPLITUDE_THRESHOLD = 30;
    private ArrayList<Integer> sensor1Inputs;
    private ArrayList<Integer> sensor2Inputs;
    private double sensor1Frequency;
    private double sensor2Frequency;

    public double getSensor1Amplitude() {
        return sensor1Amplitude;
    }

    public void setSensor1Amplitude(double sensor1Amplitude) {
        this.sensor1Amplitude = sensor1Amplitude;
    }

    public double getSensor2Amplitude() {
        return sensor2Amplitude;
    }

    public void setSensor2Amplitude(double sensor2Amplitude) {
        this.sensor2Amplitude = sensor2Amplitude;
    }

    double sensor1Amplitude;
    double sensor2Amplitude;
    int currentSensor;
    double sensor1CalibratedFrequency;
    double sensor2CalibratedFrequency;
    double sensor1CalibratedAmplitude;
    double sensor2CalibratedAmplitude;

    private ArrayList<Integer> sensor1LowerPeaks;
    private ArrayList<Integer> sensor1UpperPeaks;
    private ArrayList<Integer> sensor2LowerPeaks;
    private ArrayList<Integer> sensor2UpperPeaks;

    public double getSensorFrequency(int sensor) {
        return sensor == 1 ? getSensor1Frequency() : getSensor2Frequency();
    }

    public double getSensorAmplitude(int sensor) {
        return sensor == 1 ? getSensor1Amplitude() : getSensor2Amplitude();
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

    public void setAmplitude(int sensor, double amplitude) {
        if (sensor == 1) {
            if (sensor1CalibratedAmplitude == 0) {
                sensor1CalibratedAmplitude = amplitude;
            }
            setSensor1Amplitude(amplitude);
        }
        else {
            if (sensor2CalibratedAmplitude == 0) {
                sensor2CalibratedAmplitude = amplitude;
            }
            setSensor2Amplitude(amplitude);
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
        sensor1LowerPeaks = new ArrayList<>();
        sensor1UpperPeaks = new ArrayList<>();
        sensor2LowerPeaks = new ArrayList<>();
        sensor2UpperPeaks = new ArrayList<>();
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
        int numOfCycles = getNumOfCycles(currentSensor, currentSensorInputs);
        double freq = calcFrequency(currentSensorInputs, numOfCycles);
        setFrequency(currentSensor, freq);
        double amplitude = calcAmplitude();
        setAmplitude(currentSensor, amplitude);
        String preFix = "Sensor " + currentSensor;
        Log.d("Algorithm", preFix +  " num of peaks: " + numOfCycles);
        Log.d("Algorithm", preFix + " num of inputs: " + currentSensorInputs.size());
        Log.d("Algorithm", preFix + " frequency: " + getSensorFrequency(currentSensor));
        Log.d("Algorithm", preFix + " amplitude: " + getSensorAmplitude(currentSensor));

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

    public int getNumOfCycles(int currentSensor, ArrayList<Integer> inputs) {

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

            if (isUpwards && curInput < prevInput) {
                numOfPeaks++;
                if (currentSensor == 1) {
                    sensor1UpperPeaks.add(prevInput);
                } else {
                    sensor2UpperPeaks.add(prevInput);
                }
            } else if (!isUpwards && curInput > prevInput){
                if (currentSensor == 1) {
                    sensor1LowerPeaks.add(prevInput);
                } else {
                    sensor2LowerPeaks.add(prevInput);
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

    public boolean calcHasMetalByFrequency(int curSensor, double sensorFrequency) {
        double calibratedFreq = curSensor == 1 ? sensor1CalibratedFrequency : sensor2CalibratedFrequency;
        return Math.abs(sensorFrequency - calibratedFreq) > FREQUENCY_THRESHOLD;
    }

    public boolean calcHasMetalByAmplitude(int curSensor, double sensorAmplitude) {
        double calibratedAmplitude = curSensor == 1 ? sensor1CalibratedAmplitude : sensor2CalibratedAmplitude;
        return Math.abs(sensorAmplitude - calibratedAmplitude) > AMPLITUDE_THRESHOLD;
    }

    public double calcAmplitude() {
        return calcAverage(sensor2UpperPeaks) - calcAverage(sensor1LowerPeaks);
    }

    private double calcAverage(ArrayList<Integer> list) {
        int sum = 0;
        int n = list.size();
        for (int i = 0; i < n; i++)
            sum += list.get(i);
        return ((double) sum) / n;
    }

}
