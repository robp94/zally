package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.util.getAllHeaders
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "166",
    severity = Severity.MUST,
    title = "Avoid Link in Header Rule"
)
class AvoidLinkHeadersRule(rulesConfig: Config) {

    private val headersWhitelist = rulesConfig.getStringList("HttpHeadersRule.whitelist").toSet()

    private val description = "Do Not Use Link Headers with JSON entities"

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        val allHeaders = context.api.getAllHeaders()
        return allHeaders
            .filter { it.name !in headersWhitelist && it.name == "Link" }
            .map { context.violation(description, it.element) } // createViolation(context, it) }
    }
}
