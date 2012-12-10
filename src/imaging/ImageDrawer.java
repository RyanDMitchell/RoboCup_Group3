package imaging;

import gui.Gui;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/**
 *This module accepts images from the camera controller and passes them to the 
 *image processing module as well as drawing filtered images to the screen.
 */
public class ImageDrawer {
    private final static int width = 320;
    private final static int height =240;
    long time;
    private BufferedImage cameraImage;
    
    public int blobMinMass = 20;
    public int blobMaxMass = 1000000;    

    public int upperY = 200;
    public int lowerY = 50;
    public int upperI = 100;
    public int lowerI = 80;
    
    public Blobs blobber = new Blobs(blobMinMass,blobMaxMass,upperY,lowerY,upperI,lowerI);
    private ImageIcon icon;
    boolean imageSet;
    private Gui display;

    public ImageDrawer( ){
        cameraImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        icon = new ImageIcon();
        imageSet = false;
    }
    
    public void setDisplay(Gui display) {
        this.display = display;
    }

    public synchronized void clearAll(){
        notifyAll();
    }

    /**
     * Retrieve image from last capture
     * @return Most recent image
     */
    public synchronized BufferedImage get() {
        while(!imageSet){
            try {
                wait();
            } catch(InterruptedException e) {
                System.out.println("InterruptedException caught" + e);
            }
        }
        imageSet = false;
        notifyAll();
        return cameraImage;
    }

    /**
     * Conversion is also performed here to make ImageIcon object more accessible
     * @param image Image to be saved and converted
     */
    public synchronized void draw(BufferedImage rawImage) throws IOException {

        cameraImage = rawImage;
          //Draw raw image
        icon.setImage(rawImage);
        display.paintImageIcon(icon, 1);
        
        //draw thresholded image
        Threshold thresholder = new Threshold();  
        
        BufferedImage otsu = thresholder.otsuThresh(cameraImage);
        
        icon.setImage(otsu);
        display.paintImageIcon(icon, 4);
        
        thresholder.hystThresh(cameraImage,thresholder.F_YELLOW,upperY,lowerY);
              
        icon.setImage(cameraImage);
        display.paintImageIcon(icon, 2);
        
       
        //draw raw image with rectangles outlining the blobs
        BufferedImage blobLoc =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ArrayList<BlobFinder.Blob> blobList = new ArrayList<BlobFinder.Blob>();
        
        blobber.findBlobs(cameraImage,blobber.DUCK);
        //Print the blob data 
        //blobber.printBlobs();
        blobList = blobber.getBlobs();
        for (BlobFinder.Blob blob : blobList) blobLoc.createGraphics().drawRect(blob.xMin, blob.yMin, blob.xMax-blob.xMin, blob.yMax-blob.yMin);
        icon.setImage(blobLoc);
        display.paintImageIcon(icon, 3);
        
        imageSet = true;
        notifyAll();
    }
}
