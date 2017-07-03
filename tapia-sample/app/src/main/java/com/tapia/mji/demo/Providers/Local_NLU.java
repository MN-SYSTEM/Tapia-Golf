package com.tapia.mji.demo.Providers;

import android.content.Context;

import com.tapia.mji.demo.Actions.MyAction;
import com.tapia.mji.demo.Actions.Rotate;
import com.tapia.mji.demo.Actions.Sorry;
import com.tapia.mji.demo.Languages.Japanese;
import com.tapia.mji.demo.R;
import com.tapia.mji.tapialib.Actions.Action;
import com.tapia.mji.tapialib.Languages.Language;
import com.tapia.mji.tapialib.Providers.Interfaces.OfflineNLUProvider;
import com.tapia.mji.tapialib.Utils.LevenshteinDistance;
import com.tapia.mji.tapialib.Utils.TapiaRobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tapia.mji.demo.Actions.MyAction.MyActionType.GIVE_TIME;

/**
 * Created by Sami on 07-Jul-16.
 */
public class Local_NLU implements OfflineNLUProvider {

    Language.LanguageID language;

    ArrayList<Keyword> myKeywords = new ArrayList<>();
    OnAnalyseCompleteListener onAnalyseCompleteListener;
    static Local_NLU myInstance = null;
    static public Local_NLU getInstance(Context context, Language.LanguageID language){
        if(myInstance == null || !language.equals(myInstance.language)){
            myInstance = new Local_NLU(context,language);
        }
        return myInstance;
    }

    class Keyword{
        String[] keywordArray;
        Action.ActionType actionType;
        Keyword (String[] keywordArray, Action.ActionType actionType){
            this.keywordArray = keywordArray;
            this.actionType = actionType;
        }
    }
    Context context;

    public Local_NLU(Context context, Language.LanguageID language){
        this.language = language;
        this.context = context;
//        myKeywords.add(new Keyword(new String[]{"call","record"}, Action.ActionType.));
//        myKeywords.add(new Keyword(new String[]{"keyword", "setting"}, Action.ActionType.));
        if(language == Language.LanguageID.ENGLISH_US || language == Language.LanguageID.ENGLISH_UK) {
            myKeywords.add(new Keyword(new String[]{"what", "time"}, GIVE_TIME));
            myKeywords.add(new Keyword(new String[]{"rotate", "degree"}, MyAction.MyActionType.ROTATE));
        }
        else if(language == Language.LanguageID.JAPANESE){
            myKeywords.add(new Keyword(new String[]{"何時"}, MyAction.MyActionType.GIVE_TIME));
            myKeywords.add(new Keyword(new String[]{"何曜日"}, MyAction.MyActionType.GIVE_DATE));
            myKeywords.add(new Keyword(new String[]{"何日"}, MyAction.MyActionType.GIVE_DATE));
            myKeywords.add(new Keyword(new String[]{"回転", "度"}, MyAction.MyActionType.ROTATE));

//          竹内追加分
            myKeywords.add(new Keyword(new String[]{"おはよう"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"お早う"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"こんにちわ"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"こんにちは"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"今日は"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"こんばんわ"}, MyAction.MyActionType.GREETING));
            myKeywords.add(new Keyword(new String[]{"こんばんは"}, MyAction.MyActionType.GREETING));

            myKeywords.add(new Keyword(new String[]{"静かに"}, MyAction.MyActionType.SLEEP));

            myKeywords.add(new Keyword(new String[]{"おやすみ"}, MyAction.MyActionType.SLEEP));

            myKeywords.add(new Keyword(new String[]{"おはよう"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"お早う"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"タピア"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"たぴあ"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"おきて"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"起きて"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"おきろ"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"起きろ"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"しごと"}, MyAction.MyActionType.WAKE_UP));
            myKeywords.add(new Keyword(new String[]{"仕事"}, MyAction.MyActionType.WAKE_UP));






            myKeywords.add(new Keyword(new String[]{"うるさい"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"あやまって"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"謝って"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"しつこい"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"だめ"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"駄目"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"使えない"}, MyAction.MyActionType.SORRY));
            myKeywords.add(new Keyword(new String[]{"つかえない"}, MyAction.MyActionType.SORRY));




            myKeywords.add(new Keyword(new String[]{"元気"}, MyAction.MyActionType.HOW_ARE_YOU));
            myKeywords.add(new Keyword(new String[]{"気分は"}, MyAction.MyActionType.HOW_ARE_YOU));
            myKeywords.add(new Keyword(new String[]{"調子は"}, MyAction.MyActionType.HOW_ARE_YOU));
            myKeywords.add(new Keyword(new String[]{"きぶん"}, MyAction.MyActionType.HOW_ARE_YOU));
            myKeywords.add(new Keyword(new String[]{"ちょうし"}, MyAction.MyActionType.HOW_ARE_YOU));

            myKeywords.add(new Keyword(new String[]{"性別"}, MyAction.MyActionType.SEX));

            myKeywords.add(new Keyword(new String[]{"食べ物"}, MyAction.MyActionType.FAVORITE_FOOD));

            myKeywords.add(new Keyword(new String[]{"好き"}, MyAction.MyActionType.LIKE));
            myKeywords.add(new Keyword(new String[]{"嫌い"}, MyAction.MyActionType.NOT_LIKE));

            myKeywords.add(new Keyword(new String[]{"はい"}, MyAction.MyActionType.YES));
            myKeywords.add(new Keyword(new String[]{"うん"}, MyAction.MyActionType.YES));
            myKeywords.add(new Keyword(new String[]{"そうです"}, MyAction.MyActionType.YES));
            myKeywords.add(new Keyword(new String[]{"そうだよ"}, MyAction.MyActionType.YES));


            myKeywords.add(new Keyword(new String[]{"いいえ"}, MyAction.MyActionType.NO));
            myKeywords.add(new Keyword(new String[]{"ちがう"}, MyAction.MyActionType.NO));
            myKeywords.add(new Keyword(new String[]{"違う"}, MyAction.MyActionType.NO));
            myKeywords.add(new Keyword(new String[]{"じゃない"}, MyAction.MyActionType.NO));

            myKeywords.add(new Keyword(new String[]{"切った"}, MyAction.MyActionType.HAIR_CUT_YES));
            myKeywords.add(new Keyword(new String[]{"きった"}, MyAction.MyActionType.HAIR_CUT_YES));
            myKeywords.add(new Keyword(new String[]{"切ってない"}, MyAction.MyActionType.HAIR_CUT_NO));
            myKeywords.add(new Keyword(new String[]{"きってない"}, MyAction.MyActionType.HAIR_CUT_NO));

            myKeywords.add(new Keyword(new String[]{"がんばる"}, MyAction.MyActionType.FIGHT));

            myKeywords.add(new Keyword(new String[]{"ありがとう"}, MyAction.MyActionType.THANKS));


            myKeywords.add(new Keyword(new String[]{"認証"}, MyAction.MyActionType.RECOGNIZE));
            myKeywords.add(new Keyword(new String[]{"受付"}, MyAction.MyActionType.RECOGNIZE));
            myKeywords.add(new Keyword(new String[]{"入場"}, MyAction.MyActionType.RECOGNIZE));
        }
    }

    @Override
    public void analyseText(final List<String> sentences, final List<Action> actionToListen) {
        new Runnable() {
            @Override
            public void run() {

                ArrayList<Keyword> listToAnalyse = new ArrayList<Keyword>();
                for (Action act: actionToListen) {
                    for (Keyword keyword: myKeywords) {
                        if(keyword.actionType == act.getType()){
                            listToAnalyse.add(keyword);
                        }
                    }
                }
                Keyword keywordGroup = getSimpleBestKeywordsGroup(listToAnalyse,sentences);
                Action resultAction = null;
                if(keywordGroup != null){
                    switch ((MyAction.MyActionType)keywordGroup.actionType) {
                        case ROTATE:
                            Rotate rotate = (Rotate) Action.queryAction(actionToListen, MyAction.MyActionType.ROTATE);
                            if(rotate != null) {
                                for (String sentence : sentences) {
                                    if (sentence.contains(context.getString(R.string.direction_left0)))
                                        rotate.setOrientation(TapiaRobot.RotateOrientation.LEFT);
                                    else if(sentence.contains(context.getString(R.string.direction_right0)))
                                        rotate.setOrientation(TapiaRobot.RotateOrientation.RIGHT);
                                    else if(sentence.contains(context.getString(R.string.direction_up0)))
                                        rotate.setOrientation(TapiaRobot.RotateOrientation.UP);
                                    else if(sentence.contains(context.getString(R.string.direction_down0)))
                                        rotate.setOrientation(TapiaRobot.RotateOrientation.DOWN);
                                }
                                int degrees = -1;
                                for (String sentence : sentences) {
                                    try {
                                        if(language.equals(Language.LanguageID.JAPANESE)){
                                            sentence = Japanese.convertFullWidthNumberToHalfWidthNumber(sentence);
                                        }

                                        degrees = Integer.parseInt(sentence.replaceAll("[\\D]", ""));
                                        rotate.setDegree(degrees);
                                        break;
                                    } catch (Exception e) {
                                        degrees = -1;
                                    }
                                }
                                if (degrees != -1)
                                    resultAction = rotate;
                                else
                                    resultAction = null;
                            }
                            break;
                        case SORRY:
                            Sorry sorry = (Sorry) Action.queryAction(actionToListen, MyAction.MyActionType.SORRY);
                            sorry.setResult(sentences);
                        default:
                            resultAction = Action.queryAction(actionToListen,keywordGroup.actionType);
                            break;
                    }
                }
                if(resultAction != null) {
                    resultAction.onAction();
                }
                if(onAnalyseCompleteListener !=null) {
                    onAnalyseCompleteListener.OnAnalyseComplete(resultAction);
                }
            }
        }.run();
    }

    @Override
    public void setOnAnalyseCompleteListener(OnAnalyseCompleteListener onAnalyseCompleteListener) {
        this.onAnalyseCompleteListener = onAnalyseCompleteListener;
    }

    public Keyword getSimpleBestKeywordsGroup(ArrayList<Keyword> keywords, List<String> sentences){
        Keyword bestKeywords = null;
        for (int k = sentences.size() - 1; k >= 0; k--) {
            for (Keyword kw: keywords) {
                boolean isMatch = true;
                for (String word: kw.keywordArray) {
                    if(!sentences.get(k).toLowerCase().contains(word.toLowerCase()))
                        isMatch = false;
                }
                if(isMatch)
                    bestKeywords = kw;
            }
        }

        return bestKeywords;
    }

    public String[] getBestKeywordsGroup(ArrayList<String[]> keywords, List<String> sentences){
        int[] weights = new int[keywords.size()];

        for (int k = 0; k < keywords.size(); k++) {
            String[] keyword =  keywords.get(k);
            for (int l =  0; l < keyword.length; l++) {
                String keywordOnlyWords = keyword[l].replace(",", "").replace(".", "");
                List<String> keywordWords = Arrays.asList(keywordOnlyWords.split(" "));
                for(int i = 0; i < sentences.size(); i++){
                    String resultOnlyWords = sentences.get(i).replace(",", "").replace(".", "");
                    List<String> resultWords = Arrays.asList(resultOnlyWords.split(" "));
                    if(keywordOnlyWords.length() < 8 && keywordWords.size() > 1) {
                        if (resultOnlyWords.contains(keywordOnlyWords))
                            weights[k] += (sentences.size() + 1 - i)*keywordWords.size();
                    }
                    else {
                        for (String resultWord : resultWords) {
                            for (String keywordWord : keywordWords) {
                                if (keywordWord.length() <= 4 && keywordWord.length() > 1)
                                    if (LevenshteinDistance.computeLevenshteinDistance(keywordWord.toLowerCase(), resultWord.toLowerCase()) == 0) {
                                        weights[k] += sentences.size() + 1 - i;
                                    }
                                if (keywordWord.length() > 4 && keywordWord.length() < 8) {
                                    if (LevenshteinDistance.computeLevenshteinDistance(keywordWord.toLowerCase(), resultWord.toLowerCase()) <= 1)
                                        weights[k] += sentences.size() + 2 - i - LevenshteinDistance.computeLevenshteinDistance(keywordWord.toLowerCase(), resultWord.toLowerCase());
                                }
                                if (keywordWord.length() >= 8) {
                                    if (LevenshteinDistance.computeLevenshteinDistance(keywordWord.toLowerCase(), resultWord.toLowerCase()) <= 2)
                                        weights[k] += sentences.size() + 3 - i - LevenshteinDistance.computeLevenshteinDistance(keywordWord.toLowerCase(), resultWord.toLowerCase());
                                }
                            }
                        }
                    }
                }
                if(l==0 && weights[k] == 0)
                    break;
            }
        }
        int bestWeightPos = 0;
        int bestWeightValue =0;
        for (int j = 0; j < weights.length;j++) {
            if(bestWeightValue < weights[j]){
                bestWeightValue = weights[j];
                bestWeightPos = j;
            }
        }
        //no keyword found
        if(bestWeightValue <= 20)
            return null;
        else {
            int drawNumber = 0;
            for (int weight : weights) {
                if(weight == weights[bestWeightPos])
                    drawNumber++;
            }
            if(drawNumber > 1)
                return null;

            return keywords.get(bestWeightPos);
        }
    }
}
