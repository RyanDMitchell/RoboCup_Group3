package robocupgui;
import client.RobotClient;
import Ice.CommunicatorDestroyedException;
import edu.cmu.ri.createlab.TeRK.video.VideoException;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**Camera controller creates a thread which continuously gets images from the 
 * camera as type Image and passes them to the ImageDrawer module.
 * @author Simon Richards and Allan McInnes
 */
public class CameraController{
    private RobotClient robot;
    private final static int width = 320;
    private final static int height =240;
    private BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    long time;

    public CameraController(RobotClient robot) {
        this.robot = robot;
    }

    /**
     * Start the camera
     */
    public void start() {
        // Start the camera
        robot.getQwerkController().getVideoStreamService().startCamera();
        robot.sleepUnlessStop(100);
    }


    public BufferedImage getImage(){
        try{
            Image rawImage = robot.getQwerkController().getVideoStreamService().getFrame();
            //Draw the image onto a bufferedimage to get access
            //to the RGB value for the pixels
            boolean isFinishedDrawing = false;
            while(!isFinishedDrawing){
                isFinishedDrawing = newImage.createGraphics().drawImage(rawImage, 0, 0, null);
            }
        }
        // Ignore exceptions caused by qwerk still starting up
        catch(CommunicatorDestroyedException e){}
        // Inform user about possibly missing camera
        catch(VideoException e){
                System.out.println("Missing camera?");
        }
        // Print all other errors' stack traces
        catch(Exception e){
            e.printStackTrace();
        }
        return newImage;
    }


}
