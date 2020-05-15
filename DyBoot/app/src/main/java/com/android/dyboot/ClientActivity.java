package com.android.dyboot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ccsClientActivity";

    private EditText cmdEt;
    private TextView tv_result;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Socket server;
    private boolean socketIsAlive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        cmdEt = findViewById(R.id.cmdEt);
        tv_result = findViewById(R.id.tv_result);

        findViewById(R.id.bt_conn).setOnClickListener(this);
        findViewById(R.id.bt_send).setOnClickListener(this);
        findViewById(R.id.bt_close).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        try {
            switch (id) {
                case R.id.bt_conn:
                    openSocket();
                    break;
                case R.id.bt_close:
                    sendMsg("close");
                    break;
                case R.id.bt_send:
                    String cmd = cmdEt.getText().toString();
                    sendMsg(cmd);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tv_result.setText((CharSequence) msg.obj);
        }
    };

    private void setResultText(String text){
        Message message = mHandler.obtainMessage();
        message.obj = text;
        mHandler.sendMessage(message);
    }

    private void openSocket(){
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (server == null) {
                        socketIsAlive = true;
                        server = new Socket("127.0.0.1", 5679);
                        server.setSoTimeout(2000);
                    }

                    in = new BufferedInputStream(server.getInputStream());
                    out = new BufferedOutputStream(server.getOutputStream());

                    while (socketIsAlive) {
                        int available = in.available();
                        if (available == 0) {
                            Thread.sleep(1000);
                        } else {
                            String result = readFromSocket(in);
                            Log.i(TAG, "result==" + result);
                            setResultText("server return data="+result);

                            if (result.contains("closed")){
                                closeSocket();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void closeSocket() throws Exception {
        if (server != null) {
            socketIsAlive = false;
            server.close();
            server = null;
            in = null;
            out = null;
            setResultText("socket have closed");
        }
    }

    public void sendMsg(final String cmd){
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    out.write(cmd.getBytes());
                    out.flush();
                    Log.e(TAG, "send==" + cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String readFromSocket(InputStream in) {
        int MAX_BUFFER_BYTES = 4000;
        String msg = "";
        byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
        try {
            Log.d(TAG, "socket.available() = " + in.available());
            int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
            msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
            Log.d(TAG, "msg==" + msg);
            tempbuffer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

}
