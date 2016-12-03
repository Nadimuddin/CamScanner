package com.bridgelabz.docscanner.imagefilter;


import android.graphics.Bitmap;

/**
 * A filter to change the brightness and contrast of an image.
 */
public class ContrastFilter extends TransferFilter 
{
	private float brightness = 1.0f;
	private float contrast = 1.0f;
	
	protected float transferFunction( float f )
	{
		f = f*brightness;
		f = (f-0.5f)*contrast+0.5f;
		return f;
	}

    /**
     * Set the filter brightness.
     * @param brightness the brightness in the range 0 to 1
     * @min-value 0
     * @max-value 0
     */
	public void setBrightness(float brightness) 
	{
		this.brightness = brightness;
		initialized = false;
	}

    /**
     * Set the filter contrast.
     * @param contrast the contrast in the range 0 to 1
     * @min-value 0
     * @max-value 0
     */
	public void setContrast(float contrast) 
	{
		this.contrast = contrast;
		initialized = false;
	}

	public String toString()
	{
		return "Colors/Contrast...";
	}

	public static Bitmap changeBrightness (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		ContrastFilter briFltr = new ContrastFilter();

		//float flVal = (float) 2.6;
		float flVal = (float) 1.6;
		briFltr.setBrightness(flVal);
		int[] returnPixels = briFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;
	}


	public static Bitmap changeContrast (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		ContrastFilter conFltr = new ContrastFilter();

		//float flVal = (float) 1.2;
		float flVal = (float) 1.6;
		conFltr.setContrast(flVal);
		int[] returnPixels = conFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;

	}
}

