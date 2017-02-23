//package hw3cp;
//package cop5618;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.floor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import static java.util.concurrent.ForkJoinTask.invokeAll;

//import org.junit.BeforeClass;

public class FJBufferedImage extends BufferedImage {
	
        /** Declare the fork join pool**/
        ForkJoinPool pool = new ForkJoinPool();
        
        /**Constructors*/
	
	public FJBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
			Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
	}
	

	/**
	 * Creates a new FJBufferedImage with the same fields as source.
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
	       Hashtable<String,Object> properties=null; 
	       String[] propertyNames = source.getPropertyNames();
	       if (propertyNames != null) {
	    	   properties = new Hashtable<String,Object>();
	    	   for (String name: propertyNames){properties.put(name, source.getProperty(name));}
	    	   }
	 	   return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);		
	}
        
        /**Recursive task for setRGB**/
        private class setRGBRecTsk extends RecursiveAction {

		int x;
		int y;
		int w;
		int h;
		int[] rgbArray;
		int offset;
		int scansize;
		int NPROCS;

		public setRGBRecTsk(int x, int y, int w, int h, int[] rgbArray, int offset, int scansize, int NPROCS) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rgbArray = rgbArray;
			this.offset = offset;
			this.scansize = scansize;
			this.NPROCS = NPROCS;
		}

		@Override
		protected void compute() {
			if (NPROCS < 2) {
                                
                                 // Threashold is 2 threads based on speedup after testing for different scenarios
				FJBufferedImage.super.setRGB(x, y, w, h, rgbArray, offset, scansize);
			}
			else {
                                
                                // Dividing image into 2 halves recursively till threshold is reached
				invokeAll( 
                                         
                                          new setRGBRecTsk(x, y, w, h/2, rgbArray, offset, scansize, NPROCS/2),
                                          new setRGBRecTsk(x, y + h/2 , w, h - h/2, rgbArray, offset + (h/2 * scansize), scansize, NPROCS/2)
                                );
			}
		}
	}
        
        /**Recursive task for getRGB**/
	private class getRGBRecTsk extends RecursiveAction {
                int x;
		int y;
		int w;
		int h;
		int[] rgbArray;
		int offset;
		int scansize;
		int NPROCS;

		public getRGBRecTsk(int x, int y, int w, int h, int[] rgbArray, int offset, int scansize, int NPROCS) {
                    this.x = x;
                    this.y = y;
                    this.w = w;
                    this.h = h;
                    this.rgbArray = rgbArray;
                    this.offset = offset;
                    this.scansize = scansize;
                    this.NPROCS = NPROCS;
		}

		@Override
		protected void compute() {
			if (NPROCS < 2) {
                                
                                
                                // Threshold is 2 threads
                                FJBufferedImage.super.getRGB(x, y, w, h, rgbArray, offset, scansize);
			}
			else {
                                // Dividing image into 2 halves recursivley till threshold is reached
				invokeAll(
                                          
                                          new getRGBRecTsk(x, y, w, h/2, rgbArray, offset, scansize, NPROCS/2),
                                          new getRGBRecTsk(x, y + h/2, w, h - h/2, rgbArray, offset + (h/2 * scansize), scansize, NPROCS/2)
                                        
                                          );
			}
		}
	}

	
	@Override
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
                // Invoke the setRGB recursive task for parallel divide and conquer approach
		pool.invoke(new setRGBRecTsk(xStart, yStart, w, h, rgbArray, offset, scansize, pool.getParallelism() * 4));
	}

	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
                // Invoke the getRGB recursive task for parallel divide and conquer approach
                pool.invoke(new getRGBRecTsk(xStart, yStart, w, h, rgbArray, offset, scansize, pool.getParallelism() * 4));
		return rgbArray;
	}    
	
	

	
        
}
