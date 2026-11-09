/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import com.naruto.port.ControllerMapActivity;
import android.app.AlertDialog;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Adds visualization/reset to the stock mapping screen without replacing its grid. */
public final class ControllerUi extends View {
    private static final java.util.WeakHashMap<ControllerMapActivity,java.lang.ref.WeakReference<ControllerUi>> states=new java.util.WeakHashMap<>();
    public static void onMotion(ControllerMapActivity a,MotionEvent e){java.lang.ref.WeakReference<ControllerUi> r=states.get(a);ControllerUi v=r==null?null:r.get();if(v!=null)v.motion(e);}
    public static void onKey(ControllerMapActivity a,KeyEvent e){java.lang.ref.WeakReference<ControllerUi> r=states.get(a);ControllerUi v=r==null?null:r.get();if(v!=null)v.key(e);}
    private final ControllerMapActivity activity;
    private final Paint p=new Paint(3);
    private final HashSet<Integer> pressed=new HashSet<>();
    private final String[] actions={"lt","rt","lb","rb","up","down","left","right","l3","r3","back","start","x","y","a","b"};
    private final int[] keys={104,105,102,103,19,20,21,22,106,107,109,108,99,100,96,97};
    private final float[] xs={.13f,.87f,.13f,.87f,.30f,.30f,.24f,.36f,.20f,.66f,.45f,.55f,.75f,.83f,.83f,.91f};
    private final float[] ys={.09f,.09f,.25f,.25f,.48f,.72f,.60f,.60f,.43f,.73f,.48f,.48f,.49f,.36f,.62f,.49f};
    private float lx,ly,rx,ry,lt,rt;
    private String device="Connect a controller to test its inputs";
    private int lastKey=-1;
    public ControllerUi(ControllerMapActivity a){super(a);activity=a;setContentDescription("Controller diagram. Tap a button to remap it; physical inputs light up.");}
    private static LinearLayout findContent(View v){
        if((v instanceof ScrollView||v.getClass().getName().equals("a.i"))&&((ViewGroup)v).getChildCount()>0&&((ViewGroup)v).getChildAt(0) instanceof LinearLayout)return (LinearLayout)((ViewGroup)v).getChildAt(0);
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout found=findContent(g.getChildAt(i));if(found!=null)return found;}}return null;
    }
    public static void install(ControllerMapActivity a){
        LinearLayout content=findContent(a.getWindow().getDecorView());if(content==null)return;
        View grid=content.getChildAt(2);grid.setVisibility(View.GONE);
        ControllerUi diagram=new ControllerUi(a);content.addView(diagram,Math.min(2,content.getChildCount()),new LinearLayout.LayoutParams(-1,GameHooks.dp(a,190)));
        LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER);
        add(a,row,"Reset selected",()->reset(a,0));add(a,row,"Reset player",()->reset(a,1));add(a,row,"Reset all",()->reset(a,2));add(a,row,"List / diagram",()->{boolean show=grid.getVisibility()!=View.VISIBLE;grid.setVisibility(show?View.VISIBLE:View.GONE);diagram.setVisibility(show?View.GONE:View.VISIBLE);});
        content.addView(row,Math.min(3,content.getChildCount()));states.put(a,new java.lang.ref.WeakReference<>(diagram));
    }
    private static void add(ControllerMapActivity a,LinearLayout row,String text,Runnable task){TextView b=a.d(text,0x334b5360,0xfff5e6bd);b.setOnClickListener(v->task.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,GameHooks.dp(a,36),1);lp.setMargins(3,3,3,3);row.addView(b,lp);}
    private static void reset(ControllerMapActivity a,int mode){
        if(mode==0&&a.h==null){new AlertDialog.Builder(a).setMessage("Select a mapping first.").setPositiveButton("OK",null).show();return;}
        new AlertDialog.Builder(a).setTitle(mode==2?"Reset all mappings?":mode==1?"Reset this player's mappings?":"Reset selected mapping?")
            .setMessage("Restore the original controller assignments.").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{
                if(mode==0){for(String[] mapping:ControllerMapActivity.s)if(mapping[0].equals(a.h)){a.c(mapping[3]);break;}a.b();a.e();return;}
                android.content.SharedPreferences.Editor edit=a.a.edit();
                for(int player=1;player<=2;player++)if(mode==2||player==a.b)for(String[] mapping:ControllerMapActivity.s)if(mode!=0||mapping[0].equals(a.h))edit.remove((player==2?"pad2_":"pad_")+mapping[0]);
                edit.apply();a.h(null);a.b();a.e();a.f();
            }).show();
    }
    public void motion(MotionEvent e){if((e.getSource()&InputDevice.SOURCE_JOYSTICK)!=InputDevice.SOURCE_JOYSTICK)return;
        InputDevice d=e.getDevice();if(d!=null)device=d.getName();
        lx=e.getAxisValue(0);ly=e.getAxisValue(1);
        boolean useRx=d!=null&&d.getMotionRange(12,e.getSource())!=null&&d.getMotionRange(13,e.getSource())!=null;
        int selected=GameHooks.prefs(activity).getInt("r2_right_axes",0);if(selected==1)useRx=false;if(selected==2)useRx=true;
        rx=e.getAxisValue(useRx?12:11);ry=e.getAxisValue(useRx?13:14);lt=Math.max(e.getAxisValue(17),e.getAxisValue(23));rt=Math.max(e.getAxisValue(18),e.getAxisValue(22));invalidate();}
    public void key(KeyEvent e){lastKey=e.getKeyCode();if(e.getAction()==KeyEvent.ACTION_DOWN)pressed.add(e.getKeyCode());else pressed.remove(e.getKeyCode());InputDevice d=e.getDevice();if(d!=null)device=d.getName();invalidate();}
    private float padWidth(){return Math.min(getWidth(),getHeight()*2.6f);}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_UP){float width=padWidth();if(width<=0||getHeight()<=0)return true;float x=(e.getX()-(getWidth()-width)/2)/width,y=e.getY()/getHeight();for(int i=0;i<actions.length;i++)if(Math.hypot(x-xs[i],y-ys[i])<.075f){activity.h(actions[i]);invalidate();return true;}}return true;}
    @Override protected void onDraw(Canvas c){float w=padWidth(),h=getHeight();int saved=c.save();c.translate((getWidth()-w)/2,0);
        Path body=new Path();body.moveTo(w*.10f,h*.25f);
        body.cubicTo(w*.13f,h*.13f,w*.28f,h*.13f,w*.35f,h*.23f);body.lineTo(w*.70f,h*.23f);
        body.cubicTo(w*.79f,h*.13f,w*.92f,h*.16f,w*.95f,h*.29f);
        body.cubicTo(w*.99f,h*.49f,w*.99f,h*.88f,w*.89f,h*.91f);
        body.cubicTo(w*.82f,h*.95f,w*.77f,h*.79f,w*.71f,h*.80f);body.lineTo(w*.37f,h*.80f);
        body.cubicTo(w*.29f,h*.79f,w*.23f,h*.96f,w*.14f,h*.92f);
        body.cubicTo(w*.03f,h*.90f,w*.05f,h*.48f,w*.10f,h*.25f);body.close();
        p.setStyle(Paint.Style.FILL);p.setColor(0xee17232a);c.drawPath(body,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(h*.007f);p.setColor(0xffb6a16b);c.drawPath(body,p);p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.052f);p.setColor(0xfff5e6bd);
        String caption=lastKey<0?device:device+" · key "+lastKey;c.drawText(caption,w*.52f,h*.99f,p);
        for(int i=0;i<actions.length;i++){
            boolean active=pressed.contains(keys[i])||(i==0&&lt>.5f)||(i==1&&rt>.5f),stick=i==8||i==9;
            float x=xs[i]*w,y=ys[i]*h,r=h*(stick?.10f:.065f);
            p.setStyle(Paint.Style.FILL);p.setColor(active||actions[i].equals(activity.h)?0xffd49d30:0xff33434b);
            if(i<2){c.drawRoundRect(x-h*.13f,y-h*.045f,x+h*.13f,y+h*.06f,h*.025f,h*.025f,p);p.setColor(0xffe7ac35);float value=i==0?lt:rt;c.drawRect(x-h*.12f,y+h*.05f,x-h*.12f+h*.24f*Math.max(0,Math.min(1,value)),y+h*.065f,p);}
            else c.drawCircle(x,y,r,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(h*.005f);p.setColor(0xffafbec5);if(i>=2)c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);
            p.setColor(0xfff5e6bd);p.setTextSize(h*.05f);c.drawText(label(actions[i]),x,stick?y+h*.15f:y-(p.ascent()+p.descent())/2,p);
        }
        stick(c,w*.20f,h*.43f,lx,ly,h);stick(c,w*.66f,h*.73f,rx,ry,h);
        p.setColor(0xfff5e6bd);p.setTextSize(h*.043f);c.drawText(String.format(Locale.ROOT,"Raw L %+.2f %+.2f   R %+.2f %+.2f   LT %.2f RT %.2f   R3 %s",lx,ly,rx,ry,lt,rt,pressed.contains(107)?"ON":"OFF"),w*.52f,h*.17f,p);
        c.restoreToCount(saved);
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(!focused){pressed.clear();lx=ly=rx=ry=lt=rt=0;invalidate();}}
    private String label(String key){boolean ps=GameHooks.prefs(activity).getBoolean("r2_ps_labels",false);if(ps){if(key.equals("a"))return "✕";if(key.equals("b"))return "○";if(key.equals("x"))return "□";if(key.equals("y"))return "△";}if(key.equals("up"))return "↑";if(key.equals("down"))return "↓";if(key.equals("left"))return "←";if(key.equals("right"))return "→";return key.toUpperCase(Locale.ROOT);}
    private void stick(Canvas c,float x,float y,float ax,float ay,float h){p.setColor(0xff8a9ca4);c.drawCircle(x+ax*h*.035f,y+ay*h*.035f,h*.06f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(h*.006f);p.setColor(0xffd8c98f);c.drawCircle(x+ax*h*.035f,y+ay*h*.035f,h*.06f,p);p.setStyle(Paint.Style.FILL);}
}
