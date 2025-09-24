package com.apk.editor.utils;

import com.apk.axml.serializableItems.ResEntry;

import java.nio.charset.StandardCharsets;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 10, 2025
 */
public class ResPatcher {

    public static boolean patch(byte[] arscData, ResEntry entry, String newValue) {
        if (entry == null) return false;

        String oldValue = entry.getValue();
        if (oldValue == null || oldValue.trim().isEmpty()) return false;
        if (newValue == null) return false;

        try {
            byte[] oldBytes = oldValue.getBytes(StandardCharsets.UTF_8);
            byte[] newBytes = newValue.getBytes(StandardCharsets.UTF_8);

            int pos = indexOf(arscData, oldBytes);
            if (pos == -1) {
                // not found
                return false;
            }

            // overwrite with new bytes
            System.arraycopy(newBytes, 0, arscData, pos, newBytes.length);

            // null-pad the remainder if new string is shorter
            for (int i = newBytes.length; i < oldBytes.length; i++) {
                arscData[pos + i] = 0x00;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        outer:
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

}