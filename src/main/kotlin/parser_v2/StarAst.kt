package parser_v2

import errors.*
import kotlin.math.max

sealed interface StarConcat
data class StarConcatSingle<T: Ast>(val node: StarAst<T>): StarConcat
data class StarConcatPair<T: Ast>(val node0: StarAst<T>, val node1: StarAst<T>): StarConcat

const val STAR_MAX_NODES = 8
const val STAR_MIN_NODES = STAR_MAX_NODES / 2

sealed class StarAst<T: Ast>: AstImpl() {
    abstract val height: Int
    abstract fun ctor(nodes: List<Ast>, height: Int): StarAst<T>

    fun isEmpty() = nodes.isEmpty()
    override val errors: CortecsErrors
        get() = CortecsErrors(span, emptyList()) //todo

    fun inOrder(f: (T) -> Unit) {
        if(height == 0) for(node in nodes) f(node as T)
        else for(node in nodes) (node as StarAst<T>).inOrder(f)
    }

    fun traverseDownTheLeft(other: StarAst<T>): StarConcat =
        if(height == other.height) baseCase(other)
        else when(val instruction = traverseDownTheLeft(other.nodes.first() as StarAst<T>)) {
            is StarConcatSingle<*> -> StarConcatSingle<T>(ctor(listOf(instruction.node) + other.nodes.drop(1), other.height))
            is StarConcatPair<*> -> {
                val outNodes = listOf(instruction.node0, instruction.node1) + other.nodes.drop(1)
                if(outNodes.size <= STAR_MAX_NODES) StarConcatSingle<T>(ctor(outNodes, other.height))
                else {
                    val halfSize = outNodes.size / 2
                    StarConcatPair<T>(ctor(outNodes.take(halfSize), other.height), ctor(outNodes.drop(halfSize), other.height))
                }
            }
        }

    fun traverseDownTheRight(other: StarAst<T>): StarConcat =
        if(height == other.height) baseCase(other)
        else when(val instruction = (nodes.last() as StarAst<T>).traverseDownTheRight(other)) {
            is StarConcatSingle<*> -> StarConcatSingle<T>(ctor(nodes.dropLast(1) + instruction.node, height))
            is StarConcatPair<*> -> {
                val outNodes = nodes.dropLast(1) + instruction.node0 + instruction.node1
                if(outNodes.size <= STAR_MAX_NODES) StarConcatSingle<T>(ctor(outNodes, height))
                else {
                    val halfSize = outNodes.size / 2
                    StarConcatPair<T>(ctor(outNodes.take(halfSize), height), ctor(outNodes.drop(halfSize), height))
                }
            }
        }

    fun baseCase(other: StarAst<T>): StarConcat =
        if(nodes.size + other.nodes.size <= STAR_MAX_NODES) StarConcatSingle<T>(ctor(nodes + other.nodes, height))
        else if(nodes.size >= STAR_MIN_NODES && other.nodes.size >= STAR_MIN_NODES) StarConcatPair<T>(this, other)
        else {
            val outNodes = nodes + other.nodes
            val halfSize = outNodes.size / 2
            StarConcatPair<T>(ctor(outNodes.take(halfSize), height), ctor(outNodes.drop(halfSize), height))
        }
}

inline operator fun <S: Ast, reified T: StarAst<S>>T.plus(other: T): T {
    if(isEmpty()) return other
    if(other.isEmpty()) return this

    val instruction =
        if(height == other.height) baseCase(other)
        else if(height > other.height) traverseDownTheRight(other)
        else traverseDownTheLeft(other)

    val out = when(instruction) {
        is StarConcatSingle<*> -> instruction.node
        is StarConcatPair<*> -> ctor(listOf(instruction.node0, instruction.node1), max(height, other.height) + 1)
    }

    if(out is T) return out
    throw Exception()
}