package com.zhang_ray.camera;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class TcpClient {

    private String mHost = null;
    private int mPort = 0;
    private OutputStream outputStream = null;

    private void reConnect() throws IOException{
        Socket mSocket = new Socket(mHost, mPort);
        if (mSocket.isConnected()) {
            Logger.getLogger().d("connect to Server success");
        }

        mSocket.setSoTimeout(8000);
        outputStream = mSocket.getOutputStream();
    }

    TcpClient(String host, int port) throws IOException {
        this.mHost = host;
        this.mPort = port;
        reConnect();
    }

    void send(byte[] data) throws IOException{
        outputStream.write(data, 0, data.length);
        outputStream.flush();
    }

}
