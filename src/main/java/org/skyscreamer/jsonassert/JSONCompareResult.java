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
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.skyscreamer.jsonassert.comparator.DefaultComparator.EMPTY_CONFIG;

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareResult {

    private boolean success;
    private StringBuilder message;
    private final List<FieldComparisonFailure> fieldFailures = new ArrayList();
    private final List<FieldComparisonFailure> fieldMissing = new ArrayList();
    private final List<FieldComparisonFailure> fieldUnexpected = new ArrayList();


    private JSONCompareConfig jsonCompareConfig;

    /**
     * Default constructor.
     */
    public JSONCompareResult() {
        this(true, null, EMPTY_CONFIG);
    }

    public JSONCompareResult(JSONCompareConfig jsonCompareConfig) {
        this(true, null, jsonCompareConfig == null ? EMPTY_CONFIG : jsonCompareConfig);
    }

    public JSONCompareResult(boolean success, String message) {
        this(success, message, EMPTY_CONFIG);
    }

    public JSONCompareResult(boolean success, String message, JSONCompareConfig jsonCompareConfig) {
        this.success = success;
        this.message = new StringBuilder(message == null ? "" : message);
        this.jsonCompareConfig = jsonCompareConfig == null ? EMPTY_CONFIG : jsonCompareConfig;
    }

    /**
     * Did the comparison pass?
     *
     * @return True if it passed
     */
    public boolean passed() {
        return success;
    }

    /**
     * Did the comparison fail?
     *
     * @return True if it failed
     */
    public boolean failed() {
        return !success;
    }

    /**
     * Result message
     *
     * @return String explaining why if the comparison failed
     */
    public String getMessage() {
        return message.toString();
    }

    /**
     * Get the list of failures on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldFailures() {
        return Collections.unmodifiableList(fieldFailures);
    }

    /**
     * Get the list of missed on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldMissing() {
        return Collections.unmodifiableList(fieldMissing);
    }

    /**
     * Get the list of failures on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldUnexpected() {
        return Collections.unmodifiableList(fieldUnexpected);
    }

    /**
     * Check if comparison failed on any particular fields
     *
     * @return true if there are field failures
     */
    public boolean isFailureOnField() {
        return !fieldFailures.isEmpty();
    }

    /**
     * Check if comparison failed with missing on any particular fields
     *
     * @return true if an expected field is missing
     */
    public boolean isMissingOnField() {
        return !fieldMissing.isEmpty();
    }

    /**
     * Check if comparison failed with unexpected on any particular fields
     *
     * @return true if an unexpected field is in the result
     */
    public boolean isUnexpectedOnField() {
        return !fieldUnexpected.isEmpty();
    }

    public void fail(String message) {
        this.success = false;
        if (message.length() == 0) {
            this.message.append(message);
        } else {
            this.message.append(" ; ").append(message);
        }
    }

    /**
     * Identify that the comparison failed
     *
     * @param joinner    Which field failed
     * @param expected Expected result
     * @param actual   Actual result
     * @return result of comparision
     */
    public JSONCompareResult fail(JSONPathJoinner joinner, Object expected, Object actual) {
        String path = joinner.getPath();
        if (isFilter(path, jsonCompareConfig.getIgnorePathList())) {
            return this;
        }
        this.fieldFailures.add(new FieldComparisonFailure(path, expected, actual));
        fail(formatFailureMessage(path, expected, actual));
        return this;
    }

    /**
     * Identify that the comparison failed
     *
     * @param joinner     Which field failed
     * @param exception exception containing details of match failure
     * @return result of comparision
     */
    public JSONCompareResult fail(JSONPathJoinner joinner, ValueMatcherException exception) {
        fail(joinner.append(": ") .append(exception.getMessage()), exception.getExpected(), exception.getActual());
        return this;
    }

    private String formatFailureMessage(String field, Object expected, Object actual) {
        return field
                + "\nExpected: "
                + describe(expected)
                + "\n     got: "
                + describe(actual)
                + "\n";
    }

    /**
     * Identify the missing field
     *
     * @param joinner    missing field
     * @param expected expected result
     * @return result of comparison
     */
    public JSONCompareResult missing(JSONPathJoinner joinner, Object expected) {
        String path = joinner.getPath();
        if (isFilter(path, jsonCompareConfig.getIgnorePathList())) {
            return this;
        }
        fieldMissing.add(new FieldComparisonFailure(path, expected, null));
        fail(formatMissing(path, expected));
        return this;
    }

    private String formatMissing(String path, Object expected) {
        return path
                + "\nExpected: "
                + describe(expected)
                + "\n     but none found\n";
    }

    /**
     * Identify unexpected field
     *
     * @param joinner  unexpected field
     * @param actual actual result
     * @return result of comparison
     */
    public JSONCompareResult unexpected(JSONPathJoinner joinner, Object actual) {
        String path = joinner.getPath();
        if (isFilter(path, jsonCompareConfig.getIgnorePathList())) {
            return this;
        }
        fieldUnexpected.add(new FieldComparisonFailure(path, null, actual));
        fail(formatUnexpected(path, actual));
        return this;
    }

    private String formatUnexpected(String path, Object actual) {
        return path
                + "\nUnexpected: "
                + describe(actual)
                + "\n";
    }

    private static String describe(Object value) {
        if (value instanceof JSONArray) {
            return "a JSON array";
        } else if (value instanceof JSONObject) {
            return "a JSON object";
        } else if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }

    public boolean isFilter(String path, List<String> ignorePathList) {
        return null != ignorePathList && ignorePathList.contains(path);
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
