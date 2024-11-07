package com.techstud.scheduleuniversity.listener;

import com.techstud.scheduleuniversity.dto.parser.response.Schedule;
import org.springframework.stereotype.Service;
import reactor.core.publisher.MonoSink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class KafkaResponseHandler {

    private final ConcurrentMap<String, MonoSink<Schedule>> responseMap = new ConcurrentHashMap<>();

    public void register(String id, MonoSink<Schedule> sink) {
        responseMap.put(id, sink);
    }

    public void completeSuccess(String id, Schedule schedule) {
        MonoSink<Schedule> sink = responseMap.remove(id);
        if (sink != null) {
            sink.success(schedule);
        }
    }

    public void completeError(String id, Throwable error) {
        MonoSink<Schedule> sink = responseMap.remove(id);
        if (sink != null) {
            sink.error(error);
        }
    }

    public void remove(String id) {
        responseMap.remove(id);
    }
}
