package com.zhang_ray.camera;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class AvcEncoder {
    private MediaCodec mMediaCodec;
    private int mHeight;
    private int mWidth;
    private int mYStride;
    private int mCStride;
    private int mYSize;
    private int mCSize;

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    boolean init(int width, int height, int frameRate, int bitrate) {
        final String MIME_TYPE = "video/avc";

        this.mHeight = height;
        this.mWidth = width;

        this.mYStride = (int) Math.ceil(width / 16.0f) * 16;
        this.mCStride = (int) Math.ceil(width / 32.0f) * 16;
        this.mYSize = mYStride * height;
        this.mCSize = mCStride * height / 2;

        try {
            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    void deInit() {
        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // TODO:
    // U and V planes are reversed (https://stackoverflow.com/a/17243244)
    byte[] encode(byte[] input) {
        try {
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input, 0, input.length);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int timeoutUSec = 10000;
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUSec);

            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                outputStream.write(outData);
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUSec);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        byte[] ret = outputStream.toByteArray();
        outputStream.reset();
        return ret;
    }


}
