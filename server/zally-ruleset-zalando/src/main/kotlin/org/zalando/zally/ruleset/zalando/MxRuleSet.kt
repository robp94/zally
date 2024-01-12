package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.AbstractRuleSet
import java.net.URI

class MxRuleSet : AbstractRuleSet() {

    override val url: URI = URI.create("https://prd-serviceplatform.mx.lan/restful-api-guidelines/")
}
