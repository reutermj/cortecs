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

fun numRequirements(requirements: Requirements): Int {
    //return loop(0, iterate(requirements.requirements)) { i, it ->
    //  if isDone(it) { break i }
    //  let (_, types), nit = next(it)
    //  continue i + size(types), nit
    //}

    var i = 0
    for((_, types) in requirements.requirements) {
        i += types.size
    }
    return i
}

fun assertContainsAllErrors(superset: CortecsErrors, subset: CortecsErrors) {
    for(error in subset.errors) assertContains(superset.errors, error)
}