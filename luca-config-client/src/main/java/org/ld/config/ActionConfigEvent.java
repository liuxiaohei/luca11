package org.ld.config;

import lombok.Getter;
import org.ld.beans.ConfigChange;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class ActionConfigEvent extends ApplicationEvent {
    private final String eventDesc;
    private final Map<String, ConfigChange> propertyMap;

    public ActionConfigEvent(Object source, String eventDesc, Map<String, ConfigChange> propertyMap) {
        super(source);
        this.eventDesc = eventDesc;
        this.propertyMap = propertyMap;
    }
}
