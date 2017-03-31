# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/sharp/Android/android-sdk/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontskipnonpubliclibraryclassmembers
-keep class com.flurry.** { *; }
-keep class com.facebook.** { *; }
-keep class com.tapjoy.** { *; }
-keep class com.supersonicads.sdk.** { *; }
-keep class android.content.pm.** { *; }
-keep class android.app.ApplicationPackageManager.** { *; }
-dontwarn com.flurry.**
#-keep class com.ihs.booster.** { *; }
-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.ihs.booster.boost.MainFragment {
    private void setBackColor(int);
    private void setTxtMemoryPercentValue(int);
    private void setTxtJunkPercentValue(int);
    private void setTxtBatteryPercentValue(int);
    private void setTxtCpuTemperature(int);
}
-keep class com.ihs.booster.boost.DetailFragment {
    public void setBackColor(int);
}
-keep class com.ihs.booster.boost.memory.MemoryFragment {
    private void setMemorySize(float);
    public void setBackColor(int);
}
-keep class com.ihs.booster.boost.memory.MemoryDoneFragment {
    private void setBackColor(int);
}
-keep class com.ihs.booster.boost.junk.JunkScanFragment {
    private void setScanTotalSizeValue(float);
    private void setBackColor(int);
}
-keep class com.ihs.booster.boost.junk.JunkFragment {
    public void setBackColor(int);
}
-keep class com.ihs.booster.boost.junk.JunkDoneFragment {
    private void setBackColor(int);
    private void setAppPackageName(int);
}
-keep class com.ihs.booster.boost.battery.BatteryFragment {
    public void setBackColor(int);
}
-keep class com.ihs.booster.boost.battery.BatteryFloatClean {
    private void setSeekProgress(int);
    private void setSaveTimeTips(int);
    private void setBackColor(int);
}
-keep class com.ihs.booster.boost.junk.StubbornJunkFloatWindow {
    private void setBackColor(int);
}
-keep class com.ihs.booster.boost.cpu.CpuFragment {
    public void setBackColor(int);
    private void setCpuTemperatureTextView(float);
}
-keep class com.ihs.booster.boost.cpu.CpuDoneFragment {
    private void setBackColor(int);
}
-keep class com.ihs.booster.common.view.SmallChipView {
    public void setProgress(int);
}
-keep class com.ihs.booster.boost.floating.FloatBallView {
    private void setBallPositionForPinAnimation(int);
}
-keep class com.ihs.booster.boost.floating.FloatMemoryUsageDetailView {
    private void setProgressValue(int);
    private void setBackColor(int);
 }
-keep class com.ihs.booster.notifiction.FloatCleanView {
    private void setProgress(int);
    private void setBackColor(int);
}

-keep class com.ihs.booster.activity.OneKeyBoostActivity {
    private void setPointerAngle(int);
}
-keep class com.ihs.booster.boost.common.view.DashboardBar {
    private void setScaleValue(float);
    private void setPointerAngle(float);
 }
-keep class com.ihs.booster.boost.battery.viewholder.**{ *; }
-keep class com.ihs.booster.boost.cpu.viewholder.**{*;}
-keep class com.ihs.booster.boost.junk.viewholder.**{*;}
-keep class com.ihs.booster.common.expandablelist.**{ *; }
-keep class android.support.v7.widget.RecyclerView.**{*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.ihs.booster.boost.charging.ChargingBoostActivity {
    private void setImgBatteryAlpha(int);
 }
-keep class com.ihs.booster.boost.charging.push.WarningPush {
      private void setImgBatteryAlpha(int);
   }
-ignorewarning