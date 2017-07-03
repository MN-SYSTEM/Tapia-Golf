package com.tapia.mji.demo.Activities;

;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.m_and_n.util.HttpRequest;
import com.m_and_n.util.JsonUtil;
import com.m_and_n.view.FaceDetectView;
import com.tapia.mji.demo.Actions.No;
import com.tapia.mji.demo.Actions.Yes;
import com.tapia.mji.demo.R;
import com.tapia.mji.tapialib.Actions.Action;
import com.tapia.mji.tapialib.Activities.TapiaActivity;
import com.tapia.mji.tapialib.Exceptions.LanguageNotSupportedException;
import com.tapia.mji.tapialib.Languages.Language;
import com.tapia.mji.tapialib.Providers.Interfaces.NLUProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.STTProvider;
import com.tapia.mji.tapialib.Providers.Interfaces.TTSProvider;
import com.tapia.mji.tapialib.TapiaApp;
import com.tapia.mji.tapialib.Utils.TapiaRobot;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sami on 08-Jul-16.
 */
public class PhotoTakeActivity extends TapiaActivity  implements HttpRequest.OnHttpRequestResultListener,SurfaceHolder.Callback {
    Handler mHandler;
    SurfaceView previewView;

    Point displaySize = null;


//    顔認証演出用
    Camera mCamera = null;
    FaceDetectView faceDetectView;

//    名前確認用
    TextView nameTextView;

//    予約情報確認用
    TableLayout reserveInfo;
    TextView name;
    TextView created;
    TextView inout;
    TextView start;
    TextView groupNum;
    TextView playerNum;
    TextView comment;

    Button backBtn;

    List<Action> actionListRepeat;


    HandlerThread mHandlerThread;

    int faceDetectCnt = 0;

//    HttpRequest
    HttpRequest mHttpRequest = new HttpRequest("http://hakodateparkcc.com/api/faceDetect.php");

    static final int WAIT_MODE          = 0x0001;
    static final int DETECTING_MODE     = 0x0002;
    static final int RECOGNIZING_MODE   = 0x0004;
    static final int REPEAT_MODE        = 0x0008;
    static final int CONFIRM_MODE       = 0x0010;

    int mode = 0x0001;

    boolean isShowingOption = false;

    STTProvider.OnRecognitionCompleteListener onRecognitionCompleteListener = null;
    List<Action> actions = new ArrayList<>();

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.d("MODE","onCreate");

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        displaySize = new Point();
        disp.getSize(displaySize);


        this.mHttpRequest.setOnHttpRequestResultListener(this);

        setContentView(R.layout.activity_takephoto);

        mImageView = (ImageView)findViewById(R.id.mLoadingImageView);
        mImageView.setImageResource(R.drawable.loading);

        TapiaApp.setCurrentLanguage(Language.LanguageID.JAPANESE);

        sttProvider = TapiaApp.currentLanguage.getOnlineSTTProvider();
        ttsProvider = TapiaApp.currentLanguage.getTTSProvider();

        onlineNLUProvider = TapiaApp.currentLanguage.getOnlineNLUProvider();
        offlineNLUProvider = TapiaApp.currentLanguage.getOfflineNLUProvider();

//        YES
        actions.add(new Yes(new Yes.OnThisActionListener() {
            @Override
            public void process() {
               tapYes();
            }
        }));
//        NO
        actions.add(new No(new No.OnThisActionListener() {
            @Override
            public void process() {
                tapNo();
            }
        }));

        previewView = (SurfaceView) findViewById(R.id.previewView);
        previewView.setDrawingCacheEnabled(true);
        previewView.getHolder().addCallback(this);

        previewView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return false;
            }
        });



        actionListRepeat = new ArrayList<>();

        faceDetectView = (FaceDetectView) findViewById(R.id.faceDetectView);

//        名前確認用
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setVisibility(View.GONE);

//        予約情報確認用
        reserveInfo = (TableLayout)findViewById(R.id.reserveInfo);

        name = (TextView)findViewById(R.id.name);
        created = (TextView)findViewById(R.id.created);
        inout = (TextView)findViewById(R.id.inout);
        start = (TextView)findViewById(R.id.start);
        groupNum = (TextView)findViewById(R.id.groupNum);
        playerNum = (TextView)findViewById(R.id.playerNum);
        comment = (TextView)findViewById(R.id.comment);

        backBtn = (Button)findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                    @Override
                    public void onSpeechComplete() {
                        finish();
                    }
                });
                try {
                    ttsProvider.say("受付を完了いたしました。楽しんでくださいね。");
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapYes();
            }
        });

        findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tapNo();
            }
        });


    }

    @Override
    public void resultOnBackgroundThread(String result) {
    }

    @Override
    public void resultOnMainThread(String result) {
        recognitionComplete(result);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        try {
            mCamera.setPreviewDisplay(previewView.getHolder());
            mCamera.startPreview();
            mCamera.startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("MODE","onResume");

        if( mHandlerThread != null){
            mHandlerThread.quitSafely();
        }
        mHandlerThread = new HandlerThread("faceDetectThread");
        mHandlerThread.start();


//      カメラスタート
        mCamera = Camera.open(0);

//      顔検出コールバック設定
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                faceDetection(faces);
            }
        });

//      Previewコールバック設定
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                preview(data);
            }
        });

        sttProvider.setOnTimeOutListener(new STTProvider.OnTimeOutListener() {
            @Override
            public void OnTimeOut() {
                Log.d("OnTimeOutListener","TIMEOUT");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MODE","onPause");
        try {
            mCamera.setFaceDetectionListener(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.release();
        mCamera = null;
        if( mHandlerThread != null){
           if( mHandlerThread.quitSafely() ) mHandlerThread = null;
        }

    }

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
            camera.startFaceDetection();
            String base64Enc = Base64.encodeToString(data,Base64.URL_SAFE | Base64.NO_WRAP);
            mHttpRequest.setQuery("face="+base64Enc+"&type=jpeg");
            mHttpRequest.post();
        }
    };


    boolean isRotation = false;

    TapiaRobot.OnRotationFinishListener rotateFinishListener = new TapiaRobot.OnRotationFinishListener() {
        @Override
        public void onRotationFinish() {
            if(mHandlerThread != null && mHandlerThread.isAlive()){
                Handler handle = new Handler(mHandlerThread.getLooper());
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRotation = false;
                    }
                },100);
            }

        }
    };

    private void rotate(Camera.Face face){
        if(isRotation) return;

        RectF rect = new RectF(face.rect);
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postScale(displaySize.x / 2000f, displaySize.y / 2000f);
        matrix.postTranslate(displaySize.x / 2f, displaySize.y / 2f);
        matrix.mapRect(rect);


        float centerX = rect.centerX();
        float centerY = rect.centerY();

        if(centerX > 820){
            isRotation = true;
            TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.RIGHT,6,rotateFinishListener);
        }else if(centerX < 420){
            isRotation = true;
            TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.LEFT,6,rotateFinishListener);
        }

        if(centerY > 400){
            isRotation = true;
            TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.DOWN,6,rotateFinishListener);
        }else if(centerY < 200){
            isRotation = true;
            TapiaRobot.rotate(activity, TapiaRobot.RotateOrientation.UP,6,rotateFinishListener);
        }
    }

//    顔検出
    private void faceDetection(Camera.Face[] faces){
        if( faces.length == 0 || faces[0] == null){
            faceDetectCnt = 0;
            faceDetectView.setCameraFace(null);
            if(mode == DETECTING_MODE){
                mode = WAIT_MODE;
            }
            return;
        }

        Camera.Face face = faces[0];

        switch(mode){
            case WAIT_MODE:
                Log.d("MODE","WAIT_MODE");
                mode = DETECTING_MODE;
                faceDetectCnt = 0;
                break;
            case DETECTING_MODE:
                Log.d("MODE","DETECTING_MODE");
                faceDetectView.setCameraFace(face);
                rotate(face);
                faceDetectCnt++;
                break;
            case RECOGNIZING_MODE:
                Log.d("MODE","RECOGNIZING_MODE");
                faceDetectView.setCameraFace(face);
                rotate(face);
                faceDetectCnt = 0;
                break;
            case REPEAT_MODE:
                Log.d("MODE","REPEAT_MODE");
                faceDetectView.setCameraFace(null);
                break;
            case CONFIRM_MODE:
                Log.d("MODE","CONFIRM_MODE");
                break;
        }

    }

    private void preview(byte[] data){
        if(faceDetectCnt > 50 && mode == DETECTING_MODE){
            recognition(data);
        }
    }


    private void recognition(final byte[] data){
        if(mode != DETECTING_MODE) return;
//        認証モード
        mode = RECOGNIZING_MODE;
        //認証
        mHandler = new Handler(mHandlerThread.getLooper());
        try {
            ttsProvider.say("予約を確認いたします。");
            startLoading();
        } catch (LanguageNotSupportedException e) {
            e.printStackTrace();
        }
        ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCamera.takePicture(null,null,jpegCallback);
                    }
                });
                ttsProvider.setOnSpeechCompleteListener(null);
            }
        });
    }

    private void recognitionComplete(String result){

        stopLoading();

        if(!JsonUtil.isJSON(result)) {
            mode = WAIT_MODE;
            return;
        }

        Map<String,Object> map = JsonUtil.toMap(result);

        if(map.containsKey("error")){
            mode = REPEAT_MODE;
            try {
                ttsProvider.say("お客様の予約を確認できませんでした。もう一度やりなおしますか？");
            } catch (LanguageNotSupportedException e) {
                e.printStackTrace();
            }
            ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                @Override
                public void onSpeechComplete() {
                    ttsProvider.setOnSpeechCompleteListener(null);
                    showYesNoBtn();
                }
            });
        }else{
//          User情報
            Map<String,Object> user = (Map<String,Object>) map.get("user");
            String userName = (String)user.get("name");
            String yomi = (String)user.get("name_yomi");
            Number userId = (Number)user.get("id");

//          Reserve情報
            Map<String,Object> reserve = (Map<String,Object>) map.get("reserve");

            nameTextView.setText(userName+"様ですね？");
            nameTextView.setVisibility(View.VISIBLE);

            name.setText(userName);

            created.setText((String)reserve.get("created"));
            inout.setText((String)reserve.get("inout"));
            start.setText((String)reserve.get("start"));
            groupNum.setText( (String)reserve.get("sets") );
            playerNum.setText( ((Number)reserve.get("num")).intValue()+"");
            comment.setText((String)reserve.get("comment"));

            try {
                ttsProvider.say("あなたわ"+yomi+"さまですね？");
            } catch (LanguageNotSupportedException e) {
                e.printStackTrace();
            }

            ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                @Override
                public void onSpeechComplete() {
                    ttsProvider.setOnSpeechCompleteListener(null);
                    showYesNoBtn();
                }
            });
        }
    }

    private void tapYes(){
        nameTextView.setVisibility(View.GONE);
        hideYesNoBtn();
        switch(mode){
            case RECOGNIZING_MODE:
                showReserve();
                break;
            case REPEAT_MODE:
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            recreate();
                        }
                    });

                    ttsProvider.say("もう一度認証いたします。。");
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }

                break;
            case CONFIRM_MODE:

                break;
            default:
                break;
        }
    }

    private void tapNo(){
        nameTextView.setVisibility(View.GONE);
        hideYesNoBtn();
        switch(mode){
            case RECOGNIZING_MODE:
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            mode = REPEAT_MODE;
                            showYesNoBtn();
                        }
                    });
                    ttsProvider.say("あれ？違いました？もう一度確認してもよろしいでしょうか？");
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }

                break;
            case REPEAT_MODE:
                try {
                    ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                        @Override
                        public void onSpeechComplete() {
                            finish();
                        }
                    });
                    ttsProvider.say("認証を終了いたします。");
                } catch (LanguageNotSupportedException e) {
                    e.printStackTrace();
                }

                break;
            case CONFIRM_MODE:

                break;
            default:
                break;
        }
    }

    private void showReserve(){
        mode = CONFIRM_MODE;
        try {
            ttsProvider.say("ようこそいらっしゃいました。予約情報を表示します。");
        } catch (LanguageNotSupportedException e) {
            e.printStackTrace();
        }
        reserveInfo.setVisibility(View.VISIBLE);
        ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
            @Override
            public void onSpeechComplete() {
                ttsProvider.setOnSpeechCompleteListener(null);
            }
        });
    }

    private void showYesNoBtn(){
        isShowingOption = true;
        faceDetectView.setVisibility(View.INVISIBLE);
        findViewById(R.id.yes).setVisibility(View.VISIBLE);
        findViewById(R.id.no).setVisibility(View.VISIBLE);
        onRecognitionCompleteListener = new STTProvider.OnRecognitionCompleteListener() {
            @Override
            public void onRecognitionComplete(final List<String> results) {
                offlineNLUProvider.setOnAnalyseCompleteListener(new NLUProvider.OnAnalyseCompleteListener() {
                    @Override
                    public void OnAnalyseComplete(Action action) {
                        if (action == null) {
                            try {
                                ttsProvider.say("すみません。はい。いいえ。でお願いします。");
                            } catch (LanguageNotSupportedException e) {
                                e.printStackTrace();
                            }
                            ttsProvider.setOnSpeechCompleteListener(new TTSProvider.OnSpeechCompleteListener() {
                                @Override
                                public void onSpeechComplete() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            sttProvider.listen();
                                        }
                                    });
                                }
                            });

                        }
                    }
                });
                offlineNLUProvider.analyseText(results,actions);
            }
        };
        sttProvider.setOnRecognitionCompleteListener(onRecognitionCompleteListener);
        sttProvider.listen();
    }
    private void hideYesNoBtn(){
        isShowingOption = false;
        faceDetectView.setVisibility(View.VISIBLE);
        findViewById(R.id.yes).setVisibility(View.INVISIBLE);
        findViewById(R.id.no).setVisibility(View.INVISIBLE);
        sttProvider.stopListening();
    }

    private ObjectAnimator mObjectAnimator;
    private ImageView mImageView;

    private void startLoading(){
        mImageView.setVisibility(View.VISIBLE);
        PropertyValuesHolder vhRotation = PropertyValuesHolder.ofFloat("rotation",0.0F,360.0F);
        mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mImageView,vhRotation);

        mObjectAnimator.setDuration(1000);
        mObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);

        mObjectAnimator.setInterpolator(new LinearInterpolator());

        mObjectAnimator.start();
    }

    private void stopLoading(){
        mImageView.setVisibility(View.INVISIBLE);
        mObjectAnimator.cancel();
    }
}
