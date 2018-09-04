package com.youlu.test01.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.youlu.test01.util.MyUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServerService extends Service {

    private boolean mIsServiceDestoryed = false;

    private String[] mDefinedMessages = new String[]{
     "你好啊，哈哈","你是?","这是哪里?","和你聊天后再看下","和这么多人聊天"
    };

    @Override
    public void onCreate() {
        // 启动一个TCP服务线程
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed = true;
        super.onDestroy();
    }

    public TCPServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }


    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            // 本地监听8688端口
            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                System.out.println("Tcp服务建立失败，8888:");
                e.printStackTrace();
                return;
            }
            while (!mIsServiceDestoryed){
                // 接收客户端请求
                try {
                    final Socket client = serverSocket.accept();
                    System.out.println("接收");
                    new Thread(){
                        @Override
                        public void run() {
                            // 响应客户端
                            try {
                                responseClient(client);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        };
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void responseClient(Socket client) throws IOException {
        // 用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        // 用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter((new OutputStreamWriter(client.getOutputStream()))),true);

        out.println("欢迎来到聊天室");

        while(!mIsServiceDestoryed){
            String str = in.readLine();
            System.out.println("获取客户端的消息:" + str);
            if (str == null){
                // 客户端端口连接
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            System.out.println("发送：" + msg);
        }
        System.out.println("客户端退出");
        // 关闭流
        MyUtils.close(out);
        MyUtils.close(in);
        client.close();
    }
}
