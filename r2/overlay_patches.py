#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Small checked bridges into the stock touch view and statistics panel."""
import re

def replace_method(path, signature, replacement):
    source = path.read_text()
    source, count = re.subn(r'^'+re.escape(signature)+r'\n.*?^\.end method$', replacement, source, flags=re.M|re.S)
    if count != 1: raise ValueError('Unexpected overlay method: '+signature)
    path.write_text(source)

def apply(smali):
    touch = smali/'a/f0.smali'
    source = touch.read_text()
    source += '\n.field public r2RenderState:Lcom/naruto/r2/TouchRender$State;\n'
    # Cache the original translated artwork. Editing uses the same original renderer.
    signature = '.method public final d(Landroid/graphics/Canvas;La/c0;Z)V'
    if source.count(signature) != 1: raise ValueError('Touch artwork entry point changed')
    source = source.replace(signature, '.method public final r2DrawButton(Landroid/graphics/Canvas;La/c0;Z)V')
    source += signature+'\n    .locals 0\n    invoke-static {p0, p1, p2, p3}, Lcom/naruto/r2/TouchRender;->button(La/f0;Landroid/graphics/Canvas;La/c0;Z)V\n    return-void\n.end method\n'
    old = '    invoke-virtual/range {p0 .. p0}, Landroid/view/View;->invalidate()V'
    count = source.count(old) + source.count('    invoke-virtual {p0}, Landroid/view/View;->invalidate()V')
    if count < 5: raise ValueError('Touch invalidation sites changed')
    source = source.replace(old, '    invoke-static/range {p0 .. p0}, Lcom/naruto/r2/TouchRender;->invalidate(La/f0;)V')
    old = '    invoke-virtual {p0}, Landroid/view/View;->invalidate()V'
    source = source.replace(old, '    invoke-static {p0}, Lcom/naruto/r2/TouchRender;->invalidate(La/f0;)V')
    # Only the gameplay layer is bounded. Editor layers and group-alpha semantics stay intact.
    old = '    move/from16 v6, v16\n\n    invoke-virtual/range {v1 .. v6}, Landroid/graphics/Canvas;->saveLayerAlpha(FFFFI)I'
    new = '    move/from16 v6, v16\n\n    invoke-static/range {v0 .. v6}, Lcom/naruto/r2/TouchRender;->layer(La/f0;Landroid/graphics/Canvas;FFFFI)I'
    if source.count(old) != 1: raise ValueError('Gameplay alpha layer changed')
    source = source.replace(old, new)
    source += '\n.method protected onDetachedFromWindow()V\n    .locals 0\n    invoke-static {p0}, Lcom/naruto/r2/TouchRender;->release(La/f0;)V\n    invoke-super {p0}, Landroid/view/View;->onDetachedFromWindow()V\n    return-void\n.end method\n'
    touch.write_text(source)
    stats = smali/'a/a0.smali'
    source = stats.read_text()+'\n.field public r2StatsState:Lcom/naruto/r2/StatsOverlay$State;\n'
    # Generalize only the Context parameter; keep the exact original constructor descriptor too.
    old = '.method public constructor <init>(Lcom/naruto/port/NarutoActivity;Landroid/content/SharedPreferences;)V'
    new = '.method public constructor <init>(Landroid/app/Activity;Landroid/content/SharedPreferences;)V'
    if source.count(old) != 1: raise ValueError('Statistics constructor changed')
    source = source.replace(old, new)
    source += old+'\n    .locals 0\n    invoke-direct {p0, p1, p2}, La/a0;-><init>(Landroid/app/Activity;Landroid/content/SharedPreferences;)V\n    invoke-static {p0}, Lcom/naruto/r2/StatsOverlay;->resize(La/a0;)V\n    return-void\n.end method\n'
    stats.write_text(source)
    for signature, helper, descriptor, registers in [
        ('.method public final onDraw(Landroid/graphics/Canvas;)V','draw','La/a0;Landroid/graphics/Canvas;','{p0, p1}'),
        ('.method public setValues([F)V','update','La/a0;[F','{p0, p1}'),
        ('.method public final a()V','clamp','La/a0;','{p0}')]:
        replace_method(stats,signature,signature+'\n    .locals 0\n    invoke-static '+registers+', Lcom/naruto/r2/StatsOverlay;->'+helper+'('+descriptor+')V\n    return-void\n.end method')



def verify_stock(original, decoded):
    """Check every input/editor method and all unmodified panel gestures against R1."""
    def methods(path):
        return {m.group(1):m.group() for m in re.finditer(r'^\.method ([^\n]+)\n.*?^\.end method$',path.read_text(),re.M|re.S)}
    before=methods(original/'smali/a/f0.smali'); after=methods(decoded/'smali/a/f0.smali')
    for signature,body in before.items():
        current=after['public final r2DrawButton(Landroid/graphics/Canvas;La/c0;Z)V' if signature=='public final d(Landroid/graphics/Canvas;La/c0;Z)V' else signature]
        current=current.replace('r2DrawButton(','d(')
        current=current.replace('invoke-static/range {p0 .. p0}, Lcom/naruto/r2/TouchRender;->invalidate(La/f0;)V','invoke-virtual/range {p0 .. p0}, Landroid/view/View;->invalidate()V')
        current=current.replace('invoke-static {p0}, Lcom/naruto/r2/TouchRender;->invalidate(La/f0;)V','invoke-virtual {p0}, Landroid/view/View;->invalidate()V')
        current=current.replace('invoke-static/range {v0 .. v6}, Lcom/naruto/r2/TouchRender;->layer(La/f0;Landroid/graphics/Canvas;FFFFI)I','invoke-virtual/range {v1 .. v6}, Landroid/graphics/Canvas;->saveLayerAlpha(FFFFI)I')
        if current!=body:raise AssertionError('Unintended touch change: '+signature)
    before=methods(original/'smali/a/a0.smali'); after=methods(decoded/'smali/a/a0.smali')
    allowed={'public constructor <init>(Lcom/naruto/port/NarutoActivity;Landroid/content/SharedPreferences;)V','public final a()V','public final onDraw(Landroid/graphics/Canvas;)V','public setValues([F)V'}
    for signature,body in before.items():
        if signature not in allowed and after.get(signature)!=body:raise AssertionError('Unintended statistics gesture change: '+signature)
    print('PASS: stock touch input/editor logic, controller mappings and panel gestures preserved.')
