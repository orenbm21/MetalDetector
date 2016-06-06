int val[500];

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(7,OUTPUT);
}

void loop() {
    digitalWrite(7, HIGH);
  
  if (Serial.find("7")) { 
    for (int i = 0; i < 500; i++) {
      val[i] = analogRead(1);
    }
    for (int i = 0; i < 500; i++) {
      Serial.println(val[i]);
    }
  }
}
