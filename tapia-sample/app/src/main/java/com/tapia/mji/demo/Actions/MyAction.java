package com.tapia.mji.demo.Actions;

import com.fuetrek.fsr.entity.RecognizeEntity;
import com.tapia.mji.tapialib.Actions.Action;

/**
 * Abstract class MyAction encapsulates data and methods mostly to facilitate
 * the NLU's handling of user commands.
 *
 * To add actions to Tapia, extend abstract class MyAction.
 *
 * Note: before to add your MyActionType to the enum class here when adding an action to Tapia.
 *
 * Created by Sami on 07-Jul-16.
 */
public abstract class MyAction extends Action {
    /**
     * Constructor
     *
     * @param onActionListener the listener object listening for calls to perform the action.
     */
    protected MyAction(OnActionListener onActionListener){
        super(onActionListener);
    }

    /**
     * Enum for entity type.
     */
    enum MyEntity{
        LOCATION,
        AGE,
        DURATION,
        DATETIME,
        CONTACT,
        ORDINAL,//first , second third etc
        NUMBER,
        DISTANCE,
        TEMPERATURE,
    }

    /**
     * Enum for action type. All new actions must be added to this enum class.
     */
    public enum MyActionType implements ActionType {
        CONVERSATION,

        ROTATE,
        GIVE_TIME,
        GIVE_DATE,
        BYE,

//      竹内追加分


        GREETING,   //おはよう、こんにちは、こんばんは
        SLEEP,
        WAKE_UP,
        SORRY,
        TALK,
        HOW_ARE_YOU,
        SEX,
        FAVORITE_FOOD,
        LIKE,
        NOT_LIKE,
        YES,
        NO,
        HAIR_CUT_YES,
        HAIR_CUT_NO,
        FIGHT,
        THANKS,
        WEATHER,    //天気
        RECOGNIZE,  //認証


        RECOGNITION_YES,
        RECOGNITION_NO,


    }
}
