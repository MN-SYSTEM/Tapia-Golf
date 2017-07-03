package com.tapia.mji.demo.Actions;

import java.util.List;

/**
 * Created by Sami on 08-Jul-16.
 */
public class Sorry extends MyAction {
    public OnThisActionListener onThisActionListener;
    public Sorry(OnThisActionListener thisActionListener){
        super(thisActionListener);
        onThisActionListener = thisActionListener;
        type = MyActionType.SORRY;
    }

    public void setResult(List<String> _results){
        onThisActionListener.result = _results;
    }

    static public abstract class OnThisActionListener implements OnActionListener {
        List<String> result;
        @Override
        public void onAction() {
            process(result);
        }
        public abstract void process(List<String> result);
    }
}
