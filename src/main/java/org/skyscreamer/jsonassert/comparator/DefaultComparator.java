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
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.ArrayList;
import java.util.List;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allJSONObjects;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allSimpleValues;

/**
 * This class is the default json comparator implementation.
 * Comparison is performed according to {@link JSONCompareMode} that is passed as constructor's argument.
 */
public class DefaultComparator extends AbstractComparator {

    JSONCompareMode mode;

    /**
     * need ignore path list
     */
    List<String> ignorePathList;

    public DefaultComparator(JSONCompareMode mode) {
        this(mode, new ArrayList(0));
    }

    public DefaultComparator(JSONCompareMode mode, List<String> ignorePathList) {
        this.mode = mode;
        this.ignorePathList = ignorePathList;
    }

    @Override
    public List<String> getIgnorePathList() {
        return ignorePathList;
    }

    @Override
    public void compareJSON(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result)
            throws JSONException {
        // Check that actual contains all the expected values
        checkJsonObjectKeysExpectedInActual(prefix, expected, actual, result);

        // If strict, check for vice-versa
        if (!mode.isExtensible()) {
            checkJsonObjectKeysActualInExpected(prefix, expected, actual, result);
        }
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
            throws JSONException {
        if (expectedValue == null && actualValue == null) {
            return;
        } else if (expectedValue == null && actualValue != null) {
            result.unexpected(prefix, actualValue);
        } else if (expectedValue != null && actualValue == null) {
            result.missing(prefix, expectedValue);
        } else if (areNumbers(expectedValue, actualValue)) {
            if (areNotSameDoubles(expectedValue, actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
            if (expectedValue instanceof JSONArray) {
                compareJSONArray(prefix, (JSONArray) expectedValue, (JSONArray) actualValue, result);
            } else if (expectedValue instanceof JSONObject) {
                compareJSON(prefix, (JSONObject) expectedValue, (JSONObject) actualValue, result);
            } else if (!expectedValue.equals(actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else {
            result.fail(prefix, expectedValue, actualValue);
        }
    }

    @Override
    public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result)
            throws JSONException {
        if (expected.size() != actual.size()) {
            result.fail(prefix + "[]: Expected " + expected.size() + " values but got " + actual.size());
            return;
        } else if (expected.size() == 0) {
            return; // Nothing to compare
        }

        if (mode.hasStrictOrder()) {
            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
        } else if (allSimpleValues(expected)) {
            compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
        } else if (allJSONObjects(expected)) {
            compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
        } else {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
        }
    }

    protected boolean areNumbers(Object expectedValue, Object actualValue) {
        return expectedValue instanceof Number && actualValue instanceof Number;
    }

    protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
        return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
    }
}
