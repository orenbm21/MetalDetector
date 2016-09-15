/*
 * This Arduino software is a part of a final project in a Computer and Electric Engineering degree in the Hebrew University of Jerusalem.
 * In our project, the Arudino board gets an input of 4 sensors through its AnalogIn pins, reads 600 samples from each sensor, and then sends the samples to
 * to a BT connected device
 */

/*--- Declaring Variables ---*/   

//Sensor analogIn pin numbers
int sensorsInput[] = {0,1,2,3};
int sensorsVCC[] = {4,8};
int numOfSensors = 4;

//Digital pin numbers for led lights
int internalLed = 13;
char inSerial[15];

int val = 0;              // variable to store the analog read value
int numOfInputs = 600;    // Max number of reads that can be saved in the Arduino board flash memory without causing instabilty
int inputs[600];          // int array to store the analog reads from each sensor
int currentSensor;
//Inner Variables
int i = 0;

/*---End Variable Declaration ---*/

void setup() {
  Serial.begin(9600);  //  setup serial - check if can enhance speed of data transfer
  
  //set digital pins of led as output to be able to turn them on
   pinMode(internalLed, OUTPUT);
   pinMode(sensorsVCC[0], OUTPUT);
   pinMode(sensorsVCC[1], OUTPUT);
}

void loop() {
  
  i = 0;
  if (Serial.available() > 0) {
    
    //indicator for run start
    digitalWrite(internalLed, HIGH);
    
    while (Serial.available() > 0) {
      inSerial[i]=Serial.read(); 
      i++;      
    }
    inSerial[i]='\0';
    if(!strcmp(inSerial,"7")){ 
      for (int j = 0; j < numOfSensors; j++) {
        if (j == 0 || j == 2) {
          currentSensor = sensorsVCC[0];
        } else if (j == 1 || j == 3) {
          currentSensor = sensorsVCC[1];
        }
        digitalWrite(currentSensor, HIGH);
        delay(250);
        readFromSensorAndSend(sensorsInput[j]);
        digitalWrite(currentSensor, LOW);
      }
    }

    //indicator for run finish
    digitalWrite(internalLed, LOW);
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


