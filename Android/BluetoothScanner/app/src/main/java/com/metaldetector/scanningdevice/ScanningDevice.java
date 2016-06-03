package com.metaldetector.scanningdevice;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by orenb on 5/14/2016.
 */
public class ScanningDevice {

    private static final int DELIMITER = 10;
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

        int currentSensor = 1;
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
                            currentSensor = 2;
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
        sensors.get(sensorIndex).addInput(input);
    }

    private void calcSensorsParams() {
        for (Sensor sensor : sensors) {
            sensor.calcNumOfCycles();
            sensor.calcFrequency();
            sensor.calcAmplitude();
            sensor.clearInputs();
        }
    }

    public ArrayList<Boolean> calcHasMetal() {
        calcSensorsParams();

        ArrayList<Boolean> hasMetal = new ArrayList<>();
        for (Sensor sensor: sensors) {
            boolean sensorHasMetal = sensor.calcHasMetalByAmplitude() && sensor.calcHasMetalByFrequency();
            hasMetal.add(sensorHasMetal);
        }
        return hasMetal;
    }
}