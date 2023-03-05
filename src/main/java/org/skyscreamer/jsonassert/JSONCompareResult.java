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
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareResult {

    private StringBuilder message;

    /**
     * 存在diff的字段
     */
    private final List<FieldComparisonFailure> diffPathInfos = new ArrayList();
    /**
     * 缺失的字段
     */
    private final List<FieldComparisonFailure> missingPathInfos = new ArrayList();
    /**
     * 扩展的字段
     */
    private final List<FieldComparisonFailure> extraPathInfos = new ArrayList();

    /**
     * 其它一些无法预知的问题
     */
//    private List<FieldComparisonFailure> otherErrors = new ArrayList<>();

    /**
     * Default constructor.
     */
    public JSONCompareResult() {
        this("");
    }

    private JSONCompareResult(String message) {
        this.message = new StringBuilder(message == null ? "" : message);
    }

    /**
     * Did the comparison pass?
     *
     * @return True if it passed
     */
    public boolean passed() {
        return diffPathInfos.isEmpty() && missingPathInfos.isEmpty() && extraPathInfos.isEmpty();
    }

    /**
     * Did the comparison fail?
     *
     * @return True if it failed
     */
    public boolean failed() {
        return !passed();
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
    public List<FieldComparisonFailure> getFieldDiffs() {
        return Collections.unmodifiableList(diffPathInfos);
    }

    /**
     * Get the list of missed on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldMissing() {
        return Collections.unmodifiableList(missingPathInfos);
    }

    /**
     * Get the list of failures on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldUnexpected() {
        return Collections.unmodifiableList(extraPathInfos);
    }

//    public List<FieldComparisonFailure> getOtherErrors() {
//        return otherErrors;
//    }

    /**
     * Check if comparison failed on any particular fields
     *
     * @return true if there are field failures
     */
    public boolean isFailureOnField() {
        return !diffPathInfos.isEmpty();
    }

    /**
     * Check if comparison failed with missing on any particular fields
     *
     * @return true if an expected field is missing
     */
    public boolean isMissingOnField() {
        return !missingPathInfos.isEmpty();
    }

    /**
     * Check if comparison failed with unexpected on any particular fields
     *
     * @return true if an unexpected field is in the result
     */
    public boolean isUnexpectedOnField() {
        return !extraPathInfos.isEmpty();
    }

    public void fail(String message) {
        if (message.length() == 0) {
            this.message.append(message);
        } else {
            this.message.append(" ; ").append(message);
        }
    }

    /**
     * Identify that the comparison failed
     *
     * @param jsonPath    Which field failed
     * @param expected Expected result
     * @param actual   Actual result
     * @return result of comparision
     */
    public JSONCompareResult fail(String jsonPath, Object expected, Object actual) {
        this.diffPathInfos.add(new FieldComparisonFailure(jsonPath, expected, actual));
        fail(formatFailureMessage(jsonPath, expected, actual));
        return this;
    }


    /**
     * Identify that the comparison failed
     *
     * @param jsonPath    Which field failed
     * @param expected Expected result
     * @param actual   Actual result
     * @return result of comparision
     */
//    public JSONCompareResult error(String jsonPath, Object expected, Object actual) {
//        this.otherErrors.add(new FieldComparisonFailure(jsonPath, expected, actual));
//        return this;
//    }

    /**
     * Identify that the comparison failed
     *
     * @param jsonPath     Which field failed
     * @param exception exception containing details of match failure
     * @return result of comparision
     */
    public JSONCompareResult fail(String jsonPath, ValueMatcherException exception) {
        fail(jsonPath + ": " + exception.getMessage(), exception.getExpected(), exception.getActual());
        return this;
    }

    private String formatFailureMessage(String jsonPath, Object expected, Object actual) {
        return jsonPath
                + "\nExpected: "
                + describe(expected)
                + "\n     got: "
                + describe(actual)
                + "\n";
    }

    /**
     * Identify the missing field
     *
     * @param jsonPath    missing field
     * @param expected expected result
     * @return result of comparison
     */
    public JSONCompareResult missing(String jsonPath, Object expected) {
        missingPathInfos.add(new FieldComparisonFailure(jsonPath, expected, null));
        fail(formatMissing(jsonPath, expected));
        return this;
    }

    private String formatMissing(String jsonPath, Object expected) {
        return jsonPath
                + "\nExpected: "
                + describe(expected)
                + "\n     but none found\n";
    }

    /**
     * Identify unexpected field
     *
     * @param jsonPath  unexpected field
     * @param actual actual result
     * @return result of comparison
     */
    public JSONCompareResult unexpected(String jsonPath, Object actual) {
        extraPathInfos.add(new FieldComparisonFailure(jsonPath, null, actual));
        fail(formatUnexpected(jsonPath, actual));
        return this;
    }

    private String formatUnexpected(String jsonPath, Object actual) {
        return jsonPath
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

    @Override
    public String toString() {
        return message.toString();
    }
}
