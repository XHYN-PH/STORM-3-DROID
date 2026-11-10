#!/usr/bin/env python3
# Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT
"""Run the actual game activity and its menu in :game; game data is not bundled."""
import os,re,subprocess,time,xml.etree.ElementTree as ET
from pathlib import Path

result=Path(os.environ['RUNNER_TEMP'])/'r1-results'
tools=Path(os.environ['RUNNER_TEMP'])/'r1-tools'
adb=str(Path(os.environ['ANDROID_HOME'])/'platform-tools/adb')
package='com.naruto.storm3'
def command(*args,check=True,timeout=35):
    return subprocess.run([adb,*map(str,args)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,check=check,timeout=timeout).stdout

def notice(title,text):
    for start in range(0,len(text),3000):
        part=text[start:start+3000].replace('%','%25').replace('\r','%0D').replace('\n','%0A')
        print(f'::notice title={title}::{part}',flush=True)

def read_log(version):
    log=command('logcat','-d','-b','all');(result/f'{version}-startup.log').write_text(log)
    return log

def assert_alive():
    pid=command('shell','pidof',package+':game',check=False).strip()
    if not pid:raise AssertionError('The actual game process exited during startup/menu use.')
    return pid

def dump_menu(name):
    remote='/sdcard/r2-real-menu.xml'
    command('shell','uiautomator','dump',remote,timeout=45)
    path=result/name;command('pull',remote,path)
    return ET.parse(path).getroot()

def tap_text(root,text):
    for node in root.iter('node'):
        if node.get('text')!=text:continue
        bounds=list(map(int,re.findall(r'\d+',node.get('bounds',''))))
        if len(bounds)==4:
            command('shell','input','tap',(bounds[0]+bounds[2])//2,(bounds[1]+bounds[3])//2);return
    raise AssertionError('Missing actual game-menu action: '+text)

command('root');command('wait-for-device')
for version,file in [('R1','R1-control.apk'),('R2','R2-ci-only.apk')]:
    log=''
    try:
        print(command('install','--no-incremental','-r',result/file,timeout=90),flush=True)
        command('shell','am','force-stop',package)
        command('shell','appops','set',package,'MANAGE_EXTERNAL_STORAGE','allow')
        command('shell','mkdir','-p',f'/data/data/{package}/shared_prefs',f'/sdcard/Android/data/{package}/files/r2-empty-game')
        prefs=tools/'naruto.xml';prefs.write_text("<?xml version='1.0' encoding='utf-8' standalone='yes' ?><map><boolean name='logging_enabled' value='true'/><boolean name='menu_stats' value='true'/><int name='menu_screen_hz' value='0'/><int name='framegen_playing' value='1'/><float name='shadow_emblem_x_v2' value='24.0'/><float name='shadow_emblem_y_v2' value='24.0'/></map>")
        command('push',prefs,f'/data/data/{package}/shared_prefs/naruto.xml')
        uid=command('shell','stat','-c','%u',f'/data/data/{package}').strip()
        if not uid.isdigit():raise AssertionError('Cannot read isolated CI app UID')
        command('shell','chown','-R',f'{uid}:{uid}',f'/data/data/{package}/shared_prefs')
        command('shell','restorecon','-RF',f'/data/data/{package}/shared_prefs',check=False)
        command('logcat','-c')
        start=command('shell','am','start','-W','-n',package+'/com.naruto.port.NarutoActivity','--es','gameDirectory',f'/sdcard/Android/data/{package}/files/r2-empty-game')
        (result/f'{version}-start.txt').write_text(start);notice(version+' actual activity',start)
        time.sleep(5)
        pid=assert_alive();log=read_log(version)
        assert 'Running main function SDL_main' in log,'The native SDL entry point did not start.'
        if version=='R2':
            command('shell','input','tap','64','64');time.sleep(1)
            menu=dump_menu('R2-game-menu.xml')
            assert any(node.get('text')=='Game options' for node in menu.iter('node')),'The actual in-game menu did not open.'
            tap_text(menu,'Show FPS');time.sleep(2);assert_alive()
            tap_text(menu,'Resume');time.sleep(1)
            command('shell','input','tap','64','64');time.sleep(1)
            reopened=dump_menu('R2-game-menu-reopened.xml')
            assert any(node.get('text')=='Return to launcher' for node in reopened.iter('node')),'The game menu did not reopen.'
            assert_alive();log=read_log(version)
        assert not re.search(r'AndroidRuntime: Process: com\.naruto\.storm3:game',log),'AndroidRuntime reported an uncaught game exception.'
        notice(version+' startup result','PASS: actual :game process alive, SDL native main reached'+(', real menu opened/reopened and FPS toggle executed' if version=='R2' else ''))
    except Exception as error:
        log=read_log(version)
        relevant='\n'.join(line for line in log.splitlines() if 'AndroidRuntime' in line or 'F libc' in line or 'F DEBUG' in line)
        notice(version+' startup failure',str(error)+'\n'+(relevant[-12000:] or log[-3000:]))
        raise
    finally:
        command('shell','am','force-stop',package,check=False)
