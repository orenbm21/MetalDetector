/*
 * This Arduino software is a part of a final project in a Computer and Electric Engineering degree in the Hebrew University of Jerusalem.
 * In our project, the Arudino board gets an input of 4 sensors through its AnalogIn pins, reads 600 samples from each sensor, and then sends the samples to
 * to a BT connected device.
 */

/*--- Declaring Variables ---*/

//Sensor analogIn pin numbers
int sensorsInput[] {0,1,2,3};
int sensorsVCC[] {2,4,6,8};
int numOfSensors = 4;


//Digital pin numbers for led lights
int internalLed = 13;

int val = 0;              // variable to store the analog read value
int numOfInputs = 600;    // Max number of reads that can be saved in the Arduino board flash memory without causing instabilty
int inputs[600];          // int array to store the analog reads from each sensor

//Inner Variables
int i = 0;

/*---End Variable Declaration ---*/

void setup() {
  Serial.begin(9600);  //  setup serial - check if can enhance speed of data transfer
  
  //analogReference(EXTERNAL); //a reference voltage to improve arduino voltage sample resolution

  //set digital pins of led as output to be able to turn them on
   pinMode(internalLed, OUTPUT);

  pinMode(2, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(8, OUTPUT);
}

void loop() {
  allPinsLow();
  i = 0;
  if (Serial.find("7")) {   // 7 is a code for "start sending data" which the BT connected device sends
    //indicator for run start
    digitalWrite(internalLed, HIGH);
    
    for (int j = 0; j < numOfSensors; j++) { //goes over all sensors, gets their data and sends it to the BT connected device
      digitalWrite(sensorsVCC[j], HIGH);
      delay(2000);
      readFromSensorAndSend(sensorsInput[j]);
//      Serial.flush();// - check if possible to use this instead of delay
//      digitalWrite(sensorsVCC[j], LOW);
      delay(2000);
    }
  }
  //indicator for run finish
  digitalWrite(internalLed, LOW);
} 

void allPinsLow() {
  for (int j = 0; j < numOfSensors; j++) {    
    digitalWrite(sensorsVCC[j], LOW);
  }
}
  
void readFromSensorAndSend(int sensor) {
  for (int inputIndex = 0; inputIndex < numOfInputs; inputIndex++) {
    inputs[inputIndex] = analogRead(sensor); // read the input pin
  }
  for (int inputIndex = 0; inputIndex < numOfInputs; inputIndex++) {
    Serial.println(inputs[inputIndex]);
  }
}


