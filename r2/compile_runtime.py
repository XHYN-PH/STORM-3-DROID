#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Compile the added Java classes against signature-only upstream stubs."""
import argparse,subprocess,shutil
from pathlib import Path
from zipfile import ZipFile
ROOT=Path(__file__).resolve().parent
STUBS={
'a/b.java': 'package a; public interface b {void a(boolean value);}',
'a/c.java': 'package a; public class c extends android.view.View {public c(com.naruto.port.LauncherActivity a,boolean b){super(a);} public void setListener(a.b l){} }',
'a/k.java': 'package a; public class k {public String a,b,c,d,e; public String a(){return "";}}',
'a/f0.java': 'package a; public class f0 extends android.view.View {\n public boolean o; public f0(android.content.Context c){super(c);} public void setEditing(boolean value){} public float getOpacity(){return 1;} public void setOpacity(float v){} }',
'a/H.java':'package a; public class H implements Runnable {public void run(){}}',
'a/Z.java':'package a; public class Z extends android.widget.FrameLayout {public Z(android.content.Context c){super(c);} public a.H b,c; public void b(){}}',
'a/a0.java':'package a; public class a0 extends android.view.View {public a0(android.content.Context c){super(c);} public void setValues(float[] v){}}',
'com/naruto/port/LauncherActivity.java': 'package com.naruto.port; public class LauncherActivity extends android.app.Activity {\n public java.util.ArrayList o; public int b(float v){return 0;} public android.widget.TextView g(String s,int i,int color){return null;}\n public android.widget.LinearLayout q(String s,android.view.View v){return null;} public android.widget.LinearLayout p(String s,String d,a.c c){return null;}\n public static void h(android.view.View v){} public a.k m(){return null;} public void l(String s){} public void t(){} }',
'com/naruto/port/NarutoActivity.java': 'package com.naruto.port; public class NarutoActivity extends android.app.Activity {\n public a.f0 controls; public a.Z shadowPanel; public a.a0 statsView; public android.widget.LinearLayout preparePanel; public android.widget.ProgressBar prepareBar; public android.widget.TextView prepareCount;\n public boolean prepareSeen; public long prepareStarted; public android.os.Handler statsHandler; public Runnable statsUpdater,prepareUpdater; public int surfaceFrameRate;\n public Thread r2GameThread(){return null;} public void r2ApplyStretch(boolean v){} public void r2ApplyControllerMode(){}\n public static native float[] nativeStatsValues(); public static native int[] nativeShaderPrepare(); public static native void nativeSetTouch(int a,int b,int c,int d,int e,int f,int g); public static native void nativeSetShowStats(boolean v); public static native boolean nativeShadowSet(String key,String value);\n}'
}

def run(*parts): subprocess.run([str(x) for x in parts],check=True)
def compile(args):
 work=ROOT/'.build/runtime';shutil.rmtree(work,ignore_errors=True);work.mkdir(parents=True)
 stubs=work/'stubs';classes=work/'classes';dex=work/'dex';decoded=work/'decoded'
 for name,text in STUBS.items():
  file=stubs/name;file.parent.mkdir(parents=True,exist_ok=True);file.write_text(text+'\n')
 classes.mkdir();dex.mkdir()
 run('java','--module','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',args.android_jar,'-d',classes,*sorted(stubs.rglob('*.java')),*sorted((ROOT/'src').rglob('*.java')))
 run(args.build_tools/'d8','--release','--min-api','30','--lib',args.android_jar,'--output',dex,*sorted((classes/'com/naruto/r2').rglob('*.class')))
 apk=work/'runtime-only.apk'
 with ZipFile(args.upstream) as upstream,ZipFile(apk,'w') as output:
  output.writestr('AndroidManifest.xml',upstream.read('AndroidManifest.xml'));output.write(dex/'classes.dex','classes.dex')
 run('java','-jar',args.apktool,'d','-r','-f','-o',decoded,apk)
 if not (decoded/'smali/com/naruto/r2/LauncherUi.smali').exists():raise AssertionError('Runtime disassembly is missing')
 print('PASS: added Java classes compile and disassemble; upstream stubs are excluded from DEX.')
 return decoded/'smali'
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__)
 for key in ['android-jar','build-tools','upstream','apktool']:p.add_argument('--'+key,type=Path,required=True)
 compile(p.parse_args())
