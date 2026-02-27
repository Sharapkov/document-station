package com.itq.document_station.utill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodLogger {

    private static String getClassName(int depth) {
        return Thread.currentThread().getStackTrace()[depth].getClassName();
    }

    private static String getMethodName(int depth) {
        return Thread.currentThread().getStackTrace()[depth].getMethodName();
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger(getClassName(3) + "::" + getMethodName(3));
    }

    public static String getMethodName() {
        return LoggerFactory.getLogger(getMethodName(3)).getName();
    }
}
