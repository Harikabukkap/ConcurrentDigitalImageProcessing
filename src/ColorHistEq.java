//package hw3cp;
//package cop5618;


import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.awt.Color;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;



public class ColorHistEq {
    
        //Use these labels to instantiate you timers.  You will need 8 invocations of now()
	static String[] labels = { "getRGB", "convert to HSB", "create brightness map", "probability array",
			"parallel prefix", "equalize pixels", "setRGB" };

	static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
		Timer times = new Timer(labels);
		/**
		 * IMPLEMENT SERIAL METHOD
		 */
                ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();
		times.now();
		int[] rgbPixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		times.now(); //getRGB
                
                float[][] hsbPixelArray =
                                
				// get array of rgb pixels from image and convert to stream
				Arrays.stream(rgbPixelArray)
						
				// Create a new HSB pixel from RGB pixel
			        .mapToObj(pixel -> Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel), colorModel.getGreen(pixel), new float[3]))
				
                                // Return an array of float arrays containing the (hue,saturation,brightness) values for each image pixel        
                                .toArray(float[][]::new);        
                times.now(); //convert to HSB
               
                
                Map<Integer, Long> probabilityMap = 
                        
                                // get a stream of hsbPielArray
                                Arrays.stream(hsbPixelArray)
                                
                                // Multiply each pixel's brightness value from its float[] array of (hue,saturation,brightness) with selected number of bins
                                // and convert to int, obtain IntStream from DoubleStream            
                                .mapToInt(hsb -> (int)(hsb[2]*256)  )
                                
                                // Convert to ObjectStream of Integers         
                                .mapToObj(Integer::new)
                                
                                // Count the number of brightness values falling into a bin and group them by the bin value        
                                .collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
                times.now(); // create brightness map                      
               
               // Store the count of brightness values falling in each of the total bins into an array 
               //by iterating through keySet of probabilityMap obtained 
               int[] probabilityArray= new int[257];
               for(int k: probabilityMap.keySet()){
			probabilityArray[k] = probabilityMap.get(k).intValue();
		}
               
          
		
                // Retrieve the total number of samples
                int total_values = Arrays.stream(probabilityArray).sum();
                
                Arrays.parallelPrefix( probabilityArray,(x,y) -> x+y);
                times.now(); // parallel prefix
                
                double[] cumulativeProbability = 
                                
                                // Obtain an IntStream of probabilityArray    
                                Arrays.stream(probabilityArray)
                                                 
                                // Divide every element of prefix sum probabilityArray by total number of samples        
                                .mapToDouble( d -> (double) ((double)d / (double)total_values))
                        
                                // Obtain a double[] of cumulativeProbability        
                                .toArray();
                times.now(); // probability array
                
                
                int[] histRGBArray = 
                                
                                // Obtain a stream of hsbPixelArray    
                                Arrays.stream(hsbPixelArray)
                        
                                // Convert to Intstream of RGB pixels equalizing each pixel's brightness with cumulativeProbability array values corresponding to their bin         
                                .mapToInt(pixel -> Color.HSBtoRGB( (float)Array.get(pixel,0), (float)Array.get(pixel,1), (float)cumulativeProbability[(int) ( 256 * (float)Array.get(pixel,2) )]))
                                        
                                // Obtain an int[] of RGB pixels        
                                .toArray();
                times.now(); // equalize pixels
                
                
                newImage.setRGB(0, 0, w, h, histRGBArray, 0, w);
		times.now(); // setRGB
                
		return times;
	}



	static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {
		Timer times = new Timer(labels);
		/**
		 * IMPLEMENT PARALLEL METHOD
		 */
                ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();
		times.now();
		int[] rgbPixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		times.now(); //getRGB
                
                float[][] hsbPixelArray =
                                
				// get array of rgb pixels from image and convert to stream
				Arrays.stream(rgbPixelArray)
                                        
				// parallelize the stream
                                .parallel()
                                        
				// Create a new HSB pixel from RGB pixel
			        .mapToObj(pixel -> Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel), colorModel.getGreen(pixel), new float[3]))
				
                                // Return an array of float arrays containing the (hue,saturation,brightness) values for each image pixel        
                                .toArray(float[][]::new);        
                times.now(); //convert to HSB
               
                
                Map<Integer, Long> probabilityMap = 
                        
                                // get a stream of hsbPielArray
                                Arrays.stream(hsbPixelArray)
                                        
                                // parallelize the stream
                                .parallel()
                                
                                // Multiply each pixel's brightness value from its float[] array of (hue,saturation,brightness) with selected number of bins
                                // and convert to int, obtain IntStream from DoubleStream            
                                .mapToInt(hsb -> (int)(hsb[2]*256)  )
                                
                                // Convert to ObjectStream of Integers         
                                .mapToObj(Integer::new)
                                
                                // Count the number of brightness values falling into a bin and group them by the bin value        
                                .collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
                times.now(); // create brightness map                      
               
               // Store the count of brightness values falling in each of the total bins into an array 
               //by iterating through keySet of probabilityMap obtained 
               int[] probabilityArray= new int[257];
               for(int k: probabilityMap.keySet()){
			probabilityArray[k] = probabilityMap.get(k).intValue();
		}
		
                // Retrieve the total number of samples
                int total_values = Arrays.stream(probabilityArray).parallel().sum();
                
                Arrays.parallelPrefix( probabilityArray,(x,y) -> x+y);
                times.now(); // parallel prefix
                
                double[] cumulativeProbability = 
                                
                                // Obtain an IntStream of probabilityArray    
                                Arrays.stream(probabilityArray)
                                        
                                // parallelize the stream
                                .parallel()
                                                 
                                // Divide every element of prefix sum probabilityArray by total number of samples        
                                .mapToDouble( d -> (double) ((double)d / (double)total_values))
                        
                                // Obtain a double[] of cumulativeProbability        
                                .toArray();
                times.now(); // probability array
                
                
                int[] histRGBArray = 
                                
                                // Obtain a stream of hsbPixelArray    
                                Arrays.stream(hsbPixelArray)
                                        
                                // parallelize the stream
                                .parallel()
                        
                                // Convert to Intstream of RGB pixels equalizing each pixel's brightness with cumulativeProbability array values corresponding to their bin         
                                .mapToInt(pixel -> Color.HSBtoRGB( (float)Array.get(pixel,0), (float)Array.get(pixel,1), (float)cumulativeProbability[(int) ( 256 * (float)Array.get(pixel,2) )]))
                                        
                                // Obtain an int[] of RGB pixels        
                                .toArray();
                times.now(); // equalize pixels
                
                
                newImage.setRGB(0, 0, w, h, histRGBArray, 0, w);
		times.now(); // setRGB
                
		return times;
	}

}
