package org.zalando.zally.ruleset.zalando

import com.fasterxml.jackson.core.JsonPointer
import com.typesafe.config.Config
import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.OpenApiSections.Companion.PATHS
import org.zalando.zally.core.util.PatternUtil
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.ruleset.zalando.util.WordUtil.isPlural

@Rule(
    ruleSet = MxRuleSet::class,
    id = "134",
    severity = Severity.MUST,
    title = "Pluralize Resource Names"
)
class PluralizeResourceNamesRule(rulesConfig: Config) {

    private val slash = "/"

    private val slashes = "/+".toRegex()

    @Suppress("SpreadOperator")
    internal val whitelist = mutableListOf(
        *rulesConfig
            .getConfig(javaClass.simpleName)
            .getStringList("whitelist")
            .map { it.toRegex() }
            .toTypedArray()
    )

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        return context.validatePaths { (path, _) ->
            pathSegments(sanitizedPath(path, whitelist))
                .filter { isNonViolating(it) }
                .map { violation(context, it, PATHS.toJsonPointer() + path.toEscapedJsonPointer()) }
        }
    }

    private fun sanitizedPath(path: String, regexList: List<Regex>): String {
        return regexList.fold("/$path/".replace(slashes, slash)) { updated, regex ->
            updated.replace(regex, slash)
        }
    }

    private fun pathSegments(path: String): List<String> {
        return path.split(slashes).filter { !it.isEmpty() }
    }

    private fun isNonViolating(it: String) =
        !PatternUtil.isPathVariable(it) && !isPlural(it)

    private fun violation(context: Context, term: String, pointer: JsonPointer) =
        context.violation("Resource '$term' appears to be singular", pointer)
}
