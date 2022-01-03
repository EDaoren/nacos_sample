package com.edaoren.event.publisher;

import com.edaoren.event.constants.SpringEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring事件发布者
 *
 * @author EDaoren
 */
@Component
public class SpringEventPublisher {

    @Autowired
    private ApplicationContext applicationContext;

    public <T> void publish(SpringEvent<T> event) {
        List<SpringEvent<T>> events = new ArrayList<>();
        events.add(event);
        publish(events);
    }

    public <T> void publish(List<SpringEvent<T>> events) {
        for (SpringEvent<T> event : events) {
            applicationContext.publishEvent(event);
        }
    }
}
