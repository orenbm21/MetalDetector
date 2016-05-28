int sensor1 = 0;
int sensor2 = 1;
int val = 0;           // variable to store the value read
char inSerial[15];
int i = 0;
int inputIndex = 0;
int numOfInputs = 800;
int inputs[800];
int ledRed = 8;
int ledBlue = 9;

void setup() {
  Serial.begin(9600);  //  setup serial
  pinMode(ledRed, OUTPUT);
  pinMode(ledBlue, OUTPUT);
}

void loop() {
  i = 0;
  if (Serial.available() > 0) { 
    while (Serial.available() > 0) {
      inSerial[i]=Serial.read(); 
      i++;      
    }
    inSerial[i]='\0';
    Serial.println(inSerial);
    if(!strcmp(inSerial,"7")){ 
      digitalWrite(ledRed, HIGH);
      digitalWrite(ledBlue, LOW);
      startReading();
    } 
  }
}

void startReading() {
  for (inputIndex = 0; inputIndex < numOfInputs; inputIndex++) {
    inputs[inputIndex] = analogRead(sensor1); // read the input pin
  }
  
//  delay(10000);
  digitalWrite(ledRed, LOW);
  inputIndex = 0;
  for (inputIndex = 0; inputIndex < numOfInputs; inputIndex++) {
    Serial.println(inputs[inputIndex]);
  }
  digitalWrite(ledBlue, HIGH);
}

void sendInput() {  
  Serial.println(inputs[inputIndex]);
  inputIndex++;
  digitalWrite(ledBlue, HIGH);
}

