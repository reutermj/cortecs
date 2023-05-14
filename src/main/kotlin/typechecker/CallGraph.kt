package typechecker

import parser.*
import codegen.*
import ir.ComponentMfir
import ir.FunctionMfir
import ir.constructMfir

//Definitions:
// An overlapping cycle in G is defined recursively:
//   * a cycle in G is an overlapping cycle in G,
//   * if C1 and C2 are overlapping cycles in G and there exists a vertex v contained in both C1 and C2,
//     then C1 u C2 is an overlapping cycle in G

// An overlapping cycle is said to be maximal iff no other overlapping cycle in G contains it.

// If function f calls function g, we say that f depends on g

// high level overview
// Step 1: construct a directed graph G=<V,E> where V=set of functions and E={f->g | f,g in V; f depends on g}
//   * note: dependencies is not necessarily a DAG because mutual recursion can induce cycles in dependencies
// Step 2: construct a DAG G'=<V',E'> from G where
//   * V' contains a vertex for every maximal overlapping cycle in G and every vertex in G not contained in a maximal overlapping cycle
//     * there exists a function toV that maps vertices in V' to the set of vertices in the corresponding maximal overlapping cycle in G or a set containing the single corresponding vertex
//   * E'= {x->y | x, y in V'; there exists f in toV(x) and g in toV(y) such that f->g in E}
//  Step 3: run type inference on G' in reverse topological order
//   * run type inference on all dependencies of a node before running type inference on that node

sealed interface Thing
data class IndividualNode(val fn: FnAst): Thing
data class MaximalOverlappingCycleNode(val cycle: Set<FnAst>): Thing

fun generateCallDependencyGraph(defs: List<ProgramAst>) {
    //create base environment with all components added to it
    val monomorphicLookup = mutableMapOf<Pair<String, Type>, FunctionMfir>()
    val components = defs.filterIsInstance<ComponentAst>()
    val environment = Environment()
    for(component in components) {
        addComponentToEnvironment(environment, component)
    }

    val componentsMfir = mutableListOf<ComponentMfir>()
    for(component in components) {
        constructMfir(component, monomorphicLookup, componentsMfir)
    }

    //construct dependency graph
    val fnNodes = defs.filterIsInstance<FnAst>()
    val fnLookup = fnNodes.associateBy { it.name.value }
    val dependencyGraph = fnNodes.associateWith { getCalls(it, fnLookup) }

    //find all maximal overlapping cycles
    val visited = mutableSetOf<FnAst>()
    val foundCycles = mutableMapOf<FnAst, MutableSet<FnAst>>()
    for(fn in fnNodes) {
        findCycles(fn, dependencyGraph, listOf(), visited, foundCycles)
    }

    //construct dependency dag
    val dependencyDag = mutableMapOf<Thing, MutableSet<Thing>>()
    for((k, v) in dependencyGraph) {
        val node =
            when(val cycle = foundCycles[k]) {
                null -> IndividualNode(k)
                else -> MaximalOverlappingCycleNode(cycle)
            }

        val outEdges =
            v.map {
                when(val cycle = foundCycles[it]) {
                    null -> IndividualNode(it)
                    else -> MaximalOverlappingCycleNode(cycle)
                }
            }.filter { it != node }.toSet()

        dependencyDag.getOrPut(node) { mutableSetOf() }.addAll(outEdges)
    }

    val visitedThings = mutableSetOf<Thing>()
    for(thing in dependencyDag.keys) {
        typeCheck(environment, thing, dependencyDag, visitedThings)
    }

    for(node in fnNodes) {
        constructMfir(node, fnLookup, monomorphicLookup)
    }

    for(component in componentsMfir) {
        generateCode(lower(component))
    }
    for(fn in monomorphicLookup.values.toSet()) {
        generateCode(lower(fn))
    }
}

fun typeCheck(environment: Environment, thing: Thing, dependencyDag: MutableMap<Thing, MutableSet<Thing>>, visited: MutableSet<Thing>) {
    if(visited.contains(thing)) return

    for(dependency in dependencyDag[thing]!!) typeCheck(environment, dependency, dependencyDag, visited)

    when(thing) {
        is IndividualNode -> addFnToEnvironment(environment, thing.fn)
        is MaximalOverlappingCycleNode -> addFnClusterToEnvironment(environment, thing.cycle)
    }

    visited.add(thing)
}

fun addAll(start: Int, stack: List<FnAst>, cycle: MutableSet<FnAst>, foundCycles: MutableMap<FnAst, MutableSet<FnAst>>) {
    for(i in start until stack.size) {
        val fn = stack[i]
        cycle.add(fn)
        foundCycles[fn] = cycle
    }
}

fun findCycles(fn: FnAst, dependencyGraph: Map<FnAst, Set<FnAst>>, stack: List<FnAst>, visited: MutableSet<FnAst>, foundCycles: MutableMap<FnAst, MutableSet<FnAst>>) {
    if(visited.contains(fn)) {
        val cycle = foundCycles[fn] ?: return
        val i = stack.indexOfFirst { cycle.contains(it) }
        if(i != -1) addAll(i, stack, cycle, foundCycles)
        return
    }

    val i = stack.indexOf(fn)
    if(i != -1) {
        val j = stack.indexOfFirst { foundCycles.containsKey(it) }
        if(j != -1) addAll(j, stack, foundCycles[stack[j]]!!, foundCycles)
        else addAll(i, stack, mutableSetOf(), foundCycles)
        return
    }

    for(dependency in dependencyGraph[fn]!!) findCycles(dependency, dependencyGraph, stack + fn, visited, foundCycles)
    visited.add(fn)
}

fun getCalls(ast: Ast, fnLookup: Map<String, FnAst>): Set<FnAst> {
    //todo remove shadowed names
    return when (ast) {
        is FnAst -> ast.body.fold(setOf()) { acc, body -> acc + getCalls(body, fnLookup - ast.name.value) }

        is NameAst -> setOf(fnLookup[ast.name.value] ?: return setOf())
        is FnCallAst -> ast.arguments.fold(getCalls(ast.fn, fnLookup)) { acc, expression -> acc + getCalls(expression, fnLookup) }

        is EntityDefinitionAst -> ast.expressions.fold(setOf()) { acc, expression -> acc + getCalls(expression, fnLookup) }
        is EntitySelectionAst -> getCalls(ast.entity, fnLookup)
        is EntityRestrictionAst -> getCalls(ast.entity, fnLookup)
        is ComponentSelectionAst -> getCalls(ast.component, fnLookup)

        is LetAst -> getCalls(ast.expression, fnLookup)
        is ReturnAst -> getCalls(ast.expression, fnLookup)

        is IntConstantAst -> setOf()
        is FloatConstantAst -> setOf()
        is StringConstantAst -> setOf()
        is CharConstantAst -> setOf()

        else -> throw Exception()
    }
}
