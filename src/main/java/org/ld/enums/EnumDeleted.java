package org.ld.enums;

public enum EnumDeleted {
    NO(0, "未删除"),
    YES(1, "已删除");

    private int value;
    private String desc;

    private EnumDeleted(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }
}
