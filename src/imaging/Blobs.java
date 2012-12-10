package imaging;

import java.util.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
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
 *
 * @author rdm86
 */
public class Blobs {
    //input image variables
    private int width = 320;
    private int height = 240;
    public BufferedImage rawImg;
    public BufferedImage blobImg;
    public byte[] blobImgData;
    
    //bloblist
    public ArrayList<BlobFinder.Blob> blobList = new ArrayList<BlobFinder.Blob>();
    public ArrayList<BlobFinder.Blob> prevBlobList = new ArrayList<BlobFinder.Blob>();
    
    //Image processing parameters
    //Thresholding
    public int upperY;
    public int lowerY;
    public int upperI;
    public int lowerI;
    //Blobbing
    public int minMass;
    public int maxMass;
    public byte bgPixel = 127;
    public final byte DUCK = 1;
    public final byte BOTH = 2;
    public byte objectType = DUCK;
    
    public Blobs(int blobMinMass,int blobMaxMass,int upperYellowThresh,
        int lowerYellowThresh,int upperIntensityThresh, int lowerIntensityThresh){
        minMass = blobMinMass;
        maxMass = blobMaxMass;
        upperY = upperYellowThresh;
        lowerY = lowerYellowThresh;
        upperI = upperIntensityThresh;
        lowerI = lowerIntensityThresh;
    }
    
    public void setParameters(int blobMinMass,int blobMaxMass,int upperYellowThresh,
        int lowerYellowThresh,int upperIntensityThresh, int lowerIntensityThresh){
        minMass = blobMinMass;
        maxMass = blobMaxMass;
        upperY = upperYellowThresh;
        lowerY = lowerYellowThresh;
        upperI = upperIntensityThresh;
        lowerI = lowerIntensityThresh;
    }

  
    public void findBlobs(BufferedImage image, byte object) throws IOException{
        
        //Threshold the image
        rawImg = image;
        blobImg = image;
        objectType = object;
        Threshold threshold = new Threshold();
        
        //When finding ducks use a yellow filter function with appropriate 
        //threshold values. Likewise when finding both cups and ducks use an 
        //intensity filter.
        switch(objectType){
            case (DUCK):
                threshold.hystThresh(image,threshold.F_YELLOW,upperY,lowerY);
            break;

            case (BOTH):
                threshold.hystThresh(image,threshold.F_INTENSITY,upperI,lowerI);
            break;
        }
        
        //Get the image pixel data as an array of bytes
        blobImgData = getImgMonoBytes(image);        
        //Find the blobs
        BlobFinder blobber = new BlobFinder();
        
        prevBlobList.clear();
        for (BlobFinder.Blob blob : blobList) prevBlobList.add(blob);
        
        blobList.clear();
        blobber.detectBlobs(blobImgData, minMass, maxMass, bgPixel, blobList);
   }

   public void drawBlobs(Gui gui) throws IOException {
          //Draw raw image
        ImageIcon icon = new ImageIcon();
        icon.setImage(rawImg);
        gui.paintImageIcon(icon, 1);

        //draw thresholded image
        icon.setImage(blobImg);
        gui.paintImageIcon(icon, 2);

        //draw raw image with rectangles outlining the blobs
        BufferedImage blobLoc =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ArrayList<BlobFinder.Blob> blobList = new ArrayList<BlobFinder.Blob>();
            for (BlobFinder.Blob blob : blobList) blobLoc.createGraphics().drawRect(blob.xMin, blob.yMin, blob.xMax-blob.xMin, blob.yMax-blob.yMin);
        icon.setImage(blobLoc);
        gui.paintImageIcon(icon, 3);
  }

   //find blobs must be called first
    public int numObjFound(){
        return blobList.size();
    }
    
    public boolean isObjLostDown(){
        /* If there are more blobs in the previous list than the current list, 
         * there was at least one blob touching the bottom 
         * of the image and there are no blobs touching the bottom of the image 
         * in the current blob list it is assumed a blob was lost out the bottom 
         * of the image. 
         */
        boolean isBottomCur = false;
        boolean isBottomPrev = false;
        boolean isLostDown = false;
        
        if(prevBlobList.size() > blobList.size()){
            for (BlobFinder.Blob blob : prevBlobList){
                if(blob.yMax >= 235){
                    isBottomPrev = true;
                }
            }
            for (BlobFinder.Blob blob : blobList){
                if(blob.yMax >= 235){
                    isBottomCur = true;
                }
            }
            if((isBottomPrev == true) && (isBottomCur == false)){
                isLostDown = true;
            }
        }
        return isLostDown;
    }

    //Returns a negative if no objects detected
    public int getLeftmostObjX(){
        int leftMostX = 320;
        if (blobList.size() >= 1){
            for (BlobFinder.Blob blob : blobList){
                if(leftMostX > (blob.xMax + blob.xMin)/2){
                    leftMostX = (blob.xMax + blob.xMin)/2;
                }
            }
        }else{
            leftMostX = -1;
        }
        return leftMostX;
    }

    public int getRightmostObjX(){
        int rightMostX = 0;
        if (blobList.size() >= 1){
            for (BlobFinder.Blob blob : blobList){
                if(rightMostX < (blob.xMax + blob.xMin)/2){
                    rightMostX = (blob.xMax + blob.xMin)/2;
                }
            }
        }else{
            rightMostX = -1;
        }
        return rightMostX;
    }

    public int getBiggestObjX(){
        int biggestObj = 0;
        int Xobj = 0;
        if (blobList.size() >= 1){
            for (BlobFinder.Blob blob : blobList){
                if(biggestObj <= blob.mass){
                    biggestObj = blob.mass;
                    Xobj = (blob.xMax + blob.xMin)/2;
                }
            }
        }else{
            Xobj = -1;
        }
        return Xobj;
    }
    
    public void printBlobs(){
        // List Blobs
        System.out.printf("Found %d blobs:\n", blobList.size());
        for (BlobFinder.Blob blob : blobList) System.out.println(blob);        
    }

    public ArrayList<BlobFinder.Blob> getBlobs(){            
        return blobList;
    }
    
   
    public byte[] getImgMonoBytes(BufferedImage img) {
        //Takes a thresholded image and converts it into an array of Bytes
        //containing mono(black and white) pixel information.
        byte[] imgMono = new byte[width*height];
        int pixelValue;
        for (int x = 0; x < width; x++){
            for (int y = 0;y < height; y++){
                pixelValue = findRGBavg(img.getRGB(x,y));
                if (pixelValue == 255){ 
                    imgMono[width*y + x] = 127;
                }else if(pixelValue == 0){
                    imgMono[width*y + x] = 0;
                }else{
                    System.out.println("Invalid pixel value found. Image not thresholded");
                }
            }
        }
        return imgMono;
    }
    
    private int findRGBavg(int RGB){
        int r = (RGB >>> 16) & 0xff;
        int g = (RGB >>> 8) & 0xff;
        int b = RGB & 0xff;
        return (r+g+b)/3;
    }
}
