
package imaging;
import java.awt.image.*;
import java.awt.Color; 
import java.awt.image.BufferedImage; 
import java.io.File; 
import java.io.IOException; 
import javax.imageio.ImageIO;
/**
 *
 * @author rdm86
 */
public class Threshold{
    
    private int width = 320;
    private int height = 240;
    public final byte F_INTENSITY = 1;
    public final byte F_YELLOW = 2;
    
    public final int Hlow = 20;
    public final int Hhigh = 100;
    public final int Slow = 20;
    public final int Shigh = 80;
    public final int Ilow = 40;
    public final int Ihigh = 100;
     
    public void hystThresh(BufferedImage image, byte filter, int upper, int lower) {
        int value;
        for(int x = 0 ; x < width ; x++) {
            for(int y = 0 ; y < height ; y++) {
                value = getFilterValue(filter, image.getRGB(x, y));
                if (value >= upper) {
                    image.setRGB(x, y, 0xffffff);
                    
                    //Search adjacent pixels and if they exceed the 
                    //lower threshold value (hysterisis loop) 
                    for (int x1 = x-1 ; x1 <= x+1 ; x1++) {
                        for (int y1 = y-1 ; y1 <= y+1 ; y1++) {
                            if ((x1 < width) & (y1 < height) & (x1 >= 0) & (y1 >= 0) & (x1 != x) & (y1 != y)) {
                                value = getFilterValue(filter, image.getRGB(x1,y1));
                                if (value != 255) {
                                    if (value >= lower) {
                                        image.setRGB(x1,y1,0xffffff);
                                    }
                                    else {
                                        image.setRGB(x1,y1,0x000000);
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        //Find all the non-altered pixels and set them to white
        for(int x = 0 ; x<width ;x++) {
            for(int y=0 ; y<height ;y++) {
                if (image.getRGB(x, y) == 0xffffffff){
                    image.setRGB(x,y,0xffffff);
                }else{
                    image.setRGB(x,y,0x000000);
                }
            }
        }
    }

    private int getFilterValue(byte filter, int RGB){
        int value = 0;
        switch(filter){
            case (F_YELLOW):
                value = findYavg(RGB);
            break;
            case (F_INTENSITY):
                value = findRGBavg(RGB);
            break;
        }
        return value;
    }
    
    private int findRGBavg(int RGB){
        int r = (RGB >>> 16) & 0xff;
        int g = (RGB >>> 8) & 0xff;
        int b = RGB & 0xff;
        return (r+g+b)/3;
    }
    
      public static int findYavg(int RGB)
    {
        int red = (RGB >>> 16) & 0xff;
        int green = (RGB >>> 8) & 0xff;
        int blue = RGB & 0xff;
        
        int yellow =  100 + ((red+green)/2 - blue);
        return yellow;
    }
      
      public int findYHSI(int RGB)
      {
          int[] HSI = new int[3];
          int yellow;
          HSI = RGBtoHSI(RGB);
          int H = HSI[0];
          int S = HSI[1];
          int I = HSI[2];
          if(H >= Hlow && H <= Hhigh && S >= Slow && S <= Shigh && I >= Ilow && I <= Ihigh){
              yellow = 255;
          }else{
              yellow = 0;
          }
          return yellow;
      }
      
    public static int[] RGBtoHSI(int RGB){
        //RGB - HSV conversion
        //Extract the red, green and blue components.
        int red = (RGB >>> 16) & 0xff;
        int green = (RGB >>> 8) & 0xff;
        int blue = RGB & 0xff; 
        
        //Normalise the RGB components (0-1)
        double r = red / 255.f;
        double g = green / 255.f;
        double b = blue / 255.f;
                
        //Find the min/max RGB components
        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);
                
        //Intensity
        double I = (r+g+b)/3;
        //Saturation
        double S = 1 - ((3/(r+g+b))*min);
        //Hue
        double num = 0.5*((r-g)+(r-b));
        double denom = Math.sqrt((r-g)*(r-g) + (r-b)*(g-b));
        double H;
        if (denom != 0){
            H = Math.acos(num/denom);
        }else{
            H = 2*Math.PI;
        }
        if(b/I > g/I){
            H = 2*Math.PI - H;
        }
                
        //normalize Hue        
        H = H / (2*Math.PI);
        
        //Normalise each component
        
        int h = (int)Math.round(H*360);
        int i = (int)Math.round(I*100);
        int s = (int)Math.round(S*100);
        
        int[] HSI = new int[3];
        HSI[0] = h;
        HSI[1] = i;
        HSI[2] = s;
        
        return HSI;
    }     
    
    private static float degreesToRad(float degrees){
        return (float)(degrees * (Math.PI / 180));
    }


    public BufferedImage otsuThresh(BufferedImage image){
        /**  * Image binarization - Otsu algorithm  *  * Author: Bostjan Cigan (http://zerocool.is-a-geek.net)  *  */     
        BufferedImage grayscale = toGray(image);         
        BufferedImage binarized = binarize(grayscale);
        return binarized;
    }      
 
    // Return histogram of grayscale image     
    public static int[] imageHistogram(BufferedImage input) {

        int[] histogram = new int[256];
        for(int i=0; i<histogram.length; i++) histogram[i] = 0;

        for(int i=0; i<input.getWidth(); i++) {             
            for(int j=0; j<input.getHeight(); j++) {                 
                int red = new Color(input.getRGB (i, j)).getRed();                 
                histogram[red]++;             
            }         
        }
        return histogram;      
    }  

    // The luminance method     
    private static BufferedImage toGray(BufferedImage original) {           
        int alpha, red, green, blue;

        int newPixel;           
        BufferedImage lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());        
        for(int i=0; i<original.getWidth(); i++) {     
            for(int j=0; j<original.getHeight(); j++) {    
                // Get pixels by R, G, B               
                alpha = new Color(original.getRGB(i, j)).getAlpha();             
                red = new Color(original.getRGB(i, j)).getRed();              
                green = new Color(original.getRGB(i, j)).getGreen();              
                blue = new Color(original.getRGB(i, j)).getBlue();               
                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);             
                // Return back to original format               
                newPixel = colorToRGB(alpha, red, red, red);        
                // Write pixels into image               
                lum.setRGB(i, j, newPixel);           
            }        
        }   
        return lum; 
    }   

    // Get binary treshold using Otsu's method    
    private static int otsuTreshold(BufferedImage original){  

        int[] histogram = imageHistogram(original);  
        int total = original.getHeight() * original.getWidth();  
        float sum = 0;   

        for(int i=0; i<256; i++) sum += i * histogram[i];  

        float sumB = 0;     
        int wB = 0;   
        int wF = 0;          
        float varMax = 0;  
        int threshold = 200; 

        for(int i=0 ; i<256 ; i++) {       
            wB += histogram[i];        
            if(wB == 0) continue;             
            wF = total - wB;         
            if(wF == 0) break;           
            sumB += (float) (i * histogram[i]); 
            float mB = sumB / wB;         
            float mF = (sum - sumB) / wF;        
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF); 
            if(varBetween > varMax) {                
                varMax = varBetween;             
                threshold = i;          
            }       
        }        
        return threshold; 
    }     


    private static BufferedImage binarize(BufferedImage original) {       
        int red;

        int newPixel;  
        int threshold = otsuTreshold(original);    
        BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());    
        for(int i=0; i<original.getWidth(); i++) {        
            for(int j=0; j<original.getHeight(); j++) {  
                // Get pixels          
                red = new Color(original.getRGB(i, j)).getRed();  
                int alpha = new Color(original.getRGB(i, j)).getAlpha();            
                if(red > threshold) {               
                    newPixel = 255;              
                }else{       
                    newPixel = 0;   
                }              
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);           
                binarized.setRGB(i, j, newPixel);       
            }         
        }        
        return binarized;      
    } 

    // Convert R, G, B, Alpha to standard 8 bit  
    private static int colorToRGB(int alpha, int red, int green, int blue) {         
        int newPixel = 0;         
        newPixel += alpha;         
        newPixel = newPixel << 8;  
        newPixel += red; newPixel = newPixel << 8;   
        newPixel += green; newPixel = newPixel << 8;  
        newPixel += blue;    
        return newPixel;    
    }  
    
} 
