package typechecker

import errors.CortecsErrors
import kotlin.test.assertContains

fun assertContainsAllRequirements(superset: Requirements, subset: Requirements) {
    for((name, types) in subset.requirements) {
        val requirements = superset.requirements[name]!!
        for(type in types) {
            assertContains(requirements, type)
        }
    }
}

fun assertContainsAllErrors(superset: CortecsErrors, subset: CortecsErrors) {
    for(error in subset.errors) assertContains(superset.errors, error)
}