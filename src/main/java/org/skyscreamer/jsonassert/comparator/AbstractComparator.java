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
import com.alibaba.fastjson2.JSONPath;
import org.skyscreamer.jsonassert.JSONCompareConfig;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.findUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.formatUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.isUsableAsUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.qualify;

/**
 * This class provides a skeletal implementation of the {@link JSONComparator}
 * interface, to minimize the effort required to implement this interface.
 */
public abstract class AbstractComparator implements JSONComparator {

    /**
     *
     */
    protected JSONCompareConfig jsonCompareConfig;

    @Override
    public JSONCompareConfig getJSONCompareConfig() {
        return jsonCompareConfig;
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @throws JSONException JSON parsing error
     */
    @Override
    public final JSONCompareResult compareJSONObject(JSONObject expected, JSONObject actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult();
        compareJSONObject("$", expected, actual, result);
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
    public final JSONCompareResult compareJSONArray(JSONArray expected, JSONArray actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult();
        compareJSONArray("$", expected, actual, result);
        return result;
    }

    /**
     * 校验actual中存在的字段，expected不存在的字段
     *
     * @param prefix
     * @param expected
     * @param actual
     * @param result
     */
    protected void checkJsonObjectKeysActualInExpected(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result, List<String> actualExistKeys) {
        for (Map.Entry<String, Object> entry : actual.entrySet()) {
            if (actualExistKeys.contains(entry.getKey())) {
                continue;
            }

            boolean multiValue = entry.getValue() instanceof List ? true : false;
            String curPrefix = qualify(prefix, entry.getKey(), multiValue);
            // 忽略字段
            if (jsonCompareConfig.getNeedIgnorePaths().contains(curPrefix)) {
                continue;
            }
            if (!jsonCompareConfig.getCompareMode().isExtensible()) {
                if (!expected.containsKey(entry.getKey())) {
                    result.unexpected(qualify(prefix, entry.getKey(), multiValue), entry.getValue());
                }
            }
        }
    }

    /**
     * 校验expected中存在的字段，actual是否存在diff
     *
     * @param prefix
     * @param expected
     * @param actual
     * @param result
     * @throws JSONException
     * @return actual中在expected中存在的key。若存在改名字段，返回的是改名后的字段名
     */
    protected List<String> checkJsonObjectKeysExpectedInActual(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result) throws JSONException {
        List<String> actualExistKeys = new ArrayList<>();
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            boolean multiValue = entry.getValue() instanceof List ? true : false;
            String curPrefix = qualify(prefix, entry.getKey(), multiValue);
            // 忽略字段
            if (jsonCompareConfig.getNeedIgnorePaths().contains(curPrefix)) {
                continue;
            }

            String renameKey = jsonCompareConfig.getNeedRenamePaths().getOrDefault(prefix, Collections.emptyMap()).getOrDefault(entry.getKey(), entry.getKey());
            curPrefix = qualify(prefix, renameKey, multiValue);

            if (actual.containsKey(renameKey)) {
                Object actualValue = actual.get(renameKey);
                compareValues(curPrefix, entry.getValue(), actualValue, result);
                actualExistKeys.add(renameKey);
            } else {
                result.missing(curPrefix, entry.getValue());
            }
        }
        return actualExistKeys;
    }

    protected void compareJSONArrayOfJsonObjects(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected, prefix, jsonCompareConfig.buildUniqueKeyMap());
        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
            return;
        }
        Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);

        for (Map.Entry<Object, JSONObject> entry : expectedValueMap.entrySet()) {
            Object id = entry.getKey();
            if (!actualValueMap.containsKey(id)) {
                result.missing(formatUniqueKey(prefix, uniqueKey, id), entry.getValue());
                continue;
            }
            JSONObject expectedValue = entry.getValue();
            JSONObject actualValue = actualValueMap.get(id);
            compareValues(formatUniqueKey(prefix, uniqueKey, id), expectedValue, actualValue, result);
        }

        if (!jsonCompareConfig.getCompareMode().isExtensible()) {
            for (Map.Entry<Object, JSONObject> entry : actualValueMap.entrySet()) {
                if (!expectedValueMap.containsKey(entry.getKey())) {
                    result.unexpected(formatUniqueKey(prefix, uniqueKey, entry.getKey()), entry.getValue());
                }
            }
        }
    }

    protected void compareJSONArrayOfSimpleValues(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        Map<Object, List<Integer>> expectedIndexMap = JSONCompareUtil.getJSONArrayIndexGroup(expected);
        Map<Object, List<Integer>> actualIndexMap = JSONCompareUtil.getJSONArrayIndexGroup(actual);
        for (Map.Entry<Object, List<Integer>> entry : expectedIndexMap.entrySet()) {
            Object value = entry.getKey();
            // 因为遍历expectedCount, 所以expectedValIndex一定不为空集合
            List<Integer> expectedValIndex = entry.getValue();
            List<Integer> actualValIndex = actualIndexMap.getOrDefault(value, Collections.emptyList());

            int diffCount = expectedValIndex.size() - actualValIndex.size();
            if (diffCount > 0) {
                for (int i = expectedValIndex.size() - diffCount; i < expectedValIndex.size(); i++) {
                    result.missing(prefix + "[" + expectedValIndex.get(i) + "]", value);
                }
            } else if (diffCount < 0) {
                for (int i = actualValIndex.size() + diffCount; i < actualValIndex.size(); i++) {
                    result.missing(prefix + "[" + actualValIndex.get(i) + "]", value);
                }
            } else {
                // do nothing
            }
        }

        if (!jsonCompareConfig.getCompareMode().isExtensible()) {
            for (Map.Entry<Object, List<Integer>> entry : actualIndexMap.entrySet()) {
                if (expectedIndexMap.containsKey(entry.getKey())) {
                    continue;
                }
                for (Integer index : entry.getValue()) {
                    result.unexpected(prefix + "[" + index + "]", entry.getKey());
                }
            }
        }

    }

    protected void compareJSONArrayWithStrictOrder(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        for (int i = 0; i < expected.size(); ++i) {
            Object expectedValue = JSONCompareUtil.getObjectOrNull(expected, i);
            Object actualValue = JSONCompareUtil.getObjectOrNull(actual, i);
            compareValues(key + "[" + i + "]", expectedValue, actualValue, result);
        }
    }

    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    protected void recursivelyCompareJSONArray(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        Set<Integer> matched = new HashSet();
        for (int i = 0; i < expected.size(); ++i) {
            Object expectedElement = JSONCompareUtil.getObjectOrNull(expected, i);
            boolean matchFound = false;
            for (int j = 0; j < actual.size(); ++j) {
                Object actualElement = JSONCompareUtil.getObjectOrNull(actual, j);
                if (expectedElement == actualElement) {
                    matchFound = true;
                    break;
                }
                if ((expectedElement == null && actualElement != null) || (expectedElement != null && actualElement == null)) {
                    continue;
                }
                if (matched.contains(j) || !actualElement.getClass().equals(expectedElement.getClass())) {
                    continue;
                }
                if (expectedElement instanceof JSONObject) {
                    if (compareJSONObject((JSONObject) expectedElement, (JSONObject) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement instanceof JSONArray) {
                    if (((JSONArray) expectedElement).size() < ((JSONArray) actualElement).size()) {
                        continue;
                    }
                    if (compareJSONArray((JSONArray) expectedElement, (JSONArray) actualElement).passed()) {
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
                result.fail(key + "[" + i + "]", expectedElement, null);
                //result.fail(key + "[" + i + "] Could not find match for element " + expectedElement);
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


    public static void main(String[] args) {
        String json = "{\"id\":1,\"name\":\"zippoy\",\"xxx\":[2,3,1]}";

        JSONObject jsonObject = JSONObject.parseObject(json);
        Object xxx = JSONPath.eval(jsonObject, "$.xxx[:1]");
        System.out.println(xxx);

//        List<Integer> xxxs = com.jayway.jsonpath.JsonPath.read(json, "$.xxx[0]");
//        System.out.println(xxxs);

    }

}
