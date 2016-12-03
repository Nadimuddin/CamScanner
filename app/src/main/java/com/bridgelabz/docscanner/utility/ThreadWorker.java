package com.bridgelabz.docscanner.utility;

import android.util.Log;

import com.bridgelabz.docscanner.interfaces.ThreadInvokerMethod;

import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public class ThreadWorker implements Runnable {
    public static final String TAG = "ThreadWorker";

    Map<String, Object> m_RequestData;
    ThreadInvokerMethod m_ThreadInvokerMethod;
    static int m_NumOfThreadWorked = 0;

    // This Constructor is used if called from ThreadPool
    public ThreadWorker(ThreadInvokerMethod threadInvoker, Map<String, Object> data)
    {
        m_RequestData = data;
        m_ThreadInvokerMethod = threadInvoker;
    }

    @Override
    public void run() {
        try
        {
            m_NumOfThreadWorked++;
            m_ThreadInvokerMethod.processThreadRequest(m_RequestData);
            Log.i(TAG, "Num Of Thread Worked: "+m_NumOfThreadWorked+
                    " Req Obj: "+m_RequestData);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public String toString()
    {
        StringBuffer mesg = new StringBuffer("Thread Request Hdlr: ");
        mesg.append(m_ThreadInvokerMethod.getClass().getName());
        mesg.append("\nReq Data: "+m_RequestData);
        return mesg.toString();
    }
}
