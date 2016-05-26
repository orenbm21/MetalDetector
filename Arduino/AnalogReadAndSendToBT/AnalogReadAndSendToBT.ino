int sensor1 = 0;
int sensor2 = 1;
int val = 0;           // variable to store the value read
char inSerial[15];
int i = 0;

<<<<<<< HEAD
void setup() {
  Serial.begin(9600);  //  setup serial
=======
void setup()

{

  Serial.begin(9600);          //  setup serial  
  //output = createWriter("metal.txt");
>>>>>>> c31bea051f7e5d648b24f697d03183f11fce5e9d
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

