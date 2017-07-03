package com.m_and_n.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Admin on 2017/02/16.
 */

public class FaceDetectView extends View {

    Point displaySize = null;
    public FaceDetectView(Context context) {
        super(context);
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        displaySize = new Point();
        disp.getSize(displaySize);
    }

    public FaceDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        displaySize = new Point();
        disp.getSize(displaySize);
    }

    public FaceDetectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        displaySize = new Point();
        disp.getSize(displaySize);
    }

    public FaceDetectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        displaySize = new Point();
        disp.getSize(displaySize);
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    public void setFace(Face face){
        if(face == null){
            this.landmarks = null;
        }else{
            this.landmarks = face.getLandmarks();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void setCameraFace(Camera.Face face){
        this.landmarks = null;
        this.rect = null;
        if(face != null){
            this.landmarks = new ArrayList<>();
            if( face.leftEye != null ){
                landmarks.add( new Landmark(new PointF(face.leftEye.x,face.leftEye.y),Landmark.LEFT_EYE) );
            }
            if( face.rightEye != null ){
                landmarks.add( new Landmark(new PointF(face.rightEye.x,face.rightEye.y),Landmark.RIGHT_EYE) );
            }
            if( face.mouth != null ){
                landmarks.add( new Landmark(new PointF(face.mouth.x,face.mouth.y),Landmark.BOTTOM_MOUTH) );
            }
            if( face.rect != null ){
                rect = new RectF(face.rect);
                Matrix matrix = new Matrix();
                matrix.setScale(-1, 1);
                matrix.postScale(displaySize.x / 2000f, displaySize.y / 2000f);
                matrix.postTranslate(displaySize.x / 2f, displaySize.y / 2f);
                matrix.mapRect(rect);
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    private List<Landmark> landmarks = null;
    private RectF rect = null;

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        System.out.println("FaceDetectView onDraw");
        if(rect == null){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }else{
            Paint paint = new Paint();
            paint.setStrokeWidth(3f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.CYAN);
            canvas.drawRect(rect,paint);
        }
        if(landmarks == null) {
        }else{

            Paint paint = new Paint();
            paint.setStrokeWidth(10f);
            paint.setColor(Color.CYAN);
            for(Landmark landmark : landmarks){
//            canvas.drawCircle(landmark.getPosition().x,landmark.getPosition().y,10f,paint);
                canvas.drawCircle(landmark.getPosition().y,landmark.getPosition().x,10f,paint);
            }
        }

        if(rect == null && landmarks == null){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

    }
}
