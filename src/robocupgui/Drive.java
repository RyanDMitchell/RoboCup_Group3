package robocupgui;
import client.RobotClient;

public class Drive {

    //Robot
    private RobotClient robot;

    //Constructor function
    Drive(RobotClient ourRobotClient) {
        robot = ourRobotClient;
    }

    //states of motors and servos
    public static int motor_state = 500;
    public static int steering_state = 127;
    private static int driving_state;

    //Robot characteristics
    static final int MOTOR_SERVO = 14;
    static final int STEERING_SERVO = 15;
    static final int NEUTRAL_SPEED = 127;
    static final int LEFT_LOCK_STEERING_ANGLE = 0;
    static final int RIGHT_LOCK_STEERING_ANGLE = 255;
    static final int NEUTRAL_STEERING_ANGLE = 127;

    
    //set speeds
    final static int REVERSE_SPEED = 104;
    final static int DUCK_SPEED = 142;
    final static int FORWARD_SPEED = 148;
        
    //constants for functions
    static final int LEFT = 1;
    static final int RIGHT = 0;
    
    //variables for functions
    final static int time_360_turn = 1000;
    final static int wait_time_for_steering_to_set = 400;
    final static int BACKWARD_TURN = 0;
    final static int FORWARD_TURN = 1;
    final static int camera_looking_time = 500;
    
    
    /* MOTOR SPEED
     * DESCRIPTION: Sets the forward or revers speed of motor,
     *              reversing requires complex servo setting
     * ARGUMENTS: motor_speed = 0(full reverse) - 255(full forward).
     */
    public void setSpeed(int motor_speed)
    {
        //forward or neutral input
        if(motor_speed >= NEUTRAL_SPEED)
        {
            //set speed of motors
            robot.setServo(MOTOR_SERVO, motor_speed);
            //set state of motor
            motor_state = motor_speed;
        }
        
        //Reversing
        else{
            //Case when the robot goes from forward into a reverse value.
            if(motor_state >= NEUTRAL_SPEED)
            {
                //stop motors
                robot.setServo(MOTOR_SERVO, NEUTRAL_SPEED);
                robot.sleepUnlessStop(200);

                //neccessary for the reverse setting
                robot.setServo(MOTOR_SERVO, 100);
                robot.sleepUnlessStop(200);
                robot.setServo(MOTOR_SERVO, NEUTRAL_SPEED);
                robot.sleepUnlessStop(200);
                robot.setServo(MOTOR_SERVO, motor_speed);
                
                //set motor state to current speed
                motor_state = motor_speed;
            }
            //Already been in reverse, just changing the speed.
            else
            {
                robot.setServo(MOTOR_SERVO, motor_speed);
                motor_state = motor_speed;
            }
        }
    }

    /* STEERING ANGLE
     * DESCRIPTION: Sets steering angle of tyre servos
     * ARGUMENTS: tyre_angle = 0(left lock) - 255(right lock).
     */
    public void Steering(int tyre_angle){
        //Does not repeat steering servo setting
        if(tyre_angle != steering_state){
            //set steering to angle
            //takes into account the incorrect inputs
            if(tyre_angle > RIGHT_LOCK_STEERING_ANGLE){
                tyre_angle = RIGHT_LOCK_STEERING_ANGLE;
            }
            else if(tyre_angle < LEFT_LOCK_STEERING_ANGLE){
                tyre_angle = LEFT_LOCK_STEERING_ANGLE;
            }
            robot.setServo(STEERING_SERVO, tyre_angle);
            steering_state = tyre_angle;
        }
    }

    
    
    /* SMALL FORWARD TURN
     * DESCRIPTION: drives forward with steering lock in left or right direction.
     * ARGUMENTS: turn_direction = 0(RIGHT) - 1(LEFT),
     *            turn_time = time to complete turn.
     */
    public void smallForwardTurn(int turn_direction,int turn_time){

        //determines steering angle
        if (turn_direction == RIGHT){
            Steering(RIGHT_LOCK_STEERING_ANGLE);
        }
        else if(turn_direction == LEFT){
            Steering(LEFT_LOCK_STEERING_ANGLE);
        }
        
        //allow time to turn wheels
        RobotClient.wait(wait_time_for_steering_to_set);
        
        //drive for set time
        setSpeed(FORWARD_SPEED);
        RobotClient.wait(turn_time);
        
        //stop truck
        setSpeed(NEUTRAL_SPEED);
    }

    /* SMALL BACKWARD TURN
     * DESCRIPTION: drives backward with steering lock in left or right direction.
     * ARGUMENTS: turn_direction = 0(RIGHT) - 1(LEFT),
     *            turn_time = time to complete turn.
     */
    public void smallBackTurn(int turn_direction, int turn_time){
        
        //determines steering angle
        if (turn_direction == LEFT){
            Steering(LEFT_LOCK_STEERING_ANGLE);
        }
        else if (turn_direction == RIGHT){
            Steering(RIGHT_LOCK_STEERING_ANGLE);
        }
        
        //allow time to turn wheels
        RobotClient.wait(wait_time_for_steering_to_set);
        
        //drive for set time
        setSpeed(REVERSE_SPEED);
        RobotClient.wait(turn_time);
        
        //stop truck
        setSpeed(NEUTRAL_SPEED);
        
    }
    
    
    /* PEFORMS A THREE POINT TURN
     * DESCRIPTION: perfoms a small back turn or small forward turn, 
     *              used in the SCAN360 state to move orientation of robot in
     *              one direction.
     * ARGUMENTS: turn_direction = the direction that the robot will turn 
     *                             oreintation (0 = RIGHT(clockwise),
     *                             1 = LEFT(anticlockwise)).
     * IMPORTANT constants: time_360_turn = time for each turn.
     */
    public void threePointTurn(int turn_direction){
        
        if(driving_state == FORWARD_TURN){
            if(turn_direction == LEFT){
                smallBackTurn(RIGHT,time_360_turn);
            }
            else{
                smallBackTurn(LEFT,time_360_turn);
            }
            driving_state = BACKWARD_TURN;
        }
        else if(driving_state == BACKWARD_TURN){
            if(turn_direction == LEFT){
                smallForwardTurn(LEFT,time_360_turn);
            }
            else{
                smallForwardTurn(RIGHT,time_360_turn);
            }
            driving_state = FORWARD_TURN;
        }
        //allow time for camera to look at the image
        RobotClient.wait(camera_looking_time);
    }
    
    
    //REVERSES AT THE DEFINED SPEED IN GLOBALS
    public void reverse(){
        setSpeed(REVERSE_SPEED);
    }
    
    //DRIVES FORWARD AT THE DEFINED SPEED IN GLOBALS
    public void forward(){
        setSpeed(FORWARD_SPEED);
    }
    
    //STOPS ROBOT MOTORS
    public void stop(){
        setSpeed(NEUTRAL_SPEED);
        
        
    }
    
    //LOCK STEERING TO THE RIGHT
    public void turn_right(){
        Steering(RIGHT_LOCK_STEERING_ANGLE);
    }
    
    
    //LOCK STEERING TO THE LEFT
    public void turn_left(){
        Steering(LEFT_LOCK_STEERING_ANGLE);
    }
    
    //LOCK STEERING TO THE NEUTRAL
    public void turn_straight(){
        Steering(NEUTRAL_STEERING_ANGLE);
    }
    
}
