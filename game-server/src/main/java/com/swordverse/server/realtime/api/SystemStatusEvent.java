package com.swordverse.server.realtime.api;

import java.time.Instant;

/**
 * Diagnostic realtime event describing availability of the SwordVerse server.
 *
 * @param application application display name
 * @param status current availability state
 * @param serverTime server timestamp at event creation
 */
public record SystemStatusEvent(String application, String status, Instant serverTime) {}
