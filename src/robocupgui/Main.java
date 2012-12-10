package robocupgui;

import imaging.Blobs;
import imaging.ImageDrawer;
import gui.Gui;
import client.RobotClient;
import imaging.Threshold;
import java.awt.Image;

import java.awt.image.BufferedImage;


/**
 * @author Dion McLachlan, Daniel Bentall, Simon Richards, Allan McInnes
 */
public class Main{ 
    public static void main(String[] args) throws Exception {
        //voltage checking variables
        int count = 0;
        int voltage = 0;
        final int LOW_BATTERY = 6600;  //need to find this value out.
        
        
        RobotClient robot;        
        try {
            //Initialization of the robot object
            robot = new RobotClient("RoboCupGui","192.168.1.103");
        }
        //In case of not connecting
        catch (Exception e) {
            robot = null;
            e.printStackTrace();
            System.out.println("Failed to connect to Qwerk!");
        }
        
        //If robot working all g!!
        if (robot != null && robot.getQwerkController() != null) {
            // Controller and utility classes instantiated
            CameraController camera = new CameraController(robot);
            Gui gui = new Gui(robot);
            Collector collector = new Collector(robot);
            Drive drive = new Drive(robot);
            FollowingControl followingController = new FollowingControl();
            SensorModule sensors = new SensorModule(robot);

            // Fire up the camera
            camera.start();
            
            StateMachine statemachine = new StateMachine(robot, gui, camera);
            ImageDrawer imager = new ImageDrawer();
            imager.setDisplay(gui);         
            Threshold threshold = new Threshold();

            drive.turn_straight();
            collector.liftArm();
            collector.brushesOff();
            while(true){
                //run the state machine loop
//                System.out.printf("Time: %d\n", System.currentTimeMillis());
                 
                statemachine.run();
//                imager.draw(camera.getImage());
//                collector.liftArm();
//                robot.wait(7000);
//               collector.lowerArm();
//                robot.wait(7000);
                
                
                //check the battery voltage every 20th loop

//                if(count > 20){
//                    voltage = robot.batteryVoltage();
//                    //System.out.println("battery voltage(in mV): " + voltage);
//                    if(voltage < LOW_BATTERY){
//                        System.out.println("CHANGE THE BATTERY!!!!!!!!");
//                    }
//                    count =0;
//                    
//                }
//                count++;
            }
        }
    }
}
