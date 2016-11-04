package com.example.arashi.blinkled;

import android.os.AsyncTask;
import android.widget.TextView;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class AsyncSocket extends AsyncTask<String, Void, String> {
    private String mIpAddress = null;
    private Integer mPortNo = 0;
    private TextView mText = null;

    public AsyncSocket(String ipStr, String portStr ,TextView status) {
        mIpAddress = ipStr;
        mPortNo = Integer.parseInt(portStr);
        mText = status;
    }

    @Override
    protected String doInBackground(String... params) {
        String data = "";
        try {
            Socket socket = new Socket(mIpAddress, mPortNo);
            OutputStream out = socket.getOutputStream();
            Scanner in = new Scanner(socket.getInputStream());
            out.write(params[0].getBytes());
            out.flush();
            while(!in.hasNextLine());
            do {
                data += in.nextLine();
            } while (in.hasNextLine());
            out.close();
            in.close();
            socket.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return "Socket Failure";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        mText.setText(s);
    }
}