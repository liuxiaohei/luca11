package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Table: config_properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigProperties implements Serializable {
    /**
     * Nullable:  false
     */
    private Long id;

    /**
     * 配置的key
     *
     * Nullable:  false
     */
    private String key1;

    /**
     * 配置的value
     *
     * Nullable:  true
     */
    private String value1;

    /**
     * 应用名字
     *
     * Nullable:  false
     */
    private String application;

    /**
     * 由client端选择用哪个profile
     *
     * Nullable:  false
     */
    private String profile;

    /**
     * 由server端选择哪个lable
     *
     * Nullable:  false
     */
    private String label;

    /**
     * 是否可编辑
     *
     * Nullable:  false
     */
    private Boolean editable;

    /**
     * 属性描述
     *
     * Nullable:  false
     */
    private String desc;

    /**
     * 属性单位
     *
     * Nullable:  false
     */
    private String unit;

    /**
     * 是否被禁用
     *
     * Nullable:  false
     */
    private Boolean disable;
}