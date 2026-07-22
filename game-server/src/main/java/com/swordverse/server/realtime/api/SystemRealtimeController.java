package com.swordverse.server.realtime.api;

import java.time.Clock;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SystemRealtimeController {

    private final Clock clock;

    public SystemRealtimeController(Clock clock) {
        this.clock = clock;
    }

    @MessageMapping("/system/status")
    @SendTo("/topic/system/status")
    public SystemStatusEvent getSystemStatus() {
        return new SystemStatusEvent(
                "SwordVerse",
                "ONLINE",
                clock.instant());
    }
}
