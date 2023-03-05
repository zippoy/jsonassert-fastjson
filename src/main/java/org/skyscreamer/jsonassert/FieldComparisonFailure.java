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

/**
 * Models a failure when comparing two fields.
 */
public class FieldComparisonFailure {
    private final String jsonPath;
    private final Object expected;
    private final Object actual;
    private final boolean ignore;

    public FieldComparisonFailure(String jsonPath, Object expected, Object actual) {
        this(jsonPath, expected, actual, false);
    }

    public FieldComparisonFailure(String jsonPath, Object expected, Object actual, boolean ignore) {
        this.jsonPath = jsonPath;
        this.expected = expected;
        this.actual = actual;
        this.ignore = ignore;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }

    public boolean isIgnore() {
        return ignore;
    }

}
