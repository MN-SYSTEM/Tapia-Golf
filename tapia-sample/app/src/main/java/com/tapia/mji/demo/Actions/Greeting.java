package com.tapia.mji.demo.Actions;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sami on 08-Jul-16.
 */
public class Greeting extends MyAction {
    public OnThisActionListener onThisActionListener;
    public Greeting(OnThisActionListener thisActionListener){
        super(thisActionListener);
        onThisActionListener = thisActionListener;
        type = MyActionType.GREETING;
    }

    static public abstract class OnThisActionListener implements OnActionListener {
        @Override
        public void onAction() {
            process();
        }
        public abstract void process();
    }
}
