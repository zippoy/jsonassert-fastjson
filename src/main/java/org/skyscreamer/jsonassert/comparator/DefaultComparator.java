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
import org.skyscreamer.jsonassert.JSONCompareConfig;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allJSONObjects;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allSimpleValues;

/**
 * This class is the default json comparator implementation.
 * Comparison is performed according to {@link JSONCompareMode} that is passed as constructor's argument.
 */
public class DefaultComparator extends AbstractComparator {

    public DefaultComparator(JSONCompareMode mode) {
        this.jsonCompareConfig = JSONCompareConfig.builder().compareMode(mode).build();
    }

    public DefaultComparator(JSONCompareConfig jsonCompareConfig) {
        this.jsonCompareConfig = jsonCompareConfig;
    }

    @Override
    public void compareJSONObject(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result) throws JSONException {
        // Check that actual contains all the expected values
        List<String> actualExistKeys = checkJsonObjectKeysExpectedInActual(prefix, expected, actual, result);

        // If strict, check for vice-versa
        if (!jsonCompareConfig.getCompareMode().isExtensible()) {
            checkJsonObjectKeysActualInExpected(prefix, expected, actual, result, actualExistKeys);
        }
    }

    @Override
    public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        if ((expected == null && actual == null) || (expected.isEmpty() && actual.isEmpty())) {
            return;
        } else if (expected == null && actual.isEmpty()) {
            result.fail(prefix, null, "[]");
            return;
        } else if (expected.isEmpty() && actual == null) {
            result.fail(prefix, "[]", null);
            return;
        }

        /*if (expected.size() != actual.size()) {
            result.fail(prefix + "[]: Expected " + expected.size() + " values but got " + actual.size());
            return;
        }*/

        // 处理需要忽略的字段值
        List<Object> needIgnoreValues = jsonCompareConfig.getNeedIgnoreValues().get(prefix);
        if (needIgnoreValues != null && !needIgnoreValues.isEmpty()) {
            expected = new JSONArray(expected.stream().filter(v -> !needIgnoreValues.contains(v)).collect(Collectors.toList()));
            actual = new JSONArray(actual.stream().filter(v -> !needIgnoreValues.contains(v)).collect(Collectors.toList()));
        }

        // 是否配置的忽略顺序字段
        boolean ignoreOrder = jsonCompareConfig.getNeedIgnoreOrderPaths().contains(prefix);

        if (jsonCompareConfig.getCompareMode().isStrictOrder() && !ignoreOrder) {
            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
        } else if ((!expected.isEmpty() && allSimpleValues(expected)) || (!actual.isEmpty() && allSimpleValues(actual))) {
            compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
        } else if ((!expected.isEmpty() && allJSONObjects(expected)) || (!actual.isEmpty() && allJSONObjects(actual))) {
            compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
        } else {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
        }
    }

    @Override
    public void compareValues(String jsonPath, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        if (expectedValue == null && actualValue == null) {
            return;
        }

        if (expectedValue == null && actualValue != null) {
            if(!jsonCompareConfig.getCompareMode().isExtensible()) {
                result.unexpected(jsonPath, actualValue);
            }
        } else if (expectedValue != null && actualValue == null) {
            result.missing(jsonPath, expectedValue);
        } else if (areNumbers(expectedValue, actualValue)) {
            if (areNotSameDoubles(expectedValue, actualValue)) {
                result.fail(jsonPath, expectedValue, actualValue);
            }
        } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
            if (expectedValue instanceof JSONObject) {
                compareJSONObject(jsonPath, (JSONObject) expectedValue, (JSONObject) actualValue, result);
            } else if (expectedValue instanceof JSONArray) {
                compareJSONArray(jsonPath, (JSONArray) expectedValue, (JSONArray) actualValue, result);
            } else if (expectedValue instanceof Map) {
                compareJSONObject(jsonPath, new JSONObject((Map) expectedValue), new JSONObject((Map) actualValue), result);
            } else if (expectedValue instanceof Collection) {
                compareJSONArray(jsonPath, new JSONArray(new ArrayList((Collection) expectedValue)), new JSONArray(new ArrayList((Collection) actualValue)), result);
            } else if (jsonCompareConfig.getJsonStrDiffPaths().contains(jsonPath) && (expectedValue instanceof String)) {
                if (!isNotEquals(expectedValue, actualValue)) {
                    return;
                }
                if (isJsonStrings((String) expectedValue, (String) actualValue)) {
                    compareJSONObject(jsonPath + SPECIAL_STRING_PATH_SEPARATE, JSONParser.parseJSONObject((String) expectedValue), JSONParser.parseJSONObject((String) actualValue), result);
                } else if (isJsonArrayStrings((String) expectedValue, (String) actualValue)) {
                    compareJSONArray(jsonPath + SPECIAL_STRING_PATH_SEPARATE, (JSONArray) JSONParser.parseJSONArray((String) expectedValue), (JSONArray) JSONParser.parseJSONArray((String) actualValue), result);
                } else {
                    result.fail(jsonPath, expectedValue, actualValue);
                }
            } else if (isNotEquals(expectedValue, actualValue)) {
//                if (isAccuracyError(expectedValue, actualValue, jsonCompareConfig.getAccuracyError().getOrDefault(prefix, 0L))) {
//                    return;
//                }
                result.fail(jsonPath, expectedValue, actualValue);
            }
        } else {
            result.fail(jsonPath, expectedValue, actualValue);
        }
    }

    protected boolean areNumbers(Object expectedValue, Object actualValue) {
        return expectedValue instanceof Number && actualValue instanceof Number;
    }

    protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
        return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
    }


    protected boolean isNotEquals(Object expectedValue, Object actualValue) {
        return !expectedValue.equals(actualValue);
    }

    protected boolean isJsonStrings(String expectedValue, String actualValue) {
        return expectedValue.startsWith("{") && expectedValue.endsWith("}") && actualValue.startsWith("{") && actualValue.endsWith("}");
    }

    protected boolean isJsonArrayStrings(String expectedValue, String actualValue) {
        return expectedValue.startsWith("[") && expectedValue.endsWith("]") && actualValue.startsWith("[") && actualValue.endsWith("]");
    }

    /**
     * 判断是否时间精度误差
     *
     * @param expectedValue
     * @param actualValue
     * @return
     */
    private boolean isAccuracyError(Object expectedValue, Object actualValue, long accuracyError) {
        long expectedTimeStamp = 0, actualTimeStamp = 0;// timestamp

        if (expectedValue.getClass().isAssignableFrom(Long.class)
                && actualValue.getClass().isAssignableFrom(Long.class)) {
            expectedTimeStamp = (long) expectedValue;
            actualTimeStamp = (long) actualValue;
        } else if (expectedValue.getClass().isAssignableFrom(String.class)
                && actualValue.getClass().isAssignableFrom(String.class)) {// need format for string
            String timeFormat = getTimeFormat((String) expectedValue);
            if (null == timeFormat || "".equals(timeFormat)) {
                return false;
            }

            Date expectedDate = dateParse((String) expectedValue, timeFormat);
            Date actualDate = dateParse((String) actualValue, timeFormat);
            if (expectedDate == null || actualDate == null) {
                return false;
            }

            expectedTimeStamp = expectedDate.getTime();
            actualTimeStamp = actualDate.getTime();
        }

        /*if (!isTimeStamp(expectedTimeStamp) || !isTimeStamp(actualTimeStamp)) {
            return false;
        }*/

        return Math.abs(expectedTimeStamp - actualTimeStamp) <= accuracyError;
    }

    /**
     * get datetime format string
     *
     * @param str
     * @return if null, the param is not a datetime string
     */
    public static String getTimeFormat(String str) {
        if (null == str || "".equals(str)) {
            return null;
        }

        if (Pattern.matches("^(\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2})$", str)) {
            return "yyyy-MM-dd HH:mm:ss";
        }

        if (Pattern.matches("^(20\\d{12})$", str)) {
            return "yyyyMMddHHmmss";
        }

        return null;
    }

    /**
     * 判断时间范围
     *
     * @param value
     * @return
     */
    public static boolean isTimeStamp(long value) {
        // todo  inaccurate time
        return value > 1300000000000L && value < 2000000000000L;
    }


    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Date dateParse(String dateStr, String pattern) {
        if (dateStr == null || "".equals(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

}
