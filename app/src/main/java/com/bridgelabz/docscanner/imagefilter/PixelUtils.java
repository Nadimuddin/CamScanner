package com.bridgelabz.docscanner.imagefilter;

public class PixelUtils
{
  public static int clamp(int c)
  {
    if (c < 0) return 0;
    if (c > 255) return 255;
    return c;
  }

  public static boolean filterRGB(float margin, int imgWt, int imgHt, int icol, int irow)
  {
     if (margin <= 0.0) return true;
     double fadeWt = margin*imgWt, fadeHt = margin*imgHt;
     if (fadeWt > fadeHt) fadeWt = fadeHt; else fadeHt = fadeWt;
     double newWt = (double)imgWt - fadeWt, newHt = (double)imgHt - fadeHt;
     double dxFade = fadeWt/2.0D, dyFade = fadeHt/2.0D;
     if ((irow > dyFade && icol > dxFade) && 
         (irow < (dyFade+newHt) && icol < (dxFade+newWt))) return false;
     return true;
  }

    public static int brightness(int rgb) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
}