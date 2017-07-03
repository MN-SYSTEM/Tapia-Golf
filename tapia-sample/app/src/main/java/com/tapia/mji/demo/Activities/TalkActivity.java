package com.tapia.mji.demo.Activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.m_and_n.util.TCP;
import com.m_and_n.util.UDPconnect;
import com.tapia.mji.demo.Actions.FavoriteFood;
import com.tapia.mji.demo.Actions.Fight;
import com.tapia.mji.demo.Actions.GiveDate;
import com.tapia.mji.demo.Actions.GiveTime;
import com.tapia.mji.demo.Actions.Greeting;
import com.tapia.mji.demo.Actions.HairCutNo;
import com.tapia.mji.demo.Actions.HairCutYes;
import com.tapia.mji.demo.Actions.HowAreYou;
import com.tapia.mji.demo.Actions.Like;
import com.tapia.mji.demo.Actions.No;
import com.tapia.mji.demo.Actions.NotLike;
import com.tapia.mji.demo.Actions.Recognize;
import com.tapia.mji.demo.Actions.Rotate;
import com.tapia.mji.demo.Actions.Sex;
import com.tapia.mji.demo.Actions.Sleep;
import com.tapia.mji.demo.Actions.Sorry;
import com.tapia.mji.demo.Actions.Thanks;
import com.tapia.mji.demo.Actions.Yes;
import com.tapia.mji.demo.Common.Const;
import com.tapia.mji.demo.R;
import com.tapia.mji.tapialib.Actions.Action;
import com.tapia.mji.tapialib.Activities.TapiaActivity;
import com.tapia.mji.tapialib.Exceptions.LanguageNotSupportedException;
import com.tapia.mji.tapialib.Languages.Language;
import com.tapia.mji.tapialib.Providers.Interfaces.NLUProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.STTProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.TTSProvider;
import com.tapia.mji.tapialib.TapiaApp;
import com.tapia.mji.tapialib.Utils.TapiaAnimation;
import com.tapia.mji.tapialib.Utils.TapiaCalendar;
import com.tapia.mji.tapialib.Utils.TapiaResources;
import com.tapia.mji.tapialib.Utils.TapiaRobot;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Sami on 06-Jul-16.
 */
public class TalkActivity extends TapiaActivity implements SurfaceHolder.Callback,UDPconnect.UDPListener,TCP.TCPListener {

    TapiaAnimation tapiaAnimation;
    List<Action> actions = new ArrayList<>();
    boolean isFirstTime = true;
    TTSProvider.OnStateChangeListener onTTSstateListener;

    Camera mCamera = null;
    SurfaceView mSurfaceView;

    boolean faceExist = false;

    HandlerThread ht = null;

    TCP tcp = null;
    Socket socket = null;

    boolean tcpWait = false;

    final Camera.PictureCallback getJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if(mCamera == null) return;

            camera.startPreview();
            Log.d("TCP","aaa");
            if(socket != null){

                try {
                    Log.d("TCP","SOCKET SEND");
                    OutputStream out = socket.getOutputStream();
                    out.write(data);
                    out.flush();

                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        setContentView(R.layout.activity_talk);

        mSurfaceView = (SurfaceView)findViewById(R.id.previewView);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TapiaApp.getAppContext(),PhotoTakeActivity.class));
            }
        });

        ImageView tapiaEyes = (ImageView) findViewById(R.id.eyes);
        tapiaAnimation = new TapiaAnimation(this, tapiaEyes);
        tapiaEyes.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return false;
            }
        });
        TapiaApp.setCurrentLanguage(Language.LanguageID.JAPANESE);

        sttProvider = TapiaApp.currentLanguage.getOnlineSTTProvider();
        ttsProvider = TapiaApp.currentLanguage.getTTSProvider();
        onlineNLUProvider = TapiaApp.currentLanguage.getOnlineNLUProvider();
        offlineNLUProvider = TapiaApp.currentLanguage.getOfflineNLUProvider();


        onTTSstateListener = new TTSProvider.OnStateChangeListener() {
            @Override
            public void onStateChange(TTSProvider.State newState) {
                switch (newState) {
                    case IDLE:
                        if (sttProvider.getSTTState() == STTProvider.State.IDLE)
                            tapiaAnimation.setBackground(R.drawable.gradient_aqua);
                        break;
                    case SPEAKING:
                        tapiaAnimation.setBackground(R.drawable.gradient_yellow);
                        break;
                }
            }
        };

        actions.add(new GiveDate(new GiveDate.OnGiveDateListener() {
            @Override
            public void onGiveDate(Date date) {
                try {
                    ttsProvider.ask(String.format(getString(R.string.givedate_sentence0), TapiaApp.currentLanguage.getDateString(new Date())), sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }

            }
        }));

        actions.add(new GiveTime(new GiveTime.OnGiveTimeListener() {
            @Override
            public void onGiveTime(Date time) {
                try {
                    ttsProvider.ask(String.format(getString(R.string.givetime_sentence0), TapiaCalendar.strHour(), TapiaCalendar.strMinute()), sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }

            }
        }));

        actions.add(new Rotate(new Rotate.OnRotateListener() {
            @Override
            public void onRotate(TapiaRobot.RotateOrientation orientation, int degree) {
                String speech = getString(R.string.rotate_sentence0);
                String direction;
                switch (orientation){
                    case LEFT:
                        direction = getString(R.string.direction_left0);
                        break;
                    case RIGHT:
                        direction = getString(R.string.direction_right0);
                        break;
                    case UP:
                        direction = getString(R.string.direction_up0);
                        break;
                    case DOWN:
                        direction = getString(R.string.direction_down0);
                        break;
                    default:
                        direction = "";
                        break;
                }
                try {
                    ttsProvider.ask(String.format(speech, direction), sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
                TapiaRobot.rotate(activity, orientation, degree,null);
            }
        }));





//        竹内追加分
//      Greeting
        actions.add(new Greeting(new Greeting.OnThisActionListener() {
            @Override
            public void process() {
                Calendar calendar = Calendar.getInstance(Locale.JAPAN);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                String greeting = "";
                if ( 4 < hour && hour < 12){    //おはよう
                    greeting = "おはようございます。";
                }else if( 12 <= hour && hour < 18 ){    //こんにちは
                    greeting = "こんにちわ。";
                }else{  //こんばんは
                    greeting = "こんばんわ。";
                }
                try {
                    if(faceExist){
                        ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                            @Override
                            public void onSpeechComplete() {
                                startActivity(new Intent(TapiaApp.getAppContext(),PhotoTakeActivity.class));
                                ttsProvider.setOnSpeechCompleteListener(null);
                            }
                        });
                        greeting += "認証を開始します。";
                    }
                    ttsProvider.ask(greeting,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // Sleep
        actions.add(new Sleep(new Sleep.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            finish();
                            ttsProvider.setOnSpeechCompleteListener(null);
                        }
                    });
                    ttsProvider.say("おやすみなさい");

                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // Sorry
        actions.add(new Sorry(new Sorry.OnThisActionListener() {
            @Override
            public void process(List<String> result) {
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            finish();
                            ttsProvider.setOnSpeechCompleteListener(null);
                        }
                    });
                    ttsProvider.say("申し訳ありません。静かにしてます。");
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // HowAreYou
        actions.add(new HowAreYou(new HowAreYou.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "ぼちぼちでんな。",
                            "そこそこですよ。",
                            "悪くないですよ",
                            "ごらんのとおりです",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // Favorite Food
        actions.add(new FavoriteFood(new FavoriteFood.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "私はゆで卵が好きですよ。",
                            "こう見えてたまごが好きですよ。",
                            "ここのラーメンおいしいですよ",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // Sex
        actions.add(new Sex(new Sex.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "どちらでもありませんよ。",
                            "見てわかりませんか？",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // Like
        actions.add(new Like(new Like.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "私は好きですよ。",
                            "そうなんですね",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // Not Like
        actions.add(new NotLike(new NotLike.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "私は好きですよ。",
                            "そうなんですね",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // Yes
        actions.add(new Yes(new Yes.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "いい声ですね。",
                            "そうなんですね",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // No
        actions.add(new No(new No.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    String say = randomResponse(new String[]{
                            "いい声ですね。",
                            "そうなんですね",
                    });
                    ttsProvider.ask(say,sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // Yes
        actions.add(new HairCutYes(new HairCutYes.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.ask("やっぱりですか？",sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));


        // No
        actions.add(new HairCutNo(new HairCutNo.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.ask("あれ？気のせいでしたか",sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // FIGHT
        actions.add(new Fight(new Fight.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.ask("あなたならやれますよ！",sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // THANKS
        actions.add(new Thanks(new Thanks.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.ask("どういたしまして！",sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

        // RECOGNIZE
        actions.add(new Recognize(new Recognize.OnThisActionListener() {
            @Override
            public void process() {
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            startActivity(new Intent(TapiaApp.getAppContext(),PhotoTakeActivity.class));
                            ttsProvider.setOnSpeechCompleteListener(null);
                        }
                    });
                    ttsProvider.ask("受付を開始します。",sttProvider);
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }));

    }


    STTProvider.OnRecognitionCompleteListener onRecognitionCompleteListener = null;


    @Override
    protected void onResume() {
        super.onResume();
        final String startString;

        onRecognitionCompleteListener = new STTProvider.OnRecognitionCompleteListener() {
            @Override
            public void onRecognitionComplete(final List<String> results) {
                offlineNLUProvider.setOnAnalyseCompleteListener(new NLUProvider.OnAnalyseCompleteListener() {
                    @Override
                    public void OnAnalyseComplete(Action action) {
                        if (action == null) {
                            tapiaAnimation.stopAnimation();
//                            tapiaAnimation.startAtFrameEndAtFrame(TapiaAnimation.CONFUSED,true,26,89);
                            ttsProvider.setOnStateChangeListener(null);
//                            tapiaAnimation.setBackground(R.drawable.gradient_pink);
                            try {
                                String say = "";
                                Random r = new Random();
                                int n = r.nextInt(10) + 1;
                                switch(n){
                                    case 1:
                                        say = "今日も頑張ってください。";
                                        break;
                                    case 2:
                                        say = "映画は好きですか？";
                                        break;
                                    case 3:
                                        say = "お酒は好きですか？";
                                        break;
                                    case 4:
                                        say = "素敵なファッションですね";
                                        break;
                                    case 5:
                                        say = "ゴルフは好きですか？";
                                        break;
                                    case 6:
                                        say = "音楽は好きですか？";
                                        break;
                                    case 7:
                                        say = "ゆで卵は好きですか？";
                                        break;
                                    case 8:
                                        say = "あれ？髪切りました？";
                                        break;
                                    case 9:
                                        say = "何かいいことありました？";
                                        break;
                                    case 10:
                                        say = "ここの食堂おいしいですよね";
                                        break;
//                                    case 11:
//                                        say = "";
//                                        break;
//                                    case 12:
//                                        say = "";
//                                        break;
//                                    case 13:
//                                        say = "";
//                                        break;
//                                    case 14:
//                                        say = "";
//                                        break;
//                                    case 15:
//                                        say = "";
//                                        break;
//                                    case 16:
//                                        say = "";
//                                        break;
//                                    case 17:
//                                        say = "";
//                                        break;
//                                    case 18:
//                                        say = "";
//                                        break;
//                                    case 19:
//                                        say = "";
//                                        break;
//                                    case 20:
//                                        say = "";
//                                        break;
//                                    case 21:
//                                        say = "";
//                                        break;
//                                    case 22:
//                                        say = "";
//                                        break;
//                                    case 23:
//                                        say = "";
//                                        break;
//                                    case 24:
//                                        say = "";
//                                        break;
//                                    case 25:
//                                        say = "";
//                                        break;
//                                    case 26:
//                                        say = "";
//                                        break;
//                                    case 27:
//                                        say = "";
//                                        break;
//                                    case 28:
//                                        say = "";
//                                        break;
//                                    case 29:
//                                        say = "";
//                                        break;
//                                    case 30:
//                                        say = "";
//                                        break;
                                    default:
                                        say = "もう一度お願いします。";
                                        break;

                                }
                                ttsProvider.ask(say,sttProvider);
                            } catch (LanguageNotSupportedException e) {
                                e.printStackTrace();
                            }
                            ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                                @Override
                                public void onSpeechComplete() {
                                    tapiaAnimation.startAnimation(TapiaAnimation.PLAIN,true);
                                    tapiaAnimation.setBackground(0);
                                    ttsProvider.setOnStateChangeListener(onTTSstateListener);
                                }
                            });
                        }
                    }
                });
                offlineNLUProvider.analyseText(results,actions);
            }
        };

        ttsProvider.setOnStateChangeListener(onTTSstateListener);
        sttProvider.setOnStateChangeListener(new STTProvider.OnStateChangeListener() {
            @Override
            public void onStateChange(STTProvider.State newState) {
                switch (newState){
                    case IDLE:
                        if(ttsProvider.getTTSState() == TTSProvider.State.IDLE) {
                            tapiaAnimation.setBackground(0);
                        }
                        break;
                    case LISTENING:
                        tapiaAnimation.setBackground(R.drawable.gradient_aqua);
                        break;
                    case PROCESSING:
                        tapiaAnimation.setBackground(R.drawable.gradient_defult);
                        break;
                }
            }
        });

        sttProvider.setOnRecognitionCompleteListener(onRecognitionCompleteListener);



        sttProvider.setOnTimeOutListener(new STTProvider.OnTimeOutListener() {
            @Override
            public void OnTimeOut() {
                finish();
            }
        });
        if(isFirstTime) {
            startString = TapiaResources.getRandStr(this,"service_hello",0);
            isFirstTime = false;
            tapiaAnimation.reverseStartAtFrame(TapiaAnimation.TRANSITION1 ,false,16);
            tapiaAnimation.setOnAnimationEndListener(new TapiaAnimation.OnAnimationEndListener() {
                @Override
                public void animationEnded() {
                    tapiaAnimation.startAnimation(TapiaAnimation.PLAIN,true);
                    tapiaAnimation.setBackground(R.drawable.gradient_aqua);
                    try {
                        ttsProvider.ask(startString,sttProvider);
                    } catch (LanguageNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            startString = TapiaResources.getRandStr(this,"service_offerHelp",1);
            try {
                ttsProvider.ask(startString,sttProvider);
            } catch (LanguageNotSupportedException e) {
                e.printStackTrace();
            }
            tapiaAnimation.startAnimation(TapiaAnimation.PLAIN,true);
        }



//        Camera
        if(ht == null){
            ht = new HandlerThread("bgThread");
            ht.start();
        }else{
            if(!ht.isAlive()){
                ht.start();
            }
        }




        mCamera = Camera.open(0);

        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                Log.d("Face","Face");
                if(!faceExist){
                    faceExist = true;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(faceExist){
                                faceExist = false;
                            }
                        }
                    },300);
                }

            }
        });


        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });


        if(tcp == null){
            tcp = new TCP(this);
            tcp.connect();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        tcp.disconnect();

        if(ht != null){
            ht.quitSafely();
        }
        ht = null;

        tapiaAnimation.stopAnimation();


        try {

            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.setPreviewTexture(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCamera.setFaceDetectionListener(null);
        mCamera.setPreviewCallback(null);

        mCamera.release();
        mCamera = null;
    }


    private String randomResponse(String[] responses){
        int length = responses.length;
        String response = "";
        Random r = new Random();
        int n = r.nextInt(length);

        return responses[n];
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
            mCamera.startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    @Override
    public void send(String text) {

    }

    @Override
    public void receive(String text) {

    }

    @Override
    public void sendByte(byte[] text) {
    }

    @Override
    public void receiveByte(byte[] text) {
    }

    @Override
    public void socket(Socket socket) {

        this.socket = socket;
        Log.d("TCP","socket");
        try {
            InputStream is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            try{

                if(dis.readInt() == 1 && !tcpWait){
                    tcpWait = true;
                    mCamera.takePicture(null,null,getJpegCallback);
                    Handler h = new Handler(ht.getLooper());
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tcpWait = false;
                        }
                    },100);
                }

            }catch(Exception e){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
