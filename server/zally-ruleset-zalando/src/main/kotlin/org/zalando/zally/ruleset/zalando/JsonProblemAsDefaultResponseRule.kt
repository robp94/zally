package org.zalando.zally.ruleset.zalando

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = MxRuleSet::class,
    id = "151",
    severity = Severity.MUST,
    title = "Specify Success and Error Responses"
)
class JsonProblemAsDefaultResponseRule {
    private val validRefs = listOf(
        "https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.1.yaml#/Problem",
        "https://opensource.zalando.com/restful-api-guidelines/problem-1.0.1.yaml#/Problem",
        "https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.0.yaml#/Problem",
        "https://opensource.zalando.com/restful-api-guidelines/problem-1.0.0.yaml#/Problem",
        "https://opensource.zalando.com/problem/schema.yaml#/Problem",
        "https://zalando.github.io/problem/schema.yaml#/Problem"
    )
    private val validContentTypes = listOf("application/json", "application/problem+json")

    @Check(severity = Severity.SHOULD)
    fun checkContainsDefaultResponse(context: Context): List<Violation> = responsesPerOperation(context)
        .filterNot { "default" in it.second.keys }
        .map { context.violation("operation should contain the default response", it.first) }

    @Check(severity = Severity.SHOULD)
    fun checkDefaultResponseIsProblemJsonMediaType(context: Context): List<Violation> = responsesPerOperation(context)
        .filter { "default" in it.second.keys }
        .flatMap { it.second.getValue("default").content.orEmpty().entries }
        .filterNot { (contentType, _) -> contentType in validContentTypes }
        .map { context.violation("media-type application/problem+json should be used as default response", it.value) }

    @Check(severity = Severity.SHOULD)
    fun checkDefaultResponseIsProblemJsonSchema(context: Context): List<Violation> = responsesPerOperation(context)
        .filter { "default" in it.second.keys }
        .flatMap { it.second.getValue("default").content.orEmpty().entries }
        .filter { (contentType, _) -> contentType in validContentTypes }
        .filterNot { it.value?.schema?.`$ref` in validRefs }
        .filterNot { isProblemJsonSchema(it.value?.schema) }
        .map { context.violation("problem+json should be used as default response", it.value) }

    private fun responsesPerOperation(context: Context): Collection<Pair<Operation, Map<String, ApiResponse>>> =
        context.api.paths?.values
            .orEmpty()
            .flatMap {
                it?.readOperations().orEmpty()
                    .map { operation -> Pair(operation, operation.responses.orEmpty()) }
            }

    private fun isProblemJsonSchema(schema: Schema<*>?): Boolean {
        val props = schema?.properties.orEmpty()
        return props["type"]?.type == "string" &&
            (props["type"]?.format == "uri" || props["type"]?.format == "uri-reference") &&
            props["title"]?.type == "string" &&
            props["status"]?.type == "integer" &&
            props["status"]?.format == "int32" &&
            props["detail"]?.type == "string" &&
            props["instance"]?.type == "string" &&
            (props["instance"]?.format == "uri" || props["instance"]?.format == "uri-reference")
    }
}
