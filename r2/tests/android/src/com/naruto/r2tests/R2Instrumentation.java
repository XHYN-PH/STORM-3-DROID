/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2tests;
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

public final class R2Instrumentation extends Instrumentation {
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
    private void testController(Context context,ClassLoader loader)throws Exception {
        Intent intent=new Intent();intent.setClassName(context.getPackageName(),"com.naruto.port.ControllerMapActivity");intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Activity map=startActivitySync(intent);waitForIdleSync();screenshot(context,"controller");final Throwable[] error=new Throwable[1];
        runOnMainSync(()->{try{
            List<View> views=new ArrayList<>();collect(map.getWindow().getDecorView(),views);
            boolean found=false;for(View v:views)if(v.getClass().getName().equals("com.naruto.r2.ControllerUi"))found=true;
            check(found,"Controller visualization is installed in the stock mapping activity");
            check(hasText(views,"Reset selected")&&hasText(views,"Reset player")&&hasText(views,"Reset all"),"Controller mapping exposes all three reset actions");
            map.getClass().getMethod("h",String.class).invoke(map,"r3");
            long now=SystemClock.uptimeMillis();android.view.KeyEvent down=new android.view.KeyEvent(now,now,android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_BUTTON_THUMBR,0,0,-1,0,0,android.view.InputDevice.SOURCE_GAMEPAD);
            map.dispatchKeyEvent(down);
            check("rightstick".equals(context.getSharedPreferences("naruto",0).getString("pad_r3","")),"R3 key events persist the correct native controller binding");
            map.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_BUTTON_THUMBR));
            map.finish();
        }catch(Throwable e){error[0]=e;}});
        if(error[0]!=null)throw new RuntimeException(error[0]);
    }
    private void testSaveFiles(Context c,ClassLoader loader)throws Exception {
        File base=new File(c.getExternalFilesDir(null),"r2-fixture-"+java.util.UUID.randomUUID());File locks=new File(c.getFilesDir(),"r2-fixture-"+java.util.UUID.randomUUID());check(base.mkdirs()&&locks.mkdirs(),"Isolated Android save fixtures are created");
        File save=new File(base,"data/content/storm3fb.s/STORM3FB.S");save.getParentFile().mkdirs();write(save,"progress-A");
        File flag=new File(base,"save100.active");write(flag,"protected-flag");File original=new File(base,"save100-original/package/STORM3FB.S");original.getParentFile().mkdirs();write(original,"protected-original");
        Class<?> type=Class.forName("com.naruto.r2.SaveFiles",true,loader);Object store=type.getConstructor(File.class,File.class).newInstance(base,locks);
        try{
            ByteArrayOutputStream exported=new ByteArrayOutputStream();type.getMethod("exportCurrent",java.io.OutputStream.class).invoke(store,exported);
            check(exported.size()>0&&read(save).equals("progress-A"),"Android save export is read-only");
            File backup=(File)type.getMethod("backupCurrent").invoke(store);
            try{type.getMethod("restore",File.class).invoke(store,backup);throw new AssertionError("100% restore protection failed");}catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof java.io.IOException,"Android restore refuses an active 100% save");}
            check(read(flag).equals("protected-flag")&&read(original).equals("protected-original"),"Android save operations preserve both protected 100% files");
            check(flag.delete(),"Fixture deactivation happens outside the save manager");write(save,"progress-B");type.getMethod("restore",File.class).invoke(store,backup);
            check(read(save).equals("progress-A"),"Android filesystem supports staged save restore");
        }finally{type.getMethod("close").invoke(store);}
    }
    private void write(File f,String value)throws Exception {try(java.io.FileOutputStream out=new java.io.FileOutputStream(f)){out.write(value.getBytes("UTF-8"));}}
    private void screenshot(Context c,String label)throws Exception{android.graphics.Bitmap image=getUiAutomation().takeScreenshot();if(image==null)throw new AssertionError("Screenshot unavailable");try(java.io.FileOutputStream out=new java.io.FileOutputStream(new File(c.getExternalFilesDir(null),"r2-tests-"+label+".png"))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}android.graphics.Bitmap preview=android.graphics.Bitmap.createScaledBitmap(image,640,Math.max(1,image.getHeight()*640/image.getWidth()),true);try(java.io.FileOutputStream out=new java.io.FileOutputStream(new File(c.getExternalFilesDir(null),"r2-tests-"+label+".preview.jpg"))){preview.compress(android.graphics.Bitmap.CompressFormat.JPEG,45,out);}if(preview!=image)preview.recycle();image.recycle();}
    private void verifyR2(final Context context,final ClassLoader loader,List<View> views)throws Exception {
        waitForIdleSync();screenshot(context,"launcher");
        check("STORM 3".contentEquals(context.getApplicationInfo().loadLabel(context.getPackageManager())),"Installed app is named STORM 3");
        check(hasText(views,"More options"),"Stock launcher contains one compact More options card");
        android.graphics.drawable.Drawable icon=context.getApplicationInfo().loadIcon(context.getPackageManager());android.graphics.Bitmap iconImage=android.graphics.Bitmap.createBitmap(64,64,android.graphics.Bitmap.Config.ARGB_8888);icon.setBounds(0,0,64,64);icon.draw(new android.graphics.Canvas(iconImage));check(android.graphics.Color.alpha(iconImage.getPixel(0,0))==0&&android.graphics.Color.alpha(iconImage.getPixel(32,32))>0,"Circular app icon has transparent corners and an opaque center");iconImage.recycle();
        check(Class.forName("a.a0",true,loader).getDeclaredMethod("setValues",float[].class)!=null,"Patched stock statistics class verifies on Android");
        View display=null;for(View v:views)if(v instanceof TextView&&"Display".contentEquals(((TextView)v).getText()))display=v;
        check(display!=null,"Stock More options card exposes Display");final View displayButton=display;
        runOnMainSync(()->displayButton.performClick());waitForIdleSync();
        android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
        check(root!=null&&!root.findAccessibilityNodeInfosByText("Opening video").isEmpty(),"Display opens a stock dialog containing the native video switch");
        List<android.view.accessibility.AccessibilityNodeInfo> done=root.findAccessibilityNodeInfosByText("Done");check(!done.isEmpty()&&done.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK),"Stock settings dialog closes normally");waitForIdleSync();
        testSaveFiles(context,loader);
        Class<?> math=Class.forName("com.naruto.r2.GameHooks",true,loader);
        check(Math.abs(((Float)math.getMethod("cropScale",float.class,float.class,int.class).invoke(null,2400f,1080f,100))-1.25f)<.001f,"Proportional crop uses equal scale for both dimensions");
        final Throwable[] error=new Throwable[1];
        runOnMainSync(new Runnable(){public void run(){try{testTouch(loader);}catch(Throwable e){error[0]=e;}}});
        if(error[0]!=null)throw new RuntimeException(error[0]);
        Class<?> game=Class.forName("com.naruto.port.NarutoActivity",true,loader);
        Method stretch=game.getDeclaredMethod("applyStretch",boolean.class);stretch.setAccessible(true);
        // Deliberately unattached: execution must fail at Context access, never ART verification.
        final Throwable[] verifierError=new Throwable[1];
        runOnMainSync(()->{try{Object instance=game.getConstructor().newInstance();try{stretch.invoke(instance,false);throw new AssertionError("Unattached Activity unexpectedly ran");}catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException,"Patched game screen method verifies before reaching its Context dependency");}}catch(Throwable e){verifierError[0]=e;}});
        if(verifierError[0]!=null)throw new RuntimeException(verifierError[0]);
    }
    private static MotionEvent event(int action,int[] ids,float[] xs,float[] ys){
        MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[ids.length];
        MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[ids.length];
        for(int i=0;i<ids.length;i++){props[i]=new MotionEvent.PointerProperties();props[i].id=ids[i];props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;coords[i]=new MotionEvent.PointerCoords();coords[i].x=xs[i];coords[i].y=ys[i];coords[i].pressure=1;coords[i].size=1;}
        long t=SystemClock.uptimeMillis();return MotionEvent.obtain(t,t,action,ids.length,props,coords,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);
    }
    private void send(View view,int action,int[] ids,float[] xs,float[] ys){MotionEvent e=event(action,ids,xs,ys);view.onTouchEvent(e);e.recycle();}
    private void testTouch(ClassLoader loader)throws Exception {
        Class<?> callback=Class.forName("a.d0",true,loader),overlay=Class.forName("a.f0",true,loader);
        final int[] output=new int[7];
        Object listener=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{callback},(proxy,method,args)->{if(method.getName().equals("c")&&args!=null&&args.length==7)for(int i=0;i<7;i++)output[i]=((Integer)args[i]);return null;});
        View control=(View)overlay.getConstructor(Activity.class,callback).newInstance(launcher,listener);control.layout(0,0,1600,720);
        Object right=overlay.getField("x").get(control);Class<?> stick=right.getClass();
        send(control,MotionEvent.ACTION_DOWN,new int[]{0},new float[]{900},new float[]{620});
        check(stick.getField("a").getInt(right)==0,"A free touch on the right half captures the invisible stick");
        check(output[3]==0&&output[4]==0,"Floating right stick starts neutral at the touch origin");
        send(control,MotionEvent.ACTION_MOVE,new int[]{0},new float[]{1000},new float[]{590});
        check(output[3]>0&&output[4]>0,"Right-stick movement reaches the existing game input callback");
        send(control,MotionEvent.ACTION_MOVE,new int[]{0},new float[]{700},new float[]{620});
        check(stick.getField("a").getInt(right)==0&&output[3]<0,"Captured right finger retains its role across the center");
        send(control,MotionEvent.ACTION_UP,new int[]{0},new float[]{700},new float[]{620});
        check(output[3]==0&&output[4]==0&&stick.getField("a").getInt(right)==-1,"Lifting a finger resets right axes and releases capture");
        send(control,MotionEvent.ACTION_DOWN,new int[]{0},new float[]{316.8f},new float[]{576});
        send(control,MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{0,1},new float[]{316.8f,900},new float[]{576,620});
        send(control,MotionEvent.ACTION_MOVE,new int[]{0,1},new float[]{350,980},new float[]{570,590});
        check(output[1]!=0&&output[3]!=0,"Left and right sticks work simultaneously with independent pointers");
        send(control,MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{0,1},new float[]{350,980},new float[]{570,590});
        check(output[1]!=0&&output[3]==0&&output[4]==0,"Releasing the right finger leaves left-stick movement intact");
        send(control,MotionEvent.ACTION_CANCEL,new int[]{0},new float[]{350},new float[]{570});
        check(output[1]==0&&output[2]==0&&output[3]==0&&output[4]==0,"Cancel clears both sticks without stuck input");
        send(control,MotionEvent.ACTION_DOWN,new int[]{0},new float[]{1456},new float[]{532.8f});
        check((output[0]&8192)!=0&&stick.getField("a").getInt(right)==-1,"Existing attack button takes priority over invisible look input");
        send(control,MotionEvent.ACTION_UP,new int[]{0},new float[]{1456},new float[]{532.8f});
        check(output[0]==0,"Button release remains correct");
        launcher.getSharedPreferences("naruto",0).edit().putBoolean("r2_touch_right",false).commit();
        send(control,MotionEvent.ACTION_DOWN,new int[]{0},new float[]{900},new float[]{620});
        check(stick.getField("a").getInt(right)==-1,"Invisible right stick can be disabled");
        send(control,MotionEvent.ACTION_CANCEL,new int[]{0},new float[]{900},new float[]{620});
        launcher.getSharedPreferences("naruto",0).edit().remove("r2_touch_right").commit();
        android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(1600,720,android.graphics.Bitmap.Config.ARGB_8888);
        control.draw(new android.graphics.Canvas(bitmap));
        check(bitmap.getPixel(900,620)==0,"Right-stick touch area has no visible gameplay artwork");bitmap.recycle();
    }
    @Override public void onStart() {
        Bundle result=new Bundle(); Context context=getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("naruto", Context.MODE_PRIVATE);
        boolean originalLogging=prefs.getBoolean("logging_enabled",false);
        boolean originalPt=prefs.getBoolean("menu_ptbr",false);
        try {
            check("R2".equals(context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName),"Installed version is R2");
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
            verifyR2(context,loader,views);
            View logging=findLoggingSwitch(views);check(logging!=null,"Logging has an interactive switch");
            Field state=logging.getClass().getField("a");
            check(!state.getBoolean(logging),"Logging switch renders off initially");
            touch(logging);
            check(prefs.getBoolean("logging_enabled",false),"Turning Logging on persists its setting");
            check(state.getBoolean(logging),"Logging switch reflects its enabled state");
            Class<?> appLog=Class.forName("com.naruto.port.AppLog",true,loader);
            Method emit=appLog.getMethod("i",String.class,String.class);
            Method trace=appLog.getMethod("stackTrace",Throwable.class);
            Method stderr=appLog.getMethod("println",java.io.PrintStream.class,String.class);
            emit.invoke(null,"R1_NATIVE_TEST","R1_LOGGING_ON_MARKER");
            trace.invoke(null,new RuntimeException("R1_STACK_ON_MARKER"));
            stderr.invoke(null,System.err,"R1_STDERR_ON_MARKER");
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
            trace.invoke(null,new RuntimeException("R1_STACK_OFF_MARKER"));
            stderr.invoke(null,System.err,"R1_STDERR_OFF_MARKER");
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
            testController(context,loader);
            result.putString("r1_result","PASS");result.putInt("assertions",assertions);
            result.putString("stream","R2_ANDROID_RESULT=PASS assertions="+assertions+"\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable error) {
            StringWriter stack=new StringWriter();error.printStackTrace(new PrintWriter(stack));
            result.putString("r1_result","FAIL");result.putInt("assertions",assertions);
            result.putString("stream","R2_ANDROID_RESULT=FAIL\n"+stack.toString());
            finish(Activity.RESULT_CANCELED,result);
        } finally {
            prefs.edit().putBoolean("logging_enabled",originalLogging).putBoolean("menu_ptbr",originalPt).commit();
        }
    }
}
