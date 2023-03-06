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

package org.skyscreamer.jsonassert;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.util.Set;

/**
 * Provides API to compare two JSON entities.  This is the backend to {@link JSONAssert}, but it can
 * be programmed against directly to access the functionality.  (eg, to make something that works with a
 * non-JUnit test framework)
 */
public final class JSONCompare {

    private JSONCompare() {
    }

    private static JSONComparator buildComparator(JSONCompareMode mode) {
        return new DefaultComparator(JSONCompareConfig.builder().compareMode(mode).build());
    }

    private static JSONComparator buildComparator(JSONCompareConfig compareConfig) {
        return new DefaultComparator(compareConfig);
    }

    /**
     * Compares JSON string provided to the expected JSON string using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param comparator  Comparator to use
     * @return result of the comparison
     * @throws JSONException            JSON parsing error
     * @throws IllegalArgumentException when type of expectedStr doesn't match the type of actualStr
     */
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONComparator comparator) throws JSONException {
        Object expected = null;
        Object actual = null;
        try {
            expected = JSONParser.parseJSON(expectedStr);
        } catch (Exception e) {
            String expectedString = "{" + "\"message\":\"dump Json格式有问题，解析失败\"}";
            expected = JSONParser.parseJSON(expectedString);
        }
        try {
            actual = JSONParser.parseJSON(actualStr);
        } catch (Exception e) {
            String actualString = "{" + "\"message\":\"test Json格式有问题，解析失败\"}";
            expected = JSONParser.parseJSON(actualString);
        }

        // 删除忽略的节点
        Set<String> ignorePaths = comparator.getJSONCompareConfig().getNeedIgnorePaths();
        if(ignorePaths != null && !ignorePaths.isEmpty()) {
            for (String ignorePath : ignorePaths) {
                JSONPath.remove(expected, ignorePath);
                JSONPath.remove(actual, ignorePath);
            }
        }

        // 需要忽略字段的某些值
        /*Map<String, List<Object>> needIgnoreValues = comparator.getJSONCompareConfig().getNeedIgnoreValues();
        if(needIgnoreValues != null && !needIgnoreValues.isEmpty()) {
            for (Map.Entry<String, List<Object>> entry : needIgnoreValues.entrySet()) {
                JSONPath.remove(expected, ignorePath);
                JSONPath.remove(actual, ignorePath);
            }
        }*/

        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            return compareJSON((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            return compareJSON((JSONArray) expected, (JSONArray) actual, comparator);
        } else {
            return compareJSON(expectedStr, actualStr);
        }
    }

    /**
     * Compares JSON object provided to the expected JSON object using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expected   expected json object
     * @param actual     actual json object
     * @param comparator comparator to use
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONComparator comparator) throws JSONException {
        return comparator.compareJSONObject(expected, actual);
    }

    /**
     * Compares JSON object provided to the expected JSON object using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expected   expected json array
     * @param actual     actual json array
     * @param comparator comparator to use
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONComparator comparator) throws JSONException {
        return comparator.compareJSONArray(expected, actual);
    }

    /**
     * Compares {@link String} provided to the expected {@code JSONString}, checking that the
     * {@link String} are equal.
     *
     * @param expected Expected {@code JSONstring}
     * @param actual   {@code JSONstring} to compare
     * @return result of the comparison
     */
    public static JSONCompareResult compareJSON(final String expected, final String actual) {
        final JSONCompareResult result = new JSONCompareResult();
        if (!expected.equals(actual)) {
            result.fail("$", expected, actual);
        }
        return result;
    }

    /**
     * Compares JSON string provided to the expected JSON string, and returns the results of the comparison.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param mode        Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONCompareMode mode) throws JSONException {
        return compareJSON(expectedStr, actualStr, buildComparator(mode));
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @param mode     Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONCompareMode mode) throws JSONException {
        return compareJSON(expected, actual, buildComparator(mode));
    }


    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected Expected JSONArray
     * @param actual   JSONArray to compare
     * @param mode     Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONCompareMode mode) throws JSONException {
        return compareJSON(expected, actual, buildComparator(mode));
    }

    /**
     * Compares JSON string provided to the expected JSON string, and returns the results of the comparison.
     *
     * @param expectedStr   Expected JSON string
     * @param actualStr     JSON string to compare
     * @param compareConfig need ignore paths...
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONCompareConfig compareConfig) throws JSONException {
        return compareJSON(expectedStr, actualStr, buildComparator(compareConfig));
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected      Expected JSONObject
     * @param actual        JSONObject to compare
     * @param compareConfig need ignore paths...
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONCompareConfig compareConfig) throws JSONException {
        return compareJSON(expected, actual, buildComparator(compareConfig));
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected      Expected JSONArray
     * @param actual        JSONArray to compare
     * @param compareConfig need ignore paths...
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONCompareConfig compareConfig) throws JSONException {
        return compareJSON(expected, actual, buildComparator(compareConfig));
    }

}
