/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.store;

import java.util.Set;

/**
 * Provides CRUD DataStore primitives on a type of object and a type of key.
 *
 * @param <KeyType>   - type of key of the object being stored.
 * @param <ValueType> - type of key of the object being stored
 */
public interface Store<KeyType, ValueType extends Value<KeyType>> {
    void create(ValueType entry);

    ValueType get(KeyType key);

    void update(ValueType entry);

    void delete(KeyType key);

    Set<ValueType> getAllEntries();
}
