package com.tapia.mji.demo.Actions;

/**
 * Created by Sami on 08-Jul-16.
 */
public class Recognize extends MyAction {
    public OnThisActionListener onThisActionListener;
    public Recognize(OnThisActionListener thisActionListener){
        super(thisActionListener);
        onThisActionListener = thisActionListener;
        type = MyActionType.RECOGNIZE;
    }

    static public abstract class OnThisActionListener implements OnActionListener {
        @Override
        public void onAction() {
            process();
        }
        public abstract void process();
    }
}
