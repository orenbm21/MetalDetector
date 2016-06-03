package com.metaldetector.scanningdevice;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by orenb on 6/1/2016.
 */
public class Sensor {

    public static final int NUM_OF_INPUTS = 600;
    private static final int AMPLITUDE_THRESHOLD = 30;
    private static final int FREQUENCY_THRESHOLD = 10;

    private ArrayList<Integer> inputs;
    private double frequency;
    private double calibratedFrequency;
    private ArrayList<Integer> lowerPeaks;
    private ArrayList<Integer> upperPeaks;
    private double amplitude;
    private double calibratedAmplitude;
    private boolean isCalibrated;
    private int numOfCycles;

    public Sensor() {
        inputs = new ArrayList<>();
        calibratedFrequency = 0;
        calibratedAmplitude = 0;
        upperPeaks = new ArrayList<>();
        lowerPeaks = new ArrayList<>();
        isCalibrated = false;
    }

    private void setAmplitude(double amplitude) {
        if (!isCalibrated) {
            calibratedAmplitude = amplitude;
            return;
        }
        this.amplitude = amplitude;
    }

    private void setFrequency(double frequency) {
        if (!isCalibrated) {
            calibratedFrequency = frequency;
            return;
        }
        this.frequency = frequency;
    }

    public void addInput(int input) {
        inputs.add(input);
    }

    public boolean calcHasMetalByFrequency() {
        return Math.abs(frequency - calibratedFrequency) > FREQUENCY_THRESHOLD;
    }

    public boolean calcHasMetalByAmplitude() {
        return Math.abs(amplitude - calibratedAmplitude) > AMPLITUDE_THRESHOLD;
    }

    public void clearInputs() {
        inputs.clear();
    }

    public void calcNumOfCycles() {
        if (inputs.size() == 0 || inputs.size() == 1) {
            Log.d("Sensor", "No inputs received");
            numOfCycles = 0;
            return;
        }

        int numOfPeaks = 0;

        boolean isUpwards = inputs.get(0) < inputs.get(1);
        boolean startUpwards = isUpwards;
        for (int cur = 1; cur < inputs.size(); cur++) {
            int curInput = inputs.get(cur);
            int prevInput = inputs.get(cur - 1);

            if (isUpwards && curInput < prevInput) {
                numOfPeaks++;
                upperPeaks.add(prevInput);
            } else if (!isUpwards && curInput > prevInput){
                lowerPeaks.add(prevInput);
            }
            isUpwards = prevInput <= curInput;
        }
        numOfCycles = adjustLastCycle(isUpwards, startUpwards, numOfPeaks);
    }

    private int adjustLastCycle(boolean isUpwards, boolean startUpwards, int numOfCycles) {
        int firstInput = inputs.get(0);
        int lastInput = inputs.get(inputs.size()-1);
        if ((startUpwards && (!isUpwards || lastInput < firstInput)) || (!startUpwards && !isUpwards && lastInput > firstInput)) {
            numOfCycles--;
        }
        return numOfCycles;
    }

    public void calcFrequency() {
        if (inputs.size() == 0) {
            setFrequency(0);
        }
        double frequency = (double) (numOfCycles * 10000) / (double) inputs.size();
        setFrequency ((frequency * 100) / (double) 110);
    }

    public void calcAmplitude() {
        if (inputs.size() == 0) {
            setAmplitude(0);
            return;
        }
        double amplitude = calcPeaksAverage(upperPeaks) - calcPeaksAverage(lowerPeaks);
        setAmplitude(amplitude);
    }

    private double calcPeaksAverage(ArrayList<Integer> peaks) {
        int sum = 0;
        int n = peaks.size();
        for (int i = 0; i < n; i++)
            sum += peaks.get(i);
        return ((double) sum) / n;
    }
}
