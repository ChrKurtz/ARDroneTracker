/*
 * VideoPanel.java
 * 
 * Created on 21.05.2011, 18:42:10
 */



import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author normenhansen
 */
@SuppressWarnings("serial")
public class ProcessedVideoPanel extends javax.swing.JPanel implements DroneVideoListener
{
    private AtomicReference<BufferedImage> atomImage          = new AtomicReference<BufferedImage>();  // used for output when displaying the video stream. this variable is the frame that'll be displayed 
    private AtomicBoolean                  preserveAspect = new AtomicBoolean(true);  
    private BufferedImage                  noConnection   = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);

    /** Creates new form VideoPanel */
    public ProcessedVideoPanel()
    {
        initComponents();
        Graphics2D g2d = (Graphics2D) noConnection.getGraphics();
        Font f = g2d.getFont().deriveFont(24.0f);
        g2d.setFont(f);
        g2d.drawString("No video connection", 40, 110);
        atomImage.set(noConnection);
    }

    public void setDrone(ARDrone drone)
    {
        drone.addImageListener(this);
    }

    public void setPreserveAspect(boolean preserve)
    {
        preserveAspect.set(preserve);
    }


    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        drawDroneImage(g2d, width, height);
    }

    private void drawDroneImage(Graphics2D g2d, int width, int height)
    {
        BufferedImage im = atomImage.get();
        if(im == null)
        {
            return;
        }
        int xPos = 0;
        int yPos = 0;
        if(preserveAspect.get())
        {
            g2d.setColor(Color.BLACK);
            g2d.fill3DRect(0, 0, width, height, false);
            float widthUnit = ((float) width / 4.0f);
            float heightAspect = (float) height / widthUnit;
            float heightUnit = ((float) height / 3.0f);
            float widthAspect = (float) width / heightUnit;

            if(widthAspect > 4)
            {
                xPos = (int) (width - (heightUnit * 4)) / 2;
                width = (int) (heightUnit * 4);
            } else if(heightAspect > 3)
            {
                yPos = (int) (height - (widthUnit * 3)) / 2;
                height = (int) (widthUnit * 3);
            }
        }
        if(im != null)
        {
            g2d.drawImage(im, xPos, yPos, width, height, null);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setLayout(new java.awt.GridLayout(4, 6));
    }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
    
    
    
    ////////////////////////////////////////
    //////////// WRAPPED ON KAS ////////////
    ////////////////////////////////////////
	
    
    private int frameCount = 0;
    private int nFrameSkip = 4;  
      // frameReceived will NOT do processing unless frameCount==0;
      // nFrameSkip: skip `nFrameSkip` frames before re-processing
    
	public boolean isTargetFound()
	{
		return success;
	}
	
	public double getTargetX()
    {
    	return tgt_x; 
    }
	
	public double getTargetY()
    {
    	return tgt_y; 
    }
    
	public double getTargetExtent()
	{
		// Some sort of measure of the size of the target
		// perhaps radius, perhaps diameter, perhaps something else.
		// In mother Russia, circle measures you.
		return tgt_r;
	}
	
	@Override
    public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
		if( frameCount == 0 )
		{
	        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);  // create blank frame
	        im.setRGB(startX, startY, w, h, rgbArray, offset, scansize);  // copy pixels across 
	
	        processImage( im );
	        
	        atomImage.set( processedImage );
	        repaint();
		}
        
		frameCount = (frameCount+1) % nFrameSkip;
    }
    
	
	
    ///////////////////////////////////////
    //////////// KAS PROC /////////////////
    ///////////////////////////////////////
    
    private static BufferedImage rawImage;
    private static String imgpath = "./data";
    private static int WIDTH = 320;
    private static int HEIGHT = 240;
    private static double TGT_R = 115.0 / 255.0;
    private static double TGT_G = 49.0 / 255.0;
    private static double TGT_B = 75.0 / 255.0;
    private static double DIST_THR = 0.07;
    private static double CONV_THR = 0.1;
    private static int CONV_R = 10;

    // Results
    private static double tgt_x;
    private static double tgt_y;
    private static double tgt_r;
    private static BufferedImage processedImage;
    private static boolean success;

    // Temporary buffers
    private static double[][] buf = new double[HEIGHT][WIDTH];
    private static double[][] conv = new double[HEIGHT][WIDTH];
    private static int count = 0;

    private static void clearBuffer() {
        for (int row = 0; row < HEIGHT; ++row) {
            for (int col = 0; col < WIDTH; ++col) {
                buf[row][col] = 0.0;
            }
        }
    }

    private static double sq(double x) {
        return x * x;
    }

    public static BufferedImage getImageFromArray(double[][] pixels, int width, int height) {
        int[] tmp = new int[3 * height * width];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        double max = 0.0;
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                max = Math.max(max, pixels[row][col]);
            }
        }
        double scale = 1.0 / max;
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                image.setRGB(col, row, 0x00010101 * (int)(255.0 * scale * pixels[row][col]));
            }
        }
        return image;
    }

    private static void computeDifference() {
        for (int j = 0; j < HEIGHT; ++j) {
            for (int i = 0; i < WIDTH; ++i) {
                Color col = new Color(rawImage.getRGB(i, j));
                double r = col.getRed()   / 255.0;
                double g = col.getGreen() / 255.0;
                double b = col.getBlue()  / 255.0;
                double diff = Math.sqrt(sq(r - TGT_R) + sq(g - TGT_G) + sq(b - TGT_B)) / Math.sqrt(3.0);
                if (diff < DIST_THR) {
                    buf[j][i] = 1.0;
                }
            }
        }
    }

    private static void convolve() {
        double[][] mask = new double[CONV_R*2+1][CONV_R*2+1];
        int c = 0;
        for (int dj = -CONV_R; dj <= CONV_R; ++dj) {
            for (int di = -CONV_R; di <= CONV_R; ++di) {
                if (Math.sqrt(sq((double)dj) + sq((double)di)) <= CONV_R) {
                    mask[dj + CONV_R][di + CONV_R] = 1.0;
                    ++c;
                }
            }
        }


        for (int j = 0; j < HEIGHT; ++j) {
            for (int i = 0; i < WIDTH; ++i) {
                double sum = 0.0;
                for (int dj = -CONV_R; dj <= CONV_R; ++dj) {
                    for (int di = -CONV_R; di <= CONV_R; ++di) {
                        int ii = Math.max(0, Math.min(WIDTH - 1, i + di));
                        int jj = Math.max(0, Math.min(HEIGHT - 1, j + dj));
                        sum += mask[dj + CONV_R][di + CONV_R] * buf[jj][ii];
                    }
                }

                conv[j][i] = sum / (double)c;

                if (conv[j][i] < CONV_THR) {
                    conv[j][i] = 0.0;
                }
            }
        }
    }

    private static void findTarget() {
        // count = 0;
        tgt_x = 0.0;
        tgt_y = 0.0;
        double tconv = 0.0;
        for (int j = 0; j < HEIGHT; ++j) {
            for (int i = 0; i < WIDTH; ++i) {
                tgt_x += i * conv[j][i];
                tgt_y += j * conv[j][i];
                tconv += conv[j][i];
            }
        }
        tgt_x /= tconv;
        tgt_y /= tconv;


        tgt_r = Math.max(1, 4 * Math.sqrt(tconv));
        success = (tgt_r > 4);
    }

    private static void visualise(BufferedImage image) {

        processedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        double maxconv = 0.0;
        for (int j = 0; j < HEIGHT; ++j) {
            for (int i = 0; i < WIDTH; ++i) {
                maxconv = Math.max(maxconv, conv[j][i]);
            }
        }
        for (int j = 0; j < HEIGHT; ++j) {
            for (int i = 0; i < WIDTH; ++i) {
                Color col = new Color(image.getRGB(i, j));
                double r = col.getRed()   / 255.0;
                double g = col.getGreen() / 255.0;
                double b = col.getBlue()  / 255.0;
                double gray = Math.min(1.0, 0.21 * r + 0.71 * g + 0.07 * b);
                double val = conv[j][i] / maxconv;
                int cc = Color.HSBtoRGB((float)val, (float)val, (float)gray);
                processedImage.setRGB(i, j, cc);
            }
        }

        Graphics2D g = processedImage.createGraphics();
        g.setColor(Color.WHITE);
        g.drawOval((int)Math.floor(tgt_x - tgt_r/4), (int)Math.floor(tgt_y - tgt_r/4), (int)Math.round(tgt_r/2.0), (int)Math.round(tgt_r/2.0));

    }

    private static void processImage(BufferedImage image) {
    	rawImage = image;  // MJW
    	
        clearBuffer();
        int height = image.getHeight();
        int width = image.getWidth();
        processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        count = 0;

        computeDifference();
        convolve();
        findTarget();
        visualise(image);
    }
}
