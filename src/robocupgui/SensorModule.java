package robocupgui;
import client.RobotClient;

/** SensorModule is the Module Constructed for taking samples of UltraSonic and
 * GP2D10 infrared sensors. Within this module there are functions associated 
 * with the conversion of analogue signals to a physical distance (cm) for the
 * sensors.
 * 
 * 27/07/2012
 * @author Matthew Claridge  mjc317
 */
public class SensorModule {
    private RobotClient robot;
    
    //constants
    public static final int INVALID_MEASUREMENT = 10000;
    public static final int stuck_distance_IR = 25;
    
    
    //sensors variables initialized
    int Gleft = 0;
    int Gright = 0;
    int Gfront = 0;
//    boolean Gback = false;
    boolean Gright_front = false;
    boolean Gleft_front = false;
    boolean Gright_back = false;
    boolean Gleft_back = false;
    boolean stuck_flag = false;
    
    
    
    //Constructor function
    SensorModule(RobotClient ourRobotClient) {
        robot = ourRobotClient;
    }
 
    
    /*TakeSamplesAnalogue takes in the number of samples, and the analogue pin
     *and returns the average value read over the number of samples.
     */
    public int TakeSamplesAnalogue(int numberOfSamples, int portNumber) {            
        int sensorTotal = 0;       
        for (int j=0; j<numberOfSamples; j++)
        {
            sensorTotal += robot.analog(portNumber);
            robot.wait(1); 
        }      
        
     return  sensorTotal/numberOfSamples;
    }
    
      
    /*MetricGP2D10 converts any returned analogue value from a GP2D10 sensor
     *and returns the metric equivalent value in cm.    
     */
    public int MetricGP2D10(int sensorRawValue){
        int metricSensor = INVALID_MEASUREMENT;
        if ((50 < sensorRawValue) && (sensorRawValue < 2500)){
            metricSensor = (int) (112723*Math.pow(sensorRawValue, -1.196));
        }            
        return metricSensor;            
    }
    
    
    /*MetricUltraSonic converts any returned analogue value from a UltraSonic
     *sensor and returns the metric equivalent value in cm.
     */
    public int MetricUltraSonic(int sensorRawValue){
        int metricSensor = INVALID_MEASUREMENT;
        if ((50 < sensorRawValue) && (sensorRawValue < 1600)){
            metricSensor = (int) (0.285*sensorRawValue - 3.396);
        }
        return metricSensor;
    }
    
    
    /*GetLeftMetric returns the Metric value of the Left sensor currently wired
     *to pin 3. The number of samples can be specified.    
     */
    public int GetLeftMetric(int samples){
        int meanValue = TakeSamplesAnalogue((samples) , 3);
        return MetricGP2D10(meanValue);
    }    
    
    
    /*GetRightMetric returns the Metric value of the Right sensor currently wired
     *to pin 2. The number of samples can be specified.    
     */
    public int GetRightMetric(int samples){
        int meanValue = TakeSamplesAnalogue((samples), 2);
        return MetricGP2D10(meanValue);
    }

    /*GetFrontMetric returns the Metric value of the Front sensor currently wired
     *to pin 6. The number of samples can be specified.
     */
    public int GetFrontMetric(int samples){
        int meanValue = TakeSamplesAnalogue((samples), 6);
        return MetricGP2D10(meanValue);
    }

    /*GetDuckSensor polls the sensor on pin 5 mounted on the front panel of the
     *collector to check if a duck is present. Returns true for duck present and
     *false for duck absent.
     */
    public boolean GetDuckSensor(int samples){
        int duckPresentThresh = 2900;
        int sensorRaw = TakeSamplesAnalogue(samples, 5);
//        System.out.printf("duck sensor:%d\n",sensorRaw);
        boolean duckPresent = false;
        if(sensorRaw <= duckPresentThresh){
            duckPresent = true;
        }
        return duckPresent;
    }
    
    /*GetFrontMetric returns the Metric Value of the Front sensor (UltraSonic)
     *currently wired to pin 0. The number of samples can be specified.
     */
    public int GetFrontUltraMetric(int samples){
        int meanValue = TakeSamplesAnalogue((samples), 0);
        return MetricUltraSonic(meanValue);
    }
    
    /*GetBackSensor returns true or false for the proximity sensor located
     * at the back of the robot in digital port I 1
     */
    public boolean GetBackSensor() {
        return robot.digital(1);
    }
    public boolean GetBackLeft() {
        return robot.digital(1);
    }
    public boolean GetBackRight() {
        return robot.digital(3);
    }
    public boolean GetFrontLeft() {
        return robot.digital(0);
    }
    public boolean GetFrontRight() {
        return robot.digital(2);
    }

    //print out the current sensor values
    public void printSensor(int samples){
        
        boolean back = GetBackSensor();
        int left = GetLeftMetric(samples);
        int right = GetRightMetric(samples);
        int front = GetFrontMetric(samples);
        System.out.println("Front d = " + front + " cm");
        System.out.println("Left d = " + left + " cm");
        System.out.println("Right d = " + right + " cm");
        System.out.println("Back d = " + back + " cm");
    }
    
    /*Test Analogue prints out all the Metric Values for the Analogue sensors  
     */
    public void TestAnalogue(int Samples){
        int left = GetLeftMetric(Samples);
        int right = GetRightMetric(Samples);
        int front = GetFrontMetric(Samples);
        System.out.println("Left Distance:" + left + "cm");
        System.out.println("Right Distance:" + right + "cm");
        System.out.println("Front Distance:" + front + "cm");
    }
    
    /*Test Digital prints out all the Digital sensors results 
     */
    public void TestDigital() {
        boolean backSensor = GetBackSensor();
        System.out.println("BackSensor:" + backSensor);
    }
    
    
    /*Check all sensors and set to variables that can be used in logic
     * in other objects
     */
    public void CheckSensors() {
        Gleft = GetLeftMetric(20);
        Gright = GetRightMetric(20);
        Gfront = GetFrontMetric(20);
        Gright_front = GetFrontRight();
        Gleft_front = GetFrontLeft();
        Gright_back = GetBackRight();
        Gleft_back= GetBackLeft();
        
    }
    
    /*Checks if the duck is close, or touching an obstacle
     * returns true if close.
     */
    public boolean isStuck(){
        if((Gleft < stuck_distance_IR)||(Gright < stuck_distance_IR)||
           (Gfront < stuck_distance_IR)||(Gright_front == true)||
           (Gleft_front == true)||(Gright_back == true)||
           (Gleft_back == true))
        {
            stuck_flag = true;
            
        }
        else{
            stuck_flag = false;
        }
        return stuck_flag;
    }
}
