package com.bridgelabz.docscanner.imagefilter;

/**
 * A class containing static math methods useful for image processing.
 */
public class ImageMath {
	/**
	 * Apply a bias to a number in the unit interval, moving numbers towards 0 or 1
	 * according to the bias parameter.
	 * @param a the number to bias
	 * @param b the bias parameter. 0.5 means no change, smaller values bias towards 0, larger towards 1.
	 * @return the output value
	 */
	public static float bias(float a, float b) {
		return a/((1.0f/b-2)*(1.0f-a)+1);
	}

	/**
	 * A variant of the gamma function.
	 * @param a the number to apply gain to
	 * @param b the gain parameter. 0.5 means no change, smaller values reduce gain, larger values increase gain.
	 * @return the output value
	 */
	public static float gain(float a, float b) {

		float c = (1.0f/b-2.0f) * (1.0f-2.0f*a);
		if (a < 0.5)
			return a/(c+1.0f);
		else
			return (c-a)/(c-1.0f);
	}

	/**
	 * A smoothed step function. A cubic function is used to smooth the step between two thresholds.
	 * @param a the lower threshold position
	 * @param b the upper threshold position
	 * @param x the input parameter
	 * @return the output value
	 */
	public static float smoothStep(float a, float b, float x) {
		if (x < a)
			return 0;
		if (x >= b)
			return 1;
		x = (x - a) / (b - a);
		return x*x * (3 - 2*x);
	}

    public static int lerp(float t, int a, int b)
    {
        return (int)(a + t * (b - a));
    }

    /**
     * Linear interpolation of ARGB values.
     * @param t the interpolation parameter
     * @param rgb1 the lower interpolation range
     * @param rgb2 the upper interpolation range
     * @return the interpolated value
     */
    public static int mixColors(float t, int rgb1, int rgb2) {
        int a1 = (rgb1 >> 24) & 0xff;
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int a2 = (rgb2 >> 24) & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        a1 = lerp(t, a1, a2);
        r1 = lerp(t, r1, r2);
        g1 = lerp(t, g1, g2);
        b1 = lerp(t, b1, b2);
        return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
    }
}