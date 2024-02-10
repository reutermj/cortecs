package parser_v2

import errors.*
import kotlin.math.max

sealed interface StarConcat
data class StarConcatSingle(val node: StarAst): StarConcat
data class StarConcatPair(val node0: StarAst, val node1: StarAst): StarConcat

const val STAR_MAX_NODES = 8
const val STAR_MIN_NODES = STAR_MAX_NODES / 2

sealed class StarAst: AstImpl() {
    abstract val height: Int
    abstract fun ctor(nodes: List<Ast>, height: Int): StarAst

    fun isEmpty() = nodes.isEmpty()
    override val errors: CortecsErrors
        get() = TODO("Not yet implemented")

    fun traverseDownTheLeft(other: StarAst): StarConcat =
        if(height == other.height) baseCase(other)
        else when(val instruction = traverseDownTheLeft(other.nodes.first() as StarAst)) {
            is StarConcatSingle -> StarConcatSingle(ctor(listOf(instruction.node) + other.nodes.drop(1), other.height))
            is StarConcatPair -> {
                val outNodes = listOf(instruction.node0, instruction.node1) + other.nodes.drop(1)
                if(outNodes.size <= STAR_MAX_NODES) StarConcatSingle(ctor(outNodes, other.height))
                else {
                    val halfSize = outNodes.size / 2
                    StarConcatPair(ctor(outNodes.take(halfSize), other.height), ctor(outNodes.drop(halfSize), other.height))
                }
            }
        }

    fun traverseDownTheRight(other: StarAst): StarConcat =
        if(height == other.height) baseCase(other)
        else when(val instruction = (nodes.last() as StarAst).traverseDownTheRight(other)) {
            is StarConcatSingle -> StarConcatSingle(ctor(nodes.dropLast(1) + instruction.node, height))
            is StarConcatPair -> {
                val outNodes = nodes.dropLast(1) + instruction.node0 + instruction.node1
                if(outNodes.size <= STAR_MAX_NODES) StarConcatSingle(ctor(outNodes, height))
                else {
                    val halfSize = outNodes.size / 2
                    StarConcatPair(ctor(outNodes.take(halfSize), height), ctor(outNodes.drop(halfSize), height))
                }
            }
        }

    fun baseCase(other: StarAst): StarConcat =
        if(nodes.size + other.nodes.size <= STAR_MAX_NODES) StarConcatSingle(ctor(nodes + other.nodes, height))
        else if(nodes.size >= STAR_MIN_NODES && other.nodes.size >= STAR_MIN_NODES) StarConcatPair(this, other)
        else {
            val outNodes = nodes + other.nodes
            val halfSize = outNodes.size / 2
            StarConcatPair(ctor(outNodes.take(halfSize), height), ctor(outNodes.drop(halfSize), height))
        }
}

inline operator fun <reified T: StarAst>T.plus(other: T): T {
    if(isEmpty()) return other
    if(other.isEmpty()) return this

    val instruction =
        if(height == other.height) baseCase(other)
        else if(height > other.height) traverseDownTheRight(other)
        else traverseDownTheLeft(other)

    val out = when(instruction) {
        is StarConcatSingle -> instruction.node
        is StarConcatPair -> ctor(listOf(instruction.node0, instruction.node1), max(height, other.height) + 1)
    }

    if(out is T) return out
    throw Exception()
}