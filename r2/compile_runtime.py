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
'a/c0.java':'package a; public class c0 {}',
'a/e0.java':'package a; public class e0 {public int a; public float b,c,d,e,f,g,h;}',
'a/d0.java':'package a; public interface d0 {void a(String s,float f,boolean b,boolean c);void b(boolean b);void c(int b,int lx,int ly,int rx,int ry,int lt,int rt);}',
'a/f0.java': '''package a; public class f0 extends android.view.View {
 public float U,V,B,g; public boolean o; public android.util.SparseIntArray C; public a.e0 x; public a.d0 L;
 public f0(android.content.Context c){super(c);} public a.c0 a(float x,float y){return null;} public static void n(a.e0 s){} public void q(){} }''',
'com/naruto/port/LauncherActivity.java':'''package com.naruto.port; public class LauncherActivity extends android.app.Activity {
 public java.util.ArrayList o; public int b(float v){return 0;} public android.widget.TextView g(String s,int i,int color){return null;}
 public android.widget.LinearLayout q(String s,android.view.View v){return null;} public android.widget.LinearLayout p(String s,String d,a.c c){return null;}
 public static void h(android.view.View v){} public a.k m(){return null;} public void l(String s){} }''',
'com/naruto/port/ControllerMapActivity.java':'''package com.naruto.port; public class ControllerMapActivity extends android.app.Activity {
 public android.content.SharedPreferences a;public int b;public String h;public static String[][] s;
 public android.widget.TextView d(String text,int bg,int color){return null;}
 public void h(String s){}public void c(String s){}public void b(){}public void e(){}public void f(){} }''',
'org/libsdl/app/SDL.java':'package org.libsdl.app;public class SDL {public static android.content.Context getContext(){return null;}}'
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
