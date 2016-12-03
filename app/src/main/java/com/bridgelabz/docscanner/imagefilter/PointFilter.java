package com.bridgelabz.docscanner.imagefilter;

/**
 * An abstract superclass for point filters. The interface is the same as the old RGBImageFilter.
 */
public abstract class PointFilter 
{
   protected int width;
   protected int height;
	
   // This specifies either the Border Margin or Fade Margin in percentage
   public float margin = 0.0f;

   protected boolean canFilterIndexColorModel = false;

   // Child Class can overwrite this function if needed. This will be the method
	// first called for doing pre analysis of filter param
	public void initialize( int[] src ,int w, int h) {}
	
	public int[] filter( int[] src ,int w, int h) 
   {
		int[] inPixels = new int[w];
		int[] outPixels = new int[w*h];

		return filter(src , w, h, inPixels, outPixels);
   }

   public int[] filter( int[] src ,int w, int h, int[] inPixels, int[] outPixels) 
   {
      width = w;
      height = h;
      initialize(src, w, h);
      setDimensions( width, height);
      if (inPixels == null) inPixels = new int[width];
      if (outPixels == null) outPixels = new int[width*height];
              
      for ( int y = 0; y < height; y++ ) 
      {
         int index = 0;
         for(int i=(y*width);i<((y*width) + width);++i){
            inPixels[index] = src[i];
            index++;
         }
         
         for ( int x = 0; x < width; x++ ){
            if (PixelUtils.filterRGB(margin, width, height, x, y))
               inPixels[x] = filterRGB( x, y, inPixels[x] );
         }        
         
         index = 0;
         for(int i=(y*width);i<((y*width) + width);++i){
            outPixels[i] = inPixels[index];
            index++;
         }        
      }

      return outPixels;
   }
    
	public void setDimensions(int w, int h) {
		width = w;
    	height = h;
	}

	public abstract int filterRGB(int x, int y, int rgb);
}