package com.ulangch.networkanalyzer.utils;

import android.content.Context;
import android.os.Environment;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by xyzc on 18-2-27.
 */

public class AnalyzerUtils {
    private static final String TAG = "AnalyzerUtils";
    private static final String SEPERATOR_FILE = "/";
    private static final char[] HEX_ALPHABET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f',};

    public static final String DEFAULT_FILE_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NetworkAnalyzer";

    public static File getCacheDir(Context ctx) {
        return ctx.getCacheDir();
    }

    public static File getAppFileDir(Context ctx) {
        return ctx.getFilesDir();
    }

    public static File createFile(String path, String name) {
       return createFile(path + SEPERATOR_FILE + name);
    }

    public static File createFile(String name) {
        return createFile(new File(name));
    }

    public static File createFile(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            loge(TAG, "Exception occur while create file " + file.getAbsolutePath(), e);
            return null;
        }
        return file;
    }

    public static File createDir(String path, String name) {
        return createDir(path + SEPERATOR_FILE + name);
    }

    public static File createDir(String name) {
        return createDir(new File(name));
    }

    public static File createDir(File file) {
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            file.mkdir();
        }
        return file;
    }

    public static void ensureAnalyzerStorageDirectory() {
        ensureDirectory(DEFAULT_FILE_STORAGE_PATH);
    }

    public static void ensureDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = HEX_ALPHABET[(b >> 4) & 0xf];
            buf[c++] = HEX_ALPHABET[b & 0xf];
        }
        return new String(buf);
    }

    public static byte[] hexStringToBytes(String str) {
        if (str == null || (str.length() & 1) == 1) {
            throw new NumberFormatException("Odd length hex string: " + str.length());
        }
        byte[] data = new byte[str.length() >> 1];
        int position = 0;
        for (int n = 0; n < str.length(); n += 2) {
            data[position] =
                    (byte) (((charToDecimal(str.charAt(n)) & 0x0f) << 4) |
                            (charToDecimal(str.charAt(n + 1)) & 0x0f));
            position++;
        }
        return data;
    }

    public static byte hexStringToByte(String str) {
        if (str == null || str.length() != 2) {
            throw new NumberFormatException("Hex string length: " + str.length());
        }
        return (byte) (((charToDecimal(str.charAt(0)) & 0x0f) << 4) |
                (charToDecimal(str.charAt(1)) & 0x0f));
    }

    private static int charToDecimal(char ch) throws NumberFormatException {
        if (ch <= '9' && ch >= '0') {
            return ch - '0';
        } else if (ch >= 'a' && ch <= 'f') {
            return ch + 10 - 'a';
        } else if (ch <= 'F' && ch >= 'A') {
            return ch + 10 - 'A';
        } else {
            throw new NumberFormatException("Bad hex-character: " + ch);
        }
    }

    /**
     * @return index
     */
    public static int byteArraySearch(byte[] parent, byte[] child) {
        if (parent == null || child == null) {
            return -1;
        }
        for (int i = 0; i < parent.length; i++) {
            if (parent[i] == child[0]) {
                int j;
                for (j = 1; j < child.length; j++) {
                    if ((parent.length <= (i+j)) || (parent[i+j] != child[j])) {
                        break;
                    }
                }
                if (j == child.length) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void copyFilePermissions(File from, File to) throws IOException {
        try {
            final StructStat stat = Os.stat(from.getAbsolutePath());
            Os.chmod(to.getAbsolutePath(), stat.st_mode);
            Os.chown(to.getAbsolutePath(), stat.st_uid, stat.st_gid);
        } catch (ErrnoException e) {
            throw new IOException();
        }
    }

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logd(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void loge(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void loge(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
    }
}
