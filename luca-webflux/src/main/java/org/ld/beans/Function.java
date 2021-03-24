package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Function implements Serializable {

    /**
     * 函数名称
     * eg: 加/大于/且/取整
     */
    private String name;

    /**
     * 函数描述
     */
    private String description;

    /**
     * 函数的返回类型，参考inceptor文档。返回类型为boolean的可以在过滤器中使用
     */
    private String returnType;

    public Integer getParamNumber() {
        int n = 0;
        int index;
        String strRes = "%s";
        index = rule.indexOf(strRes);
        while (index != -1) {
            n++;
            index = rule.indexOf(strRes, index + 1);
        }
        return n;
    }

    /**
     * 转换为sql的表达式
     * eg: ${p1} + ${p2}
     */
    private String rule;

}
