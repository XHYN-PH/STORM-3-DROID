/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import a.f0;
import a.e0;
import android.view.MotionEvent;
import android.graphics.*;

/** Invisible floating right stick, using the upstream multitouch/axis transport. */
public final class RightInput {
    public static void acquire(f0 c,MotionEvent event){
        int action=event.getActionMasked();if(action!=0&&action!=5)return;
        if(c.o||c.x.a>=0||!GameHooks.prefs(c.getContext()).getBoolean("r2_touch_right",true))return;
        int i=event.getActionIndex(),id=event.getPointerId(i);float x=event.getX(i),y=event.getY(i);
        if(x<c.V*.5f||c.C.get(id,0)!=0||c.a(x,y)!=null)return;
        c.C.put(id,3);c.B=Math.max(1,c.U*.13f);
        c.x.a=id;c.x.b=x;c.x.c=y;c.x.d=x;c.x.e=y;c.x.f=0;c.x.g=0;c.x.h=1;
    }
    public static float[] radial(float x,float y,float dead,float gain,boolean invertY){
        if(!Float.isFinite(x)||!Float.isFinite(y))return new float[]{0,0};
        float length=(float)Math.hypot(x,y),dz=Math.max(0,Math.min(.45f,dead));
        if(length<=dz)return new float[]{0,0};
        float magnitude=Math.min(1,(Math.min(1,length)-dz)/(1-dz)*Math.max(.25f,Math.min(3,gain)));
        return new float[]{x/length*magnitude,y/length*magnitude*(invertY?-1:1)};
    }
    public static boolean update(f0 c,e0 stick,float x,float y,float radius){
        if(stick!=c.x)return false;
        float dx=x-stick.b,dy=y-stick.c,len=(float)Math.hypot(dx,dy),r=Math.max(1,radius);
        float visual=len>r?r/len:1;stick.d=stick.b+dx*visual;stick.e=stick.c+dy*visual;
        android.content.SharedPreferences p=GameHooks.prefs(c.getContext());
        float[] axes=radial(dx/r,dy/r,p.getFloat("r2_right_deadzone",.08f),p.getFloat("r2_touch_gain",1),p.getBoolean("r2_right_invert",false));
        stick.f=axes[0];stick.g=axes[1];return true;
    }
    public static void drawEditor(f0 c,Canvas canvas){
        if(!c.o)return;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(0x33ffffff);
        canvas.drawRect(c.V*.5f,0,c.V,c.U,p);p.setColor(0xfff5e6bd);p.setTextSize(c.U*.025f);p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Invisible right stick · drag any free area",c.V*.75f,c.U*.95f,p);
    }
    public static void release(f0 c){if(c.x==null||c.L==null)return;if(c.x.a>=0)c.C.delete(c.x.a);f0.n(c.x);c.q();c.invalidate();}
}
