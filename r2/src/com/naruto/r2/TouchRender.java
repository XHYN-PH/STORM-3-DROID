/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;

import a.c0;
import a.e0;
import a.f0;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/** Retain the stock drawing commands; input dispatch and the editor are unchanged. */
public final class TouchRender {
    private TouchRender() {}
    public static final class State {
        final Entry[] entries = new Entry[18];
        final RectF layer = new RectF();
        long visual;
        boolean hasVisual;
        public boolean enabled = true;
        public int recordings, hits;
        public int stickRecordings,stickHits;
        final RenderNode[] stickNodes=new RenderNode[4];
        float stickRadius=Float.NaN;
    }
    private static int alpha(int color,int opacity){return (color&0xffffff)|(opacity<<24);}
    public static void stick(f0 view,Canvas canvas) {
        State state=state(view);
        if(!state.enabled||view.o||!canvas.isHardwareAccelerated()||!(view.A>0)){view.r2DrawStick(canvas);return;}
        if(state.stickRadius!=view.A){for(RenderNode node:state.stickNodes)if(node!=null)node.discardDisplayList();state.stickRadius=view.A;}
        e0 s=view.w;boolean active=s.a!=-1;
        float x=active?s.b:view.y+(s.b-view.y)*s.h,y=active?s.c:view.z+(s.c-view.z)*s.h;
        float knobX=active?s.d:x,knobY=active?s.e:y;
        drawStickNode(canvas,state,active?1:0,x,y,false);
        drawStickNode(canvas,state,active?3:2,knobX,knobY,true);
    }
    private static void drawStickNode(Canvas canvas,State state,int index,float x,float y,boolean knob) {
        RenderNode node=state.stickNodes[index];float radius=state.stickRadius;
        if(node==null){node=new RenderNode("STORM stick artwork");node.setClipToBounds(false);state.stickNodes[index]=node;}
        if(!node.hasDisplayList()){
            int pad=Math.max(1,(int)Math.ceil(radius+4)),extent=pad*2;node.setPosition(-pad,-pad,pad,pad);Canvas recording=node.beginRecording(extent,extent);
            try{recording.translate(pad,pad);recordStick(recording,radius,(index&1)!=0,knob);}finally{node.endRecording();}
            state.stickRecordings++;
        }else state.stickHits++;
        int save=canvas.save();canvas.translate(x,y);canvas.drawRenderNode(node);canvas.restoreToCount(save);
    }
    private static void recordStick(Canvas canvas,float radius,boolean active,boolean knob) {
        int opacity=active?230:120;Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);stroke.setStrokeJoin(Paint.Join.ROUND);
        if(knob){
            float r=radius*.32f,offset=r*.3f;fill.setColor(-1);
            fill.setShader(new RadialGradient(-offset,-offset,r*1.4f,new int[]{alpha(-1,opacity),alpha(-1644826,opacity)},null,Shader.TileMode.CLAMP));
            canvas.drawCircle(0,0,r,fill);stroke.setStrokeWidth(r*.18f);stroke.setColor(alpha(-30178,opacity));canvas.drawCircle(0,0,r*.82f,stroke);return;
        }
        fill.setColor(alpha(-15986922,active?90:45));canvas.drawCircle(0,0,radius,fill);
        stroke.setStrokeWidth(.025f*radius);stroke.setColor(alpha(-1,opacity));canvas.drawCircle(0,0,radius,stroke);
        fill.setColor(alpha(-1,opacity*3/4));Path arrow=new Path();arrow.moveTo(.8f*radius,0);arrow.lineTo(.7f*radius,-.07f*radius);arrow.lineTo(.7f*radius,.07f*radius);arrow.close();
        for(int i=0;i<4;i++){int save=canvas.save();canvas.rotate(i*90f);canvas.drawPath(arrow,fill);canvas.restoreToCount(save);}
    }
    private static final class Entry {
        final c0 button;
        final RenderNode[] nodes = new RenderNode[2];
        final int[] ink = new int[2];
        float x, y, radius, width, height;
        Entry(c0 button) { this.button = button; }
        void discard() { for (RenderNode node : nodes) if (node != null) node.discardDisplayList(); }
    }
    private static State state(f0 view) {
        if (view.r2RenderState == null) view.r2RenderState = new State();
        return view.r2RenderState;
    }
    public static void button(f0 view, Canvas canvas, c0 button, boolean pressed) {
        State state = state(view);
        if (!state.enabled || view.o || !canvas.isHardwareAccelerated() || view.getWidth() <= 0 || view.getHeight() <= 0) {
            view.r2DrawButton(canvas, button, pressed);
            return;
        }
        Entry entry = null;
        for (int i = 0; i < view.a.length; i++) if (view.a[i] == button && i < state.entries.length) {
            entry = state.entries[i];
            if (entry == null) state.entries[i] = entry = new Entry(button);
            break;
        }
        if (entry == null) { view.r2DrawButton(canvas, button, pressed); return; }
        if (entry.x != button.j || entry.y != button.k || entry.radius != button.l || entry.width != view.V || entry.height != view.U) {
            entry.discard();
            entry.x = button.j; entry.y = button.k; entry.radius = button.l;
            entry.width = view.V; entry.height = view.U;
        }
        int index = pressed ? 1 : 0;
        RenderNode node = entry.nodes[index];
        if (node == null) {
            node = new RenderNode("STORM touch button");
            node.setClipToBounds(false);
            entry.nodes[index] = node;
        }
        if (!node.hasDisplayList()) {
            node.setPosition(0, 0, view.getWidth(), view.getHeight());
            Canvas recording = node.beginRecording(view.getWidth(), view.getHeight());
            try { view.r2DrawButton(recording, button, pressed); }
            finally { node.endRecording(); }
            entry.ink[index] = view.W;
            state.recordings++;
        } else state.hits++;
        view.W = entry.ink[index];
        canvas.drawRenderNode(node);
    }
    private static long mix(long h, float value) { return h * 31 + Float.floatToIntBits(value); }
    private static long stick(long h, e0 s) {
        h = h * 31 + s.a;
        h = mix(h, s.b); h = mix(h, s.c); h = mix(h, s.d); h = mix(h, s.e);
        h = mix(h, s.f); h = mix(h, s.g); return mix(h, s.h);
    }
    public static void invalidate(f0 view) {
        State state = state(view);
        long h = view.D;
        h = h * 31 + (view.o ? 1 : 0);
        h = h * 31 + view.c; h = h * 31 + view.d;
        h = mix(h, view.f); h = mix(h, view.U); h = mix(h, view.V);
        h = mix(h, view.y); h = mix(h, view.z); h = mix(h, view.A); h = mix(h, view.B);
        h = stick(stick(h, view.w), view.x);
        for (c0 b : view.a) { h = mix(h, b.j); h = mix(h, b.k); h = mix(h, b.l); h = h * 31 + (b.p ? 1 : 0); }
        if (!state.enabled || view.o || !state.hasVisual || state.visual != h) {
            state.visual = h; state.hasVisual = true;
            view.invalidate();
        }
    }
    /** Preserve group alpha. Clip only the transparency layer's provably empty bands. */
    public static int layer(f0 view, Canvas canvas, float left, float top, float right, float bottom, int alpha) {
        if (view.o || !state(view).enabled) return canvas.saveLayerAlpha(left, top, right, bottom, alpha);
        RectF ink = state(view).layer;
        ink.setEmpty();
        // Generous bounds include the pressed glow, label shadow and combo badges.
        // Keep the full width because labels are clamped to screen edges by stock code.
        for (c0 b : view.a) if (!b.p) ink.union(left, b.k - 1.7f * b.l - .04f * view.U,
                right, b.k + 2.7f * b.l + .08f * view.U);
        e0 s = view.w;
        float x = s.a != -1 ? s.b : view.y + (s.b - view.y) * s.h;
        float y = s.a != -1 ? s.c : view.z + (s.c - view.z) * s.h;
        float r = view.A * 1.8f + view.U * .02f;
        ink.union(x-r, y-r, x+r, y+r);
        // The stock secondary-stick marker remains part of the stock overlay.
        if (view.x.a != -1) { r = view.B * .4f + view.U * .01f; ink.union(view.x.b-r, view.x.c-r, view.x.b+r, view.x.c+r); }
        if (ink.isEmpty() || !Float.isFinite(ink.top) || !Float.isFinite(ink.bottom))
            return canvas.saveLayerAlpha(left, top, right, bottom, alpha);
        return canvas.saveLayerAlpha(left, Math.max(top, ink.top), right, Math.min(bottom, ink.bottom), alpha);
    }
    public static void release(f0 view) {
        State state = view.r2RenderState;
        if (state != null){for (Entry entry : state.entries) if (entry != null) entry.discard();for(RenderNode node:state.stickNodes)if(node!=null)node.discardDisplayList();}
        view.r2RenderState = null;
    }
}
