package org.skyscreamer.jsonassert;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <br>
 *
 * @author zippoy
 * @date 2019-11-01
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
public class JSONPathJoinner {

    public static final JSONPathJoinner EMPTY_PATH_JOINNER = JSONPathJoinner.builder().prefix("").current("").suffix("").build();

    public static final String SPECIAL_STRING_PATH_SEPARATE = ".$";

    private String prefix;
    private String current;
    private String suffix;

    private StringBuffer buffer = new StringBuffer();


    public static JSONPathJoinner of(String path) {
        return JSONPathJoinner.builder().prefix("").current(path).suffix("").build();
    }

    public static JSONPathJoinner ofArray(String path, int i) {
        return JSONPathJoinner.builder().prefix("").current(path + "[" + i + "]").suffix("").build();
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
