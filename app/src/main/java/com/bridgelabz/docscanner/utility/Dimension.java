package com.bridgelabz.docscanner.utility;

/**
 * Created by bridgeit on 25/11/16.
 */

public class Dimension
{
    public int width = 0;
    public int height = 0;

    public Dimension() { width = 0; height = 0; }
    public Dimension(int w, int h) { width = w; height = h; }
    public String toString() { return "Width: "+width+" Height: "+height; }
}
