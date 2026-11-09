/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import android.view.*;
import android.content.*;
import org.libsdl.app.SDL;

public final class ControllerInput {
    public static float axisValue(MotionEvent event,int axis,int pointer){
        float original=event.getAxisValue(axis,pointer);InputDevice device=event.getDevice();
        if(device==null)return original;
        Context context=SDL.getContext();if(context==null)return original;
        SharedPreferences p=GameHooks.prefs(context);
        boolean hasRx=device.getMotionRange(MotionEvent.AXIS_RX,event.getSource())!=null&&device.getMotionRange(MotionEvent.AXIS_RY,event.getSource())!=null;
        int canonicalX=hasRx?MotionEvent.AXIS_RX:MotionEvent.AXIS_Z,canonicalY=hasRx?MotionEvent.AXIS_RY:MotionEvent.AXIS_RZ;
        boolean right=axis==canonicalX||axis==canonicalY,left=axis==MotionEvent.AXIS_X||axis==MotionEvent.AXIS_Y;
        if(!left&&!right)return original;
        int selection=p.getInt("r2_right_axes",0),sourceX=canonicalX,sourceY=canonicalY;
        if(right&&selection==1){sourceX=MotionEvent.AXIS_Z;sourceY=MotionEvent.AXIS_RZ;}
        if(right&&selection==2){sourceX=MotionEvent.AXIS_RX;sourceY=MotionEvent.AXIS_RY;}
        if(left){sourceX=MotionEvent.AXIS_X;sourceY=MotionEvent.AXIS_Y;}
        InputDevice.MotionRange xr=device.getMotionRange(sourceX,event.getSource()),yr=device.getMotionRange(sourceY,event.getSource());
        InputDevice.MotionRange target=device.getMotionRange(axis,event.getSource());
        if(xr==null||yr==null||target==null||xr.getRange()<=0||yr.getRange()<=0||target.getRange()<=0)return original;
        float x=(event.getAxisValue(sourceX,pointer)-xr.getMin())/xr.getRange()*2-1;
        float y=(event.getAxisValue(sourceY,pointer)-yr.getMin())/yr.getRange()*2-1;
        float[] calibrated=RightInput.radial(x,y,p.getFloat(right?"r2_right_deadzone":"r2_left_deadzone",.08f),1,right&&p.getBoolean("r2_right_invert",false));
        float value=(axis==(right?canonicalX:MotionEvent.AXIS_X))?calibrated[0]:calibrated[1];
        return target.getMin()+(value+1)*.5f*target.getRange();
    }
}
