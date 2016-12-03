package com.bridgelabz.docscanner.imagefilter;

public abstract class TransferFilter extends PointFilter 
{

	protected int[] rTable, gTable, bTable;
	protected boolean initialized = false;
	
	public TransferFilter() 
	{
		canFilterIndexColorModel = true;
	}

	public int filterRGB(int x, int y, int rgb) 
	{
		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = rTable[r];
		g = gTable[g];
		b = bTable[b];
		return a | (r << 16) | (g << 8) | b;
	}

	public int[] filter( int[] src ,int w, int h) 
	{
		if (!initialized)
			initialize();
		
		return super.filter( src, w, h );
	}

   public int[] filter( int[] src ,int w, int h, int[] inPixels, int[] outPixels ) 
   {
      if (!initialized)
         initialize();
      
      return super.filter( src, w, h, inPixels, outPixels );
   }

	protected void initialize() 
	{
		initialized = true;
		rTable = gTable = bTable = makeTable();
	}

	protected int[] makeTable() 
	{
		int[] table = new int[256];
		for (int i = 0; i < 256; i++)
			table[i] = PixelUtils.clamp( (int)( 255 * transferFunction( i / 255.0f ) ) );
		return table;
	}

	protected float transferFunction( float v ) 
	{
		return 0;
	}
}