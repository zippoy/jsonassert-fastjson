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
import org.skyscreamer.jsonassert.JSONCompareConfig;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONPathJoinner;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allJSONObjects;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allSimpleValues;

/**
 * This class is the default json comparator implementation.
 * Comparison is performed according to {@link JSONCompareMode} that is passed as constructor's argument.
 */
public class DefaultComparator extends AbstractComparator {

    public static final JSONCompareConfig EMPTY_CONFIG = new JSONCompareConfig();

    JSONCompareMode mode;

    /**
     *
     */
    private JSONCompareConfig config;

    public DefaultComparator(JSONCompareMode mode) {
        this(mode, EMPTY_CONFIG);
    }

    public DefaultComparator(JSONCompareMode mode, JSONCompareConfig config) {
        if (null == mode) {
            throw new IllegalArgumentException("JSONCompareMode is null");
        }
//        if (!mode.hasStrictOrder() && (null != noStrictOrderPathList || !noStrictOrderPathList.isEmpty())) {
//            // todo
//        }
        this.mode = mode;
        this.config = null == config ? EMPTY_CONFIG : config;
    }

    @Override
    public JSONCompareConfig getConfig() {
        if (null == config) {
            config = EMPTY_CONFIG;
        }
        return config;
    }

    @Override
    public void compareJSON(JSONPathJoinner joinner, JSONObject expected, JSONObject actual, JSONCompareResult result)
            throws JSONException {
        // Check that actual contains all the expected values
        checkJsonObjectKeysExpectedInActual(joinner, expected, actual, result);

        // If strict, check for vice-versa
        if (!mode.isExtensible()) {
            checkJsonObjectKeysActualInExpected(joinner, expected, actual, result);
        }
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
            } else if (!expectedValue.equals(actualValue)) {
                result.fail(joinner, expectedValue, actualValue);
            }
        } else {
            result.fail(joinner, expectedValue, actualValue);
        }
    }

    @Override
    public void compareJSONArray(JSONPathJoinner joinner, JSONArray expected, JSONArray actual, JSONCompareResult result)
            throws JSONException {
//        if (expected.size() != actual.size()) {
//            // TODO
//            // result.fail(prefix + "[]: Expected " + expected.size() + " values but got " + actual.size());
//            return;
//        } else
        if (expected.isEmpty() && actual.isEmpty()) {
            return; // Nothing to compare
        }

        if (mode.hasStrictOrder()) {
            compareJSONArrayWithStrictOrder(joinner, expected, actual, result);
        } else if (allSimpleValues(expected)) {
            compareJSONArrayOfSimpleValues(joinner, expected, actual, result);
        } else if (allJSONObjects(expected)) {
            compareJSONArrayOfJsonObjects(joinner, expected, actual, result);
        } else {
            // An expensive last resort
            recursivelyCompareJSONArray(joinner, expected, actual, result);
        }
    }

    protected boolean areNumbers(Object expectedValue, Object actualValue) {
        return expectedValue instanceof Number && actualValue instanceof Number;
    }

    protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
        return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
    }
}
