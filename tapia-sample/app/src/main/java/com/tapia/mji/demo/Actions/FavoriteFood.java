package com.tapia.mji.demo.Actions;

/**
 * Created by Sami on 08-Jul-16.
 */
public class FavoriteFood extends MyAction {
    public OnThisActionListener onThisActionListener;
    public FavoriteFood(OnThisActionListener thisActionListener){
        super(thisActionListener);
        onThisActionListener = thisActionListener;
        type = MyActionType.FAVORITE_FOOD;
    }

    static public abstract class OnThisActionListener implements OnActionListener {
        @Override
        public void onAction() {
            process();
        }
        public abstract void process();
    }
}
