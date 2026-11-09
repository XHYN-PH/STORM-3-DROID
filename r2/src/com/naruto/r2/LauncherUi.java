/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import com.naruto.port.LauncherActivity;
import a.c;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** New controls use the launcher's existing cards, buttons and animated switches. */
public final class LauncherUi {
    private static final int EXPORT=8201;
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    public static void install(LauncherActivity a){
        LinearLayout root=null;
        if(!a.o.isEmpty()){
            View v=(View)a.o.get(0);
            while(v.getParent() instanceof View){View parent=(View)v.getParent();if((parent instanceof ScrollView||parent.getClass().getName().equals("a.i"))&&v instanceof LinearLayout){root=(LinearLayout)v;break;}v=parent;}
        }
        if(root==null)throw new IllegalStateException("Stock settings container was not found.");
        add(a,root,a.q("More options",buttons(a,new String[]{"Display","Controls","Saves","Tools"},new Runnable[]{()->display(a),()->controls(a),()->saves(a),()->tools(a)})));

    }
    private static void display(LauncherActivity a){LinearLayout body=dialogBody(a);
        body.addView(dialogToggle(a,"Opening video","Show the launcher video","r2_video",true));
        body.addView(dialogToggle(a,"Performance history","FPS and estimated frame-time history","r2_graph",false));
        body.addView(buttons(a,new String[]{"Screen mode","Brightness","Shadows"},new Runnable[]{()->screen(a),()->gamma(a),()->choice(a,"Shadow quality","r2_shadow",new String[]{"Auto (original)","360 · lower","720 · original cap","1080 · higher"},new int[]{0,360,720,1080},0,null)}));
        new AlertDialog.Builder(a).setTitle("Display").setView(body).setMessage("Changes apply the next time the game starts.").setPositiveButton("Done",null).show();}
    private static void controls(LauncherActivity a){new AlertDialog.Builder(a).setTitle("Controls").setItems(new String[]{"Stick settings","Rumble","Button labels"},(d,w)->{if(w==0)input(a);else if(w==1)rumble(a);else new AlertDialog.Builder(a).setTitle("Controller labels").setSingleChoiceItems(new String[]{"Xbox","PlayStation"},GameHooks.prefs(a).getBoolean("r2_ps_labels",false)?1:0,(dd,i)->{GameHooks.prefs(a).edit().putBoolean("r2_ps_labels",i==1).apply();dd.dismiss();}).show();}).show();}
    private static void saves(LauncherActivity a){new AlertDialog.Builder(a).setTitle("Save files").setItems(new String[]{"Export current save","Manage backups"},(d,w)->{if(w==0)export(a,null,false);else backups(a);}).show();}
    private static void tools(LauncherActivity a){new AlertDialog.Builder(a).setTitle("Tools").setItems(new String[]{"GPU memory budget","Streaming cache","Driver information","Export diagnostics"},(d,w)->{if(w==0)choice(a,"GPU memory budget","r2_budget",new String[]{"Auto (recommended)","768 MB","1024 MB","1400 MB","2048 MB"},new int[]{0,768,1024,1400,2048},0,null);else if(w==1)choice(a,"Streaming cache","r2_cache",new String[]{"Auto (recommended)","64 MB","128 MB","256 MB"},new int[]{0,64,128,256},0,null);else if(w==2)driver(a);else export(a,null,true);}).show();}
    private static void add(LauncherActivity a,LinearLayout root,View card){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=a.b(6);root.addView(card,lp);}
    private static LinearLayout toggle(LauncherActivity a,String title,String detail,String key,boolean initial){c control=new c(a,GameHooks.prefs(a).getBoolean(key,initial));control.setListener(on->GameHooks.prefs(a).edit().putBoolean(key,on).apply());return a.p(title,detail,control);}
    private static LinearLayout buttons(LauncherActivity a,String[] titles,Runnable[] tasks){LinearLayout row=new LinearLayout(a);for(int i=0;i<titles.length;i++){final Runnable task=tasks[i];TextView b=a.g(titles[i],0,0xfff5e6bd);b.setTextSize(11);b.setOnClickListener(v->{LauncherActivity.h(v);task.run();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,a.b(36),1);lp.setMargins(a.b(2),0,a.b(2),0);row.addView(b,lp);}return row;}
    private static void choice(LauncherActivity a,String title,String key,String[] names,int[] values,int initial,Runnable after){int current=GameHooks.prefs(a).getInt(key,initial),selected=0;for(int i=0;i<values.length;i++)if(values[i]==current)selected=i;new AlertDialog.Builder(a).setTitle(title).setSingleChoiceItems(names,selected,(dialog,index)->{GameHooks.prefs(a).edit().putInt(key,values[index]).apply();if(after!=null)after.run();dialog.dismiss();}).setNegativeButton("Cancel",null).show();}
    private interface FloatValue{void set(float value);}
    private static View slider(LauncherActivity a,String label,float initial,float min,float max,FloatValue changed){LinearLayout row=new LinearLayout(a);row.setOrientation(1);TextView text=new TextView(a);text.setTextColor(0xfff5e6bd);text.setText(String.format(Locale.ROOT,"%s: %.2f",label,initial));row.addView(text);SeekBar bar=new SeekBar(a);bar.setMax(1000);bar.setProgress(Math.round((initial-min)/(max-min)*1000));bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int n,boolean user){float value=min+(max-min)*n/1000f;text.setText(String.format(Locale.ROOT,"%s: %.2f",label,value));if(user)changed.set(value);}});row.addView(bar);return row;}
    private static LinearLayout dialogBody(LauncherActivity a){LinearLayout body=new LinearLayout(a);body.setOrientation(1);body.setPadding(a.b(18),a.b(8),a.b(18),a.b(8));return body;}
    private static void gamma(LauncherActivity a){LinearLayout body=dialogBody(a);body.addView(slider(a,"Brightness / gamma",GameHooks.prefs(a).getFloat("r2_gamma",1),.5f,2,v->GameHooks.prefs(a).edit().putFloat("r2_gamma",v).apply()));new AlertDialog.Builder(a).setTitle("Brightness").setView(body).setNeutralButton("Reset",(d,w)->GameHooks.prefs(a).edit().remove("r2_gamma").apply()).setPositiveButton("Done",null).show();}
    private static void screen(LauncherActivity a){choice(a,"Screen mode","r2_screen",new String[]{"Fit · full image, correct proportions","Stretch · original fullscreen option","Crop · fill screen, correct proportions"},new int[]{0,1,2},GameHooks.prefs(a).getInt("menu_screen_v3",0),()->{int mode=GameHooks.prefs(a).getInt("r2_screen",0);GameHooks.prefs(a).edit().putInt("menu_screen_v3",mode==1?1:0).putBoolean("stretch",mode==1).apply();if(mode==2){LinearLayout body=dialogBody(a);TextView preview=new TextView(a);preview.setText("Cropping fills the display by cutting off part of the top/bottom or sides. It does not stretch the picture.");preview.setTextColor(0xfff5e6bd);body.addView(preview);body.addView(slider(a,"Crop amount",GameHooks.prefs(a).getInt("r2_crop",100)/100f,0,1,v->GameHooks.prefs(a).edit().putInt("r2_crop",Math.round(v*100)).apply()));new AlertDialog.Builder(a).setTitle("Fullscreen crop").setView(body).setNeutralButton("Reset",(d,w)->GameHooks.prefs(a).edit().remove("r2_crop").apply()).setPositiveButton("Done",null).show();}});}
    private static View dialogToggle(LauncherActivity a,String title,String detail,String key,boolean initial){View card=toggle(a,title,detail,key,initial);a.o.remove(card);return card;}
    private static void input(LauncherActivity a){LinearLayout body=dialogBody(a);android.content.SharedPreferences p=GameHooks.prefs(a);
        body.addView(slider(a,"Left stick dead zone",p.getFloat("r2_left_deadzone",.08f),0,.4f,v->p.edit().putFloat("r2_left_deadzone",v).apply()));
        body.addView(slider(a,"Right stick dead zone",p.getFloat("r2_right_deadzone",.08f),0,.4f,v->p.edit().putFloat("r2_right_deadzone",v).apply()));
        body.addView(slider(a,"Touch right-stick sensitivity",p.getFloat("r2_touch_gain",1),.25f,3,v->p.edit().putFloat("r2_touch_gain",v).apply()));
        body.addView(dialogToggle(a,"Invisible right stick","Drag a free area on the right half","r2_touch_right",true));
        body.addView(dialogToggle(a,"Invert right-stick Y","Touch and controller","r2_right_invert",false));
        body.addView(buttons(a,new String[]{"Right-stick axes"},new Runnable[]{()->choice(a,"Controller right-stick axes","r2_right_axes",new String[]{"Auto","Z / RZ","RX / RY"},new int[]{0,1,2},0,null)}));
        ScrollView scroll=new ScrollView(a);scroll.addView(body);new AlertDialog.Builder(a).setTitle("Stick settings").setView(scroll).setNeutralButton("Reset",(d,w)->{p.edit().remove("r2_left_deadzone").remove("r2_right_deadzone").remove("r2_touch_gain").remove("r2_touch_right").remove("r2_right_invert").remove("r2_right_axes").apply();}).setPositiveButton("Done",null).show();
    }
    private static void rumble(LauncherActivity a){LinearLayout body=dialogBody(a);body.addView(buttons(a,new String[]{"Output","Test vibration"},new Runnable[]{()->choice(a,"Rumble output","r2_rumble",new String[]{"Off","Auto · use the requested device","Phone","Controller","Phone and controller"},new int[]{0,1,2,3,4},1,()->Haptics.stop(a,-1)),()->Haptics.test(a)}));body.addView(slider(a,"Strength",GameHooks.prefs(a).getInt("r2_rumble_strength",70)/100f,0,1,v->GameHooks.prefs(a).edit().putInt("r2_rumble_strength",Math.round(v*100)).apply()));TextView text=new TextView(a);text.setText("Gameplay vibration follows feedback sent by the game. The test button checks your device's motor.");text.setTextColor(0xfff5e6bd);body.addView(text);new AlertDialog.Builder(a).setTitle("Rumble").setView(body).setPositiveButton("Done",(d,w)->Haptics.stop(a,-1)).setOnDismissListener(d->Haptics.stop(a,-1)).show();}
    private static void driver(LauncherActivity a){a.k driver=a.m();String text=driver==null?"System Vulkan driver":driver.a()+"\n"+driver.c+"\n"+driver.e;new AlertDialog.Builder(a).setTitle("GPU driver").setMessage(text+"\n\nDevice: "+Build.MODEL+"\nGPU driver imports remain limited to supported Qualcomm/Adreno devices.").setNeutralButton("Use system driver",(d,w)->a.l("")).setPositiveButton("Done",null).show();}
    private static void export(LauncherActivity a,File backup,boolean diagnostic){GameHooks.prefs(a).edit().putString("r2_pending_export",diagnostic?"diagnostic":backup==null?"save":"backup").putString("r2_pending_backup",backup==null?"":backup.getAbsolutePath()).apply();Intent pick=new Intent(Intent.ACTION_CREATE_DOCUMENT);pick.addCategory(Intent.CATEGORY_OPENABLE);pick.setType(diagnostic?"text/plain":"application/zip");pick.putExtra(Intent.EXTRA_TITLE,diagnostic?"STORM3-diagnostics.txt":"STORM3-save-"+System.currentTimeMillis()+".zip");a.startActivityForResult(pick,EXPORT);}
    private interface Work{String run()throws Exception;}
    private static void job(LauncherActivity a,String title,Work task){AlertDialog progress=new AlertDialog.Builder(a).setTitle(title).setMessage("Please wait…").setCancelable(false).show();worker.execute(()->{String message;try{message=task.run();}catch(Exception e){message="Could not complete the operation: "+e.getMessage();}String result=message;a.runOnUiThread(()->{if(!a.isFinishing()&&!a.isDestroyed()){progress.dismiss();if(result!=null)new AlertDialog.Builder(a).setMessage(result).setPositiveButton("OK",null).show();}});});}
    public static boolean activityResult(LauncherActivity a,int request,int result,Intent data){if(request!=EXPORT)return false;if(result!=Activity.RESULT_OK||data==null||data.getData()==null){GameHooks.prefs(a).edit().remove("r2_pending_export").remove("r2_pending_backup").apply();return true;}Uri uri=data.getData();String kind=GameHooks.prefs(a).getString("r2_pending_export","");String backupPath=GameHooks.prefs(a).getString("r2_pending_backup","");GameHooks.prefs(a).edit().remove("r2_pending_export").remove("r2_pending_backup").apply();if(!kind.equals("save")&&!kind.equals("backup")&&!kind.equals("diagnostic")){new AlertDialog.Builder(a).setMessage("The export request expired. Please try again.").setPositiveButton("OK",null).show();return true;}File backup=kind.equals("backup")?new File(backupPath):null;boolean diagnostic=kind.equals("diagnostic");job(a,"Export",()->{OutputStream stream=a.getContentResolver().openOutputStream(uri,"wt");if(stream==null)throw new IOException("The selected destination could not be opened.");try(OutputStream out=stream){if(diagnostic)out.write(report(a).getBytes(StandardCharsets.UTF_8));else try(SaveFiles files=new SaveFiles(a.getExternalFilesDir(null),a.getFilesDir())){if(backup==null)files.exportCurrent(out);else files.exportBackup(backup,out);}}a.runOnUiThread(()->{if(a.isFinishing()||a.isDestroyed())return;new AlertDialog.Builder(a).setTitle("Export saved").setMessage("Share the exported file?").setNegativeButton("Not now",null).setPositiveButton("Share",(d,w)->{Intent share=new Intent(Intent.ACTION_SEND).setType(diagnostic?"text/plain":"application/zip").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);share.setClipData(ClipData.newRawUri("STORM 3 export",uri));a.startActivity(Intent.createChooser(share,"Share export"));}).show();});return null;});return true;}
    private static String report(LauncherActivity a){
        android.content.SharedPreferences p=GameHooks.prefs(a);
        StringBuilder text=new StringBuilder("STORM 3 R2 diagnostics\nDevice: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append("\nAndroid: ").append(Build.VERSION.RELEASE).append("\nDriver: ").append(p.getString("gpu_driver","")).append("\nLogging: ").append(p.getBoolean("logging_enabled",false)).append('\n');
        for(Map.Entry<String,?> entry:p.getAll().entrySet())if((entry.getKey().startsWith("r2_")||entry.getKey().startsWith("menu_"))&&!entry.getKey().startsWith("r2_pending_"))text.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        if(p.getBoolean("logging_enabled",false)){
            LinkedHashSet<Integer> pids=new LinkedHashSet<>();pids.add(android.os.Process.myPid());
            ActivityManager manager=(ActivityManager)a.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes=manager==null?null:manager.getRunningAppProcesses();
            if(processes!=null)for(ActivityManager.RunningAppProcessInfo process:processes)if(process.processName.equals(a.getPackageName()+":game"))pids.add(process.pid);
            for(int pid:pids)collectLog(text,pid);
        }else text.append("Logging is off; no logs were collected.\n");
        return text.toString();
    }
    private static void collectLog(StringBuilder text,int pid){
        Process proc=null;
        try{
            proc=new ProcessBuilder("logcat","-d","-t","300","--pid="+pid).redirectErrorStream(true).start();
            text.append("\nProcess ").append(pid).append(" logs:\n");
            try(Reader in=new InputStreamReader(proc.getInputStream(),StandardCharsets.UTF_8)){char[] buffer=new char[4096];int n,total=0;while((n=in.read(buffer))!=-1&&total<262144){text.append(buffer,0,n);total+=n;}}
        }catch(IOException e){text.append("Log collection unavailable.\n");}
        finally{if(proc!=null)proc.destroy();}
    }
    private static void backups(LauncherActivity a){try(SaveFiles files=new SaveFiles(a.getExternalFilesDir(null),a.getFilesDir())){File[] entries=files.list();String[] names=new String[entries.length];for(int i=0;i<entries.length;i++)names[i]=entries[i].getName();new AlertDialog.Builder(a).setTitle("Save backups").setItems(names,(d,w)->backupActions(a,entries[w])).setNeutralButton("Create backup",(d,w)->job(a,"Save backup",()->{try(SaveFiles saves=new SaveFiles(a.getExternalFilesDir(null),a.getFilesDir())){return "Backup created: "+saves.backupCurrent().getName();}})).setNegativeButton("Done",null).show();}catch(IOException e){new AlertDialog.Builder(a).setMessage(e.getMessage()).setPositiveButton("OK",null).show();}}
    private static void backupActions(LauncherActivity a,File backup){new AlertDialog.Builder(a).setTitle(backup.getName()).setItems(new String[]{"Export backup","Restore backup","Delete backup"},(d,w)->{if(w==0){export(a,backup,false);return;}new AlertDialog.Builder(a).setTitle(w==1?"Restore this backup?":"Delete this backup?").setMessage(w==1?"The current save will be backed up first. Restoring is blocked while the 100% save is active.":"Only this backup will be removed. The active save and bundled 100% save are kept.").setNegativeButton("Cancel",null).setPositiveButton(w==1?"Restore":"Delete",(dd,ww)->job(a,"Save backups",()->{try(SaveFiles files=new SaveFiles(a.getExternalFilesDir(null),a.getFilesDir())){if(w==1)files.restore(backup);else files.deleteBackup(backup);return w==1?"Backup restored.":"Backup deleted.";}})).show();}).show();}
}
