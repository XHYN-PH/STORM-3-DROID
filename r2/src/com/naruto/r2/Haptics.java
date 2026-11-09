/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import android.content.Context;
import android.os.*;
import android.view.InputDevice;
import org.libsdl.app.SDL;

public final class Haptics {
    public static int amplitude(float low,float high,int strength){
        if(!Float.isFinite(low)||!Float.isFinite(high))return 0;
        return Math.round(Math.max(0,Math.min(1,Math.max(low,high)))*Math.max(0,Math.min(100,strength))*2.55f);
    }
    private static void vibrate(Vibrator v,int amplitude,int millis){
        if(v==null||!v.hasVibrator())return;
        try{if(amplitude<=0||millis<=0){v.cancel();return;}
            v.vibrate(VibrationEffect.createOneShot(Math.min(1000,millis),v.hasAmplitudeControl()?Math.max(1,Math.min(255,amplitude)):VibrationEffect.DEFAULT_AMPLITUDE));
        }catch(SecurityException|IllegalArgumentException ignored){}
    }
    public static void rumble(int id,float low,float high,int millis){Context c=SDL.getContext();if(c!=null)play(c,id,low,high,millis);}
    public static void run(int id,float strength,int millis){rumble(id,strength,strength,millis);}
    public static void stop(int id){Context c=SDL.getContext();if(c!=null)stop(c,id);}
    public static void play(Context c,int id,float low,float high,int millis){
        int mode=GameHooks.prefs(c).getInt("r2_rumble",1),gain=GameHooks.prefs(c).getInt("r2_rumble_strength",70);
        int amp=amplitude(low,high,gain);InputDevice d=InputDevice.getDevice(id);
        if(mode==0){stop(c,id);return;}
        if(mode==2||mode==4||(mode==1&&id==999999))vibrate((Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE),amp,millis);
        if(mode==3||mode==4||(mode==1&&id!=999999))if(d!=null)vibrate(d.getVibrator(),amp,millis);
    }
    public static void test(Context c){play(c,999999,.7f,.7f,250);for(int id:InputDevice.getDeviceIds()){InputDevice d=InputDevice.getDevice(id);if(d!=null&&(d.getSources()&InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD)play(c,id,.7f,.7f,250);}}
    public static void stop(Context c,int id){
        vibrate((Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE),0,0);
        if(id>=0){InputDevice d=InputDevice.getDevice(id);if(d!=null)vibrate(d.getVibrator(),0,0);}
        else for(int device:InputDevice.getDeviceIds()){InputDevice d=InputDevice.getDevice(device);if(d!=null)vibrate(d.getVibrator(),0,0);}
    }
}
