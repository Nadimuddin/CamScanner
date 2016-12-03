package com.bridgelabz.docscanner.interfaces;

import java.util.Map;

/**
 * Created by bridgeit on 25/11/16.
 */

public interface ThreadInvokerMethod {

    // This function is called by the new thread to process request
    public void processThreadRequest(Map<String, Object> requestData) throws Exception;
}
