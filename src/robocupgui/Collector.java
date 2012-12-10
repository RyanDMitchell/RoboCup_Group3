package robocupgui;

import client.RobotClient;


public class Collector {
    
    private static RobotClient robot;
    
    private static int leftServoID = 0;
    private static int rightServoID = 1; 
    
    private static int raisedPosL = 190;
    private static int raisedPosR = 64;
    
    private static int loweredPosL =  10;
    private static int loweredPosR =  245;
   
    public Collector(RobotClient inrobot){
        robot = inrobot;
    }
    
    public void liftArm(){
        robot.setServo(leftServoID, raisedPosL);
        robot.setServo(rightServoID, raisedPosR);
    }
    
    public void lowerArm(){
        robot.setServo(leftServoID, loweredPosL);
        robot.setServo(rightServoID, loweredPosR);        
    }
    
    public void brushesOn(){
        robot.setDigitalOn(0);
        robot.setDigitalOn(1);
    }
    
    public void brushesOff(){
        robot.setDigitalOff(0);
        robot.setDigitalOff(1);
    }
}