package org.ld.beans;

import java.io.Serializable;

/**
 * Table: config_properties
 */
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

    private String updateSql;

    private static final long serialVersionUID = 1L;

    public String getUpdateSql() {
        return this.updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public ConfigProperties() {
    }

    public ConfigProperties(Long id, String key1, String value1, String application, String profile, String label, Boolean editable, String desc, String unit) {
        this.id = id;
        this.key1 = key1;
        this.value1 = value1;
        this.application = application;
        this.profile = profile;
        this.label = label;
        this.editable = editable;
        this.desc = desc;
        this.unit = unit;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}