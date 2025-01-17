package org.zalando.zally.ruleset.zalando

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "110",
    severity = Severity.MUST,
    title = "Response As JSON Object"
)
class SuccessResponseAsJsonObjectRule {

    private val description = "Always return JSON objects as top-level data structures to support extensibility"

    @Check(severity = Severity.MUST)
    fun checkJSONObjectIsUsedAsSuccessResponseType(context: Context): List<Violation> =
        context.api.paths.orEmpty().values
            .flatMap {
                it?.readOperations().orEmpty()
                    .flatMap { it.responses.orEmpty().filter { (resCode, _) -> isSuccess(resCode) }.values }
            }
            .flatMap {
                it?.content.orEmpty().entries
                    .filter { (mediaType, _) -> mediaType.contains("json") }
            }.mapNotNull { it.value?.schema }
            .filterNot { schema -> schema.type.isNullOrEmpty() || "object" == schema.type }
            .map { schema -> context.violation(description, schema) }

    private fun isSuccess(codeString: String) = codeString.toIntOrNull() in 200..299
}
