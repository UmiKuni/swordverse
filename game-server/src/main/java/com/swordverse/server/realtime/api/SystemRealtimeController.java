package com.swordverse.server.realtime.api;

import java.time.Clock;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/** Exposes authenticated diagnostic STOMP commands for verifying SwordVerse realtime delivery. */
@Controller
public class SystemRealtimeController {

    private final Clock clock;

    /**
     * Creates the diagnostic controller.
     *
     * @param clock server clock used to timestamp status events
     */
    public SystemRealtimeController(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the current SwordVerse availability event to subscribers of the system status topic.
     *
     * @return current server status
     */
    @MessageMapping("/system/status")
    @SendTo("/topic/system/status")
    public SystemStatusEvent getSystemStatus() {
        return new SystemStatusEvent("SwordVerse", "ONLINE", clock.instant());
    }
}
