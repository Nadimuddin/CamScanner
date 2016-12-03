package com.bridgelabz.docscanner.interfaces;

import android.view.View;

/**
 * Created by bridgeit on 25/11/16.
 */

public interface XONClickListener {

    public void onClick(int actionBut);
    public void onOK(int dialogTitleResId, View customView);
    public void onCancel(int dialogTitleResId);
}
