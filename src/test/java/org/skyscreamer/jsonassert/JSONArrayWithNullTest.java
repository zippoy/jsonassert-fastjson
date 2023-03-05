package org.skyscreamer.jsonassert;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

public class JSONArrayWithNullTest {
    @Test
    public void testJSONArrayWithNullValue() throws JSONException {
        JSONArray jsonArray1 = getJSONArray1();
        JSONArray jsonArray2 = getJSONArray2();

        JSONAssert.assertEquals(jsonArray1, jsonArray2, true);
        JSONAssert.assertEquals(jsonArray1, jsonArray2, false);
    }

    @Test
    public void testJSONArrayWithNullValueAndJsonObject() throws JSONException {
        JSONArray jsonArray1 = getJSONArray1();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("hey", "value");

        JSONArray jsonArray2 = getJSONArray2();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("hey", "value");

        JSONAssert.assertEquals(jsonArray1, jsonArray2, true);
        JSONAssert.assertEquals(jsonArray1, jsonArray2, false);
    }

    private JSONArray getJSONArray1() {
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.add(1);
        jsonArray1.add(null);
        jsonArray1.add(3);
        jsonArray1.add(2);
        return jsonArray1;
    }

    private JSONArray getJSONArray2() {
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.add(1);
        jsonArray1.add(null);
        jsonArray1.add(3);
        jsonArray1.add(2);
        return jsonArray1;
    }
}
