package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 转换函数类型表
 * @author guangya.zhao
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FunctionType {
    /**
     * 函数分类code
     * eg: relation,arith
     */
    private String code;

    /**
     * 函数分类名称
     * eg: 关系运算符，算术运算符
     */
    private String name;

    private List<Function> functions;

}
