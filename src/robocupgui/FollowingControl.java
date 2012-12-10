package robocupgui;

public class FollowingControl {

    //Globals for controller
    static int prev_error_wall = 0;
    static int prev_error_duck = 0;
    static int prev_error_dist = 0;
    static long prev_time_wall = 0;
    static long prev_time_duck = 0;
    static long prev_time_dist = 0;
    static long total_error_dist = 0;
    static long current_time = 0;



    
    /* CONTROL FUNCTION FOR FOLLOWING WALLS
     * DESCRIPTION: follows walls at a given distance on the right or left side.
     * ARGUMENTS: wallToFollow = left or right wall.
     *            sensorMeasurement = left or right sensor.
     *            desiredDisplacement = distance at which robot should follow walls.
     * OUTPUT: Wheel angle of robot(0 = left lock angle, 255 = right lock angle).
     */
    public int followWallControl(int wallToFollow, int sensorMeasurement, int desiredDisplacement){
        //control constants
        double kd = 0;    //last functionality assement values 10,1,100
        double kp = 2.4;
        int SCALING_VALUE = 100;
        
        
        //Error calculation:
        //Positive = if too close.
        //Negative = if too far.
        int error = desiredDisplacement - sensorMeasurement;

        
        //if following the right wall, negate the error.
        if(wallToFollow == StateMachine.RIGHT_WALL){
            error = (-1)*error;
        }
        
        //record time and calculate change in time for the derivative control. 
        current_time = System.currentTimeMillis();
        long time_difference =  current_time - prev_time_wall;
        
 
        /* Calculate derivative of the errors.
         * RIGHT WALL:
         * Positive = moving towards wall.
         * Negative = moving away from wall.
         * 
         * LEFT WALL:
         * Positive = moving away from wall.
         * Negative = moving towards wall.
         */
        double error_derivative = (((error - prev_error_wall)*SCALING_VALUE)/time_difference);
        
        //determine controlled output steering angle
        int wheelAngle = (int)Math.round(Drive.NEUTRAL_STEERING_ANGLE + error*kp + error_derivative*kd);
        
        
        //set variables used for next loop
        prev_error_wall = error;
        prev_time_wall = current_time;
        
        //cut off the wheel angles outputted
        if(wheelAngle > 180){
            wheelAngle = 180;
        }
        else if(wheelAngle <70){
            wheelAngle = 70;
        }
        
        return wheelAngle;
    }


    public int followDuckControl(int duck_x){

//        double kd = 0.002;
//        double kp = 0.4;
        double kd = 0.002;
        double kp = 0.35;

        int pixel_width = 320;
        int error = ((pixel_width/2) - duck_x);

        current_time = System.currentTimeMillis();
        long time_difference = current_time - prev_time_duck;
        double error_derivative = ((error - prev_error_duck))/time_difference;
        int wheelAngle = (int)Math.round(127 -(error*kp + error_derivative*kd));
        if( wheelAngle > 200){
            wheelAngle = 200;
        }
        if (wheelAngle < 55){
            wheelAngle = 55;
        }
        //System.out.printf("Duck error: %d  Wheelangle:%d\n",error, wheelAngle);

        //set variables used for next loop
        prev_error_duck = error;
        prev_time_duck = current_time;

        return wheelAngle;
    }


    public int getIntoArmPosControl(int frontDist, int desiredDist){
        double kd = 0.1;
        double kp = 0.5;
        double ki = 0.03;
        int error;
        int drive_servo = 127;

        current_time = System.currentTimeMillis();
        long time_step = current_time - prev_time_dist;
        if(prev_time_dist != 0 && time_step < 5000){

            if(frontDist != SensorModule.INVALID_MEASUREMENT){
                error = frontDist - desiredDist;
            }else{
                error = 0;
                System.out.printf("Front sensor is reading invalid while trying to use PID control");
            }

            //acculmulate error

            total_error_dist = total_error_dist + (error*time_step)/100;
            //System.out.printf("Integral sum: %d\n",(error*time_step)/100);
            //Limit integral term to prevent windup
            if(total_error_dist*ki>= 30){
                total_error_dist = Math.round(30.f/ki);
            }
            if(total_error_dist*ki<= -30){
                total_error_dist = Math.round(-30.f/ki);
            }
            double error_derivative = ((error - prev_error_duck))/time_step;

            drive_servo = (int)Math.round(127 + error*kp + error_derivative*kd + total_error_dist*ki);
            if( drive_servo > 150){
                drive_servo = 150;
            }
            if (drive_servo < 101){
                drive_servo = 101;
            }
            System.out.printf("Error:%d P:%f D:%f I:%f Drive Servo: %d\n",error,error*kp,error_derivative*kd,total_error_dist*ki,drive_servo);

            //set variables used for next loop
            prev_error_dist = error;
        }
        prev_time_dist = current_time;
        return drive_servo;
    }
}
