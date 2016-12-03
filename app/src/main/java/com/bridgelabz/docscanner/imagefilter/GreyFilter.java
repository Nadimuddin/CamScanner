package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;

/**
 * A filter which 'grays out' an image by averaging each pixel with white.
 */
public class GreyFilter extends PointFilter {

	public GreyFilter() {
		canFilterIndexColorModel = true;
	}

	public int filterRGB(int x, int y, int rgb) {
		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = (r+255)/2;
		g = (g+255)/2;
		b = (b+255)/2;
		return a | (r << 16) | (g << 8) | b;
	}

	public String toString() {
		return "Colors/Gray Out";
	}

	public static Bitmap changeToGrey (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		GreyFilter grFltr = new GreyFilter();

		int[] returnPixels = grFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;
	}
}