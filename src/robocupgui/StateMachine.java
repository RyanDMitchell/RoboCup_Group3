package robocupgui;

import client.RobotClient;
import gui.Gui;
import imaging.Blobs;
import imaging.ImageDrawer;
import java.io.IOException;

public class StateMachine {

        //Calibration constants
        //debug
        public boolean talk = true;
        
        //Image processing values
        public int blobMinMass = 40;
        public int blobMaxMass = 1000000;
        public int upperY = 180;
        public int lowerY = 50;
        public int upperI = 100;
        public int lowerI = 80;
 
        //sensors variables initialized
        int left_sensor = 0;
        int right_sensor = 0;
        int front_sensor = 0;
        boolean back_sensor = true;
        boolean right_front_sensor = true;
        boolean left_front_sensor = true;
        boolean right_back_sensor = true;
        boolean left_back_sensor = true;
        
        
        //Robot global state IDs
        public static final int IDLE = 0;
        public static final int FOLLOW_WALLS = 1;
        public static final int COLLECT_DUCK = 2;
        public static final int SCAN360 = 3;
        public static final int DRIVE_TO_DUCK = 4;
        public static final int COMPLETED = 5;
        public static final int TEST = 6;
        
        //Robot state variables
        public static int globalState = FOLLOW_WALLS;//initialize the first state
        public static int prevGlobalState = FOLLOW_WALLS;
        public boolean sameGlobalState = false;
            
        
        //FOLLOW WALLS
            //constants

            final static int desired_follow_distance = 25; //in cm to the wall alongside robot
            final static int turning_distance = 80;//in cm to the wall in front of robot required to begin corner turning
            final static int numWallsTillScan = 1; //Number of walls to pass before changing to scan360 state
            final static int turningTime = 2000; //in milliseconds maximum time to turn a corner
            final int WALL_TIME = 4000;
            final static int min_side_reverse = 20;//distance to wall on one side that stops robot from performing reverse manouvre
            final int follow_wall_speed = 143;
            final static int minimum_turn = 40; //in cm to the wall in front of robot required to go into reverse state
            final int turned_corner_distance = 100;//distance required to exit the corner turning state
            final int reversed_distance = 60;//distance required to reverse to before turning corner again
            final int reverse_speed = 104;
            
            
            //variables
            long startTime, currTime;
            int number_of_walls_passed = 0;
            long intial_wall_time = 0;
            long current_wall_time = 0;
            int wall_control = 127;//controller output to follow wall
            long wall_tim =0;
            
            //state IDs
            final static int FIND_WALL = 0;
            final static int LEFT_WALL = 1;
            final static int RIGHT_WALL = 2;
            final static int TURNING_CORNER = 3;
            final static int REVERSING = 4;
            
            //state variables
            public static int followWallState = FIND_WALL;//initialize the folow wall state
            public static int prevFollowWallState;
            public boolean sameFollowWallState = false;            
            
        //DRIVE TO DUCK
            //constants
            final static int MAX_REACQUIRE_ATTEMPTS =25;
            
            //variables
            long duckStateTime = 0;
            int numReaccuireAttempts = 0;
            int MAX_REACCUIRE_ATTEMPTS = 3;
            int MAX_DRIVE_TIME = 30000; //in ms
            int numOfDucks = 0;
            int prevDuckx = 0;
            int numReacquireAttempts = 0;
            

        //SCAN 360
            //constants
            final static int RIGHT_SCAN_DIRECTION = 0;
            final static int LEFT_SCAN_DIRECTION = 1;
            final static int complete_scan = 8;//number of turns to complete 360
            //variables
            int scan_direction = RIGHT_SCAN_DIRECTION;
            static int number_of_scans_completed = 0;
                        
            
        //COLLECT DUCK
            //constants
            final static int BRUSHES_RUN_TIME = 5000;    //in ms
            final static int MIN_COLLECT_DIST = 35;    //in cm
            final static int LOWER_ARM_DIST = 30;    //in cm
            final static int REVERSE_TIME = 300;    //in ms
            final static int MAX_ALLOWED_ATTEMPTS = 15;
            final static int LOWER_ARM_TOLERANCE = 6; // in cm
            final static int REVERSE_ARM_LOWERED_DIST = 60; // in cm
            final static int REVERSE_ARM_RAISED_DIST = 120; // in cm
            final static int TIME_AT_GOAL = 600; // in ms

            //variables
            boolean rollersRun = false;
            int numCollectedDucks = 0;
            int numOfCollectAttempts = 0;
            boolean needToLowerArm = false;
            boolean isArmDown = false;
            boolean atGoal = false;
            long initGoalTime = 0;

            //STATE variables
            final static int USE_ARM = 1;    //in ms
            final static int USE_ROLLERS = 2;    //in ms

            boolean sameCollectDuckState = false;
            int collectDuckState = USE_ROLLERS;
            int prevCollectDuckState = USE_ROLLERS;
            
        //COMPLETED 
            //constants
            
            
            //variables
       
        //Global objects
        public RobotClient robot;
        public SensorModule sensors;
        public Gui gui;
        public Blobs blobber;
        public CameraController camera;
        public Collector collect;
        public static Drive drive;
        public static FollowingControl followingController;
        public static Stuck stuck;
    
        
    //Constructor function
    public StateMachine(RobotClient frobot, Gui fgui, CameraController fcamera){
        robot = frobot;
        sensors = new SensorModule(robot);
        gui = fgui;        
        camera = fcamera;
        blobber = new Blobs(blobMinMass,blobMaxMass,upperY,lowerY,upperI,lowerI);
        collect = new Collector(robot);
        drive = new Drive(robot);
        followingController = new FollowingControl();
        stuck = new Stuck(robot,drive,sensors);

    }
    
    //Sets respective global state string to a variable and returns it
    String GetGlobalStateString(){
        String line = "UNKNOWN G STATE";
        switch(globalState){
            case(IDLE):             line = "IDLE";break;
            case(FOLLOW_WALLS):     line = "FOLLOW_WALLS";break;
            case(COLLECT_DUCK):     line = "COLLECT_DUCK";break;
            case(SCAN360):          line = "SCAN360";break;
            case(DRIVE_TO_DUCK):    line = "DRIVE_TO_DUCK";break;
            case(COMPLETED):        line = "COMPLETED";break;
        }
        return line;
    }

    //Sets respective follow wall state string to a variable and returns it
    String GetFollowWallStateString(){
        String line = "UNKNOWN F STATE";
        switch(followWallState){
            case(FIND_WALL):        line = "FIND_WALL";break;
            case(LEFT_WALL):        line = "LEFT_WALL";break;
            case(RIGHT_WALL):       line = "RIGHT_WALL";break;
            case(TURNING_CORNER):   line = "TURNING_CORNER";break;
            case(REVERSING):        line = "REVERSING";break;
        }
        return line;
    }
    
    //Sets respective collect duck state string to a variable and returns it
    String GetCollectDuckStateString(){
        String line = "UNKNOWN C STATE";
        switch(collectDuckState){
            case(USE_ROLLERS):    line = "USE_ROLLERS";break;
            case(USE_ARM):        line = "USE_ARM";break;

        }
        return line;
    }

    
   /*Displays the global state and reverts that the state has entered once.
    * Can be in state to do first entering state operations, 
    * do the operations first and then use this function.
    */
   public void DisplayGlobalState(){
        if ((!sameGlobalState)&&(talk)){
            System.out.println("Global STATE: " + GetGlobalStateString());
        }
        //reverts the first entry of state variable
        sameGlobalState = true;
    } 
    
   
   /*Displays the follow wall state and reverts that the state has entered once.
    * Can be in state to do first entering state operations, 
    * do the operations first and then use this function.
    */
    public void DisplayFollowWallState(){
        if ((!sameFollowWallState)&&(talk)){
            System.out.println("Follow Walls STATE: " + GetFollowWallStateString());
        }
        //reverts the first entry of state variable
        sameFollowWallState = true;
    }

       /*Displays the collect duck state and reverts that the state has entered once.
    * Can be in state to do first entering state operations,
    * do the operations first and then use this function.
    */
    public void DisplayCollectDuckState(){
        if ((!sameCollectDuckState)&&(talk)){
            System.out.println("Collect duck STATE: " + GetCollectDuckStateString());
        }
        //reverts the first entry of state variable
        sameCollectDuckState = true;
    }
    
   /* Sets the global state and sets that the state has entered once.
    * sets the previuos state aswell
    */
    public void SetGlobalState(int newState){
        prevGlobalState = globalState;
        globalState = newState;
        sameGlobalState = false;    
    }
    
   /* Sets the follow wall state and sets that the state has entered once.
    * sets the previuos state aswell
    */
    public void SetFollowWallState(int newState){
        prevFollowWallState = followWallState;
        followWallState = newState;
        sameFollowWallState = false;         
    }

       /* Sets the follow wall state and sets that the state has entered once.
    * sets the previuos state aswell
    */
    public void SetCollectDuckState(int newState){
        prevCollectDuckState = collectDuckState;
        collectDuckState = newState;
        sameCollectDuckState = false;
    }
  
    public void run() throws IOException{

        //if statement used for stopping the trucks actions using slider on gui
//                if(gui.getProperty1() < 127){
//                    globalState = RobotClient.IDLE;
//                    gui.setProperty1(127);
//                }
              
                //The state machine of the robot
                switch(globalState){
                    
                     /*Global state machine: State "DRIVE_TO_DUCK"
                     * Performs a PID control loop on the duck position in the
                     * camera image to drive towards the duck.
                     * Entry from states: SCAN_360 and FOLLOW_WALLS
                     * Entry conditions: Find duck in webcam image.
                     * Exit to states: COLLECT_DUCK.
                     * Exit conditions: Duck lost from webcam image.
                     */
                    case (DRIVE_TO_DUCK):
                        if (!sameGlobalState){
                            drive.setSpeed(Drive.DUCK_SPEED);
                            numReacquireAttempts = 0;
                            duckStateTime = System.currentTimeMillis();
                        }

                        DisplayGlobalState();
                        //Compute the blob information of the image
                        blobber.findBlobs(camera.getImage(), blobber.DUCK);
                        blobber.drawBlobs(gui);
                        
                        
                        //check the direction that the duck exited the image.
                        if(blobber.isObjLostDown()){
                            System.out.printf("\nDuck Lost down\n");
                            SetGlobalState(COLLECT_DUCK);
                            drive.stop();
                        }
                        // If a duck is seen in the webcam image
                        if(blobber.numObjFound() > 0){
                            drive.setSpeed(Drive.DUCK_SPEED);
                            numReacquireAttempts = 0;
                            //if close to any wall
                            if((sensors.GetFrontMetric(5) <= 40)){
                                SetGlobalState(COLLECT_DUCK);
                                System.out.printf("\nTo close to a wall\n");
                                drive.stop();
                            }else{
                                System.out.printf("\ndriving to duck");
                                
                                drive.Steering(followingController.followDuckControl(blobber.getBiggestObjX()));
                                
                                if (sensors.GetFrontLeft()){
                                    drive.smallBackTurn(Drive.RIGHT, 800);
                                    drive.smallBackTurn(Drive.LEFT, 900);
                                    System.out.printf("FL bumper\n");
                                }
                                else if (sensors.GetFrontRight()){
                                    drive.smallBackTurn(Drive.LEFT, 800);
                                    drive.smallBackTurn(Drive.RIGHT, 900);
                                    System.out.printf("FR bumper\n");
                                }
                            }
                        }else{ //If no ducks are seen
                            if(sensors.GetDuckSensor(10) == true){
                                SetGlobalState(COLLECT_DUCK);
                                SetCollectDuckState(USE_ROLLERS);
                            }
                            System.out.printf("Duck lost, Reaccuiring. ");
                            drive.stop();
                            if(numReacquireAttempts >= MAX_REACQUIRE_ATTEMPTS){
                                System.out.printf("\nCould not reaccuire. ");
                                drive.stop();
                                SetGlobalState(FOLLOW_WALLS);
                                SetFollowWallState(FIND_WALL);
                            }
                            numReacquireAttempts++;

                        }
                        //Check to see if been driving to duck for too long
 //                       if((System.currentTimeMillis() - duckStateTime) >= MAX_DRIVE_TIME){
//                            System.out.printf("\nIn drive to duck for %dms, might be stuck. \n",MAX_DRIVE_TIME);
//                            SetGlobalState(FOLLOW_WALLS);
 //                           System.out.printf("Follow wall");
 //                       }
                       

                        numOfDucks = blobber.numObjFound();
                        break;       
                                      
                    
                    /*Global state machine: State "IDLE"
                     * The robot goes into an idle state
                     * Entry from states: ANY
                     * Entry conditions: Task completed or manual input
                     * Exit to states: SCAN360
                     * Exit conditions: manual input
                     */
                    case (IDLE):
                        DisplayGlobalState();
                        drive.stop();
                        //robot turns back on if gui slider up above 127
                        if(gui.getProperty1() > 127){
                            //Reset to the beginning
                            SetGlobalState(FOLLOW_WALLS);
                            SetFollowWallState(FIND_WALL);
                            //set slider back to 127
                            gui.setProperty1(127);
                        }                        
                        break;
                        

                    case (COMPLETED):
                        System.out.printf("Completed.\n");
                        
                        break;
                        
                    case (COLLECT_DUCK):
                        if (!sameGlobalState){
                            drive.Steering(Drive.NEUTRAL_STEERING_ANGLE);
                            rollersRun = false;
                            numOfCollectAttempts = 0;
                        }
                        DisplayGlobalState();
                        switch (collectDuckState){
                            case (USE_ROLLERS):
                                DisplayCollectDuckState();
                                if (sensors.GetDuckSensor(5) == true){
                                    if(rollersRun == true){
                                        //Drive forward for a bit
                                        drive.forward();
                                        robot.wait(200);
                                        drive.stop();
                                        numOfCollectAttempts++;
                                        System.out.printf("Duck detected and rollers have been run. Driving forward attempt %d/%d\n",numOfCollectAttempts,MAX_ALLOWED_ATTEMPTS);
                                        if(numOfCollectAttempts>=MAX_ALLOWED_ATTEMPTS){
                                            System.out.printf("Tried more than %d times to drive foward into duck.\n", MAX_ALLOWED_ATTEMPTS);
                                            SetGlobalState(FOLLOW_WALLS);
                                            SetFollowWallState(FIND_WALL);
                                        }
                                    }
                                    //Run the rollers if a duck is detected by the duck sensor
                                    System.out.printf("Detect duck: brushes on\n");
                                    drive.stop();
                                    collect.brushesOn();
                                    robot.wait(BRUSHES_RUN_TIME);
                                    collect.brushesOff();
                                    //Remember that an attempt was made to collect the duck
                                    rollersRun = true;
                                }else{
                                    if(rollersRun == true){
                                        rollersRun = false;
                                        //if an attempt to collect the duck was made
                                        //and the duck sensor reads false then the duck
                                        //has been collected
                                        System.out.printf("duck collected\n");
                                        numCollectedDucks++;
                                        if(numCollectedDucks >= 10){
                                            /*NEED TO SIGNAL COMPLETION HERE */
                                            System.out.printf("Three ducks collected\n");
                                            SetGlobalState(COMPLETED);
                                        }else{
                                            /* NEED TO SIGNAL COLLECTION HERE */
                                            SetGlobalState(FOLLOW_WALLS);
                                            SetFollowWallState(FIND_WALL);
                                        }
                                    }else{
                                        //if the duck sensor reads false and the rollers have not been run
                                        //to attempt to collect a duck then
                                        if(sensors.GetFrontMetric(10) < MIN_COLLECT_DIST){
                                            //if too close to front wall to collect duck
                                            //go into use arm state
                                            System.out.printf("Too close to wall so use arm(dist= %dcm)\n",sensors.GetFrontMetric(10));
                                            SetCollectDuckState(USE_ARM);
                                        }else{
                                            //check the bumper switches to see if the robot is puching against something
                                            if (sensors.GetFrontLeft()){
                                                drive.smallBackTurn(Drive.RIGHT, 1000);
                                                drive.smallBackTurn(Drive.LEFT, 500);
                                                SetGlobalState(DRIVE_TO_DUCK);
                                                System.out.printf("FL bumper\n");
                                            }
                                            else if (sensors.GetFrontRight()){
                                                drive.smallBackTurn(Drive.LEFT, 1000);
                                                drive.smallBackTurn(Drive.RIGHT, 500);
                                                SetGlobalState(DRIVE_TO_DUCK);
                                                System.out.printf("FR bumper\n");
                                            }else{
                                                //Drive forward for a bit
                                                drive.forward();
                                                robot.wait(200);
                                                drive.stop();
                                                numOfCollectAttempts++;
                                                System.out.printf("Duck not detected. Driving forward attempt %d/%d\n",numOfCollectAttempts,MAX_ALLOWED_ATTEMPTS);
                                                if(numOfCollectAttempts>=MAX_ALLOWED_ATTEMPTS){
                                                    System.out.printf("Tried more than %d times to drive foward into duck.\n", MAX_ALLOWED_ATTEMPTS);
                                                    SetGlobalState(FOLLOW_WALLS);
                                                    SetFollowWallState(FIND_WALL);
                                                }
                                            }
                                        }
                                    }
                                }
                                if(numOfCollectAttempts >= MAX_ALLOWED_ATTEMPTS){
                                    SetGlobalState(FOLLOW_WALLS);
                                    SetFollowWallState(FIND_WALL);
                                }
                                break;

                            case (USE_ARM):
                                DisplayCollectDuckState();
                                if(isArmDown == false){
                                    //Do PID iteration to get into position to lower arm
                                    drive.setSpeed(followingController.getIntoArmPosControl(sensors.GetFrontMetric(10), LOWER_ARM_DIST));
                                    int arm_clearance = sensors.GetFrontMetric(10) -  LOWER_ARM_DIST;
                                    System.out.printf("At Goal:%b.  Clearance:%d Time at goal:%d \n",atGoal, arm_clearance, System.currentTimeMillis() - initGoalTime);
                                    if(arm_clearance  <= LOWER_ARM_TOLERANCE && arm_clearance >= -LOWER_ARM_TOLERANCE){
                                        if(atGoal == false){
                                            initGoalTime = System.currentTimeMillis();
                                            System.out.printf("Reached distance goal for first time.\n");
                                            atGoal = true;
                                        }else if((System.currentTimeMillis() - initGoalTime) > TIME_AT_GOAL){
                                                drive.stop();
                                                collect.lowerArm();
                                                isArmDown = true;
                                                System.out.printf("At distance goal for %d ms so lower arm.\n",System.currentTimeMillis() - initGoalTime );
                                                atGoal = false;
                                                robot.wait(600);
                                        }
                                    }else{
                                        atGoal = false;
                                    }
                                }else{
                                    int arm_clearance = sensors.GetFrontMetric(10) -  REVERSE_ARM_LOWERED_DIST;
                                    if(arm_clearance  <= LOWER_ARM_TOLERANCE && arm_clearance >= -LOWER_ARM_TOLERANCE){
                                        System.out.printf("Reached distance from wall to raise arm.\n)");

                                        drive.stop();
                                        collect.liftArm();
                                        robot.wait(300);
                                        isArmDown = false;
                                        SetCollectDuckState(USE_ROLLERS);
                                    }else{
                                        if (sensors.GetBackLeft() || sensors.GetBackRight()){
                                            collect.liftArm();
                                            isArmDown = false;
                                            SetGlobalState(FOLLOW_WALLS);
                                            SetFollowWallState(FIND_WALL);
                                            System.out.printf("Back bumpers hit while arm down.\n");
                                        }else{
                                            drive.setSpeed(followingController.getIntoArmPosControl(sensors.GetFrontMetric(10), REVERSE_ARM_LOWERED_DIST));
                                        }
                                    }
                                }
                                break;
                        }
                        break;
                        
                        
                     /*Global state machine: State "SCAN360"
                     * Robot completes a scanning maneuver to attempt to locate
                     * ducks.
                     * Entry from states: FOLLOW_WALLS or COLLECT_DUCK.
                     * Entry conditions: Two walls have been followed or attempt
                     * to collect a duck has failed and the duck position has 
                     * been lost
                     * Exit to states: FOLLOW_WALL or COLLECT_DUCK
                     * Exit conditions: Scan completed or duck found.
                     */  
                        
                    case (SCAN360):
                        //first through run, check the previous follow wall state, set scan direction
                        //away from that wall
                        if(sameGlobalState == false){
                              number_of_walls_passed = 0;
                              if(followWallState == LEFT_WALL){
                                  scan_direction = RIGHT_SCAN_DIRECTION;
                              }
                              else if(followWallState == RIGHT_WALL){
                                  scan_direction = LEFT_SCAN_DIRECTION;
                              }
                              number_of_scans_completed = 0;
                              number_of_walls_passed = 0;
                         }
                        DisplayGlobalState();
                        
                        
                        //check webcam(check the webcam, if duck seen set state to drive to duck)
                        blobber.findBlobs(camera.getImage(), blobber.DUCK);
                        blobber.drawBlobs(gui);
                        if (blobber.numObjFound() >= 1){
                            SetGlobalState(DRIVE_TO_DUCK);
                        }
                         
                        
                        else{

                            //perform angle turning manouvre
                            drive.threePointTurn(scan_direction);
                            number_of_scans_completed++;
                            //check turn angle(check number of turns, if greater than 17 then return
                            //to following wall
                            if(number_of_scans_completed == complete_scan){
                                if(followWallState == RIGHT_WALL){
                                    drive.smallBackTurn(1,1500);
                                    drive.smallForwardTurn(0,2000);
                                }
                                else{
                                    drive.smallBackTurn(0,1500);
                                    drive.smallForwardTurn(1,2000);
                                }
                                SetGlobalState(FOLLOW_WALLS); 
                            }
                        
                            else{
                                //check all sensors(if any sensor to close then perform Stuck manouvre)
                                //
                                sensors.CheckSensors();
                                //perfrom manoevre
                                if(sensors.isStuck()){
                                    stuck.stuck_360_manouvre();
                                }
                            }
                        }
                        
                        break;
                        
                        
                                  /*Global state machine: State "FOLLOW_WALLS"
                     * Robot uses a nested state machine followWallState to 
                     * perform a PID control loop to follow a wall at a set 
                     * distance.
                     * Entry from states: SCAN360
                     * Entry conditions: A scan of the arena has been completed.
                     * Exit to states: DRIVE_TO_DUCK or SCAN360
                     * Exit conditions: 2 corners have been navigated or duck found.
                     */ 
                    case(FOLLOW_WALLS):
                        DisplayGlobalState();
                        
                        
                        //the wall following state machine
                        switch(followWallState){
                            
                            /*follow wall state machine: State "FIND_WALL"
                            * Robot attempts to find a wall to follow either left,
                            * right or in front. Drives straight ahead untill a 
                            * wall is found.
                            * Entry from states: TURN_CORNER
                            * Entry conditions: Been turning for set time or 
                            * corner has been successfully navigated (front 
                            * sensor returns no wall present).
                            * Exit to states: CORNER_TURNING, REVERSING, 
                            * LEFT_WALL or RIGHT_WALL
                            * Exit conditions: Front wall too close to turn, 
                            * front wall far enough away to turn, left wall 
                            * detected or right wall detected.
                            */
                            case(FIND_WALL):
                                DisplayFollowWallState();
                                //Record all required sensor values
                                left_sensor = sensors.GetLeftMetric(10);
                                right_sensor = sensors.GetRightMetric(10);
                                front_sensor = sensors.GetFrontMetric(10);
                                left_front_sensor = sensors.GetFrontLeft();
                                right_front_sensor = sensors.GetFrontRight();
                                
                                //Too close to a wall in front
                                if (front_sensor < minimum_turn||left_front_sensor == true||right_front_sensor == true){
                                    drive.stop();
                                    SetFollowWallState(REVERSING);
                                }                                
                                //getting close to wall, need to turn corner
                                else if (front_sensor < turning_distance){
                                    drive.stop();
                                    SetFollowWallState(TURNING_CORNER);
                                }                               
                                //left wall closest
                                else if((left_sensor < right_sensor)){
                                   SetFollowWallState(LEFT_WALL);
                                    //set the wall time starting
                                    intial_wall_time = System.currentTimeMillis();
                                }                                
                                //right wall closest
                                else if((right_sensor < left_sensor)){
                                    SetFollowWallState(RIGHT_WALL);
                                    //set the wall time starting
                                    intial_wall_time = System.currentTimeMillis();
                                }                                
                                //still need to find wall
                                else{
                                    //set steering forward, and drive forward
                                    drive.turn_straight();
                                    drive.setSpeed(follow_wall_speed);
                                }
                                break;
                                
                            /*follow wall state machine: State "LEFT_WALL"
                            * Robot performs a PID control iteration to follow 
                            * a wall on the lhs of the robot
                            * Entry from states: FIND_WALL
                            * Entry conditions: left wall found and is closer 
                            * than right wall and no wall in front of robot.
                            * Exit to states: CORNER_TURNING and REVERSING
                            * Exit conditions: front wall far enough away to turn,
                            * Front wall too close to turn.
                            */
                            case(LEFT_WALL):
                                //get current time of the system
                                //determine how long the wall following has been
                                //if over a specific time then 1 wall has been followed
                                current_wall_time = System.currentTimeMillis();
                                
                                //FIRST RUN THROUGH
                                if(sameFollowWallState == false){
                                    //scan once 1 walls have been followed
                                    if(number_of_walls_passed >= numWallsTillScan){
                                        SetGlobalState(SCAN360);
                                        
                                    }
                                }
                                else{                          
                                    //Record neccessary sensor values
                                    front_sensor = sensors.GetFrontMetric(10);

                                    //if too close to end wall and need to turn
                                    //set into reverse state
                                    if (front_sensor < minimum_turn){
                                        drive.stop();
                                        SetFollowWallState(REVERSING);
                                    }                               

                                    //if come to the end of wall, turn a right turn
                                    //and not too close to the end
                                    else if(front_sensor < turning_distance){
                                        drive.stop();
                                        SetFollowWallState(TURNING_CORNER);
                                    }


                                    //follow the left hand wall
                                    else{
                                        //record bumper switches
                                        left_front_sensor = sensors.GetFrontLeft();

                                        //hit the wall that was attempting to follow
                                        if(left_front_sensor == true){
                                            //reverse away from it
                                            drive.smallBackTurn(1,500);
                                        }
                                        else{
                                            right_front_sensor = sensors.GetFrontRight();
                                            //right sensor hit, probably the middle barrier
                                            if(right_front_sensor == true){
                                                //stuck manovre
                                                drive.smallBackTurn(1,500);
                                                drive.smallBackTurn(0,1000);
                                            }
                                            //neither bumper hit
                                            else{
                                                left_sensor = sensors.GetLeftMetric(10);
                                                //if in the middle following the barrier
                                                if(left_sensor == sensors.INVALID_MEASUREMENT && front_sensor < 100){
                                                    SetFollowWallState(FIND_WALL);
                                                }
                                                else{
                                                    //follow wall
                                                    wall_control = followingController.followWallControl(LEFT_WALL, left_sensor, desired_follow_distance);
                                                    drive.Steering(wall_control);
                                                    drive.setSpeed(follow_wall_speed);
                                                }
                                            }
                                        }
                                        }
                                    }
                                    DisplayFollowWallState();
                                
                                break;
                            
                            /*follow wall state machine: State "RIGHT_WALL"
                            * Robot performs a PID control iteration to follow 
                            * a wall on the rhs of the robot
                            * Entry from states: FIND_WALL
                            * Entry conditions: right wall found and is closer 
                            * than left wall and no wall in front of robot.
                            * Exit to states: CORNER_TURNING and REVERSING
                            * Exit conditions: front wall far enough away to turn,
                            * Front wall too close to turn.
                            */
                            case(RIGHT_WALL):
                                //get current time of the system
                                //determine how long the wall following has been
                                //if over a specific time then 1 wall has been followed
                                current_wall_time = System.currentTimeMillis();
                                
                               //FIRST RUN THROUGH
                                if(sameFollowWallState == false){
                                    //scan once 2 walls have been followed
                                    if(number_of_walls_passed >= numWallsTillScan){
                                        SetGlobalState(SCAN360);
                                    }
                                }
                                else{                   
                                    //Record neccessary sensor values
                                    front_sensor = sensors.GetFrontMetric(10);

                                    //if too close to end wall and need to turn
                                    //set into reverse state
                                    if (front_sensor < minimum_turn){
                                        drive.stop();
                                        SetFollowWallState(REVERSING);
                                    }                               

                                    //if come to the end of wall, turn a right turn
                                    //and not too close to the end
                                    else if(front_sensor < turning_distance){
                                        drive.stop();
                                        SetFollowWallState(TURNING_CORNER);
                                    }


                                    //follow the left hand wall
                                    else{
                                        //record bumper switches
                                        right_front_sensor = sensors.GetFrontRight();

                                        //hit the wall that was attempting to follow
                                        if(right_front_sensor == true){
                                            //reverse away from it
                                            drive.smallBackTurn(0,500);
                                        }
                                        else{
                                            left_front_sensor = sensors.GetFrontLeft();
                                            //right sensor hit, probably the middle barrier
                                            if(left_front_sensor == true){
                                                //stuck manovre
                                                drive.smallBackTurn(0,500);
                                                drive.smallBackTurn(1,1000);
                                            }
                                            //neither bumper hit
                                            else{
                                                right_sensor = sensors.GetRightMetric(10);
                                                //if in the middle following the barrier
                                                if(right_sensor == sensors.INVALID_MEASUREMENT && front_sensor < 100){
                                                    SetFollowWallState(FIND_WALL);
                                                }
                                                else{
                                                    //follow wall
                                                    wall_control = followingController.followWallControl(RIGHT_WALL, right_sensor, desired_follow_distance);
                                                    drive.Steering(wall_control);
                                                    drive.setSpeed(follow_wall_speed);
                                                }
                                            }
                                        }
                                    }
                                }
 
                                DisplayFollowWallState();
                                break;                                
                                
                            /*follow wall state machine: State "TURNING_CORNER"
                            * Robot enters a while loop to turn a corner
                            * Entry from states: FIND_WALL, RIGHT_WALL, LEFT_WALL
                            * Entry conditions: Front wall detected and gives 
                            * enough clearance to turn robot.
                            * Exit to states: FIND_WALL and REVERSING
                            * Exit conditions: front wall not detected any more,
                            * Front wall too close to turn.
                            */
                            case(TURNING_CORNER):
                                //FIRST RUN THROUGH
                                if(sameFollowWallState == false){
                                    drive.stop();
                                    left_sensor = sensors.GetLeftMetric(10);
                                    right_sensor = sensors.GetRightMetric(10);
                                    //if following left wall, turn right
                                    if(prevFollowWallState == LEFT_WALL || (left_sensor < right_sensor)){
                                        drive.turn_right();
                                    }                                
                                    //if following the right wall or just get close infront, turn left
                                    else{
                                        drive.turn_left();
                                    }
                                    //check if have followed a wall
                                    if((current_wall_time - intial_wall_time) > WALL_TIME){
                                        number_of_walls_passed++;
                                    }
                                }
                                DisplayFollowWallState();

                                //Record neccessary sensor values
                                front_sensor = sensors.GetFrontMetric(10);
                                right_front_sensor = sensors.GetFrontRight();
                                left_front_sensor = sensors.GetFrontLeft();
                                
                                //if too close to the wall, reverse up
                                if (front_sensor < minimum_turn||left_front_sensor == true||right_front_sensor == true){
                                    drive.stop();
                                    SetFollowWallState(REVERSING);
                                }
                                //if get round corner and have enough distance infront
                                else if (front_sensor > turned_corner_distance){
                                    if(prevFollowWallState == RIGHT_WALL){
                                        SetFollowWallState(RIGHT_WALL);
                                        drive.stop();
                                            
                                        //set the wall time starting again
                                        intial_wall_time = System.currentTimeMillis();
                                    }
                                    else if(prevFollowWallState == LEFT_WALL){
                                        SetFollowWallState(LEFT_WALL);
                                        drive.stop();

                                        //set the wall time starting
                                        intial_wall_time = System.currentTimeMillis();
                                    }
                                    else{
                                        SetFollowWallState(FIND_WALL);
                                        drive.stop();
                                    }
                                }
                                //still navigating the corner
                                else{
                                    drive.setSpeed(follow_wall_speed);
                                }
                                break;
                                
                                
                                /*follow wall state machine: State "REVERSING"
                                * Robot enters a while loop to reverse away from 
                                * a wall
                                * Entry from states: TURNING_CORNER, RIGHT_WALL, 
                                * LEFT_WALL, FIND_WALL
                                * Entry conditions: Front wall detected and not 
                                * enough clearance to turn
                                * Exit to states: CORNER_TURNING
                                * Exit conditions: front wall gives enough clearance to turn
                                */
                                case(REVERSING):
                                    DisplayFollowWallState();
                                    
                                    //Record neccessary sensor values
                                    front_sensor = sensors.GetFrontMetric(10);
                                    sensors.Gleft_back = sensors.GetBackLeft();
                                    sensors.Gright_back = sensors.GetBackRight();
                                    sensors.Gleft_front = sensors.GetFrontLeft();
                                    sensors.Gright_front = sensors.GetFrontRight();
                                    

                                    if(front_sensor > reversed_distance && sensors.Gright_front == false && sensors.Gleft_front == false) {
                                        drive.stop();
                                        SetFollowWallState(TURNING_CORNER);
                                    }
                                    
                                    else{
                                        if(sensors.Gright_front=true){
                                            drive.smallBackTurn(0,500);
                                        }
                                        else if(sensors.Gleft_front=true){
                                            drive.smallBackTurn(1,500);
                                        }
                                        //turn to the left
                                        else if(sensors.Gright_back == true){
                                                drive.smallForwardTurn(1,1000);
                                        }
                                        //turn to the right
                                        else if(sensors.Gleft_back == true){
                                                drive.smallForwardTurn(0,1000);
                                        }
                                        else{
                                            drive.turn_straight();
                                            drive.setSpeed(reverse_speed);
                                        }
                                    }
                                                                
                                    break;                        
                            }
                            
//                            System.out.println("Follow Walls STATE: " + number_of_walls_passed);
//                            System.out.println("Walls followed: " + followWallState);
//                            System.out.println("initial wall time: " + intial_wall_time);
//                            System.out.println("current wall time: " + current_wall_time);
//                            wall_tim = current_wall_time -intial_wall_time;
//                            System.out.println("current wall time: " + wall_tim);
                            
                        //Check for ducks in the follow walls state
                        blobber.findBlobs(camera.getImage(), blobber.DUCK);
                        blobber.drawBlobs(gui);
                        //check duck sensor whilst driving around
                        if (sensors.GetDuckSensor(5) == true){
                            SetGlobalState(COLLECT_DUCK);
                            SetCollectDuckState(USE_ROLLERS);
//                            System.out.printf("Duck whilst driving ");
                        }
                        else if (blobber.numObjFound() >= 1){
                                SetGlobalState(DRIVE_TO_DUCK);
                                scan_direction = FIND_WALL;                            
                        }

                        
                    break;
                }
    }
}