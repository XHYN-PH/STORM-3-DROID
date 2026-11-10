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
    private volatile boolean completed;
    private volatile String lastProgress="starting";
    private Activity launcher;
    private void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
        progress("PASS: " + message);
    }
    private void progress(String message) {
        lastProgress=message;
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
        final Activity map=startActivitySync(intent);waitForIdleSync();SystemClock.sleep(1000);screenshot(context,"controller");final Throwable[] error=new Throwable[1];
        runOnMainSync(()->{try{
            List<View> views=new ArrayList<>();collect(map.getWindow().getDecorView(),views);
            check(!hasText(views,"Reset selected")&&!hasText(views,"Diagram"),"Added controller preview and reset UI are removed");
            java.util.Map rows=(java.util.Map)map.getClass().getField("f").get(map);
            check(rows.size()==16,"Stock controller mapping list contains all original bindings");
            ((View)rows.get("a")).performClick();check("a".equals(map.getClass().getField("h").get(map)),"Original mapping selection works");
            ((View)rows.get("a")).performClick();check(map.getClass().getField("h").get(map)==null,"Original mapping selection cancels normally");
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
            final File destination=new File(c.getExternalFilesDir(null),"r2-reimport-"+java.util.UUID.randomUUID());if(!destination.mkdirs())throw new java.io.IOException("Cannot create reimport fixture");
            final Context originalContext=c;
            Context isolated=new android.content.ContextWrapper(c){@Override public File getExternalFilesDir(String kind){return destination;}@Override public File getFilesDir(){File f=new File(destination,"private");f.mkdirs();return f;}@Override public android.content.SharedPreferences getSharedPreferences(String name,int mode){return originalContext.getSharedPreferences("r2_reimport_"+name,mode);}};
            File zip=new File(c.getCacheDir(),"r2-export-test-"+java.util.UUID.randomUUID()+".zip");try(java.io.FileOutputStream out=new java.io.FileOutputStream(zip)){out.write(exported.toByteArray());}
            Class.forName("a.l",true,loader).getMethod("i",Context.class,android.net.Uri.class).invoke(null,isolated,android.net.Uri.fromFile(zip));
            check(read(new File(destination,"data/content/storm3fb.s/STORM3FB.S")).equals("progress-A"),"Exported ZIP reimports through the unchanged stock importer in an isolated fixture");zip.delete();
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
        android.content.pm.ActivityInfo gameInfo=context.getPackageManager().getActivityInfo(new android.content.ComponentName(context.getPackageName(),"com.naruto.port.NarutoActivity"),0);
        int gameTheme=context.getResources().getIdentifier("GameTheme","style",context.getPackageName());check(gameTheme!=0&&gameInfo.theme==gameTheme,"Game activity uses the isolated stock-compatible window theme");
        android.view.ContextThemeWrapper gameContext=new android.view.ContextThemeWrapper(context,gameTheme);android.content.res.TypedArray window=gameContext.obtainStyledAttributes(new int[]{android.R.attr.windowNoTitle,android.R.attr.windowFullscreen});try{check(window.getBoolean(0,false)&&window.getBoolean(1,false),"Game window keeps the original fullscreen and no-title flags");}finally{window.recycle();}
        android.util.TypedValue dialogs=new android.util.TypedValue();check(gameContext.getTheme().resolveAttribute(android.R.attr.alertDialogTheme,dialogs,true)&&dialogs.resourceId==context.getResources().getIdentifier("StormDialog","style",context.getPackageName()),"Modern game dialogs remain separate from the native rendering window");

        check(hasText(views,"Performance"),"Performance presets are exposed in the launcher");
        check(hasText(views,"More options"),"Stock launcher contains one compact More options card");
        android.graphics.drawable.Drawable icon=context.getApplicationInfo().loadIcon(context.getPackageManager());android.graphics.Bitmap iconImage=android.graphics.Bitmap.createBitmap(64,64,android.graphics.Bitmap.Config.ARGB_8888);icon.setBounds(0,0,64,64);icon.draw(new android.graphics.Canvas(iconImage));check(android.graphics.Color.alpha(iconImage.getPixel(0,0))==0&&android.graphics.Color.alpha(iconImage.getPixel(32,32))>0,"Circular app icon has transparent corners and an opaque center");iconImage.recycle();
        check(Class.forName("a.a0",true,loader).getDeclaredMethod("setValues",float[].class)!=null,"Patched stock statistics class verifies on Android");
        View display=null;for(View v:views)if(v instanceof TextView&&"Display".contentEquals(((TextView)v).getText()))display=v;
        check(display!=null,"Stock More options card exposes Display");final View displayButton=display;
        runOnMainSync(()->displayButton.performClick());waitForIdleSync();
        android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
        check(root!=null&&!root.findAccessibilityNodeInfosByText("Opening video").isEmpty(),"Display opens a stock dialog containing the native video switch");
        screenshot(context,"display");
        List<android.view.accessibility.AccessibilityNodeInfo> done=root.findAccessibilityNodeInfosByText("Done");check(!done.isEmpty(),"Display has a Done action");android.graphics.Rect doneBounds=new android.graphics.Rect();done.get(0).getBoundsInScreen(doneBounds);android.util.DisplayMetrics metrics=context.getResources().getDisplayMetrics();check(doneBounds.top>=0&&doneBounds.bottom<=metrics.heightPixels&&doneBounds.height()>=Math.round(metrics.density*32),"Display Done action stays visible at the configured font scale");check(done.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK),"Stock settings dialog closes normally");waitForIdleSync();
        testSaveFiles(context,loader);
        Class<?> math=Class.forName("com.naruto.r2.GameHooks",true,loader);
        check(Math.abs(((Float)math.getMethod("cropScale",float.class,float.class,int.class).invoke(null,2400f,1080f,100))-1.25f)<.001f,"Proportional crop uses equal scale for both dimensions");
        testModernMenu(context,loader);
        testShutdownAndLookup(context,loader);
        Class<?> game=Class.forName("com.naruto.port.NarutoActivity",true,loader);
        Method stretch=game.getDeclaredMethod("applyStretch",boolean.class);stretch.setAccessible(true);
        // Deliberately unattached: execution must fail at Context access, never ART verification.
        progress("Verifying the patched game screen method on the UI thread");
        final Throwable[] verifierError=new Throwable[1];
        runOnMainSync(()->{try{Object instance=game.getConstructor().newInstance();try{stretch.invoke(instance,false);throw new AssertionError("Unattached Activity unexpectedly ran");}catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException,"Patched game screen method verifies before reaching its Context dependency");}}catch(Throwable e){verifierError[0]=e;}});
        if(verifierError[0]!=null)throw new RuntimeException(verifierError[0]);
    }
    private void testShutdownAndLookup(Context context,ClassLoader loader)throws Exception {
        final Throwable[] error=new Throwable[1];final Object[] instance={null};Class<?> game=Class.forName("com.naruto.port.NarutoActivity",true,loader),loop=Class.forName("com.naruto.r2.GameLoop",true,loader),hooks=Class.forName("com.naruto.r2.GameHooks",true,loader);
        runOnMainSync(()->{try{instance[0]=game.getConstructor().newInstance();android.os.Handler handler=(android.os.Handler)game.getField("statsHandler").get(instance[0]);Runnable stats=(Runnable)game.getField("statsUpdater").get(instance[0]),prepare=(Runnable)game.getField("prepareUpdater").get(instance[0]);handler.postDelayed(stats,60000);handler.postDelayed(prepare,60000);loop.getMethod("pause",game).invoke(null,instance[0]);check(!handler.hasCallbacks(stats)&&!handler.hasCallbacks(prepare),"Game pause cancels both existing polling tasks");check(Boolean.FALSE.equals(loop.getMethod("tick",int.class,android.view.KeyEvent.Callback.class).invoke(null,2,null)),"Unrelated emblem fade task keeps its original path");}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
        java.util.concurrent.CountDownLatch finishEngine=new java.util.concurrent.CountDownLatch(1);Thread engine=new Thread(()->{try{finishEngine.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}},"R2-simulated-SDL-thread");engine.start();
        File lockPath=new File(context.getFilesDir(),"r2-shutdown-test-"+java.util.UUID.randomUUID());java.io.RandomAccessFile file=new java.io.RandomAccessFile(lockPath,"rw");java.nio.channels.FileLock lock=file.getChannel().tryLock();Field lockField=hooks.getDeclaredField("gameLock"),fileField=hooks.getDeclaredField("gameLockFile");lockField.setAccessible(true);fileField.setAccessible(true);Field threadField=Class.forName("org.libsdl.app.SDLActivity",true,loader).getDeclaredField("mSDLThread");threadField.setAccessible(true);Object previousThread=threadField.get(null);
        try{lockField.set(null,lock);fileField.set(null,file);hooks.getField("returnToLauncher").setBoolean(null,false);threadField.set(null,engine);runOnMainSync(()->{try{hooks.getMethod("gameStop",Activity.class).invoke(null,instance[0]);}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);check(lock.isValid(),"Save lock remains held while the simulated game thread is alive");finishEngine.countDown();engine.join(5000);long deadline=SystemClock.uptimeMillis()+5000;while(lock.isValid()&&SystemClock.uptimeMillis()<deadline)SystemClock.sleep(20);waitForIdleSync();check(!lock.isValid()&&!engine.isAlive(),"Save lock is released only after the simulated game thread stops");}
        finally{finishEngine.countDown();threadField.set(null,previousThread);if(lock.isValid())lock.release();file.close();lockField.set(null,null);fileField.set(null,null);lockPath.delete();hooks.getField("active").setBoolean(null,true);}
        Class<?> handlerClass=Class.forName("org.libsdl.app.SDLJoystickHandler_API16",true,loader),stick=Class.forName("org.libsdl.app.SDLJoystickHandler_API16$SDLJoystick",true,loader);java.lang.reflect.Constructor<?> constructor=handlerClass.getDeclaredConstructor();constructor.setAccessible(true);Object handler=constructor.newInstance();Field devices=handlerClass.getDeclaredField("mJoysticks");devices.setAccessible(true);java.util.ArrayList<Object> list=(java.util.ArrayList<Object>)devices.get(handler);java.lang.reflect.Constructor<?> stickConstructor=stick.getDeclaredConstructor();stickConstructor.setAccessible(true);Object first=stickConstructor.newInstance(),second=stickConstructor.newInstance();Field deviceId=stick.getDeclaredField("device_id");deviceId.setAccessible(true);deviceId.setInt(first,7);deviceId.setInt(second,42);list.add(first);list.add(second);Method find=handlerClass.getDeclaredMethod("getJoystick",int.class);find.setAccessible(true);check(find.invoke(handler,7)==first&&find.invoke(handler,42)==second,"Allocation-free joystick lookup returns the matching device");check(find.invoke(handler,99)==null,"Missing joystick lookup remains safe");list.remove(first);check(find.invoke(handler,7)==null&&find.invoke(handler,42)==second,"Joystick lookup stays correct after device removal");
    }
    private View named(List<View> views,String text){for(View v:views)if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return v;return null;}
    private void testModernMenu(Context context,ClassLoader loader)throws Exception {
        for(String retired:new String[]{"Haptics","FrameGraph","RightInput","ControllerUi","ControllerInput"}){try{Class.forName("com.naruto.r2."+retired,true,loader);throw new AssertionError("Retired class present "+retired);}catch(ClassNotFoundException expected){check(true,"Retired runtime removed: "+retired);}}
        Class<?> menu=Class.forName("com.naruto.r2.GameMenu",true,loader),actions=Class.forName("com.naruto.r2.GameMenu$Actions",true,loader);
        final java.util.List<String> calls=new java.util.ArrayList<>();Object listener=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{actions},(proxy,m,args)->{calls.add(m.getName());return null;});
        final android.app.AlertDialog[] dialog=new android.app.AlertDialog[1];final Throwable[] error=new Throwable[1];
        runOnMainSync(()->{try{dialog[0]=(android.app.AlertDialog)menu.getMethod("open",Activity.class,actions).invoke(null,launcher,listener);List<View> views=new ArrayList<>();collect(dialog[0].getWindow().getDecorView(),views);
            check(hasText(views,"Game options")&&hasText(views,"Return to launcher")&&hasText(views,"Resume"),"Modern game menu exposes resume and launcher return");
            check(hasText(views,"Control opacity")&&hasText(views,"Brightness")&&hasText(views,"Touch controls"),"Expanded game menu contains live controls");
            check(!hasText(views,"Rumble")&&!hasText(views,"Performance history")&&!hasText(views,"Export diagnostics"),"Removed features do not appear in the game menu");
            View fps=named(views,"Show FPS");((View)fps.getParent().getParent()).performClick();check(calls.contains("stats"),"Game-menu FPS switch invokes its action");
        }catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
        waitForIdleSync();screenshot(context,"game-menu");runOnMainSync(()->dialog[0].getButton(-1).performClick());waitForIdleSync();
        check(!dialog[0].isShowing(),"Resume closes the game menu");
        final int[] exits={0};
        runOnMainSync(()->{try{dialog[0]=(android.app.AlertDialog)menu.getMethod("confirmExit",Activity.class,Runnable.class).invoke(null,launcher,(Runnable)()->exits[0]++);check(dialog[0].isShowing(),"Exit confirmation opens before leaving");dialog[0].getButton(-2).performClick();check(exits[0]==0,"Cancelling exit keeps the game running");}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
        waitForIdleSync();
        runOnMainSync(()->{try{dialog[0]=(android.app.AlertDialog)menu.getMethod("confirmExit",Activity.class,Runnable.class).invoke(null,launcher,(Runnable)()->exits[0]++);dialog[0].getButton(-1).performClick();}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);waitForIdleSync();check(exits[0]==1,"Confirmed exit invokes its supplied action exactly once");
        final SharedPreferences prefs=context.getSharedPreferences("naruto",0);java.util.Map<String,?> original=new java.util.HashMap<>(prefs.getAll());
        Class.forName("com.naruto.r2.Performance",true,loader).getMethod("balanced",Activity.class,int.class).invoke(null,launcher,3);
        check(prefs.getInt("menu_resolution_v5",0)==3&&prefs.getInt("menu_antialiasing",0)==1&&prefs.getInt("menu_framegen_mode",1)==0&&prefs.getInt("menu_screen_hz",-1)==0,"Balanced 720p writes lighter graphics and automatic display settings");
        Class.forName("com.naruto.r2.Performance",true,loader).getMethod("balanced",Activity.class,int.class).invoke(null,launcher,4);check(prefs.getInt("menu_resolution_v5",0)==4,"Balanced 900p is selectable");
        SharedPreferences.Editor edit=prefs.edit().clear();for(java.util.Map.Entry<String,?> e:original.entrySet()){Object v=e.getValue();if(v instanceof String)edit.putString(e.getKey(),(String)v);else if(v instanceof Integer)edit.putInt(e.getKey(),(Integer)v);else if(v instanceof Float)edit.putFloat(e.getKey(),(Float)v);else if(v instanceof Boolean)edit.putBoolean(e.getKey(),(Boolean)v);else if(v instanceof Long)edit.putLong(e.getKey(),(Long)v);}edit.commit();
    }
    @Override public void onStart() {
        Thread watchdog=new Thread(()->{try{Thread.sleep(120000);}catch(InterruptedException done){return;}if(completed)return;StringBuilder trace=new StringBuilder();for(java.util.Map.Entry<Thread,StackTraceElement[]> entry:Thread.getAllStackTraces().entrySet())if(entry.getKey().getName().equals("main")||entry.getKey().getName().contains("Instr")){trace.append(entry.getKey().getName()).append('\n');int n=0;for(StackTraceElement frame:entry.getValue()){trace.append(frame).append('\n');if(++n==12)break;}}Bundle status=new Bundle();status.putString("stream",trace+"R2_STALL_STAGE="+lastProgress+"\n");sendStatus(1,status);},"R2-watchdog");watchdog.setDaemon(true);watchdog.start();
        Bundle result=new Bundle(); Context context=getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("naruto", Context.MODE_PRIVATE);
        boolean originalLogging=prefs.getBoolean("logging_enabled",false);
        boolean originalPt=prefs.getBoolean("menu_ptbr",false);
        try {
            check("R2.1".equals(context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName),"Installed version is R2.1");
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
            View stockLog=named(views,"Send log");check(stockLog!=null&&stockLog.getVisibility()==View.VISIBLE&&stockLog.hasOnClickListeners(),"Original Send log action remains visible and connected");
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
            completed=true;watchdog.interrupt();
            prefs.edit().putBoolean("logging_enabled",originalLogging).putBoolean("menu_ptbr",originalPt).commit();
        }
    }
}
