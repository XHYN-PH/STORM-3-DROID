/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r1tests;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class R1Instrumentation extends Instrumentation {
    private int assertions;
    private Activity launcher;
    private void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
        progress("PASS: " + message);
    }
    private void progress(String message) {
        Bundle status = new Bundle(); status.putString("stream", message + "\n"); sendStatus(1, status);
    }
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private void collect(View view, List<View> views) {
        views.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i=0; i<group.getChildCount(); i++) collect(group.getChildAt(i), views);
        }
    }
    private boolean hasText(List<View> views, String text) {
        for (View view: views) if (view instanceof TextView && text.contentEquals(((TextView)view).getText())) return true;
        return false;
    }
    private View findLoggingSwitch(List<View> views) {
        for (View view: views) {
            if (!(view instanceof TextView) || !"Logging".contentEquals(((TextView)view).getText())) continue;
            View current=view;
            while (current.getParent() instanceof ViewGroup) {
                current=(View)current.getParent();
                List<View> children=new ArrayList<>(); collect(current, children);
                for (View child:children) if (child.getClass().getName().equals("a.c")) return child;
            }
        }
        return null;
    }
    private void touch(final View view) {
        runOnMainSync(new Runnable() { public void run() {
            long time=SystemClock.uptimeMillis();
            MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,5,5,0);
            MotionEvent up=MotionEvent.obtain(time,time+10,MotionEvent.ACTION_UP,5,5,0);
            view.dispatchTouchEvent(down); view.dispatchTouchEvent(up); down.recycle(); up.recycle();
        }});
    }
    private String read(File file) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try (FileInputStream in=new FileInputStream(file)) {
            byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))>0)out.write(buffer,0,n);
        }
        return new String(out.toByteArray(), "UTF-8");
    }
    @Override public void onStart() {
        Bundle result=new Bundle(); Context context=getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("naruto", Context.MODE_PRIVATE);
        boolean originalLogging=prefs.getBoolean("logging_enabled",false);
        boolean originalPt=prefs.getBoolean("menu_ptbr",false);
        try {
            check("R1".equals(context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName),"Installed version is R1");
            check(!originalLogging,"Logging defaults to off on a fresh install");
            ClassLoader loader=context.getClassLoader();
            Class<?> control=Class.forName("com.naruto.port.LoggingControl",true,loader);
            Method initialize=control.getMethod("initialize",Context.class);
            check(Boolean.TRUE.equals(initialize.invoke(null,context)),"Actual ARM64 logging helper initializes on Android");
            Intent intent=new Intent(); intent.setClassName(context.getPackageName(),"com.naruto.port.LauncherActivity");
            intent.putExtra("trailer",false); intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            progress("Starting launcher with its existing trailer=false test hook");
            launcher=startActivitySync(intent);
            final List<View> views=new ArrayList<>();
            runOnMainSync(new Runnable(){public void run(){collect(launcher.getWindow().getDecorView(),views);}});
            check(hasText(views,"Logging"),"English Logging control is present");
            check(hasText(views,"Modified and Translated by XHYN_PH"),"YouTube footer has the requested text");
            check(!hasText(views,"PT-BR")&&!hasText(views,"Portuguese text"),"PT-BR option is absent");
            View logging=findLoggingSwitch(views);check(logging!=null,"Logging has an interactive switch");
            Field state=logging.getClass().getField("a");
            check(!state.getBoolean(logging),"Logging switch renders off initially");
            touch(logging);
            check(prefs.getBoolean("logging_enabled",false),"Turning Logging on persists its setting");
            check(state.getBoolean(logging),"Logging switch reflects its enabled state");
            Class<?> appLog=Class.forName("com.naruto.port.AppLog",true,loader);
            Method emit=appLog.getMethod("i",String.class,String.class);
            emit.invoke(null,"R1_NATIVE_TEST","R1_LOGGING_ON_MARKER");
            Class<?> sdl=Class.forName("org.libsdl.app.SDL",true,loader);
            Method load=sdl.getMethod("loadLibrary",String.class,Context.class);
            progress("Loading the actual upstream ffmpeg/main libraries without starting gameplay");
            load.invoke(null,"ffmpeg",context);load.invoke(null,"main",context);
            Method install=control.getDeclaredMethod("nativeInstall");install.setAccessible(true);
            check(Boolean.TRUE.equals(install.invoke(null)),"Logging hooks install against the actual ARM64 engine libraries");
            check(Boolean.TRUE.equals(install.invoke(null)),"Installing native logging hooks again is safe");
            touch(logging);
            check(!prefs.getBoolean("logging_enabled",true),"Turning Logging off persists its setting");
            check(!state.getBoolean(logging),"Logging switch reflects its disabled state");
            emit.invoke(null,"R1_NATIVE_TEST","R1_LOGGING_OFF_MARKER");
            prefs.edit().putBoolean("menu_ptbr",true).commit();
            final Throwable[] writeError=new Throwable[1];
            runOnMainSync(new Runnable(){public void run(){
                try {launcher.getClass().getMethod("t").invoke(launcher);}catch(Throwable error){writeError[0]=error;}
            }});
            if(writeError[0]!=null)throw new RuntimeException(writeError[0]);
            String config=read(new File(context.getExternalFilesDir(null),"naruto.ini"));
            check(config.matches("(?s).*\\blanguage\\s*=\\s*en(?:\\r?\\n|$).*"),"Config writer forces English even with an old PT-BR preference");
            check(!config.matches("(?s).*\\blanguage\\s*=\\s*pt(?:\\r?\\n|$).*"),"Generated configuration contains no Portuguese language override");
            check(Boolean.TRUE.equals(initialize.invoke(null,context)),"Repeated logging initialization succeeds");
            result.putString("r1_result","PASS");result.putInt("assertions",assertions);
            result.putString("stream","R1_ANDROID_RESULT=PASS assertions="+assertions+"\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable error) {
            StringWriter stack=new StringWriter();error.printStackTrace(new PrintWriter(stack));
            result.putString("r1_result","FAIL");result.putInt("assertions",assertions);
            result.putString("stream","R1_ANDROID_RESULT=FAIL\n"+stack.toString());
            finish(Activity.RESULT_CANCELED,result);
        } finally {
            prefs.edit().putBoolean("logging_enabled",originalLogging).putBoolean("menu_ptbr",originalPt).commit();
        }
    }
}
