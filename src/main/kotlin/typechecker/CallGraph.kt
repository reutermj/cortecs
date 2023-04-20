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

fun generateCallDependencyGraph(defs: List<Program>) {
    val fnNames = mutableSetOf<String>()
    val fnLookup = mutableMapOf<String, Fn>()
    val dependencyGraph = mutableMapOf<String, Set<String>>()
    var env = Environment()

    for(def in defs) {
        if(def is Fn) {
            fnNames.add(def.name.name.value)
        } else if(def is Component) {
            env = generateComponentConstraints(env, def).environment
        }
    }

    for(def in defs) {
        if(def is Fn) {
            fnLookup[def.name.name.value] = def
            dependencyGraph[def.name.name.value] = (def.body.fold(setOf<String>()) { acc, fnBody -> acc + getCalls(fnBody, fnNames) } - def.name.name.value )
        }
    }

    val visited = mutableSetOf<String>()
    for(fn in fnNames) {
        dfs(fn, dependencyGraph, fnLookup, env, CallGraphState(listOf(), setOf(), setOf()), visited)
    }
}

sealed interface CallGraphThing
data class Yay(val env: Environment): CallGraphThing
data class Sad(val env: Environment, val state: CallGraphState): CallGraphThing

data class CallGraphState(val stack: List<String>, val component: Set<String>, val backwards: Set<String>)

fun dfs(fnName: String, dependencyGraph: Map<String, Set<String>>, fnLookup: Map<String, Fn>, env: Environment,
        state: CallGraphState = CallGraphState(listOf(), setOf(), setOf()),
        visited: MutableSet<String> = mutableSetOf()): CallGraphThing {
    if(visited.contains(fnName)) return Yay(env)


    //   8--|
    //   ^  |
    //   |  v
    //1->2->3-|
    //   ^  ^ |
    //   | /  |
    //   |/   |
    //   5<-4<-
    //   ^  |
    //   |  |
    //7<-6<--
    //|  ^
    //---|

    //leaf node: no need to follow dependencies
    val dependencies = dependencyGraph[fnName]
    if(dependencies?.isEmpty() != false) {
        visited.add(fnName)
        val e = generateProgramConstraints(env, fnLookup[fnName]!!)
        return Yay(e.environment)
    }

    //works for the 5->2, 5->3, and 6->7 cycles but not the 6->5 dependency
    //5 needs to be compiled at the same time as 4 and before 6, but since 4
    //depends on 6, we cant compile the cycle before 6
    //so we need to add 6 to the component
    var i = state.stack.indexOf(fnName)
    if(i != -1) {
        val compp = state.component.toMutableSet()
        val backp = state.backwards.toMutableSet()

        //inefficiency in cases where 5->3 is processed before 5->2
        while(i < state.stack.size) {
            compp.add(state.stack[i])
            backp.add(state.stack[i])
            i++
        }

        return Sad(env, state.copy(component = compp, backwards = backp))
    }

    if(state.component.contains(fnName)) {
        return Sad(env, state)
    }

    //follow dependencies
    var envp = env
    var statep = state.copy(stack = state.stack + fnName)

    for(name in dependencies) {
        when(val thing = dfs(name, dependencyGraph, fnLookup, envp, statep, visited)) {
            is Yay -> envp = thing.env
            is Sad -> {
                envp = thing.env
                statep = thing.state.copy(component = thing.state.component + fnName, backwards = thing.state.backwards + fnName)
            }
        }
    }

    if(!statep.component.contains(fnName)) {
        visited.add(fnName)
        val e = generateProgramConstraints(envp, fnLookup[fnName]!!)
        return Yay(e.environment)
    }


    statep = CallGraphState(state.stack, statep.component, statep.backwards - fnName)
    if(statep.backwards.isEmpty()) {
        val e = generateFnClusterConstraints(envp, statep.component.map { fnLookup[it]!! })
        visited.addAll(statep.component)
        return Yay(e.environment)
    } else {
        return Sad(envp, statep)
    }

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