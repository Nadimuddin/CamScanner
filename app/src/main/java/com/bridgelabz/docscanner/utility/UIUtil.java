package com.bridgelabz.docscanner.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.Toast;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.interfaces.XONClickListener;

import java.util.Vector;

/**
 * Created by bridgeit on 24/11/16.
 */

public class UIUtil
{
    public static Paint m_GPathPaint, m_GPathPointPaint;
    public static int PAINT_POINT_SIZE = 48;
    static {

      m_GPathPaint = new Paint();
      m_GPathPaint.setColor(Color.parseColor("#26A69A"));
      m_GPathPaint.setStrokeWidth(5);
      m_GPathPaint.setStyle(Paint.Style.STROKE);
      m_GPathPointPaint = new Paint();
      m_GPathPointPaint.setAntiAlias(true);
      m_GPathPointPaint.setDither(true);
      m_GPathPointPaint.setColor(Color.WHITE);
      m_GPathPointPaint.setStyle(Paint.Style.FILL);
      m_GPathPointPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
      m_GPathPointPaint.setStrokeCap(Paint.Cap.ROUND);
      m_GPathPointPaint.setStrokeWidth(PAINT_POINT_SIZE);// set the paint cap to round too

    }

    public static Vector<Point> drawRectPath(Canvas canvas, Rect rect, Vector<Point> pts,
                                             boolean drawPt)
    {
        if (rect == null) return null;
        if (pts == null) pts = new Vector<Point>();
        else pts.removeAllElements();
        pts.add(new Point(rect.left, rect.top));
        pts.add(new Point(rect.right, rect.top));
        pts.add(new Point(rect.right, rect.bottom));
        pts.add(new Point(rect.left, rect.bottom));

        drawLine(canvas, pts, true, drawPt);
        return pts;
    }

    public static void drawLine(Canvas canvas, Vector<Point> pts, boolean closePath, boolean drawPt)
    {
        Path path = new Path(); Point pt = null;
        for (int i = 0; i < pts.size(); i++) {
            pt = pts.elementAt(i);
            if (i == 0) path.moveTo(pt.x, pt.y);
            else path.lineTo(pt.x, pt.y);
            if (drawPt) canvas.drawPoint((float)pt.x, (float) pt.y, m_GPathPointPaint);
        }
        if (closePath) path.close();
        canvas.drawPath(path, m_GPathPaint);
    }

    public static Dialog createSingleChoiceDialog(Activity act, int iconResId, final int titResId,
                                                  int choiceResId, final XONClickListener listner)
    {
        return new AlertDialog.Builder(act)
                .setIcon(iconResId) .setTitle(titResId)
                .setSingleChoiceItems(choiceResId, 0,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            { listner.onClick(whichButton); }
                        })
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            { listner.onOK(titResId, null); }
                        })
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            { listner.onCancel(titResId); }
                        })
                .create();
    }

    public static void showShortMessage(Activity act, int rid)
    {
        showShortMessage(act, (String) act.getText(rid));
    }

    public static void showShortMessage(Activity act, String text)
    {
        Toast.makeText(act,  text, Toast.LENGTH_SHORT).show();
    }

    public static void showAckMesgDialog(Activity act, int iconResId, final int titleResId,
                                         String mesg, final XONClickListener listner)
    {
        new AlertDialog.Builder(act)
                .setIcon(iconResId) .setTitle(titleResId) .setMessage(mesg)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (listner != null) listner.onOK(titleResId, null);
                    }
                })
                .create().show();
    }
}