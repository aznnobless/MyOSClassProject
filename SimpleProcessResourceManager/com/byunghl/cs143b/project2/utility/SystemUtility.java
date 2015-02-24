package com.byunghl.cs143b.project2.utility;

import java.io.BufferedWriter;
import java.io.IOException;

public class SystemUtility {

    private static SystemUtility instance = new SystemUtility();
    private StringBuilder oneCycleResultStrBuffer;

    private SystemUtility() {
        oneCycleResultStrBuffer = new StringBuilder();
    }

    public void displayMessage(boolean isAutomatedMode, String message) {
        if(isAutomatedMode)
            oneCycleResultStrBuffer.append(message);
        else
            System.out.println(message);
    }

    public void endCycle(boolean isAutomatedMode, BufferedWriter bw) throws IOException{
        if(isAutomatedMode)
            bw.write(oneCycleResultStrBuffer.toString().trim());

        oneCycleResultStrBuffer.delete(0 , oneCycleResultStrBuffer.toString().length());

    }

    public boolean isBufferNotEmpty() {
        return (oneCycleResultStrBuffer.toString().length() > 0);
    }
//
//    static public void displayErrorMessage(boolean isAutomatedMode, BufferedWriter bw, String message) throws IOException{
//
//        if(isAutomatedMode) {
//
//            bw.write(message);
//
//
//        } else {
//            System.err.println(message);
//        }
//    }

    static public SystemUtility getInstance() {
        return instance;
    }



}
