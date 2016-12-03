/*
package com.bridgelabz.docscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bridgelabz.docscanner.R;
import com.bridgelabz.docscanner.utility.IntentUtil;
import com.bridgelabz.docscanner.utility.UIUtil;

public class XON_Main_UI extends Activity {

    Button galleryBtn;
    private static final int PICK_FROM_FILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        galleryBtn = (Button) findViewById(R.id.gallery_btn);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIUtil.showShortMessage(XON_Main_UI.this, R.string.gallery_tip);
                IntentUtil.selectPicture(XON_Main_UI.this, PICK_FROM_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK) return;
        if (requestCode == PICK_FROM_FILE) {
            try {
                Uri imageUri = data.getData();
                processImage(imageUri);
                return;
            } catch(Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    public void processImage(Uri imageUri)
    {
        IntentUtil.processIntent(this, XONImageCropActivity.class, imageUri);
    }
}*/
