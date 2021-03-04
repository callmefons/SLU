package com.example.sluapplication;

import android.util.Log;

import java.util.regex.Pattern;

public class Utility {

    public static String getPrevClassName() {
        String value = Thread.currentThread().getStackTrace()[4].getClassName();
        String[] values = value.split(Pattern.quote("."));
        if (values.length==0) return value;
        return values[values.length-1];
    }

    public static void log(String message){
        logV(message);
    }

    public static void logV(String message){
        Log.v(getPrevClassName(), message);
    }

    public static void logD(String message){
        Log.d(getPrevClassName(), message);
    }

    public static void logW(Object message){
        Log.w(getPrevClassName(), message.toString());
    }

    public static void logE(String message){
        Log.e(getPrevClassName(), message);
    }

    public static void logE(String message, Object o){
        Log.e(getPrevClassName(), message + " " + o);
    }

    public static void logI(String message){
        Log.i(getPrevClassName(), message);
    }
}
