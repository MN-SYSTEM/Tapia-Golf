package com.m_and_n.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by Admin on 2017/02/12.
 */

public class HttpRequest {
    public static String sendGet(String url, String query) {
        String result = "";
        BufferedReader bufferedReader = null;

        try {
            URL sendUrl = new URL(url + "?" + query);
            URLConnection connection = sendUrl.openConnection();

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            connection.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += "\n" + line;
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String sendPost(String url, String query) {
        String result = "";
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;

        try {
            URL sendUrl = new URL(url);
            URLConnection connection = sendUrl.openConnection();

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            // postの場合必要
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // 出力ストリーム
            printWriter = new PrintWriter(connection.getOutputStream());
            // リクエストパラメータ
            printWriter.print(query);
            printWriter.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += "\n" + line;
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (printWriter != null){
                    printWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }



    public static String createHttpQuery(Map<String,String> queryData){
        String query = "";
        for(Map.Entry<String,String> e:queryData.entrySet()){
            query += e.getKey() + "=" + e.getValue();
        }
        return query.substring(1,query.length()-1);
    }

    private String mUrl;
    private String mQuery;
    private OnHttpRequestResultListener mHttpRequestResultListener = null;

    public HttpRequest(String url,String query){
        this.mUrl = url;
        this.mQuery = query;
    }

    public HttpRequest(String url,Map<String,String> queryData){
        this(url,HttpRequest.createHttpQuery(queryData));
    }

    public HttpRequest(String url){
        this(url,"");
    }

    public HttpRequest(){
        this("","");
    }

    public void setUrl(String url){
        this.mUrl = url;
    }
    public void setQuery(String query){
        this.mQuery = query;
    }
    public void setQuery(Map<String,String> queryData){
        this.mQuery = HttpRequest.createHttpQuery(queryData);
    }

    public void setOnHttpRequestResultListener(OnHttpRequestResultListener httpRequestResultListener){
        this.mHttpRequestResultListener = httpRequestResultListener;
    }

    public void removeOnHttpRequestResultListener(){
        this.mHttpRequestResultListener = null;
    }

    public void get(){
        final String url = mUrl;
        final String query = mQuery;
        final OnHttpRequestResultListener httpRequestResultListener = this.mHttpRequestResultListener != null ? this.mHttpRequestResultListener : null;
        //Http request thread
        HandlerThread thread = new HandlerThread("HTTP_REQUEST");
        thread.start();
        Handler handler = new Handler(thread.getLooper());

        //Get Main Thread Handler
        final Handler mainHandler = new Handler(Looper.getMainLooper());


        handler.post(new Runnable(){
            @Override
            public void run() {
                final String result = HttpRequest.sendGet(url,query);

                if(httpRequestResultListener != null) httpRequestResultListener.resultOnBackgroundThread(result);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(httpRequestResultListener != null) httpRequestResultListener.resultOnMainThread(result);
                    }
                });
            }
        });
    }

    public void post(){
        final String url = mUrl;
        final String query = mQuery;
        final OnHttpRequestResultListener httpRequestResultListener = this.mHttpRequestResultListener != null ? this.mHttpRequestResultListener : null;
        //Http request thread
        HandlerThread thread = new HandlerThread("HTTP_REQUEST");
        thread.start();
        Handler handler = new Handler(thread.getLooper());

        //Get Main Thread Handler
        final Handler mainHandler = new Handler(Looper.getMainLooper());


        handler.post(new Runnable(){
            @Override
            public void run() {
                final String result = HttpRequest.sendPost(url,query);
                System.out.println(result);
                if(httpRequestResultListener != null) httpRequestResultListener.resultOnBackgroundThread(result);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(httpRequestResultListener != null) httpRequestResultListener.resultOnMainThread(result);
                    }
                });
            }
        });
    }

    public interface OnHttpRequestResultListener{
        void resultOnBackgroundThread(String result);
        void resultOnMainThread(String result);
    }
}
