package com.example.spendsmart.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public final class ReceiptStore {
    private ReceiptStore() {
    }

    public static File receiptsDir(Context context) {
        File dir = new File(context.getFilesDir(), "receipts");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public static String copyToReceipts(Context context, Uri source) {
        if (source == null) {
            return null;
        }
        try {
            File dest = new File(receiptsDir(context), UUID.randomUUID().toString() + ".jpg");
            try (InputStream in = context.getContentResolver().openInputStream(source);
                 FileOutputStream out = new FileOutputStream(dest)) {
                if (in == null) {
                    return null;
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return dest.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public static File createCameraTarget(Context context) {
        return new File(receiptsDir(context), "capture_" + System.currentTimeMillis() + ".jpg");
    }
}
