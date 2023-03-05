/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.skyscreamer.jsonassert.comparator;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class that contains Json manipulation methods.
 */
public final class JSONCompareUtil {
    private JSONCompareUtil() {
    }

    /**
     * Converts the provided {@link JSONArray} to a Map of {@link JSONObject}s where the key of each object
     * is the value at {@code uniqueKey} in each object.
     *
     * @param array     the JSON array to convert
     * @param uniqueKey the key to map the JSON objects to
     * @return the map of {@link JSONObject}s from {@code array}
     * @throws JSONException JSON parsing error
     */
    public static Map<Object, JSONObject> arrayOfJsonObjectToMap(JSONArray array, String uniqueKey) throws JSONException {
        Map<Object, JSONObject> valueMap = new HashMap(array.size());
        for (Object obj : array) {
            JSONObject jsonObject = (JSONObject) obj;
            valueMap.put(jsonObject.get(uniqueKey), jsonObject);
        }
        return valueMap;
    }

    /**
     * Searches for the unique key of the {@code expected} JSON array.
     *
     * @param jsonArray the array to find the unique key of
     * @return the unique key if there's any, otherwise null
     * @throws JSONException JSON parsing error
     */
    public static String findUniqueKey(JSONArray jsonArray) throws JSONException {
        // Find a unique key for the object (id, name, whatever)
        JSONObject obj = (JSONObject) jsonArray.get(0); // There's at least one at this point
        for (String candidate : getKeys(obj)) {
            if (isUsableAsUniqueKey(candidate, jsonArray)) {
                return candidate;
            }
        }
        // No usable unique key :-(
        return null;
    }


    /**
     * Searches for the unique key of the {@code expected} JSON array.
     *
     * @param jsonArray the array to find the unique key of
     * @param prefix    {@code jsonArray}的父节点jsonPath
     * @param configUniqueKeys 配置的唯一id的jsonPath集合
     * @return the unique key if there's any, otherwise null
     * @throws JSONException JSON parsing error
     */
    public static String findUniqueKey(JSONArray jsonArray, String prefix, Map<String, Set<String>> configUniqueKeys) throws JSONException {
        if (configUniqueKeys == null || configUniqueKeys.isEmpty()) {
            return findUniqueKey(jsonArray);
        }
        // Find a unique key for the object (id, name, whatever)
        JSONObject obj = (JSONObject) jsonArray.get(0); // There's at least one at this point
        Set<String> keys = getKeys(obj);

        // 取配置的key
        Set<String> maybeKeys = new HashSet<>(keys);
        maybeKeys.retainAll(configUniqueKeys.keySet());
        if (maybeKeys.size() > 0) {
            for (String maybeKey : maybeKeys) {
                if (configUniqueKeys.get(maybeKey).contains(prefix)) {
                    return maybeKey;
                }
            }
        }

        // 最可能的uniqueKey集合
        List<String> mostMaybeKeys = new ArrayList();
        List<String> otherKeys = new ArrayList();
        for (String key : keys) {
            if (key.matches("^.*(?i)(id|key)$")) {
                mostMaybeKeys.add(key);
            } else {
                maybeKeys.add(key);
            }
        }
        mostMaybeKeys.addAll(otherKeys);

        for (String candidate : mostMaybeKeys) {
            if (isUsableAsUniqueKey(candidate, jsonArray)) {
                return candidate;
            }
        }

        // No usable unique key :-(
        return null;
    }


    /**
     * <p>Looks to see if candidate field is a possible unique key across a array of objects.
     * Returns true IFF:</p>
     * <ol>
     * <li>array is an array of JSONObject
     * <li>candidate is a top-level field in each of of the objects in the array
     * <li>candidate is a simple value (not JSONObject or JSONArray)
     * <li>candidate is unique across all elements in the array
     * </ol>
     *
     * @param candidate is usable as a unique key if every element in the
     * @param array     is a JSONObject having that key, and no two values are the same.
     * @return true if the candidate can work as a unique id across array
     * @throws JSONException JSON parsing error
     */
    public static boolean isUsableAsUniqueKey(String candidate, JSONArray array) throws JSONException {
        Set<Object> seenValues = new HashSet(array.size());
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                return false;
            }

            JSONObject o = (JSONObject) item;
            if (!o.containsKey(candidate)) {
                return false;
            }

            Object value = o.get(candidate);
            if (isSimpleValue(value) && !seenValues.contains(value)) {
                seenValues.add(value);
            } else {
                return false;
            }
        }
        return seenValues.size() == array.size();
    }

    /**
     * Converts the given {@link JSONArray} to a list of {@link Object}s.
     *
     * @param jsonArray the JSON array to convert
     * @return the list of objects from the {@code expected} array
     * @throws JSONException JSON parsing error
     */
    public static List<Object> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<Object> jsonObjects = new ArrayList(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); ++i) {
            jsonObjects.add(getObjectOrNull(jsonArray, i));
        }
        return jsonObjects;
    }

    /**
     * Returns the value present in the given index position. If null value is present, it will return null
     *
     * @param jsonArray the JSON array to get value from
     * @param index     index of object to retrieve
     * @return value at the given index position
     * @throws JSONException JSON parsing error
     */
    public static Object getObjectOrNull(JSONArray jsonArray, int index) throws JSONException {
        return jsonArray.size() <= index ? null : jsonArray.get(index);
    }

    /**
     * Returns whether all of the elements in the given array are simple values.
     *
     * @param array the JSON array to iterate through on
     * @return true if all the elements in {@code array} are simple values
     * @throws JSONException JSON parsing error
     * @see #isSimpleValue(Object)
     */
    public static boolean allSimpleValues(JSONArray array) throws JSONException {
        for (Object obj : array) {
            if (!isSimpleValue(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given object is a simple value: not {@link JSONObject} and not {@link JSONArray}.
     *
     * @param o the object to inspect
     * @return true if {@code o} is a simple value
     */
    public static boolean isSimpleValue(Object o) {
        return !(o instanceof JSONObject) && !(o instanceof JSONArray);
    }

    /**
     * Returns whether all elements in {@code array} are {@link JSONObject} instances.
     *
     * @param array the array to inspect
     * @return true if all the elements in the given array are JSONObjects
     * @throws JSONException JSON parsing error
     */
    public static boolean allJSONObjects(JSONArray array) throws JSONException {
        for (int i = 0; i < array.size(); ++i) {
            if (!(array.get(i) instanceof JSONObject)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether all elements in {@code array} are {@link JSONArray} instances.
     *
     * @param array the array to inspect
     * @return true if all the elements in the given array are JSONArrays
     * @throws JSONException JSON parsing error
     */
    public static boolean allJSONArrays(JSONArray array) throws JSONException {
        for (int i = 0; i < array.size(); ++i) {
            if (!(array.get(i) instanceof JSONArray)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Collects all keys in {@code jsonObject}.
     *
     * @param jsonObject the {@link JSONObject} to get the keys of
     * @return the set of keys
     */
    public static Set<String> getKeys(JSONObject jsonObject) {
        return new TreeSet(jsonObject.keySet());
    }

    public static String qualify(String prefix, String key) {
        return "".equals(prefix) ? key : prefix + "." + key;
    }

//    public static String qualify(String prefix, Object value) {
//        return "".equals(prefix) ? key : prefix + "." + key;
//    }

    public static String qualify(String prefix, String key, String extraRoot) {
        return "".equals(prefix) ? key : prefix + "." + key + "#" + extraRoot;
    }

    public static String formatUniqueKey(String prefix, String uniqueKey, Object value) {
        if (value instanceof Number) {
            return prefix + "[?(@." + uniqueKey + "==" + value + ")]";
        } else {
            return prefix + "[?(@." + uniqueKey + "=='" + value + "')]";
        }
    }

    /**
     * Creates a cardinality map from {@code coll}.
     *
     * @param list the collection of items to convert
     * @param <T>  the type of elements in the input collection
     * @return key -> item,  value -> item's index list
     */
    public static <T> Map<T, List<Integer>> getJSONArrayIndexGroup(final List<T> list) {
        Map<T, List<Integer>> count = new HashMap();
        for (int i = 0; i < list.size(); i++) {
            count.computeIfAbsent(list.get(i), k -> new ArrayList<>()).add(i);
        }
        return count;
    }

    /**
     * 得到JSONArray中某个值的下标
     * @param jsonArray
     * @param value
     * @return
     */
    public static int getIndexInJSONArray(JSONArray jsonArray, Object value) {
        for (int i = 0; i < jsonArray.size(); i++) {
            if(jsonArray.get(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

}
