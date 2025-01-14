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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.util.List;

/**
 * Provides API to compare two JSON entities.  This is the backend to {@link JSONAssert}, but it can
 * be programmed against directly to access the functionality.  (eg, to make something that works with a
 * non-JUnit test framework)
 */
public final class JSONCompare {
    private JSONCompare() {
    }

    private static JSONComparator getComparatorForMode(JSONCompareMode mode) {
        return new DefaultComparator(mode);
    }

    private static JSONComparator getComparatorForMode(JSONCompareMode mode, List<String> ignorePathList) {
        return new DefaultComparator(mode, ignorePathList);
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
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONComparator comparator)
            throws JSONException {

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

        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            return compareJSON((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            return compareJSON((JSONArray) expected, (JSONArray) actual, comparator);
        } else if (expected instanceof JSONAware && actual instanceof JSONAware) {
            return compareJson((JSONAware) expected, (JSONAware) actual);
        } else {
            return new JSONCompareResult().fail("", expected, actual);
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
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONComparator comparator)
            throws JSONException {
        return comparator.compareJSON(expected, actual);
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
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONComparator comparator)
            throws JSONException {
        return comparator.compareJSON(expected, actual);
    }

    /**
     * Compares {@link JSONAware} provided to the expected {@code JSONString}, checking that the
     * {@link JSONAware#toJSONString()} are equal.
     *
     * @param expected Expected {@code JSONstring}
     * @param actual   {@code JSONstring} to compare
     * @return result of the comparison
     */
    public static JSONCompareResult compareJson(final JSONAware expected, final JSONAware actual) {
        final JSONCompareResult result = new JSONCompareResult();
        final String expectedJson = expected.toJSONString();
        final String actualJson = actual.toJSONString();
        if (!expectedJson.equals(actualJson)) {
            result.fail("$", expectedJson, actualJson);
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
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONCompareMode mode)
            throws JSONException {
        return compareJSON(expectedStr, actualStr, getComparatorForMode(mode));
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
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONCompareMode mode)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode));
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
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONCompareMode mode)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode));
    }

    /**
     * Compares JSON string provided to the expected JSON string, and returns the results of the comparison.
     *
     * @param expectedStr    Expected JSON string
     * @param actualStr      JSON string to compare
     * @param mode           Defines comparison behavior
     * @param ignorePathList need ignore paths
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, JSONCompareMode mode, List<String> ignorePathList)
            throws JSONException {
        return compareJSON(expectedStr, actualStr, getComparatorForMode(mode, ignorePathList));
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected       Expected JSONObject
     * @param actual         JSONObject to compare
     * @param mode           Defines comparison behavior
     * @param ignorePathList need ignore paths
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual, JSONCompareMode mode, List<String> ignorePathList)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode, ignorePathList));
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected       Expected JSONArray
     * @param actual         JSONArray to compare
     * @param mode           Defines comparison behavior
     * @param ignorePathList need ignore paths
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual, JSONCompareMode mode, List<String> ignorePathList)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode, ignorePathList));
    }

}
