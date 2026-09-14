package com.potato.jarvis.security

enum class Risk(val level: Int) { READ(0), LOW(1), CONFIRM(2), BIOMETRIC(3), DENY(4) }
enum class Decision { ALLOW, CONFIRM, BIOMETRIC, DENY }

data class ApprovalPresentation(val decision: Decision, val requiresBiometric: Boolean, val message: String)

/** Client-side presentation only; the backend is the authoritative security boundary. */
class SecurityGateway {
    fun decision(level: Int): Decision = when {
        level >= Risk.DENY.level -> Decision.DENY
        level >= Risk.BIOMETRIC.level -> Decision.BIOMETRIC
        level >= Risk.CONFIRM.level -> Decision.CONFIRM
        else -> Decision.ALLOW
    }

    fun presentation(tool: String, risk: Int, requiresBiometric: Boolean = risk >= Risk.BIOMETRIC.level): ApprovalPresentation {
        val resolved = if (requiresBiometric) Decision.BIOMETRIC else decision(risk)
        val message = when (resolved) {
            Decision.ALLOW -> "$tool is allowed by the server policy."
            Decision.CONFIRM -> "$tool will change data. Confirm the exact action."
            Decision.BIOMETRIC -> "$tool requires biometric confirmation for this action."
            Decision.DENY -> "$tool is blocked by the security policy."
        }
        return ApprovalPresentation(resolved, requiresBiometric, message)
    }
}
