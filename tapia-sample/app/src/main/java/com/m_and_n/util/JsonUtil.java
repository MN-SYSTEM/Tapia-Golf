package com.m_and_n.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2017/02/12.
 */

public class JsonUtil {
    public static boolean isArray(String jsonStr){
        char first = jsonStr.charAt(1);
        char last = jsonStr.charAt(jsonStr.length()-1);
        return ( first == '[' && last == ']' );
    }
    public static boolean isObject(String jsonStr){
        char first = jsonStr.charAt(1);
        char last = jsonStr.charAt(jsonStr.length()-1);
        return ( first == '{' && last == '}' );
    }

    public static boolean isJSON(String str){
        return isArray(str) || isObject(str);
    }

    public static int ARRAY;
    public static int OBJECT;
    static{
        ARRAY = 0x0001;
        OBJECT = 0x0002;
    }

    public JsonUtil(){
    }


    public static Map<String,Object> toMap(String json){
        Gson gson = new Gson();
        HashMap<String,Object> result = null;
        if(JsonUtil.isObject(json)){
            try {
                result = (HashMap<String,Object>) gson.fromJson(json, HashMap.class);
            } catch(ClassCastException e) {
                e.getStackTrace();
            }
        }
        return result;
    }

    public static List<Object> toArray(String json){
        Gson gson = new Gson();
        List<Object> result = null;
        if(JsonUtil.isArray(json)){
            try {
                result = (List<Object>)gson.fromJson(json, List.class);
            } catch(ClassCastException e) {
                e.getStackTrace();
            }
        }
        return result;
    }

    public static String toJson(Map<String,Object> arg){
        Gson gson = new Gson();
        return gson.toJson(arg);
    }
    public static String toJson(List<Object> arg){
        Gson gson = new Gson();
        return gson.toJson(arg);
    }

}
