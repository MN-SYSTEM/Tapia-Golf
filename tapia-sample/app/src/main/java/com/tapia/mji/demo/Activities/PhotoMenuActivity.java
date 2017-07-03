package com.tapia.mji.demo.Activities;

import android.content.Intent;

import com.tapia.mji.tapialib.Activities.MenuActivity;
import com.tapia.mji.tapialib.TapiaApp;

import java.util.ArrayList;

/**
 * Created by Sami on 12-Dec-16.
 */

public class PhotoMenuActivity extends MenuActivity {


    static final int TAKE_PHOTO = 0;
    static final int RECOGNIZED = 1;
    static final int TALK = 2;
    static final int SLEEP = 3;
    static final int SETTING = 4;

    @Override
    public ArrayList<MenuItem> setMenuList() {
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(TALK, "話す"));
        menuItems.add(new MenuItem(RECOGNIZED, "認証"));
        menuItems.add(new MenuItem(SLEEP, "寝る"));
        menuItems.add(new MenuItem(SETTING, "設定"));
        return menuItems;
    }

    @Override
    public void onItemClick(MenuItem item) {
        switch (item.id){
            case RECOGNIZED:
                startActivity(new Intent(TapiaApp.getAppContext(),PhotoTakeActivity.class));
                break;
            case TALK:
                startActivity(new Intent(TapiaApp.getAppContext(),TalkActivity.class));
                break;
            case SLEEP:
                finish();
                break;
            case SETTING:
                startActivity(new Intent(TapiaApp.getAppContext(),SettingActivity.class));
                break;
        }
    }

    @Override
    public void onExitClick() {
        finish();
    }

    @Override
    public void onLeftClick() {

    }

    @Override
    public void onRightClick() {

    }
}
