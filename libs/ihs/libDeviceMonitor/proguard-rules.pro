
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.ihs.devicemonitor.sdcard.FileObserver$ObserverThread{
    *** onEvent*(...);
    *** init*(...);
    *** observe*(...);
    *** startWatching*(...);
    *** stopWatching*(...);
}