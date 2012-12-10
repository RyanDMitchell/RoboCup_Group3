/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robocupgui;
import client.RobotClient;
/**
 *
 * @author George
 */
public class Stuck {
    private RobotClient robot;
    private Drive drive;
    private SensorModule sensors;
    
    //times for the stuck manouvres
    final static int manouvre_time_1 = 1000;
    final static int manouvre_time_2 = 1000;
    final static int manouvre_time_3 = 1500;
    final static int manouvre_time = 800;
    
    
    //constructor function
    Stuck(RobotClient ourRobotClient, Drive ourDrive, SensorModule ourSensors){
        robot = ourRobotClient;
        drive = ourDrive;
        sensors = ourSensors;
    }
    
    //360 scan manouvres
    public void stuck_360_manouvre()
    {
        if(sensors.Gright_back == true||sensors.Gleft_back == true){
            drive.turn_straight();
            drive.forward();
            RobotClient.wait(manouvre_time);
            drive.stop();
        }
        else if(sensors.Gright_front == true||sensors.Gleft_front == true||sensors.Gfront < sensors.stuck_distance_IR){
            drive.turn_straight();
            drive.reverse();
            RobotClient.wait(manouvre_time);
            drive.stop();
        }
        
        //unneccessary atm as bumper switches cope
//        else if(sensors.Gright < sensors.stuck_distance_IR){
//            drive.smallBackTurn(1,manouvre_time_1);
//            drive.smallBackTurn(0, manouvre_time_2);
//            drive.turn_straight();
//            drive.forward();
//            RobotClient.wait(manouvre_time_3);
//            drive.stop();
//        }    
//        else if(sensors.Gleft < sensors.stuck_distance_IR){
//            drive.smallBackTurn(0,manouvre_time_1);
//            drive.smallBackTurn(1, manouvre_time_2);
//            drive.turn_straight();
//            drive.forward();
//            RobotClient.wait(manouvre_time_3);
//            drive.stop();
//        }
        
    }
}
