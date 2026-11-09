/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
import com.naruto.r2.*;
import java.util.*;
public class RuntimeMathTest {
 static int checks;
 static void check(boolean value,String text){checks++;if(!value)throw new AssertionError(text);}
 static boolean near(float a,float b){return Math.abs(a-b)<.0001f;}
 public static void main(String[] args){
  float[] v=RightInput.radial(0,0,.08f,1,false);check(v[0]==0&&v[1]==0,"Resting stick remains neutral");
  v=RightInput.radial(.01f,-.01f,.08f,1,false);check(v[0]==0&&v[1]==0,"Dead zone suppresses drift");
  v=RightInput.radial(3,4,.08f,1,false);check(near((float)Math.hypot(v[0],v[1]),1),"Diagonal output is circularly clamped");
  check(near(v[0],.6f)&&near(v[1],.8f),"Clamping retains stick direction");
  v=RightInput.radial(0,1,0,1,true);check(v[1]==-1,"Right Y inversion works");
  v=RightInput.radial(.5f,0,0,.5f,false);check(near(v[0],.25f),"Sensitivity gain works");
  v=RightInput.radial(Float.NaN,0,0,1,false);check(v[0]==0&&v[1]==0,"Invalid axes never reach game");
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
  check(Haptics.amplitude(1,1,100)==255,"Rumble clamps to motor maximum");
  check(Haptics.amplitude(1,1,0)==0&&Haptics.amplitude(0,0,100)==0,"Rumble off/stop produce zero amplitude");
  check(Haptics.amplitude(Float.NaN,1,100)==0,"Invalid feedback cannot trigger vibration");
  check(Haptics.amplitude(2,-2,200)==255,"Untrusted rumble strengths are bounded");
  System.out.println("PASS: "+checks+" input/display/configuration/rumble assertions");
 }
}
