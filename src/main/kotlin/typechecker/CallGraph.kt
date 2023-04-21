package typechecker

import parser.*

fun generateCallDependencyGraph(defs: List<ProgramAst>) {
    val fnNames = mutableSetOf<String>()
    val fnLookup = mutableMapOf<String, FnAst>()
    val dependencyGraph = mutableMapOf<String, Set<String>>()
    var env = Environment()

    for(def in defs) {
        if(def is FnAst) {
            fnNames.add(def.name.value)
        } else if(def is ComponentAst) {
            env = generateComponentConstraints(env, def).environment
        }
    }

    for(def in defs) {
        if(def is FnAst) {
            fnLookup[def.name.value] = def
            dependencyGraph[def.name.value] = (def.body.fold(setOf<String>()) { acc, fnBody -> acc + getCalls(fnBody, fnNames) } - def.name.value )
        }
    }

    val visited = mutableSetOf<String>()
    for(fn in fnNames) {
        when(val envp = dfs(fn, dependencyGraph, fnLookup, env, CallGraphState(listOf(), setOf(), setOf()), visited)) {
            is Yay -> env = envp.env
            is Sad -> throw Exception()
        }
    }
}

sealed interface CallGraphThing
data class Yay(val env: Environment): CallGraphThing
data class Sad(val env: Environment, val state: CallGraphState): CallGraphThing

data class CallGraphState(val stack: List<String>, val component: Set<String>, val backwards: Set<String>)

fun dfs(fnName: String, dependencyGraph: Map<String, Set<String>>, fnLookup: Map<String, FnAst>, env: Environment,
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
        is NameAst -> if(fnDefs.contains(ast.name.value)) setOf(ast.name.value) else setOf()
        is FnCallAst -> ast.arguments.fold(getCalls(ast.fn, fnDefs)) { acc, expression -> acc + getCalls(expression, fnDefs) }

        is EntityDefinitionAst -> ast.expressions.fold(setOf()) { acc, expression -> acc + getCalls(expression, fnDefs) }
        is EntitySelectionAst -> getCalls(ast.entity, fnDefs)
        is EntityRestrictionAst -> getCalls(ast.entity, fnDefs)
        is ComponentSelectionAst -> getCalls(ast.component, fnDefs)

        is LetAst -> getCalls(ast.expression, fnDefs)
        is ReturnAst -> getCalls(ast.expression, fnDefs)

        is IntConstantAst -> setOf()
        is FloatConstantAst -> setOf()
        is StringConstantAst -> setOf()
        is CharConstantAst -> setOf()

        else -> throw Exception()
    }