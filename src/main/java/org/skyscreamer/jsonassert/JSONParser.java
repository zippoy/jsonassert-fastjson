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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

/**
 * Simple JSON parsing utility.
 */
public class JSONParser {
    // regular expression to match a number in JSON format.  see http://www.json.org/fatfree.html.
    // "A number can be represented as integer, real, or floating point. JSON does not support octal or hex
    // ... [or] NaN or Infinity".
    private static final String NUMBER_REGEX = "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

    private JSONParser() {
    }

    /**
     * Takes a JSON string and returns either a {@link JSONObject} or {@link JSONArray},
     * depending on whether the string represents an object or an array.
     *
     * @param s Raw JSON string to be parsed
     * @return JSONObject or JSONArray
     * @throws JSONException JSON parsing error
     */
    public static Object parseJSON(final String s) throws JSONException {
        if (s.trim().startsWith("{")) {
            return parseJSONObject(s);
        } else if (s.trim().startsWith("[")) {
            return parseJSONArray(s);
        } else if (s.trim().startsWith("\"")
                || s.trim().matches(NUMBER_REGEX)) {
            return (JSONAware) () -> s;
        }
        throw new JSONException("Unparsable JSON string: " + s);
    }


    public static JSONObject parseJSONObject(final String s) {
        return JSON.parseObject(s, Feature.CustomMapDeserializer);
    }

    public static Object parseJSONArray(final String s) {
        return JSON.parse(s, Feature.CustomMapDeserializer);
    }


    public static void main(String[] args) {
        String json = "{{\"customerName\":\"何功武\"}:{\"customerName\":\"刘振华\"}}";


        Object obj = JSONParser.parseJSON(json);

        System.out.println(obj);
    }

}
