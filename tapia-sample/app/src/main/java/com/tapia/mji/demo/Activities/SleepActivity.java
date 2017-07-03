package com.tapia.mji.demo.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.tapia.mji.demo.Actions.Sleep;
import com.tapia.mji.demo.Actions.WakeUp;
import com.tapia.mji.demo.R;
import com.tapia.mji.tapialib.Actions.Action;
import com.tapia.mji.tapialib.Activities.TapiaActivity;
import com.tapia.mji.tapialib.Exceptions.LanguageNotSupportedException;
import com.tapia.mji.tapialib.Providers.Interfaces.NLUProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.STTProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.TTSProvider;
import com.tapia.mji.tapialib.TapiaApp;
import com.tapia.mji.tapialib.Utils.TapiaAnimation;
import com.tapia.mji.tapialib.Utils.TapiaAudio;
import com.tapia.mji.tapialib.Utils.TapiaRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sami on 12-Jul-16.
 */
public class SleepActivity extends TapiaActivity {
    TapiaAnimation tapiaAnimation;
    SharedPreferences preferences;


    STTProvider.OnRecognitionCompleteListener onRecognitionCompleteListener = null;
    List<Action> actions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eyes_layout);
        ImageView tapiaEyes = (ImageView) findViewById(R.id.eyes);
        TapiaAudio.setVolume(this, TapiaAudio.getCurrent(),false);
        tapiaAnimation = new TapiaAnimation(this,tapiaEyes);
        tapiaEyes.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(activity,PhotoMenuActivity.class));
                return false;
            }
        });

        sttProvider = TapiaApp.currentLanguage.getOnlineSTTProvider();
        ttsProvider = TapiaApp.currentLanguage.getTTSProvider();
        onlineNLUProvider = TapiaApp.currentLanguage.getOnlineNLUProvider();
        offlineNLUProvider = TapiaApp.currentLanguage.getOfflineNLUProvider();

//      WakeUp
        actions.add(new WakeUp(new WakeUp.OnThisActionListener() {
            @Override
            public void process() {
                startActivity(new Intent(activity,TalkActivity.class));
            }
        }));
    }


    @Override
    protected void onResume() {
        super.onResume();
        tapiaAnimation.startAnimation(TapiaAnimation.EXHAUSTED,true);
        TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.DOWN,200,null);



        onRecognitionCompleteListener = new STTProvider.OnRecognitionCompleteListener() {
            @Override
            public void onRecognitionComplete(final List<String> results) {
                sttProvider.stopListening();
                offlineNLUProvider.setOnAnalyseCompleteListener(new NLUProvider.OnAnalyseCompleteListener() {
                    @Override
                    public void OnAnalyseComplete(Action action) {
                        if (action == null) {
                            sttProvider.listen();
                        }
                    }
                });
                offlineNLUProvider.analyseText(results,actions);
            }
        };
        sttProvider.setOnRecognitionCompleteListener(onRecognitionCompleteListener);
        sttProvider.listen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sttProvider.stopListening();
        tapiaAnimation.stopAnimation();
        TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.UP,15,null);
    }
}
