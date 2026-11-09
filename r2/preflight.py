#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Validate sources and isolated save operations before assembling an R2 APK."""
import argparse,subprocess,sys,shutil,re,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent
p=argparse.ArgumentParser(description=__doc__)
for key in ['android-jar','build-tools','upstream','apktool']:p.add_argument('--'+key,type=Path,required=True)
p.add_argument('--r1-decoded',type=Path)
a=p.parse_args();work=ROOT/'.build';work.mkdir(exist_ok=True)
def run(*parts):subprocess.run([str(x) for x in parts],check=True)
# Each fixture is new and outside any real app save tree.
run('java','--module','jdk.compiler/com.sun.tools.javac.Main','-d',work/'save-tests',ROOT/'src/com/naruto/r2/SaveFiles.java',ROOT/'tests/SaveFilesTest.java')
run('java','-cp',work/'save-tests','SaveFilesTest')
run(sys.executable,ROOT/'compile_runtime.py','--android-jar',a.android_jar,'--build-tools',a.build_tools,'--upstream',a.upstream,'--apktool',a.apktool)
run('java','--module','jdk.compiler/com.sun.tools.javac.Main','-cp',str(work/'runtime/classes')+':'+str(a.android_jar),'-d',work/'runtime-tests',ROOT/'tests/RuntimeMathTest.java')
run('java','-cp',str(work/'runtime-tests')+':'+str(work/'runtime/classes')+':'+str(a.android_jar),'RuntimeMathTest')
if a.r1_decoded is None:
 print('PASS: R2 host/source checks complete; decoded source checks run before APK assembly in build_r1.py.');sys.exit(0)
decoded=work/'preflight-decoded';shutil.rmtree(decoded,ignore_errors=True);shutil.copytree(a.r1_decoded,decoded)
run(sys.executable,ROOT/'customize.py',decoded)
run(sys.executable,ROOT.parent/'r1/verify_sources.py','--original',a.upstream,'--decoded',decoded,'--version-name','R2','--version-code','48')
# No excluded feature should be exposed under another label.
code='\n'.join(f.read_text() for f in (ROOT/'src').rglob('*.java'))
for unsupported in ['shader_prewarm','controller_profile','layout_profile','settings_backup']:
 if unsupported in code:raise AssertionError('Excluded feature found: '+unsupported)
print('PASS: R2 source preflight complete. No distributable APK has been assembled by this command.')
