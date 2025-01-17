package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.OpenApiSections.Companion.PATHS
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "143",
    severity = Severity.MUST,
    title = "Resources must be identified via path segments"
)
class IdentifyResourcesViaPathSegments {
    private val pathStartsWithParameter = "Path must start with a resource"
    private val pathParameterContainsPrefixOrSuffix = "Path parameter must not contain prefixes and suffixes"

    private val pathStartingWithAParameter = """(^/\{[^/]+\}|/)""".toRegex()

    @Check(severity = Severity.MUST)
    fun pathStartsWithResource(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathStartingWithAParameter.matches(it.key) },
        action = { context.violations(pathStartsWithParameter, PATHS.toJsonPointer() + it.key.toEscapedJsonPointer()) }
    )

    private val pathContainingPrefixedOrSuffixedParameter = """.*/([^/]+\{[^/]+\}|\{[^/]+\}[^/]+).*""".toRegex()

    @Check(severity = Severity.MUST)
    fun pathParameterDoesNotContainPrefixAndSuffix(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathContainingPrefixedOrSuffixedParameter.matches(it.key) },
        action = {
            context.violations(
                pathParameterContainsPrefixOrSuffix,
                PATHS.toJsonPointer() + it.key.toEscapedJsonPointer()
            )
        }
    )
}
