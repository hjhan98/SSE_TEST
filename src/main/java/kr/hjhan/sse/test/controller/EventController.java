package kr.hjhan.sse.test.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/events")
// TEST
public class EventController {
    // 생성된 eventStream 목록 저장을 위한 로컬 맵
    private static final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    @GetMapping("/api/subscribe")
    public SseEmitter subscribe(String id){
        SseEmitter emitter = new SseEmitter();
        clients.put(id, emitter);

        emitter.onTimeout(() -> clients.remove(id));
        emitter.onCompletion(()-> clients.remove(id));
        publish("이거 못하면 찐찌버거");
        return emitter;
    }

    @GetMapping("/api/publish")
    public void publish(String msg){
        //disconnect Ids 집합 (순서 X, 중복허용 X)
        Set<String> deadIds = new HashSet<>();

        clients.forEach((id, emitter) -> {
            try{
                emitter.send(msg, MediaType.APPLICATION_JSON);
            }catch (Exception ex){
                deadIds.add(id);
                log.warn("disconnected Id : {}", id);
            }
        });
        deadIds.forEach(clients::remove);
    }
}
