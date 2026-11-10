#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Reproduce the R1 APK from the pinned upstream APK and the published changes."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parent
UPSTREAM_SHA256 = '15fcff17b64b29de84a205ea763cef71172f1086d61981c94b0d42f6257c9ecf'

def run(*args):
    subprocess.run([str(x) for x in args], check=True)

def digest(path):
    with path.open('rb') as source:
        return hashlib.file_digest(source, 'sha256').hexdigest()

def build(args):
    original = args.upstream.resolve()
    if digest(original) != UPSTREAM_SHA256:
        raise ValueError('Upstream APK checksum mismatch; refusing to build')
    out = args.output.resolve()
    out.parent.mkdir(parents=True, exist_ok=True)
    work = ROOT/'.build'
    work.mkdir(exist_ok=True)
    decoded = work/'decoded'
    run('gcc', '-std=c11', '-Wall', '-Wextra', '-Werror', '-g', '-fsanitize=address,undefined',
        '-fno-omit-frame-pointer', '-pthread', ROOT/'tests/test_logging.c', '-ldl', '-o', work/'test-logging')
    run(work/'test-logging')
    run('java', '-jar', args.apktool, 'd', '-f', '-p', work/'framework', '-o', decoded, original)
    run(sys.executable, ROOT/'translate.py', decoded)
    yaml = decoded/'apktool.yml'
    text = yaml.read_text()
    text, n_code = re.subn(r'(?m)^  versionCode: .*$', '  versionCode: 47', text)
    text, n_name = re.subn(r'(?m)^  versionName: .*$', '  versionName: R1', text)
    if n_code != 1 or n_name != 1:
        raise ValueError('Expected fully decoded version metadata')
    yaml.write_text(text)
    strings = decoded/'res/values/strings.xml'
    value = strings.read_text()
    value, count = re.subn(r'(<string name="app_name">)[^<]*(</string>)', r'\g<1>Naruto Storm 3 FB R1\2', value)
    if count != 1:
        raise ValueError('Expected one app name')
    strings.write_text(value)
    compiler = args.ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android30-clang'
    helper = decoded/'lib/arm64-v8a/libxhynlog.so'
    run(compiler, '-shared', '-fPIC', '-O2', '-std=c11', '-Wall', '-Wextra', '-Werror',
        '-fvisibility=hidden', '-Wl,-z,relro,-z,now', '-Wl,-z,max-page-size=16384',
        ROOT/'native-logging/logcontrol.c', '-ldl', '-llog', '-o', helper)
    (decoded/'assets/r1-notice.txt').write_text(
        'R1 modifications and English translation by XHYN_PH.\n'
        'https://github.com/XHYN-PH/STORM-3-DROID\n'
        'YouTube: https://youtube.com/@xhyn_ph\n\n'
        'Original R1 helper code and build tools are MIT-licensed.\n'
        'The upstream engine, game, third-party components and original content retain their own rights.\n'
        'Their source code is not supplied by this customization project.\n')
    # Keep copyright/license notices and add the new work's attribution.
    licenses = decoded/'assets/licenses.txt'
    licenses.write_text('R1 modifications and English translation by XHYN_PH\n'
                        'R1 source: https://github.com/XHYN-PH/STORM-3-DROID\n'
                        'Original R1 code: MIT License; upstream notices follow unchanged.\n\n' + licenses.read_text())
    verification_version = ['--version-name', 'R1', '--version-code', '47']
    if args.r2:
        r2 = ROOT.parent/'r2'
        run(sys.executable, r2/'compile_runtime.py', '--android-jar', args.android_jar,
            '--build-tools', args.build_tools, '--upstream', original, '--apktool', args.apktool)
        run(sys.executable, r2/'customize.py', decoded)
        run(sys.executable, r2/'verify_linkage.py', decoded)
        verification_version = ['--version-name', 'R2.2', '--version-code', '50']
    run(sys.executable, ROOT/'verify_sources.py', '--original', original, '--decoded', decoded, *verification_version)
    unsigned = work/'R1-unsigned.apk'
    aligned = work/'R1-aligned.apk'
    run('java', '-jar', args.apktool, 'b', '-f', '-p', work/'framework', '-o', unsigned, decoded)
    run(args.build_tools/'zipalign', '-P', '16', '-f', '4', unsigned, aligned)
    run(args.build_tools/'apksigner', 'sign', '--ks', args.keystore, '--ks-key-alias', args.alias,
        '--ks-pass', 'file:'+str(args.password_file.resolve()),
        '--out', out, aligned)
    run(args.build_tools/'apksigner', 'verify', '--verbose', '--print-certs', out)
    run(args.build_tools/'zipalign', '-c', '-P', '16', '4', out)
    with ZipFile(out) as apk:
        if apk.testzip() is not None:
            raise ValueError('APK archive integrity failure')
        if 'lib/arm64-v8a/libxhynlog.so' not in apk.namelist():
            raise ValueError('Missing native logging helper')
    report = json.loads((ROOT/'translation-report.json').read_text())
    report.update({'version': 'R2.2' if args.r2 else 'R1', 'version_code': 50 if args.r2 else 47, 'built': True, 'signed': True,
                   'native_logging_helper_compiled': True, 'artifact_sha256': digest(out),
                   'host_logging_assertions': 83, 'android_runtime_tested': False})
    (out.parent/'build-report.json').write_text(json.dumps(report, indent=2, ensure_ascii=False)+'\n')
    (out.parent/'SHA256SUMS').write_text(digest(out)+'  '+out.name+'\n')
    print('R1 APK built and signed:', out)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ['upstream','apktool','ndk','build-tools','keystore','password-file','output']:
        parser.add_argument('--'+name, type=Path, required=True)
    parser.add_argument('--r2',action='store_true',help='Apply the selected R2 features after the R1 base customization')
    parser.add_argument('--android-jar',type=Path,help='Android API 30 android.jar; required with --r2')
    parser.add_argument('--alias', default='xhyn-r1')
    args=parser.parse_args()
    if args.r2 and not args.android_jar:parser.error('--r2 requires --android-jar')
    build(args)
