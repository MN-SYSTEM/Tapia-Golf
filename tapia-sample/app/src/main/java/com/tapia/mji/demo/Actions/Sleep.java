package com.tapia.mji.demo.Actions;

/**
 * Created by Sami on 08-Jul-16.
 */
public class Sleep extends MyAction {
    public OnThisActionListener onThisActionListener;
    public Sleep(OnThisActionListener thisActionListener){
        super(thisActionListener);
        onThisActionListener = thisActionListener;
        type = MyActionType.SLEEP;
    }

    static public abstract class OnThisActionListener implements OnActionListener {
        @Override
        public void onAction() {
            process();
        }
        public abstract void process();
    }
}
