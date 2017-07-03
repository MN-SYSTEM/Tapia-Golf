package com.m_and_n.util;

/**
 * Created by Admin on 2016/12/21.
 */
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class UDPconnect {

    public static int MAX_SIZE = 60000;

    public String ip = "192.168.1.210";
    public int port = 5432;

    public interface UDPListener {
        void send(String text);
        void receive(String text);
        void sendByte(byte[] text);
        void receiveByte(byte[] text);
    }


    private DatagramSocket recSocket = null;
    private DatagramSocket sendSocket = null;
    private InetAddress inetAddress = null;

    HandlerThread backThread;
    Handler backHandler;

    private UDPListener udpListener;

    public UDPconnect(String _ip,int _port) throws Exception{
        ip = _ip;
        port = _port;

        sendSocket = new DatagramSocket();
        inetAddress = InetAddress.getByName(ip);

    }

    public void startReceive(){
        try {
            recSocket = new DatagramSocket(52525);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(backThread == null){
            backThread = new HandlerThread("background");
            backThread.start();
            backHandler = new Handler(backThread.getLooper());
            backHandler.post(new Runnable(){
                @Override
                public void run(){
                    try {
                        receive();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            recSocket.close();
            if( !backThread.isAlive() ){
                backThread.quitSafely();
            }
            backThread = null;
            startReceive();
        }
    }

    public void stopReceive(){
        if ( backThread != null ) {
            if (!backThread.isAlive()) {
                backThread.quitSafely();
            }
        }
        backThread = null;
    }


    public void setInterface(UDPListener _udpSend){
            udpListener = _udpSend;
        }

    public void receive(){
        DatagramPacket packet = new DatagramPacket(new byte[65535],65535);
        try {
            Log.d("UDP","wait");
            this.recSocket.receive(packet);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.udpListener.receiveByte(packet.getData());
        this.receive();
    }

    public void send(final byte[] data) throws Exception{
        HandlerThread sendThread = new HandlerThread("sendThread");
        sendThread.start();
        Handler sendHandler = new Handler(sendThread.getLooper());
        sendHandler.post(new Runnable() {
            @Override
            public void run() {
                _send(data);
                Log.d("UDP",inetAddress.getHostAddress()+" SEND");
            }
        });
        sendThread.quitSafely();
    }

    private void _send(byte[] data){
        int total = (int)Math.ceil(data.length / MAX_SIZE);
        int index = 0;
        boolean isOk = true;

        UDPByte udpByte = new UDPByte(data,MAX_SIZE);
        ArrayList<byte[]> list = udpByte.split();

        for(byte[] _data : list){
            Log.d("UDP",index+"");
            DatagramPacket packet = new DatagramPacket(_data,_data.length,inetAddress,port);
            try {
                sendSocket.send(packet);
                isOk &= true;
            } catch (IOException e) {
                isOk = false;
                e.printStackTrace();
            }
            index++;
        }
        if(isOk && udpListener != null) {
            Log.d("UDP","SENDED");
            udpListener.sendByte(data);
        }
    }

    static class UDPByte{
        private int totalSize = 0;
        private int splitSize = 0;

        private byte[] data = null;

        public UDPByte(byte[] _data,int _splitSize){
            data = _data;
            totalSize = _data.length;
            Log.d("BYTE",totalSize+"");
            splitSize = _splitSize;
        }

        static class HeaderByte {
            int bytes = 0;
            int value = 0;

            HeaderByte(int _bytes, int _value) {
                bytes = _bytes;
                value = _value;
            }

            public byte[] toByteArray() {
                return ByteBuffer.allocate(bytes).putInt(value).array();
            }
        }


        static class HeaderBuilder{

            ArrayList<HeaderByte> headerByte = new ArrayList<>();
            int totalBytes = 0;

            public HeaderBuilder(){}


            public HeaderBuilder set(int _bytes,int value){
                totalBytes += _bytes;
                HeaderByte _headerByte = new HeaderByte(_bytes,value);
                headerByte.add(_headerByte);
                return this;
            }

            public byte[] build(){
                ByteBuffer buf = ByteBuffer.allocate(totalBytes);
                for(HeaderByte h : headerByte){
                    buf.put( h.toByteArray() );
                }
                return buf.array();
            }
        }

        public HeaderBuilder headerBuilder(){
            return new HeaderBuilder();
        }

        public ArrayList<byte[]> split(){
            int rest = totalSize;
            int offset = 0;

            int index = 0;
            int totalIndex = (int)Math.ceil(totalSize / splitSize)+1;



            int id = Calendar.getInstance().get(Calendar.MILLISECOND);



            ArrayList<byte[]> list = new ArrayList<>();

            while(rest > 0){
                int length = splitSize < rest ? splitSize : rest;

                byte[] header = headerBuilder().set(4,totalIndex).set(4,index).set(4,id).set(4,length).build();
                printByte(header);
                Log.d("BYTE","TOTAL:"+totalIndex+" , INDEX:"+index+" , ID: "+id);

                byte[] splitedData = Arrays.copyOfRange(data,offset,offset+length);

                list.add( ByteBuffer.allocate(header.length + splitedData.length).put(header).put(splitedData).array() );


                index ++;
                offset += length;
                rest -= length;
            }

            return list;
        }

    }

    static void printByte(byte[] bytes){
        String text = "";
        for(byte b : bytes){
            text += b + " ";
        }
        System.out.println(text);
    }
}
