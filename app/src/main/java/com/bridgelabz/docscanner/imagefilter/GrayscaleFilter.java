package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;

/**
 * A filter which converts an image to grayscale using the NTSC brightness calculation.
 */
public class GrayscaleFilter extends PointFilter 
{
   
	public GrayscaleFilter() 
	{
		canFilterIndexColorModel = true;
	}

	public int filterRGB(int x, int y, int rgb) 
	{
	   int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		rgb = (r * 77 + g * 151 + b * 28) >> 8;	// NTSC luma
		return a | (rgb << 16) | (rgb << 8) | rgb;
	}

	public String toString() {
		return "Colors/Grayscale";
	}

	public static Bitmap changeToGrayScale (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		GrayscaleFilter gsFltr = new GrayscaleFilter();

		int[] returnPixels = gsFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;
	}
}


