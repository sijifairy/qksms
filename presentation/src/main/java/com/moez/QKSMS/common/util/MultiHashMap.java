/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moez.QKSMS.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A utility map from keys to a list of values.
 */
public class MultiHashMap<K, V> extends HashMap<K, List<V>> {

    public MultiHashMap() {
    }

    public MultiHashMap(int size) {
        super(size);
    }

    /**
     * @return Number of items for this key *AFTER* this operation.
     */
    public int putToList(K key, V value) {
        List<V> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            list.add(value);
            put(key, list);
        } else {
            list.add(value);
        }
        return list.size();
    }

    @Override
    public MultiHashMap<K, V> clone() {
        MultiHashMap<K, V> map = new MultiHashMap<>(size());
        for (Entry<K, List<V>> entry : entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return map;
    }
}
