package com.potato.jarvis

import com.potato.jarvis.security.Decision
import com.potato.jarvis.security.SecurityGateway
import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityGatewayTest {
    private val gateway = SecurityGateway()
    @Test fun risk0Allows() = assertEquals(Decision.ALLOW, gateway.decision(0))
    @Test fun risk2Confirms() = assertEquals(Decision.CONFIRM, gateway.decision(2))
    @Test fun risk3RequiresBiometric() = assertEquals(Decision.BIOMETRIC, gateway.decision(3))
    @Test fun risk4AndAboveDeny() {
        assertEquals(Decision.DENY, gateway.decision(4))
        assertEquals(Decision.DENY, gateway.decision(999))
    }
}
