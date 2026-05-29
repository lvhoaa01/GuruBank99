package com.example.bankingapp.screenshot;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Test-only helper: capture the current screen and save it under the app's
 * external files dir, organised per RTM group folder (A–E).
 *
 *   /sdcard/Android/data/com.example.bankingapp/files/screenshots/<group>/<TC_ID>.png
 *
 * No runtime permission needed (scoped storage, app-specific dir).
 * Pull afterwards with:
 *   adb pull /sdcard/Android/data/com.example.bankingapp/files/screenshots
 */
public final class ScreenshotHelper {

    private ScreenshotHelper() { }

    /** Capture immediately (assumes UI already settled). */
    public static void capture(String group, String testCaseId) {
        Instrumentation inst = InstrumentationRegistry.getInstrumentation();
        Bitmap bmp = inst.getUiAutomation().takeScreenshot();
        if (bmp == null) {
            throw new IllegalStateException("takeScreenshot returned null for " + testCaseId);
        }
        Context ctx = inst.getTargetContext();
        // Internal storage (/data/data/<pkg>/files/screenshots/<group>) — pull qua run-as.
        // Tránh scoped-storage chặn adb đọc /sdcard/Android/data/<pkg> trên API 30+.
        File dir = new File(new File(ctx.getFilesDir(), "screenshots"), group);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create dir " + dir);
        }
        File out = new File(dir, testCaseId + ".png");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save screenshot " + out, e);
        } finally {
            bmp.recycle();
        }
    }

    /** Let async DB work + LiveData post settle, then capture. */
    public static void settleAndCapture(String group, String testCaseId, long settleMillis) {
        SystemClock.sleep(settleMillis);
        capture(group, testCaseId);
    }
}
