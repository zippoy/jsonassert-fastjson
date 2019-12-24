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

import com.alibaba.fastjson.JSONException;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONPathJoinner;
import org.skyscreamer.jsonassert.ValueMatcherException;

import java.util.Arrays;
import java.util.Collection;

public class CustomComparator extends DefaultComparator {

    private final Collection<Customization> customizations;

    public CustomComparator(JSONCompareMode mode,  Customization... customizations) {
        super(mode);
        this.customizations = Arrays.asList(customizations);
    }

    @Override
    public void compareValues(JSONPathJoinner joinner, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        String path = joinner.getPath();
        Customization customization = getCustomization(path);
        if (customization != null) {
            try {
    	        if (!customization.matches(joinner.getPath(), actualValue, expectedValue, result)) {
                    result.fail(joinner, expectedValue, actualValue);
                }
            }
            catch (ValueMatcherException e) {
                result.fail(joinner, e);
            }
        } else {
            super.compareValues(joinner, expectedValue, actualValue, result);
        }
    }

    private Customization getCustomization(String prefix) {
        for (Customization c : customizations) {
            if (c.appliesToPath(prefix)) {
                return c;
            }
        }
        return null;
    }
}
