package org.ld.config;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigEventListener implements ApplicationListener<ActionConfigEvent> {

    @Override
    public void onApplicationEvent(ActionConfigEvent event) {
        System.out.println("接收事件");
        System.out.println(event.getPropertyMap().toString());
    }
}
