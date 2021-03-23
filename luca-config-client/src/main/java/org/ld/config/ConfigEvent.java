package org.ld.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class ConfigEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    @Setter
    @Getter
    private String msg;

    public ConfigEvent(Object source,String msg) {
        super(source);
        this.msg = msg;
    }
}
