package typechecker

import parser.*

fun getFnDefs(fns: List<Program>): Pair<Set<String>, Map<String, Fn>> {
    val fnNames = mutableSetOf<String>()
    val fnLookup = mutableMapOf<String, Fn>()
    for(fn in fns) {
        if(fn is Fn) {
            fnNames.add(fn.name.name.value)
            fnLookup[fn.name.name.value] = fn
        }
    }

    return Pair(fnNames, fnLookup)
}

fun generateCallDependencyGraph(fns: List<Program>): Map<String, Set<String>> {
    val (fnNames, fnLookup) = getFnDefs(fns)
    val dependencyGraph =
        fns.filterIsInstance<Fn>().associate {
            it.name.name.value to (it.body.fold(setOf<String>()) { acc, fnBody -> acc + getCalls(fnBody, fnNames) } - it.name.name.value )
        }

    return dependencyGraph
}

//Not in a cycle: generate type, return environment
//in a cycle: finish generating dependencies, add self to a different environment for the cycle, return both environments
//do this until the cycle is fully explored

sealed interface CallGraphThing
data class Yay(val env: Environment): CallGraphThing
data class Sad(val env: Environment, val cycle: Environment)

fun dfs(fnName: String, dependencyGraph: Map<String, Set<String>>, fnLookup: Map<String, Fn>, env: Environment, component: Map<String, Set<String>>, visited: MutableSet<String> = mutableSetOf()): Environment {
    if(visited.contains(fnName)) return env

    val dependencies = dependencyGraph[fnName]
    if(dependencies == null) {
        val (c, e) = generateProgramConstraints(env, fnLookup[fnName]!!)
        return e
    }

    var envp = env

    for(name in dependencies) {
        envp = dfs(name, dependencyGraph, fnLookup, envp, component, visited)
    }

    visited.add(fnName)
    val (c, e) = generateProgramConstraints(envp, fnLookup[fnName]!!)
    return e
}


fun getCalls(ast: Ast, fnDefs: Set<String>): Set<String> =
    when (ast) {
        is Name -> if(fnDefs.contains(ast.name.value)) setOf(ast.name.value) else setOf()
        is FnCall -> ast.arguments.fold(getCalls(ast.fn, fnDefs)) { acc, expression -> acc + getCalls(expression, fnDefs) }

        is EntityDefinition -> ast.expressions.fold(setOf()) { acc, expression -> acc + getCalls(expression, fnDefs) }
        is EntitySelection -> getCalls(ast.entity, fnDefs)
        is EntityRestriction -> getCalls(ast.entity, fnDefs)
        is ComponentSelection -> getCalls(ast.component, fnDefs)

        is Let -> getCalls(ast.expression, fnDefs)
        is Return -> getCalls(ast.expression, fnDefs)

        is IntConstant -> setOf()
        is FloatConstant -> setOf()
        is StringConstant -> setOf()
        is CharConstant -> setOf()

        else -> throw Exception()
    }