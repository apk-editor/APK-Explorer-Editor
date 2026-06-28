package com.apk.editor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Helper para evitar el bug de Collections.sort() con CopyOnWriteArrayList en Android 7
 * Ver: https://issuetracker.google.com/issues/37032533
 */
public class SafeSortHelper {
    
    /**
     * Ordena una lista con comparador personalizado
     */
    public static <T> void safeSort(List<T> list, Comparator<? super T> comparator) {
        if (list == null || list.size() <= 1) return;
        
        if (list instanceof CopyOnWriteArrayList) {
            // Copiar a ArrayList temporal para evitar UnsupportedOperationException
            ArrayList<T> temp = new ArrayList<>(list);
            temp.sort(comparator);
            list.clear();
            list.addAll(temp);
        } else {
            list.sort(comparator);
        }
    }
    
    /**
     * Ordena una lista con orden natural (Comparable)
     */
    public static <T extends Comparable<? super T>> void safeSort(List<T> list) {
        if (list == null || list.size() <= 1) return;
        
        if (list instanceof CopyOnWriteArrayList) {
            ArrayList<T> temp = new ArrayList<>(list);
            Collections.sort(temp);
            list.clear();
            list.addAll(temp);
        } else {
            Collections.sort(list);
        }
    }
}
