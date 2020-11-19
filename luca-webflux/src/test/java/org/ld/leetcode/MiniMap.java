package org.ld.leetcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@SuppressWarnings("unchecked")
public class MiniMap<K, V> {

    Entry<K, V>[] array;
    int size;

    public MiniMap() {
        this.size = 16;
    }

    public V get(Object key) {
        if (size != 0) {
            for (int i = 0; i < size; i++) {
                if (array[i].key.equals(key)) {
                    return array[i].value;
                }
            }
        }
        return null;
    }

    public V put(K key, V value) {
        if (size == array.length) {
            Entry<K, V>[] newArray = new Entry[array.length * 2 + 1];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }
        for (Entry<K, V> kvEntry : array) {
            if (kvEntry.key.equals(key)) {
                kvEntry.value = value;
                return value;
            }
        }
        array[size] = new Entry<>(key, value);
        size++;
        return value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Entry<K, V>  {
        K key;
        V value;
    }
}
