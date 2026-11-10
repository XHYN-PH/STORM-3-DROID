/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;
import android.app.Activity;
import android.content.SharedPreferences;

public final class Performance {
 public static void balanced(Activity a,int resolution){SharedPreferences p=GameHooks.prefs(a);p.edit().putInt("menu_resolution_v5",resolution==4?4:3).putInt("menu_antialiasing",1).putInt("menu_upscaling",1).putInt("menu_framegen",1).putInt("menu_framegen_mode",0).putInt("menu_screen_hz",0).putInt("menu_fps",2).putBoolean("menu_performance",false).apply();}
}
