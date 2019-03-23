package com.zhang_ray.camera;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Logger {
    final static private String sTAG = BuildConfig.APPLICATION_ID;

    private static Logger sInstance = null;
    private Lock mLock = null;

    private Logger() {
        mLock = new ReentrantLock();
    }

    static synchronized Logger getLogger() {
        if (sInstance == null) {
            sInstance = new Logger();
        }
        return sInstance;
    }

    private String getFunctionName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        if (stackTraceElements == null) {
            return null;
        }

        for (StackTraceElement st : stackTraceElements) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            return "[" + st.getFileName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    private String createMessage(String msg) {
        String functionName = getFunctionName();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS").format(new Date());
        String message = (functionName == null ? msg : (functionName + "\t" + msg));
        return currentTime + "\t" + message;
    }

    void i(String format, Object... args) {
        mLock.lock();
        try {
            String message = createMessage(getInputString(format, args));
            Log.i(sTAG, message);
        } finally {
            mLock.unlock();
        }
    }

    void v(String format, Object... args) {
        mLock.lock();
        try {
            String message = createMessage(getInputString(format, args));
            Log.v(sTAG, message);
        } finally {
            mLock.unlock();
        }
    }

    void d(String format, Object... args) {
        mLock.lock();
        try {
            String message = createMessage(getInputString(format, args));
            Log.d(sTAG, message);
        } finally {
            mLock.unlock();
        }
    }

    void e(String format, Object... args) {
        mLock.lock();
        try {
            String message = createMessage(getInputString(format, args));
            Log.e(sTAG, message);
        } finally {
            mLock.unlock();
        }
    }

    private String getInputString(String format, Object... args) {
        if (format == null) {
            return "null log format";
        }

        return String.format(format, args);
    }

    public void error(Exception e) {
        StringBuffer sb = new StringBuffer();
        mLock.lock();
        try {
            String name = getFunctionName();
            StackTraceElement[] sts = e.getStackTrace();

            if (name != null) {
                sb.append(name + " - " + e + "\r\n");
            } else {
                sb.append(e + "\r\n");
            }
            if (sts != null && sts.length > 0) {
                for (StackTraceElement st : sts) {
                    if (st != null) {
                        sb.append("[ " + st.getFileName() + ":"
                                + st.getLineNumber() + " ]\r\n");
                    }
                }
            }
            Log.e(sTAG, sb.toString());
        } finally {
            mLock.unlock();
        }
    }

    void w(String format, Object... args) {
        mLock.lock();
        try {
            String message = createMessage(getInputString(format, args));
            Log.w(sTAG, message);
        } finally {
            mLock.unlock();
        }
    }

}
