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
        View optionsTitle=named(views,"More options");List<View> optionViews=new ArrayList<>();if(optionsTitle!=null)collect((View)optionsTitle.getParent(),optionViews);
        check(hasText(optionViews,"Display")&&hasText(optionViews,"Saves")&&hasText(optionViews,"Tools")&&!hasText(optionViews,"Performance"),"Added presets are removed while the stock Performance selector is retained");
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
        testOverlays(context,loader);
        Class<?> math=Class.forName("com.naruto.r2.GameHooks",true,loader);
        check(Math.abs(((Float)math.getMethod("cropScale",float.class,float.class,int.class).invoke(null,2400f,1080f,100))-1.25f)<.001f,"Proportional crop uses equal scale for both dimensions");
        testModernMenu(context,loader);
        testShutdownAndLookup(context,loader);
        testSessionDetailsAndClosing(context,loader);
        Class<?> game=Class.forName("com.naruto.port.NarutoActivity",true,loader);
        for(String bridge:new String[]{"r2ShaderPrepare","r2StatsValues","r2SetTouch","r2SetShowStats","r2ShadowSet"}){boolean exposed=false;for(Method method:game.getDeclaredMethods())if(method.getName().equals(bridge))exposed=java.lang.reflect.Modifier.isPublic(method.getModifiers())&&java.lang.reflect.Modifier.isStatic(method.getModifiers());check(exposed,"Native bridge is accessible to runtime helpers: "+bridge);}

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
            check(hasText(views,"Control opacity")&&hasText(views,"Brightness")&&hasText(views,"Touch controls")&&hasText(views,"FPS / statistics size"),"Expanded game menu contains live controls");
            check(!hasText(views,"Rumble")&&!hasText(views,"Performance history")&&!hasText(views,"Export diagnostics"),"Removed features do not appear in the game menu");
            View fps=named(views,"Show FPS");((View)fps.getParent().getParent()).performClick();check(calls.contains("stats"),"Game-menu FPS switch invokes its action");
        }catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
        waitForIdleSync();screenshot(context,"game-menu");runOnMainSync(()->dialog[0].getButton(-1).performClick());waitForIdleSync();
        check(!dialog[0].isShowing(),"Resume closes the game menu");
        final int[] exits={0};
        runOnMainSync(()->{try{dialog[0]=(android.app.AlertDialog)menu.getMethod("confirmExit",Activity.class,Runnable.class).invoke(null,launcher,(Runnable)()->exits[0]++);check(dialog[0].isShowing(),"Exit confirmation opens before leaving");dialog[0].getButton(-2).performClick();check(exits[0]==0,"Cancelling exit keeps the game running");}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
        waitForIdleSync();
        runOnMainSync(()->{try{dialog[0]=(android.app.AlertDialog)menu.getMethod("confirmExit",Activity.class,Runnable.class).invoke(null,launcher,(Runnable)()->exits[0]++);dialog[0].getButton(-1).performClick();}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);waitForIdleSync();check(exits[0]==1,"Confirmed exit invokes its supplied action exactly once");

    }
    private android.graphics.Bitmap region(View view) {
        waitForIdleSync(); SystemClock.sleep(100);
        final int[] xy=new int[2]; runOnMainSync(()->view.getLocationOnScreen(xy));
        android.graphics.Bitmap screen=getUiAutomation().takeScreenshot();
        android.graphics.Bitmap result=android.graphics.Bitmap.createBitmap(screen,xy[0],xy[1],view.getWidth(),view.getHeight());
        if(result!=screen)screen.recycle(); return result;
    }
    private boolean samePixels(android.graphics.Bitmap left,android.graphics.Bitmap right) {
        if(left.getWidth()!=right.getWidth()||left.getHeight()!=right.getHeight())return false;
        int[] a=new int[left.getWidth()*left.getHeight()],b=new int[a.length];
        left.getPixels(a,0,left.getWidth(),0,0,left.getWidth(),left.getHeight());
        right.getPixels(b,0,right.getWidth(),0,0,right.getWidth(),right.getHeight());
        int changed=0;
        for(int i=0;i<a.length;i++)if(a[i]!=b[i]) {
            int delta=Math.max(Math.abs(android.graphics.Color.red(a[i])-android.graphics.Color.red(b[i])),Math.max(Math.abs(android.graphics.Color.green(a[i])-android.graphics.Color.green(b[i])),Math.abs(android.graphics.Color.blue(a[i])-android.graphics.Color.blue(b[i]))));
            if(delta>8)changed++;
        }
        return changed<=a.length/1000;
    }
    private void testOverlays(Context context,ClassLoader loader)throws Exception {
        Class<?> touchClass=Class.forName("a.f0",true,loader),listenerClass=Class.forName("a.d0",true,loader),render=Class.forName("com.naruto.r2.TouchRender",true,loader),stats=Class.forName("a.a0",true,loader),statsHelper=Class.forName("com.naruto.r2.StatsOverlay",true,loader);
        final android.widget.FrameLayout[] frame={null}; final View[] touchView={null},statsView={null}; final Throwable[] error={null};
        final List<int[]> inputs=new ArrayList<>(); Object listener=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{listenerClass},(proxy,method,args)->{if(method.getName().equals("c")){int[] state=new int[7];for(int i=0;i<7;i++)state[i]=(Integer)args[i];inputs.add(state);}return null;});
        android.content.SharedPreferences prefs=context.getSharedPreferences("naruto",0); prefs.edit().remove("r2_stats_scale").remove("stats_collapsed").commit();
        try {
            runOnMainSync(()->{try{
                frame[0]=new android.widget.FrameLayout(launcher);frame[0].setBackgroundColor(android.graphics.Color.BLACK);
                launcher.addContentView(frame[0],new ViewGroup.LayoutParams(-1,-1));
                touchView[0]=(View)touchClass.getConstructor(Activity.class,listenerClass).newInstance(launcher,listener);
                frame[0].addView(touchView[0],new android.widget.FrameLayout.LayoutParams(-1,-1));
                touchClass.getMethod("setOpacity",float.class).invoke(touchView[0],.7f);
            }catch(Throwable e){error[0]=e;}}); if(error[0]!=null)throw new RuntimeException(error[0]);
            waitForIdleSync();check(touchView[0].isHardwareAccelerated(),"Touch cache is tested on a real hardware Canvas");
            Field stateField=touchClass.getField("r2RenderState");render.getMethod("invalidate",touchClass).invoke(null,touchView[0]);
            Object state=stateField.get(touchView[0]);Field enabled=state.getClass().getField("enabled"),recordings=state.getClass().getField("recordings"),hits=state.getClass().getField("hits");
            for(int mask:new int[]{0,8192,4096,32768}) {
                runOnMainSync(()->{try{enabled.setBoolean(state,false);touchClass.getField("D").setInt(touchView[0],mask);touchView[0].invalidate();}catch(Throwable e){error[0]=e;}});
                android.graphics.Bitmap original=region(touchView[0]);
                runOnMainSync(()->{try{enabled.setBoolean(state,true);touchView[0].invalidate();}catch(Throwable e){error[0]=e;}});
                android.graphics.Bitmap cached=region(touchView[0]);
                check(samePixels(original,cached),"Cached stock touch artwork matches original rendering, button mask "+mask);original.recycle();cached.recycle();
            }
            int recorded=recordings.getInt(state),hit=hits.getInt(state);
            runOnMainSync(()->touchView[0].invalidate());waitForIdleSync();SystemClock.sleep(100);
            check(recordings.getInt(state)==recorded&&hits.getInt(state)>hit,"Unchanged buttons reuse recorded artwork without rebuilding gradients");
            runOnMainSync(()->{try{touchClass.getField("D").setInt(touchView[0],0);touchView[0].invalidate();}catch(Throwable e){error[0]=e;}});
            android.graphics.Bitmap gameplay=region(touchView[0]);
            runOnMainSync(()->{try{touchClass.getMethod("setEditing",boolean.class).invoke(touchView[0],true);}catch(Throwable e){error[0]=e;}});
            waitForIdleSync();SystemClock.sleep(100);
            runOnMainSync(()->{try{touchClass.getMethod("setEditing",boolean.class).invoke(touchView[0],false);}catch(Throwable e){error[0]=e;}});
            android.graphics.Bitmap afterEditor=region(touchView[0]);
            check(samePixels(gameplay,afterEditor),"Leaving the stock layout editor redraws gameplay even when the layout is unchanged");gameplay.recycle();afterEditor.recycle();
            runOnMainSync(()->{try{
                Object[] buttons=(Object[])touchClass.getField("a").get(touchView[0]);Object button=buttons[1];float x=button.getClass().getField("j").getFloat(button),y=button.getClass().getField("k").getFloat(button);
                touchClass.getField("D").setInt(touchView[0],0);inputs.clear();
                long t=SystemClock.uptimeMillis();
                for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE,MotionEvent.ACTION_CANCEL}){MotionEvent event=MotionEvent.obtain(t,t+10,action,x,y,0);touchView[0].onTouchEvent(event);event.recycle();}
                check(inputs.size()>=2&&(inputs.get(0)[0]&4096)!=0&&inputs.get(inputs.size()-1)[0]==0,"Stock touch press and cancellation still deliver native button transitions immediately");
                statsView[0]=(View)stats.getConstructor(Activity.class,SharedPreferences.class).newInstance(launcher,prefs);
                statsHelper.getMethod("resize",stats).invoke(null,statsView[0]);
                float d=context.getResources().getDisplayMetrics().density;
                frame[0].addView(statsView[0],new android.widget.FrameLayout.LayoutParams(Math.round(230*d),Math.round(100*d)));
                stats.getMethod("setValues",float[].class).invoke(statsView[0],(Object)new float[]{30,20,2000,40,600,2,60});
            }catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
            long until=SystemClock.uptimeMillis()+5000;
            while(stats.getField("a").get(statsView[0])==null&&SystemClock.uptimeMillis()<until)SystemClock.sleep(20);
            waitForIdleSync();check(stats.getField("a").get(statsView[0])!=null&&statsView[0].getVisibility()==View.VISIBLE,"Statistics sample publishes after asynchronous memory collection");
            check(Math.abs(statsView[0].getScaleX()-.7f)<.001f&&Math.abs(statsView[0].getScaleY()-.7f)<.001f,"Stock statistics panel defaults to 70 percent in both dimensions");
            runOnMainSync(()->{try{prefs.edit().putFloat("r2_stats_scale",.5f).apply();statsHelper.getMethod("resize",stats).invoke(null,statsView[0]);statsView[0].setX(100000);statsView[0].setY(100000);statsHelper.getMethod("clamp",stats).invoke(null,statsView[0]);}catch(Throwable e){error[0]=e;}});
            waitForIdleSync();check(statsView[0].getScaleX()==.5f&&statsView[0].getX()+statsView[0].getWidth()*.5f<=frame[0].getWidth()+1&&statsView[0].getY()+statsView[0].getHeight()*.5f<=frame[0].getHeight()+1,"Changing panel size keeps its visible bounds on screen");
            runOnMainSync(()->{try{
                float d=context.getResources().getDisplayMetrics().density;long t=SystemClock.uptimeMillis();
                for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){MotionEvent e=MotionEvent.obtain(t,t+10,action,statsView[0].getWidth()-16*d,14*d,0);statsView[0].onTouchEvent(e);e.recycle();}
                check(stats.getField("i").getBoolean(statsView[0]),"Stock collapse gesture still works on the resized panel");
                stats.getMethod("setValues",float[].class).invoke(statsView[0],(Object)new float[]{30,20,2000,40,600});
                stats.getMethod("setValues",float[].class).invoke(statsView[0],(Object)null);
            }catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);
            SystemClock.sleep(300);waitForIdleSync();check(statsView[0].getVisibility()==View.GONE&&stats.getField("a").get(statsView[0])==null,"Late background samples cannot resurrect a hidden statistics panel");
            runOnMainSync(()->{try{statsHelper.getMethod("sizeDialog",Activity.class,stats).invoke(null,launcher,statsView[0]);}catch(Throwable e){error[0]=e;}});waitForIdleSync();
            check(!getUiAutomation().getRootInActiveWindow().findAccessibilityNodeInfosByText("FPS / statistics size").isEmpty(),"FPS size adjustment uses the existing dialog style");
            sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);waitForIdleSync();
        } finally {
            prefs.edit().remove("r2_stats_scale").remove("stats_collapsed").commit();
            runOnMainSync(()->{if(frame[0]!=null&&frame[0].getParent() instanceof ViewGroup)((ViewGroup)frame[0].getParent()).removeView(frame[0]);});
        }
    }
    private void testSessionDetailsAndClosing(Context context,ClassLoader loader)throws Exception {
        Class<?> details=Class.forName("com.naruto.r2.SessionDetails",true,loader),exit=Class.forName("com.naruto.r2.ExitFeedback",true,loader);
        File external=context.getExternalFilesDir(null),privateFile=new File(context.getFilesDir(),"r2-last-session.txt"),log=new File(external,"naruto.log"),gpu=new File(external,"gpu_info.txt"),shared=new File(external,"session-details.txt");
        File[] files={privateFile,log,gpu,shared};byte[][] previous=new byte[files.length][];
        for(int i=0;i<files.length;i++)if(files[i].isFile())previous[i]=java.nio.file.Files.readAllBytes(files[i].toPath());
        SharedPreferences prefs=context.getSharedPreferences("naruto",0);boolean hadDirectory=prefs.contains("r23_private_test"),hadToken=prefs.contains("r23_token_test");String oldDirectory=prefs.getString("r23_private_test",null),oldToken=prefs.getString("r23_token_test",null);
        final java.util.concurrent.atomic.AtomicReference<Intent> captured=new java.util.concurrent.atomic.AtomicReference<>();
        ActivityMonitor monitor=new ActivityMonitor(){@Override public ActivityResult onStartActivity(Intent intent){if(Intent.ACTION_CHOOSER.equals(intent.getAction())){captured.set(intent);return new ActivityResult(Activity.RESULT_CANCELED,null);}return null;}};
        try {
            long now=System.currentTimeMillis();String session="Session started epoch ms: "+(now-1000)+"\nSnapshot epoch ms: "+now+"\nLast foreground Android display rate: 60.00 Hz\nPhase: paused\nApp: STORM 3 R2.3\n";
            java.nio.file.Files.write(privateFile.toPath(),session.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            java.nio.file.Files.write(log.toPath(),"0.1 Render resolution 1600x900 (1.25x)\n1.0 AMD optical FG: 2x (asked 2x, screen 121 Hz)\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            java.nio.file.Files.write(gpu.toPath(),"GPU: Turnip Adreno (TM) 750\nvendor 5143, driver test, Vulkan 1.4\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            prefs.edit().putString("r23_private_test","PRIVATE_GAME_PATH_DO_NOT_SHARE").putString("r23_token_test","PRIVATE_TOKEN_DO_NOT_SHARE").commit();
            Method selected=details.getDeclaredMethod("selected",Context.class);selected.setAccessible(true);String current=(String)selected.invoke(null,launcher);
            String report=(String)details.getMethod("prepare",Context.class,String.class).invoke(null,context,current);
            check(report.contains("App: STORM 3 R2.3")&&report.contains("Refresh selected:")&&report.contains("Frame generation selected:"),"Stock log details include app version and selected graphics settings");
            check(report.contains("60.00 Hz")&&report.contains("2x / 2x / 121 Hz")&&report.contains("1600x900"),"Foreground display observations and final engine reports remain separately labelled");
            check(!report.contains("PRIVATE_GAME_PATH_DO_NOT_SHARE")&&!report.contains("PRIVATE_TOKEN_DO_NOT_SHARE"),"Log details never dump arbitrary preferences or game paths");
            android.net.Uri uri=android.net.Uri.parse("content://"+context.getPackageName()+".logs/session-details.txt");
            try(java.io.InputStream stream=context.getContentResolver().openInputStream(uri)){check(stream!=null&&stream.read()=='S',"Original LogProvider can share the new metadata attachment");}
            try{context.getContentResolver().openInputStream(android.net.Uri.parse("content://"+context.getPackageName()+".logs/r2-last-session.txt"));throw new AssertionError("Private metadata exposed");}catch(java.io.FileNotFoundException expected){check(true,"Provider whitelist still rejects private files");}
            log.setLastModified(now-60000);report=(String)details.getMethod("prepare",Context.class,String.class).invoke(null,context,current);
            check(report.contains("Not established")&&!report.contains("Engine render resolution:"),"Old renderer logs are not presented as observations from the current session");
            log.setLastModified(System.currentTimeMillis());addMonitor(monitor);
            runOnMainSync(()->{try{launcher.getClass().getMethod("n",boolean.class).invoke(launcher,false);}catch(Exception e){throw new RuntimeException(e);}});
            long deadline=SystemClock.uptimeMillis()+5000;while(captured.get()==null&&SystemClock.uptimeMillis()<deadline)SystemClock.sleep(20);
            check(captured.get()!=null,"Original Send log still opens its normal share chooser");
            Intent payload=(Intent)captured.get().getParcelableExtra(Intent.EXTRA_INTENT);java.util.ArrayList<android.net.Uri> attachments=payload.getParcelableArrayListExtra(Intent.EXTRA_STREAM);boolean foundNative=false,foundDetails=false;
            for(android.net.Uri attachment:attachments){foundNative|="naruto.log".equals(attachment.getLastPathSegment());foundDetails|="session-details.txt".equals(attachment.getLastPathSegment());}
            check(foundNative&&foundDetails&&(payload.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0,"The existing share retains native logs and adds readable session details");
        } finally {
            removeMonitor(monitor);SharedPreferences.Editor restore=prefs.edit();if(hadDirectory)restore.putString("r23_private_test",oldDirectory);else restore.remove("r23_private_test");if(hadToken)restore.putString("r23_token_test",oldToken);else restore.remove("r23_token_test");restore.commit();
            for(int i=0;i<files.length;i++){if(previous[i]==null)java.nio.file.Files.deleteIfExists(files[i].toPath());else java.nio.file.Files.write(files[i].toPath(),previous[i]);}
        }
        File marker=new File(context.getFilesDir(),"r2-exit.pending"),lockFile=new File(context.getFilesDir(),"r2-game.lock");
        try(java.io.RandomAccessFile file=new java.io.RandomAccessFile(lockFile,"rw")) {
            java.nio.channels.FileLock lock=file.getChannel().lock();
            try {
                java.nio.file.Files.write(marker.toPath(),"closing\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                runOnMainSync(()->{try{exit.getMethod("watch",Activity.class).invoke(null,launcher);}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();
                check(!getUiAutomation().getRootInActiveWindow().findAccessibilityNodeInfosByText("Closing game").isEmpty(),"Closing feedback uses the launcher's existing dialog style");
                SystemClock.sleep(200);check(lock.isValid()&&marker.exists(),"Closing feedback waits without releasing the game/save lock");
                runOnMainSync(()->{try{exit.getMethod("destroy",Activity.class).invoke(null,launcher);}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();
                check(marker.exists()&&lock.isValid(),"Activity cleanup cancels only its watcher, preserving shutdown state and saves");
                runOnMainSync(()->{try{exit.getMethod("watch",Activity.class).invoke(null,launcher);}catch(Exception e){throw new RuntimeException(e);}});lock.release();
                long deadline=SystemClock.uptimeMillis()+5000;while(marker.exists()&&SystemClock.uptimeMillis()<deadline)SystemClock.sleep(20);waitForIdleSync();
                check(!marker.exists()&&getUiAutomation().getRootInActiveWindow().findAccessibilityNodeInfosByText("Closing game").isEmpty(),"Reopened closing feedback clears only after the owner releases its lock");
            } finally {if(lock.isValid())lock.release();runOnMainSync(()->{try{exit.getMethod("destroy",Activity.class).invoke(null,launcher);}catch(Exception e){throw new RuntimeException(e);}});marker.delete();}
        }
    }
    @Override public void onStart() {
        Thread watchdog=new Thread(()->{try{Thread.sleep(120000);}catch(InterruptedException done){return;}if(completed)return;StringBuilder trace=new StringBuilder();for(java.util.Map.Entry<Thread,StackTraceElement[]> entry:Thread.getAllStackTraces().entrySet())if(entry.getKey().getName().equals("main")||entry.getKey().getName().contains("Instr")){trace.append(entry.getKey().getName()).append('\n');int n=0;for(StackTraceElement frame:entry.getValue()){trace.append(frame).append('\n');if(++n==12)break;}}Bundle status=new Bundle();status.putString("stream",trace+"R2_STALL_STAGE="+lastProgress+"\n");sendStatus(1,status);},"R2-watchdog");watchdog.setDaemon(true);watchdog.start();
        Bundle result=new Bundle(); Context context=getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("naruto", Context.MODE_PRIVATE);
        boolean originalLogging=prefs.getBoolean("logging_enabled",false);
        boolean originalPt=prefs.getBoolean("menu_ptbr",false);
        try {
            check("R2.3".equals(context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName),"Installed version is R2.3");
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
            Method earlyCutoff=control.getDeclaredMethod("nativeEarlyCutoff");earlyCutoff.setAccessible(true);
            check(Boolean.TRUE.equals(earlyCutoff.invoke(null)),"Logging off selects the early cutoff in the engine ABI");
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
