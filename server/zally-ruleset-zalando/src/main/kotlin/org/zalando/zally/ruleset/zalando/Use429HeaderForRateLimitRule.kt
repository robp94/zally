package org.zalando.zally.ruleset.zalando

import io.swagger.v3.oas.models.responses.ApiResponse
import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "153",
    severity = Severity.MUST,
    title = "Use 429 With Header For Rate Limits"
)
class Use429HeaderForRateLimitRule {

    private val description = "Response has to contain rate limit information via headers"
    private val xRateLimitHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

    @Check(severity = Severity.MUST)
    fun checkHeadersForRateLimiting(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            operation?.responses?.let { responses ->
                responses
                    .filter {
                        violatingResponse(it)
                    }
                    .flatMap { (status, _) ->
                        context.violations(
                            description,
                            context.getJsonPointer(responses) + status.toEscapedJsonPointer()
                        )
                    }
            }.orEmpty()
        }

    private fun violatingResponse(entry: Map.Entry<String, ApiResponse?>): Boolean {
        val headers = entry.value?.headers.orEmpty().keys
        return "429" == entry.key &&
            "Retry-After" !in headers &&
            !headers.containsAll(xRateLimitHeaders)
    }
}
