package com.tapia.mji.demo.Activities;


import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tapia.mji.demo.Common.Const;
import com.tapia.mji.demo.R;
import com.tapia.mji.tapialib.Activities.TapiaActivity;
import com.tapia.mji.tapialib.Languages.Language;
import com.tapia.mji.tapialib.TapiaApp;
import com.tapia.mji.tapialib.Utils.TapiaAnimation;
import com.tapia.mji.tapialib.Utils.TapiaAudio;

import java.io.IOException;

/**
 * Created by Sami on 30-Jun-16.
 */
public class SettingActivity extends TapiaActivity {

    TapiaAnimation tapiaAnimation;
    SharedPreferences preferences;

    EditText mUdpIpEditText;

    Button mBackBtn;
    Button mUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        mUdpIpEditText = (EditText)findViewById(R.id.mUdpIpEditText);

        mUdpIpEditText.setText(Const.UDP_IP);

        mBackBtn = (Button)findViewById(R.id.mBackBtn);
        mUpdateBtn = (Button)findViewById(R.id.mUpdateBtn);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

    }

    private void update(){
        Const.setIP(mUdpIpEditText.getText().toString());
    }
}
