package com.bridgelabz.docscanner.imagefilter;

import android.graphics.Bitmap;

/**
 * A filter which changes the gain and bias of an image - similar to ContrastFilter.
 */
public class GainFilter extends TransferFilter {

	private float gain = 0.5f;
	private float bias = 0.5f;
	
	protected float transferFunction( float f ) {
		f = ImageMath.gain(f, gain);
		f = ImageMath.bias(f, bias);
		return f;
	}

    /**
     * Set the gain.
     * @param gain the gain
     * @min-value: 0
     * @max-value: 1
     */
	public void setGain(float gain) {
		this.gain = gain;
		initialized = false;
	}

    /**
     * Set the bias.
     * @param bias the bias
     * @min-value: 0
     * @max-value: 1
     */
	public void setBias(float bias) {
		this.bias = bias;
		initialized = false;
	}

	public String toString() {
		return "Colors/Gain...";
	}

	public static Bitmap changeToBias (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		GainFilter biasFltr = new GainFilter();

		float flVal = (float) 0.8;
		biasFltr.setBias(flVal);
		int[] returnPixels = biasFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;
	}

	public static Bitmap changeToGain (Bitmap bitmap) {
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		GainFilter gainFltr = new GainFilter();

		float flVal = (float) 0.4;
		gainFltr.setGain(flVal);
		int[] returnPixels = gainFltr.filter(pixels, width, height, null, null);
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);

		return returnBitmap;
	}
}