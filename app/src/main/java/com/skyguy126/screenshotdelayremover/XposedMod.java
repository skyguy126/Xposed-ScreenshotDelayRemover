package com.skyguy126.screenshotdelayremover;

import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

    private XSharedPreferences prefs = new XSharedPreferences(Shared.PACKAGE_NAME, Shared.PREFS_FILE_NAME);
    private final long DELAY = prefs.getInt("delay", 0);

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("android"))
            return;

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M) {
            XposedBridge.log("[SDR] This module only works with Android 6.0.x");
            return;
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager",
                    lpparam.classLoader, "interceptScreenshotChord", new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            boolean mScreenshotChordEnabled = XposedHelpers.getBooleanField(param.thisObject,
                                    "mScreenshotChordEnabled");
                            boolean mScreenshotChordVolumeDownKeyTriggered = XposedHelpers.getBooleanField(param.thisObject,
                                    "mScreenshotChordVolumeDownKeyTriggered");
                            boolean mScreenshotChordPowerKeyTriggered = XposedHelpers.getBooleanField(param.thisObject,
                                    "mScreenshotChordPowerKeyTriggered");
                            boolean mScreenshotChordVolumeUpKeyTriggered = XposedHelpers.getBooleanField(param.thisObject,
                                    "mScreenshotChordVolumeUpKeyTriggered");
                            long mScreenshotChordVolumeDownKeyTime = XposedHelpers.getLongField(param.thisObject,
                                    "mScreenshotChordVolumeDownKeyTime");
                            long mScreenshotChordPowerKeyTime = XposedHelpers.getLongField(param.thisObject,
                                    "mScreenshotChordPowerKeyTime");
                            long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = XposedHelpers.getLongField(param.thisObject,
                                    "SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS");

                            if (mScreenshotChordEnabled && mScreenshotChordVolumeDownKeyTriggered
                                    && mScreenshotChordPowerKeyTriggered && !mScreenshotChordVolumeUpKeyTriggered) {

                                final long now = SystemClock.uptimeMillis();
                                if (now <= mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS
                                        && now <= mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {

                                    XposedHelpers.setBooleanField(param.thisObject, "mScreenshotChordVolumeDownKeyConsumed", true);
                                    XposedHelpers.callMethod(param.thisObject, "cancelPendingPowerKeyAction");

                                    Handler mHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
                                    Runnable mScreenshotRunnable = (Runnable) XposedHelpers.getObjectField(param.thisObject,
                                            "mScreenshotRunnable");
                                    mHandler.postDelayed(mScreenshotRunnable, DELAY);
                                }
                            }

                            return null;
                        }
                    });

        } catch (Throwable t) {
            XposedBridge.log("[SDR] Error: " + t.getMessage());
            return;
        }

        XposedBridge.log("[SDR] Initialized");
    }
}