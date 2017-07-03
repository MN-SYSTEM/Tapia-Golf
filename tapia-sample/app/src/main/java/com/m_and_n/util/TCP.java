package com.m_and_n.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Admin on 2017/06/22.
 */

public class TCP {

    ServerSocket mServerSocket = null;
    HandlerThread mBGThread = null;
    TCPListener mTCPListener = null;

    public TCP(TCPListener tcpListener){
        this.mTCPListener = tcpListener;
    }

    public interface TCPListener{
        void socket(Socket socket);
    }

    public void connect(){
        if(mServerSocket == null){
            try {
                mServerSocket = new ServerSocket(51515);

                threadStart();

                Handler handler = new Handler(mBGThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            if(mServerSocket == null) break;
                            Socket socket = null;
                            try {
                                Log.d("TCP","WAIT");
                                socket = mServerSocket.accept();
                                mTCPListener.socket(socket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect(){
        try {
            if(mServerSocket != null){
                mServerSocket.close();
            }

            mServerSocket = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
        threadEnd();

    }


    public void threadStart(){
        if(mBGThread == null){
            mBGThread = new HandlerThread("TCPThread");
            mBGThread.start();
        }else{
            mBGThread.start();
        }
    }

    public void threadEnd(){
        if(mBGThread != null){
            if(mBGThread.quitSafely()){
            }
        }
        mBGThread = null;
    }


}
