package com.zhang_ray.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    Camera.Size mSize = null;

    private final static int DEFAULT_FRAME_RATE = 30;
    private final static int DEFAULT_BIT_RATE = 5000000;

    Camera mCamera = null;
    SurfaceHolder mPreviewHolder;
    byte[] mPreviewBuffer;
    boolean mIsStreaming = false;
    AvcEncoder mEncoder;
    String mIP = "192.168.1.111";
    int mPort = 6666;
    TcpClient mTcpClient = null;
    final ArrayList<byte[]> mEncDataList = new ArrayList<byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mPreviewHolder = ((SurfaceView) findViewById(R.id.svCameraPreview)).getHolder();
        mPreviewHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera();
                startStream();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                stopCamera();
                startCamera();
                startStream();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopCamera();
            }

        });
    }


    @Override
    protected void onPause() {
        try {
            stopStream();
            mEncoder.deInit();
        }
        catch (Exception e){
            Logger.getLogger().error(e);
        }

        super.onPause();
    }

    private void startStream() {
        try {
            mEncoder = new AvcEncoder();
            mEncoder.init(mSize.width, mSize.height, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE);

            mIsStreaming = true;

            (new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mTcpClient = new TcpClient(mIP, mPort);
                        while (mIsStreaming) {
                            boolean empty = false;

                            synchronized (mEncDataList) {
                                if (mEncDataList.size() == 0) {
                                    empty = true;
                                } else {
                                    mTcpClient.send(mEncDataList.remove(0));
                                }
                            }
                            if (empty) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    Logger.getLogger().error(e);
                                }
                            }
                        }
                    }catch (Exception e){
                        Logger.getLogger().error(e);
                    }
                }
            })).start();
        } catch (Exception e) {
            Logger.getLogger().error(e);
        }
    }

    private void stopStream() {
        mIsStreaming = false;

        if (mEncoder != null) {
            mEncoder.deInit();
        }

        mEncoder = null;
    }

    private Camera.Size getLargestSize(Camera.Parameters params) {
        final List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        int theMaxSizeValue = 0;
        Camera.Size result = null;
        for (Camera.Size theSize : prevSizes) {
            int width = theSize.width;
            int height = theSize.height;
            int sizeValue = width * height;
            if (sizeValue > theMaxSizeValue) {
                theMaxSizeValue = sizeValue;
                result = theSize;
            }
        }

        return result;
    }

    private void startCamera() {
        try {
            mCamera = Camera.open();

            mSize = getLargestSize(mCamera.getParameters());
            mPreviewHolder.setFixedSize(mSize.width, mSize.height);

            setVideoSize((SurfaceView)findViewById(R.id.svCameraPreview), mSize.width, mSize.height);

            int stride = (int) Math.ceil(mSize.width / 16.0f) * 16;
            int cStride = (int) Math.ceil(mSize.width / 32.0f) * 16;
            final int frameSize = stride * mSize.height;
            final int qFrameSize = cStride * mSize.height / 2;

            mPreviewBuffer = new byte[frameSize + qFrameSize * 2];

            mCamera.setPreviewDisplay(mPreviewHolder);
            mCamera.setDisplayOrientation(90);
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(mSize.width, mSize.height);
            mCamera.setParameters(params);
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mCamera.addCallbackBuffer(mPreviewBuffer);

                    if (mIsStreaming) {
                        byte[] encData = mEncoder.encode(data);
                        if (encData.length > 0) {
                            synchronized (mEncDataList) {
                                mEncDataList.add(encData);
                            }
                        }
                    }
                }
            });
            mCamera.startPreview();
        } catch (IOException | RuntimeException e) {
            Logger.getLogger().error(e);
        }
    }

    private void stopCamera() {
        if (mCamera == null) {
            return;
        }
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private void setVideoSize(SurfaceView surfaceView, int videoWidth, int videoHeight) {
        float videoProportion = (float) videoHeight/ (float) videoWidth ;

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        surfaceView.setLayoutParams(lp);
    }
}
