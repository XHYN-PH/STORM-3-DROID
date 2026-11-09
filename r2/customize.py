#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Apply R2 to the verified English R1 decoded tree; no APK assembly/signing."""
import re,shutil,argparse
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def change(path,signature,fn):
 source=path.read_text();pattern=re.compile(r'^'+re.escape(signature)+r'\n.*?^\.end method$',re.M|re.S)
 matches=list(pattern.finditer(source))
 if len(matches)!=1:raise ValueError('Unexpected method '+str(path)+' '+signature)
 new=fn(matches[0].group());source=source[:matches[0].start()]+new+source[matches[0].end():];path.write_text(source)
def start(body,call):
 pattern=r'(\n    \.locals \d+\n)'
 updated,n=re.subn(pattern,r'\1\n'+call+'\n',body,count=1)
 if n!=1:raise ValueError('Method locals not found')
 return updated
def returns(body,call):return body.replace('    return-void',call+'\n\n    return-void')
def call(method,signature,last=0):return f'    invoke-static/range {{p0 .. p{last}}}, Lcom/naruto/r2/{method};->{signature}'
def customize(root):
 smali=root/'smali';runtime=ROOT/'.build/runtime/decoded/smali/com/naruto/r2'
 if not runtime.is_dir():raise ValueError('Compile the R2 runtime first')
 shutil.copytree(runtime,smali/'com/naruto/r2',dirs_exist_ok=False)
 launcher=smali/'com/naruto/port/LauncherActivity.smali';game=smali/'com/naruto/port/NarutoActivity.smali';touch=smali/'a/f0.smali';controller=smali/'com/naruto/port/ControllerMapActivity.smali'
 change(launcher,'.method public final onCreate(Landroid/os/Bundle;)V',lambda b:returns(start(b,call('GameHooks','beforeLauncher(Landroid/app/Activity;)V')),call('LauncherUi','install(Lcom/naruto/port/LauncherActivity;)V')))
 change(launcher,'.method public final t()V',lambda b:returns(b,call('GameHooks','config(Landroid/app/Activity;)V')))
 change(launcher,'.method public final onActivityResult(IILandroid/content/Intent;)V',lambda b:start(b,'''    invoke-static/range {p0 .. p3}, Lcom/naruto/r2/LauncherUi;->activityResult(Lcom/naruto/port/LauncherActivity;IILandroid/content/Intent;)Z

    move-result v0

    if-eqz v0, :r2_original_result

    return-void

    :r2_original_result'''))
 change(game,'.method public onCreate(Landroid/os/Bundle;)V',lambda b:returns(start(b,call('GameHooks','gameStart(Landroid/app/Activity;)V')),call('GameHooks','gameReady(Landroid/app/Activity;)V')))
 change(game,'.method public onDestroy()V',lambda b:returns(b,call('GameHooks','gameStop(Landroid/app/Activity;)V')))
 # Keep the stock in-game Fit/Stretch menu authoritative when used.
 change(game,'.method private applyStretch(Z)V',lambda b:start(b,call('GameHooks','stockScreen(Landroid/app/Activity;Z)V',1)))
 def acquire(body):
  needle='    :goto_c\n    if-ne v3, v4, :cond_1a'
  if body.count(needle)!=1:raise ValueError('Unexpected touch pointer-acquisition block')
  return body.replace(needle,'    :goto_c\n'+call('RightInput','acquire(La/f0;Landroid/view/MotionEvent;)V',1)+'\n\n    if-ne v3, v4, :cond_1a')
 change(touch,'.method public final onTouchEvent(Landroid/view/MotionEvent;)Z',acquire)
 def draw(body):
  needle='    invoke-virtual {v7, v2, v1, v3, v9}, Landroid/graphics/Canvas;->drawCircle(FFFLandroid/graphics/Paint;)V'
  if body.count(needle)!=1:raise ValueError('Unexpected dormant right-stick indicator')
  return returns(body.replace(needle,'    # Right stick stays invisible during gameplay.'),call('RightInput','drawEditor(La/f0;Landroid/graphics/Canvas;)V',1))
 change(touch,'.method public final onDraw(Landroid/graphics/Canvas;)V',draw)
 source=touch.read_text();old='.method public final h(La/e0;FFF)V';assert source.count(old)==1;source=source.replace(old,'.method public final r2OriginalUpdate(La/e0;FFF)V')
 source+='''
.method public final h(La/e0;FFF)V
    .locals 1
    invoke-static/range {p0 .. p4}, Lcom/naruto/r2/RightInput;->update(La/f0;La/e0;FFF)Z
    move-result v0
    if-nez v0, :r2_done
    invoke-virtual/range {p0 .. p4}, La/f0;->r2OriginalUpdate(La/e0;FFF)V
    :r2_done
    return-void
.end method

.method public onWindowFocusChanged(Z)V
    .locals 0
    invoke-super {p0, p1}, Landroid/view/View;->onWindowFocusChanged(Z)V
    if-nez p1, :r2_focused
    invoke-static {p0}, Lcom/naruto/r2/RightInput;->release(La/f0;)V
    :r2_focused
    return-void
.end method
''';touch.write_text(source)
 change(controller,'.method public final onCreate(Landroid/os/Bundle;)V',lambda b:returns(b,call('ControllerUi','install(Lcom/naruto/port/ControllerMapActivity;)V')))
 change(controller,'.method public final dispatchGenericMotionEvent(Landroid/view/MotionEvent;)Z',lambda b:start(b,call('ControllerUi','onMotion(Lcom/naruto/port/ControllerMapActivity;Landroid/view/MotionEvent;)V',1)))
 change(controller,'.method public final dispatchKeyEvent(Landroid/view/KeyEvent;)Z',lambda b:start(b,call('ControllerUi','onKey(Lcom/naruto/port/ControllerMapActivity;Landroid/view/KeyEvent;)V',1)))
 joystick=smali/'org/libsdl/app/SDLJoystickHandler_API16.smali';text=joystick.read_text()
 text,n=re.subn(r'invoke-virtual (\{[^}]+\}), Landroid/view/MotionEvent;->getAxisValue\(II\)F',r'invoke-static \1, Lcom/naruto/r2/ControllerInput;->axisValue(Landroid/view/MotionEvent;II)F',text)
 if n<1:raise ValueError('SDL axis normalization path not found')
 joystick.write_text(text)
 manager=smali/'org/libsdl/app/SDLControllerManager.smali'
 for name,types,target in [('hapticRumble','IFFI','rumble'),('hapticRun','IFI','run'),('hapticStop','I','stop')]:
  signature=f'.method public static {name}({types})V';last=len(types)-1
  change(manager,signature,lambda b,s=signature,t=types,l=last,f=target:s+'\n    .locals 0\n    invoke-static/range {p0 .. p'+str(l)+'}, Lcom/naruto/r2/Haptics;->'+f+'('+t+')V\n    return-void\n.end method')
 change(smali/'a/a0.smali','.method public setValues([F)V',lambda b:start(b,'    invoke-static/range {p1 .. p1}, Lcom/naruto/r2/FrameGraph;->sample([F)V'))
 strings=root/'res/values/strings.xml';text=strings.read_text();text,n=re.subn(r'(<string name="app_name">)[^<]*(</string>)',r'\g<1>STORM 3\2',text);assert n==1;strings.write_text(text)
 image=root/'res/drawable/storm3_icon.png';image.parent.mkdir(exist_ok=True);shutil.copyfile(ROOT/'assets/storm3_icon.png',image)
 manifest=root/'AndroidManifest.xml';text=manifest.read_text().replace('android:icon="@mipmap/ic_storm3"','android:icon="@drawable/storm3_icon"').replace('android:roundIcon="@mipmap/ic_storm3"','android:roundIcon="@drawable/storm3_icon"');manifest.write_text(text)
 yaml=root/'apktool.yml';text=yaml.read_text().replace('  versionCode: 47','  versionCode: 48').replace('  versionName: R1','  versionName: R2');yaml.write_text(text)
 print('PASS: R2 hooks, stock menu additions, invisible right stick, app name/icon and version applied.')
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('decoded',type=Path);customize(p.parse_args().decoded)
