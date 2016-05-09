int analogPin = 0;     // potentiometer wiper (middle terminal) connected to analog pin 3
int val = 0;           // variable to store the value read
int i = 0;
//PrintWriter output;

void setup()

{

  Serial.begin(9600);          //  setup serial  
  //output = createWriter("metal.txt");
}



void loop()

{
    val = analogRead(analogPin);    // read the input pin
    //val = val*(5/1024);             //conversion to real voltage
    Serial.println(val);             //print in phone
    //output.println(val);
/*
  if (i<100000) {
    
    val = analogRead(analogPin);    // read the input pin
    val = val*(5/1024);             //conversion to real voltage
    Serial.println(val);             //print in phone
    i++;
  }
  */
  
}
