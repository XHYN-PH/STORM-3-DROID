/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
import com.naruto.r2.*;
import java.util.*;
public class RuntimeMathTest {
 static int checks;
 static void check(boolean value,String text){checks++;if(!value)throw new AssertionError(text);}
 static boolean near(float a,float b){return Math.abs(a-b)<.0001f;}
 public static void main(String[] args){
  check(near(GameHooks.cropScale(2400,1080,100),1.25f),"20:9 crop fills display with uniform scale");
  check(GameHooks.cropScale(1920,1080,100)==1,"16:9 fit is unchanged");
  check(GameHooks.cropScale(2400,1080,0)==1,"Zero crop returns original image");
  check(near(GameHooks.cropScale(2400,1080,50),1.125f),"Crop strength interpolates correctly");
  check(GameHooks.cropScale(0,0,100)==1,"Unlaid-out surfaces remain safe");
  Map<String,String> values=new LinkedHashMap<>();values.put("xnr.renut_brightness_gamma","1.25");
  String text=GameHooks.merge("language = en\nxnr.renut_brightness_gamma = 1\nxnr.renut_brightness_gamma=2\ncustom = 7\n",values);
  check(text.contains("language = en")&&text.contains("custom = 7"),"Config writer preserves unrelated entries");
  check(text.indexOf("xnr.renut_brightness_gamma")==text.lastIndexOf("xnr.renut_brightness_gamma"),"Owned config values cannot duplicate");
  check(text.contains("xnr.renut_brightness_gamma = 1.25"),"Chosen settings replace launcher defaults");
  System.out.println("PASS: "+checks+" display/configuration/performance assertions");
 }
}
