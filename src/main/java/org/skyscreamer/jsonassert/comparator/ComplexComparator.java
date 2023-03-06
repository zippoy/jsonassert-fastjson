package org.skyscreamer.jsonassert.comparator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareConfig;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONParser;
import org.skyscreamer.jsonassert.JSONPathJoinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class is the default json comparator implementation.
 * Comparison is performed according to {@link JSONCompareMode} that is passed as constructor's argument.
 *
 * @author zippoy
 * @date 2019-10-11
 */
public class ComplexComparator extends DefaultComparator {

    /**
     * whether to enable diff of JSON that is string, is not a json object
     */
    private boolean openStrJSONDiff;

    public ComplexComparator(JSONCompareMode mode) {
        this(mode, EMPTY_CONFIG, false);
    }

    public ComplexComparator(JSONCompareMode mode, JSONCompareConfig jsonCompareConfig) {
        this(mode, jsonCompareConfig, false);
    }

    public ComplexComparator(JSONCompareMode mode, JSONCompareConfig jsonCompareConfig, boolean openStrJSONDiff) {
        super(mode, jsonCompareConfig);
        this.openStrJSONDiff = openStrJSONDiff;
    }

    @Override
    public void compareValues(JSONPathJoinner joinner, Object expectedValue, Object actualValue, JSONCompareResult result)
            throws JSONException {

        if (expectedValue == null && actualValue == null) {
            return;
        } else if (expectedValue == null && actualValue != null) {
            result.unexpected(joinner, actualValue);
        } else if (expectedValue != null && actualValue == null) {
            result.missing(joinner, expectedValue);
        } else if (areNumbers(expectedValue, actualValue)) {
            if (areNotSameDoubles(expectedValue, actualValue)) {
                result.fail(joinner, expectedValue, actualValue);
            }
        } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
            if (expectedValue instanceof JSONArray) {
                compareJSONArray(joinner, (JSONArray) expectedValue, (JSONArray) actualValue, result);
            } else if (expectedValue instanceof JSONObject) {
                compareJSON(joinner, (JSONObject) expectedValue, (JSONObject) actualValue, result);
            } else if (expectedValue instanceof Map) {
                compareJSON(joinner, new JSONObject((Map) expectedValue), new JSONObject((Map) actualValue), result);
            } else if (openStrJSONDiff && (expectedValue instanceof String)) {
                if (!areNotSame(expectedValue, actualValue)) {
                    return;
                } else if (areJSONStrings((String) expectedValue, (String) actualValue)) {
                    compareJSON(joinner.appendChildPrefix(), JSONParser.parseJSONObject((String) expectedValue), JSONParser.parseJSONObject((String) actualValue), result);
                } else if (areJSONArrayStrings((String) expectedValue, (String) actualValue)) {
                    compareJSONArray(joinner.appendChildPrefix(), (JSONArray) JSONParser.parseJSONArray((String) expectedValue), (JSONArray) JSONParser.parseJSONArray((String) actualValue), result);
                } else {
                    result.fail(joinner, expectedValue, actualValue);
                }
            } else if (areNotSame(expectedValue, actualValue)) {
                if (isTimeAccuracyError(expectedValue, actualValue)) {
                    return;
                }
                result.fail(joinner, expectedValue, actualValue);
            }
        } else {
            result.fail(joinner, expectedValue, actualValue);
        }
    }

    protected boolean areNotSame(Object expectedValue, Object actualValue) {
        return !expectedValue.equals(actualValue);
    }

    protected boolean areJSONStrings(String expectedValue, String actualValue) {
        return expectedValue.startsWith("{") && expectedValue.endsWith("}")
                && actualValue.startsWith("{") && actualValue.endsWith("}");
    }

    protected boolean areJSONArrayStrings(String expectedValue, String actualValue) {
        return expectedValue.startsWith("[") && expectedValue.endsWith("]")
                && actualValue.startsWith("[") && actualValue.endsWith("]");
    }


    /**
     * Allowable time error(ms)
     */
    private static int TIME_ERROR_MS = 600_000;

    public static int getTimeErrorMs() {
        return TIME_ERROR_MS;
    }

    /**
     * 判断是否时间精度误差
     *
     * @param expectedValue
     * @param actualValue
     * @return
     */
    private boolean isTimeAccuracyError(Object expectedValue, Object actualValue) {
        long expectedTimeStamp = 0, actualTimeStamp = 0;// timestamp

        if (expectedValue.getClass().isAssignableFrom(String.class)
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
        } else if (expectedValue.getClass().isAssignableFrom(Long.class)
                && actualValue.getClass().isAssignableFrom(Long.class)) {
            expectedTimeStamp = (long) expectedValue;
            actualTimeStamp = (long) actualValue;
        }

        if (!isTimeStamp(expectedTimeStamp) || !isTimeStamp(actualTimeStamp)) {
            return false;
        }

        return Math.abs(expectedTimeStamp - actualTimeStamp) < getTimeErrorMs();
    }

    /**
     * get datetime format string
     *
     * @param str
     * @return if null, the param is not a datetime string
     */
    private String getTimeFormat(String str) {
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
    private boolean isTimeStamp(long value) {
        if (value > 1300000000000L && value < 2000000000000L) {// todo  inaccurate time
            return true;
        }
        return false;
    }

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
