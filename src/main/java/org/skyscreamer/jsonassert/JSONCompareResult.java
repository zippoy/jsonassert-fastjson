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

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareResult {

    private boolean success;
    private StringBuilder message;
    private final List<FieldComparisonFailure> fieldFailures = new ArrayList();
    private final List<FieldComparisonFailure> fieldMissing = new ArrayList();
    private final List<FieldComparisonFailure> fieldUnexpected = new ArrayList();

    /**
     * 需要忽略的path
     */
    private List<String> ignorePathList;

    /**
     * Default constructor.
     */
    public JSONCompareResult() {
        this(true, null, null);
    }

    public JSONCompareResult(List<String> ignorePathList) {
        this(true, null, ignorePathList);
    }

    private JSONCompareResult(boolean success, String message, List<String> ignorePathList) {
        this.success = success;
        this.message = new StringBuilder(message == null ? "" : message);
        this.ignorePathList = ignorePathList == null ? new ArrayList(0) : ignorePathList;
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
     * @param field    Which field failed
     * @param expected Expected result
     * @param actual   Actual result
     * @return result of comparision
     */
    public JSONCompareResult fail(String field, Object expected, Object actual) {
        if (isFilter(field, ignorePathList)) {
            return this;
        }
        this.fieldFailures.add(new FieldComparisonFailure(field, expected, actual));
        fail(formatFailureMessage(field, expected, actual));
        return this;
    }

    /**
     * Identify that the comparison failed
     *
     * @param field     Which field failed
     * @param exception exception containing details of match failure
     * @return result of comparision
     */
    public JSONCompareResult fail(String field, ValueMatcherException exception) {
        fail(field + ": " + exception.getMessage(), exception.getExpected(), exception.getActual());
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
     * @param field    missing field
     * @param expected expected result
     * @return result of comparison
     */
    public JSONCompareResult missing(String field, Object expected) {
        if (isFilter(field, ignorePathList)) {
            return this;
        }
        fieldMissing.add(new FieldComparisonFailure(field, expected, null));
        fail(formatMissing(field, expected));
        return this;
    }

    private String formatMissing(String field, Object expected) {
        return field
                + "\nExpected: "
                + describe(expected)
                + "\n     but none found\n";
    }

    /**
     * Identify unexpected field
     *
     * @param field  unexpected field
     * @param actual actual result
     * @return result of comparison
     */
    public JSONCompareResult unexpected(String field, Object actual) {
        if (isFilter(field, ignorePathList)) {
            return this;
        }
        fieldUnexpected.add(new FieldComparisonFailure(field, null, actual));
        fail(formatUnexpected(field, actual));
        return this;
    }

    private String formatUnexpected(String field, Object actual) {
        return field
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
        return ignorePathList.contains(path);
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
