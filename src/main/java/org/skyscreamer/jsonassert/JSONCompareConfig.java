package org.skyscreamer.jsonassert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class JSONCompareConfig implements Serializable {

    /**
     * Defines comparison behavior
     * 正常场景下的比较模式
     */
    private JSONCompareMode compareMode;

    /**
     * 需要忽略的字段
     * 必须是标准的JsonPath
     */
    @Builder.Default
    private Set<String> needIgnorePaths = Collections.emptySet();

    /**
     * 需要忽略顺序的字段
     * 必须是标准的JsonPath
     */
    @Builder.Default
    private Set<String> needIgnoreOrderPaths = Collections.emptySet();

    /**
     * 需要忽略字段的某些值
     */
    @Builder.Default
    private Map<String, List<Object>> needIgnoreValues = Collections.emptyMap();

    /**
     * 允许的数值精度误差，在某些场景某些字段可以设置一定的误差。小于等于设置误差不认为是diff
     * 字段类型必须是Number、可以转为date的字符串，否则不生效
     * 时间类型精度：毫秒
     */
    @Builder.Default
    private Map<String, Long> accuracyError = Collections.emptyMap();

    /**
     * 数组节点下的对象唯一key的jsonPath
     * 若配置可以加快diff速度
     */
    @Builder.Default
    private Set<String> uniqueKeys = Collections.emptySet();

    /**
     * 如果一个字段值是字符串json，是否需要diff
     * whether to enable diff of JSON that is string, is not a json object
     */
    private boolean enableStrJSONDiff;

    public JSONCompareConfig(JSONCompareMode compareMode) {
        this(compareMode, Collections.emptySet(), Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), false);
    }

    public JSONCompareConfig(JSONCompareMode compareMode, Set<String> needIgnorePaths, Set<String> needIgnoreOrderPaths) {
        this(compareMode, needIgnorePaths, needIgnoreOrderPaths, Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), false);
    }

    /**
     * 把唯一key的jsonPath转为最后一个节点为key的map，value是其对应的前缀集合
     * @return key:最后一个节点字段的名称   value: 最后一个节点名称的前缀集合
     */
    public Map<String, Set<String>> buildUniqueKeyMap() {
        if (uniqueKeys == null || uniqueKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> result = new HashMap<>();
        for (String uniqueKey : uniqueKeys) {
            int last = uniqueKey.lastIndexOf("\\.");
            result.computeIfAbsent(uniqueKey.substring(last), k -> new HashSet<>()).add(uniqueKey.substring(0, last));
            uniqueKey.split("\\.", 2);
        }

        return result;
    }

}
