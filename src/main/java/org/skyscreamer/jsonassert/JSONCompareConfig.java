package org.skyscreamer.jsonassert;

import lombok.Data;

import java.util.List;

/**
 * <br>
 *
 * @author zippoy
 * @date 2019-10-29
 */
@Data
public class JSONCompareConfig {

    /**
     * need ignore path list
     */
    private List<String> ignorePathList;

    /**
     * the path list of JSONArray that can be compared without order
     */
    private List<String> noStrictOrderPathList;

}
