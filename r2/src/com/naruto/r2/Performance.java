/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.*;
import com.naruto.port.NarutoActivity;

public final class Performance {
 public static int targetRefresh(int multiplier){return 30*Math.max(2,Math.min(4,multiplier));}
 public static float modeScore(float hz,int target){if(!Float.isFinite(hz)||hz<=0)return Float.MAX_VALUE;float cycles=hz/target;float cadence=Math.abs(cycles-Math.round(cycles));return cadence*1000+Math.abs(hz-target);}
 public static void refresh(NarutoActivity a,int multiplier,int explicit){
  int target=explicit>0?explicit:targetRefresh(multiplier);a.surfaceFrameRate=target;
  try{Display display=a.getDisplay();if(display==null)return;Display.Mode current=display.getMode(),best=current;float score=Float.MAX_VALUE;
   for(Display.Mode mode:display.getSupportedModes()){if(mode.getPhysicalWidth()!=current.getPhysicalWidth()||mode.getPhysicalHeight()!=current.getPhysicalHeight())continue;float s=explicit>0?Math.abs(mode.getRefreshRate()-target):modeScore(mode.getRefreshRate(),target);if(s<score){score=s;best=mode;}}
   WindowManager.LayoutParams lp=a.getWindow().getAttributes();lp.preferredDisplayModeId=best.getModeId();a.getWindow().setAttributes(lp);
   a.surfaceFrameRate=Math.round(best.getRefreshRate());
  }catch(RuntimeException ignored){}
 }
 public static void balanced(Activity a,int resolution){SharedPreferences p=GameHooks.prefs(a);p.edit().putInt("menu_resolution_v5",resolution==4?4:3).putInt("menu_antialiasing",1).putInt("menu_upscaling",1).putInt("menu_framegen",1).putInt("menu_framegen_mode",0).putInt("menu_screen_hz",0).putInt("menu_fps",2).putBoolean("menu_performance",false).apply();}
}
