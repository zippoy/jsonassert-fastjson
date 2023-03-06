package org.skyscreamer.jsonassert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <br>
 *
 * @author zippoy
 * @date 2019-11-01
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JSONPathJoinner {

    public static final JSONPathJoinner EMPTY_PATH_JOINNER = new JSONPathJoinner("", "", "");

    public static final String SPECIAL_STRING_PATH_SEPARATE = ".$";

    private String prefix;
    private String current;
    private String suffix;

    private final StringBuffer buffer = new StringBuffer();


    public static JSONPathJoinner of(String path) {
        return new JSONPathJoinner("", path, "");
        //return JSONPathJoinner.builder().prefix("").current(path).suffix("").build();
    }

    public static JSONPathJoinner ofArray(String path, int i) {
        return new JSONPathJoinner("", path + '[' + i + ']', "");
        //return JSONPathJoinner.builder().prefix("").current(path + "[" + i + "]").suffix("").build();
    }

    public JSONPathJoinner appendArray() {
        buffer.append('[').append(']');
        return this;
    }

    public JSONPathJoinner appendArray(int index) {
        buffer.append('[').append(index).append(']');
        return this;
    }

    public JSONPathJoinner appendChildPrefix() {
        buffer.append(SPECIAL_STRING_PATH_SEPARATE);
        return this;
    }

    public JSONPathJoinner append(String key) {
        if(buffer.length() == 0) {
            buffer.append(key);
        } else {
            buffer.append('.').append(key);
        }
        return this;
    }

    public String getPath() {
        return prefix + current + suffix;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
