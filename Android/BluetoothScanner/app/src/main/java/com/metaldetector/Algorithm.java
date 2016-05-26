package com.metaldetector;

import java.util.ArrayList;

/**
 * Created by orenb on 5/14/2016.
 */
public class Algorithm {

    public static int getNumOfPeeks(ArrayList<Integer> inputs) {
        int numOfPeeks = 0;

        int biasPoint = 0;
        ArrayList<Integer> peeks = new ArrayList<>();

        boolean isUpwards = inputs.get(0) < inputs.get(1);
        for (int cur = 1; cur < inputs.size(); cur++) {
            int curInput = inputs.get(cur);
            int prevInput = inputs.get(cur - 1);

            if (isUpwards) {
                if (curInput < prevInput) {
                    numOfPeeks++;
                    peeks.add(prevInput);
                }
            }
            isUpwards = prevInput <= curInput;
        }

        return numOfPeeks;
    }
}
