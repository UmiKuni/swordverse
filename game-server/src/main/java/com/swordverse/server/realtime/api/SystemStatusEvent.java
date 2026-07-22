package com.swordverse.server.realtime.api;

import java.time.Instant;

public record SystemStatusEvent(
        String application,
        String status,
        Instant serverTime) {

}
