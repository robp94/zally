package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.util.PatternUtil.isPathVariable
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "145",
    severity = Severity.MAY,
    title = "Consider Using (Non-) Nested URLs"
)
class NestedPathsMayBeRootPathsRule {
    private val description = "Nested paths may be top-level resource"

    @Check(severity = Severity.MAY)
    fun checkNestedPaths(context: Context): List<Violation> =
        context.api.paths.orEmpty().entries
            .map { (path, pathEntry) -> Pair(pathEntry, path.split("/").filter { isPathVariable(it) }.count()) }
            .filter { (_, numberOfPathParameters) -> numberOfPathParameters > 1 }
            .map { (path, _) -> context.violation(description, path) }
}
