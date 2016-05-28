int sensor1 = 0;
int sensor2 = 1;
int val = 0;           // variable to store the value read
char inSerial[15];
int i = 0;
int inputs[100];
int flag = 0;

void setup() {
  Serial.begin(9600);  //  setup serial
}

void loop() {
  if (flag == 0) {
    int numOfInputs = 0;
    for (i = 0; i < 100; i++) {
      inputs[i] = analogRead(sensor1); // read the input pin
    }
    for (i = 0; i < 100; i++){
      Serial.println(inputs[i]);
    }
    flag = 1;
  }
}
