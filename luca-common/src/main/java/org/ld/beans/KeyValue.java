package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class KeyValue<K, V> implements Map.Entry<K,V> {
    public K key;

    public void setKey(K key) {
        this.key = key;
    }

    public V setValue(V value) {
        this.value = value;
        return value;
    }

    public V value;
}
