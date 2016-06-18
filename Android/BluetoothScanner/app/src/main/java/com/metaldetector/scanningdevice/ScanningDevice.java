package com.metaldetector.scanningdevice;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by orenb on 5/14/2016.
 */
public class ScanningDevice {

    private static final int DELIMITER = 10;
    public static final double REF_VOLTAGE = 5;
    public static final int NUM_OF_BINS = 1024;
    private ArrayList<Sensor> sensors;

    public ScanningDevice(int numOfSensors) {
        sensors = new ArrayList<>();
        for (int i = 0; i < numOfSensors; i++) {
            sensors.add(new Sensor());
        }
    }

    public int getNumOfSensors() {
        return sensors.size();
    }

    public void analyzePackets(ArrayList<byte[]> packets) {

        try {
            int currentSensor = 0;
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
                            if (numOfReadInputs == Sensor.NUM_OF_INPUTS) {
                                currentSensor++;
                                numOfReadInputs = 0;
                            }
                            addInputToArray(currentSensor, dataNum);
                            numOfReadInputs++;
                        } catch (NumberFormatException e) {
                            Log.d("ScanningDevice", "Tried to parse a string: " + data);
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.d("ScanningDevice", "UnsupportedEncodingException");
                    }
                    readBufferPosition = 0;
                } else {
                    readBuffer[readBufferPosition++] = b;
                }
            }
        } catch (Exception e) {
            throw e;
        }
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

    private void addInputToArray(int sensorIndex, int input) {
        double voltage = convertToVoltage(input);
        sensors.get(sensorIndex).addInput(voltage);
    }

    private double convertToVoltage(double input) {
        return (input * REF_VOLTAGE ) / (double) NUM_OF_BINS;
    }

    private void calcSensorsParams() {
        for (Sensor sensor : sensors) {
            sensor.calcNumOfCycles();
            sensor.calcFrequency();
            sensor.calcAmplitude();
            sensor.clearInputs();
            sensor.setCalibrated(true);
        }
    }

    public ArrayList<Boolean> calcHasMetal() {
        calcSensorsParams();

        ArrayList<Boolean> hasMetal = new ArrayList<>();
        for (Sensor sensor: sensors) {
            boolean sensorHasMetal = sensor.calcHasMetalByAmplitude() || sensor.calcHasMetalByFrequency();
            hasMetal.add(sensorHasMetal);
        }
        return hasMetal;
    }

    public void printSensorsParams() {
        for (int i = 0; i < sensors.size(); i++) {
            Sensor curSensor = sensors.get(i);
            Log.d("Sensor " + i, " Calibrated Amplitude: " + curSensor.getCalibratedAmplitude());
            Log.d("Sensor " + i, " Amplitude: " + curSensor.getAmplitude());
            Log.d("Sensor " + i, " Amplitude Difference: " + Math.abs(curSensor.getAmplitude() - curSensor.getCalibratedAmplitude()));
            Log.d("Sensor " + i, " Calibrated Frequency: " + curSensor.getCalibratedFrequency());
            Log.d("Sensor " + i, " Frequency: " + curSensor.getFrequency());
            Log.d("Sensor " + i, " Frequency Difference: " + Math.abs(curSensor.getFrequency() - curSensor.getCalibratedFrequency()));
            Log.d("Sensor " + i, " V Max: " + curSensor.getvMax());
        }
    }

    public void prepareCalibration() {
        for (Sensor sensor: sensors) {
            sensor.setCalibrated(false);
        }
    }
}