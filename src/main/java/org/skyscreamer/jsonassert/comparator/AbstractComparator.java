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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONPathJoinner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.skyscreamer.jsonassert.JSONPathJoinner.EMPTY_PATH_JOINNER;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.findUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.formatUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.isUsableAsUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.jsonArrayToList;

/**
 * This class provides a skeletal implementation of the {@link JSONComparator}
 * interface, to minimize the effort required to implement this interface.
 */
public abstract class AbstractComparator implements JSONComparator {

    public static final String SPECIAL_STRING_PATH_SEPARATE = ".$";

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @throws JSONException JSON parsing error
     */
    @Override
    public final JSONCompareResult compareJSON(JSONObject expected, JSONObject actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult(getJsonCompareConfig());
        compareJSON(EMPTY_PATH_JOINNER, expected, actual, result);
        return result;
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected Expected JSONArray
     * @param actual   JSONArray to compare
     * @throws JSONException JSON parsing error
     */
    @Override
    public final JSONCompareResult compareJSON(JSONArray expected, JSONArray actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult(getJsonCompareConfig());
        compareJSONArray(EMPTY_PATH_JOINNER, expected, actual, result);
        return result;
    }

    protected void checkJsonObjectKeysActualInExpected(JSONPathJoinner joinner, JSONObject expected, JSONObject actual, JSONCompareResult result) {
        Set<String> actualKeys = getKeys(actual);
        for (String key : actualKeys) {
            if (!expected.containsKey(key)) {
                result.unexpected(joinner.append(key), actual.get(key));
            }
        }
    }

    protected void checkJsonObjectKeysExpectedInActual(JSONPathJoinner joinner, JSONObject expected, JSONObject actual, JSONCompareResult result) throws JSONException {
        Set<String> expectedKeys = getKeys(expected);
        for (String key : expectedKeys) {
            Object expectedValue = expected.get(key);
            if (actual.containsKey(key)) {
                Object actualValue = actual.get(key);
                compareValues(joinner.append(key), expectedValue, actualValue, result);
            } else {
                result.missing(joinner.append(key), expectedValue);
            }
        }
    }

    protected void compareJSONArrayOfJsonObjects(JSONPathJoinner joinner, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected);
        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            // An expensive last resort
            recursivelyCompareJSONArray(joinner, expected, actual, result);
            return;
        }
        Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);

        Iterator<Map.Entry<Object, JSONObject>> actualIter = actualValueMap.entrySet().iterator();

        for (Object id : expectedValueMap.keySet()) {
            Map.Entry<Object, JSONObject> actualId = actualIter.next();
            JSONObject expectedValue = expectedValueMap.get(id);
            JSONObject actualValue = actualId.getValue();
            compareValues(formatUniqueKey(joinner, uniqueKey, id), expectedValue, actualValue, result);
        }

//        for (Object id : expectedValueMap.keySet()) {
//
//            Object actualId = actualIter.next();
//            if (id instanceof Map) {
//                compareValues(formatUniqueKeyaaa(key, uniqueKey), id, actualId, result);
//                continue;
//            }
//
//            if (!actualValueMap.containsKey(id)) {
//                result.missing(formatUniqueKey(key, uniqueKey, id), expectedValueMap.get(id));
//                continue;
//            }
//            JSONObject expectedValue = expectedValueMap.get(id);
//            JSONObject actualValue = actualValueMap.get(id);
//            compareValues(formatUniqueKey(key, uniqueKey, id), expectedValue, actualValue, result);
//        }

        for (Object id : actualValueMap.keySet()) {
            if (!(id instanceof Map) && !expectedValueMap.containsKey(id)) {
                result.unexpected(formatUniqueKey(joinner, uniqueKey, id), actualValueMap.get(id));
            }
        }
    }

    protected void compareJSONArrayOfSimpleValues(JSONPathJoinner joinner, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        Map<Object, Integer> expectedCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(expected));
        Map<Object, Integer> actualCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(actual));

        for (Map.Entry<Object, Integer> expectedEntry : expectedCount.entrySet()) {
            if (actualCount.containsKey(expectedEntry.getKey())) {
                expectedEntry.setValue(0);
                actualCount.put(expectedEntry.getKey(), 0);
            }
        }

        for (Map.Entry<Object, Integer> expectedEntry : expectedCount.entrySet()) {
            if (expectedEntry.getValue() == 0) {
                continue;
            }
            for (Map.Entry<Object, Integer> actualEntry : actualCount.entrySet()) {
                if (actualEntry.getValue() == 0) {
                    continue;
                }
                compareValues(joinner.appendArray(), expectedEntry.getKey(), actualEntry.getKey(), result);
                //expectedEntry.setValue(0);
                //actualEntry.setValue(0);
                //break;
            }
        }

//        for (Object o : expectedCount.keySet()) {
//            if (!actualCount.containsKey(o)) {
//                result.missing(key + "[]", o);
//            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
//                result.fail(key + "[]: Expected " + expectedCount.get(o) + " occurrence(s) of " + o
//                        + " but got " + actualCount.get(o) + " occurrence(s)");
//            }
//        }
//        for (Object o : actualCount.keySet()) {
//            if (!expectedCount.containsKey(o)) {
//                result.unexpected(key + "[]", o);
//            }
//        }
    }


//    protected void compareJSONArrayOfSimpleValues(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
//        Map<Object, Integer> expectedCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(expected));
//        Map<Object, Integer> actualCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(actual));
//        for (Object o : expectedCount.keySet()) {
//            if (!actualCount.containsKey(o)) {
//                result.missing(key + "[]", o);
//            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
//                result.fail(key + "[]: Expected " + expectedCount.get(o) + " occurrence(s) of " + o
//                        + " but got " + actualCount.get(o) + " occurrence(s)");
//            }
//        }
//        for (Object o : actualCount.keySet()) {
//            if (!expectedCount.containsKey(o)) {
//                result.unexpected(key + "[]", o);
//            }
//        }
//    }

    protected void compareJSONArrayWithStrictOrder(JSONPathJoinner joinner, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        for (int i = 0; i < expected.size(); ++i) {
            Object expectedValue = expected.get(i);
            Object actualValue = actual.get(i);
            compareValues(joinner.appendArray(i), expectedValue, actualValue, result);
        }
    }

    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    protected void recursivelyCompareJSONArray(JSONPathJoinner joinner, JSONArray expected, JSONArray actual,
                                               JSONCompareResult result) throws JSONException {
        Set<Integer> matched = new HashSet();
        for (int i = 0; i < expected.size(); ++i) {
            Object expectedElement = expected.get(i);
            boolean matchFound = false;
            for (int j = 0; j < actual.size(); ++j) {
                Object actualElement = actual.get(j);
                if (matched.contains(j) || !actualElement.getClass().equals(expectedElement.getClass())) {
                    continue;
                }
                if (expectedElement instanceof JSONObject) {
                    if (compareJSON((JSONObject) expectedElement, (JSONObject) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement instanceof JSONArray) {
                    if (compareJSON((JSONArray) expectedElement, (JSONArray) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement.equals(actualElement)) {
                    matched.add(j);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                result.fail(joinner + "[" + i + "] Could not find match for element " + expectedElement);
                return;
            }
        }
    }


    public static int getInt(JSONArray jsonArray, int index) {
        Integer ret = jsonArray.getInteger(index);
        if (ret == null) {
            throw new JSONException("Value at " + 0 + " is null.");
        }
        return ret;
    }

}
