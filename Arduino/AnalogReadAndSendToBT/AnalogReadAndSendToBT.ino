int sensor1 = 0;
int sensor2 = 1;
int val = 0;           // variable to store the value read
char inSerial[15];
int i = 0;

void setup() {
  Serial.begin(9600);  //  setup serial
}

void loop() {
   if (Serial.available() > 0) { 
    while (Serial.available() > 0) {
      inSerial[i]=Serial.read(); 
      i++;      
    }
    inSerial[i]='\0';
    if(!strcmp(inSerial,"1212")){ 
      startReading();
    }
  }
}

void startReading() {
  readFromSensor(sensor1);
  
  readFromSensor(sensor2);
}

void readFromSensor(int sensorPin) {
  int numOfInputs = 0;
  while(numOfInputs < 10000) {
    val = analogRead(sensorPin); // read the input pin
    Serial.println(val); //pass value to phone  
    delay(50);
  }
  Serial.println("$$$");
  
}

