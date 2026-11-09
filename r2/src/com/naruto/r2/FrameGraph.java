/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.Locale;

/** Uses the engine's one-second FPS samples; never labels estimates as per-frame measurements. */
public final class FrameGraph extends View {
    private static WeakReference<FrameGraph> visible=new WeakReference<>(null);
    private final float[] history=new float[60];
    private int count,next;
    private float game=-1,output=-1;
    private final Paint paint=new Paint(3);
    public FrameGraph(Context c){super(c);visible=new WeakReference<>(this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    public static void sample(float[] values){FrameGraph g=visible.get();if(g==null||values==null||values.length==0)return;
        g.game=values[0];g.output=values.length>6&&values[6]>0?values[6]:values.length>5&&values[5]>1?Float.NaN:g.game;
        g.history[g.next]=g.game>0?1000f/g.game:Float.NaN;g.next=(g.next+1)%60;g.count=Math.min(60,g.count+1);g.invalidate();}
    @Override protected void onDetachedFromWindow(){if(visible.get()==this)visible.clear();super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),d=getResources().getDisplayMetrics().density;
        paint.setColor(0xcc111724);c.drawRoundRect(0,0,w,h,12*d,12*d,paint);paint.setColor(0xfff5e6bd);paint.setTextSize(11*d);
        c.drawText(String.format(Locale.ROOT,"Game %.0f FPS · Output %s",game,Float.isFinite(output)?String.format(Locale.ROOT,"%.0f FPS",output):"--"),8*d,16*d,paint);
        paint.setTextSize(9*d);c.drawText("Estimated game-frame ms · 1-second samples",8*d,29*d,paint);
        paint.setColor(0xffefd17b);paint.setStrokeWidth(1.5f*d);
        float lastX=0,lastY=0;boolean last=false;
        for(int i=0;i<count;i++){float value=history[(next-count+i+60)%60],x=8*d+i*(w-16*d)/59,y=h-6*d-Math.min(100,value)/100*(h-40*d);
            if(Float.isFinite(value)){if(last)c.drawLine(lastX,lastY,x,y,paint);lastX=x;lastY=y;last=true;}else last=false;}
    }
}
