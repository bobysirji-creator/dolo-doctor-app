package com.dolo.doctor.authority

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthorityRehearsalPolicyTest {
    @Test fun cacheFreshnessIsExplicitAndBounded() {
        assertEquals(AuthoritySource.CACHED_FRESH, AuthorityRehearsalPolicy.classifyAge(60_000))
        assertEquals(AuthoritySource.CACHED_STALE, AuthorityRehearsalPolicy.classifyAge(16 * 60_000L))
        assertEquals(AuthoritySource.CACHED_STALE, AuthorityRehearsalPolicy.classifyAge(-1))
    }

    @Test fun retriesRequireAnIdempotencyKeyAndTransientOutcome() {
        assertTrue(AuthorityRehearsalPolicy.canRetry("POST", true, 1, 503))
        assertTrue(AuthorityRehearsalPolicy.canRetry("PUT", true, 1, null))
        assertFalse(AuthorityRehearsalPolicy.canRetry("POST", false, 1, 503))
        assertFalse(AuthorityRehearsalPolicy.canRetry("GET", true, 1, 503))
        assertFalse(AuthorityRehearsalPolicy.canRetry("POST", true, 2, 503))
        assertFalse(AuthorityRehearsalPolicy.canRetry("POST", true, 1, 409))
        assertTrue(AuthorityRehearsalPolicy.canUseCacheAfterFailure(null))
        assertTrue(AuthorityRehearsalPolicy.canUseCacheAfterFailure(503))
        assertFalse(AuthorityRehearsalPolicy.canUseCacheAfterFailure(401))
        assertFalse(AuthorityRehearsalPolicy.canUseCacheAfterFailure(409))
    }

    @Test fun conflictsRequireRefreshInsteadOfOverwrite() {
        assertEquals(
            "Server state changed. Refresh authoritative data before trying again.",
            AuthorityRehearsalPolicy.conflictMessage(409)
        )
        assertEquals(null, AuthorityRehearsalPolicy.conflictMessage(400))
    }
}