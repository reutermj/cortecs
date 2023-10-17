package parser

class StarBuilder(override val iterator: ParserIterator): AstBuilder() {
    override fun add(node: Ast) {
        //noop. star builder adds the returned value
    }

    inline fun <reified T: Ast>reuseStar(): StarAst<T>? {
        if(!iterator.hasNext()) return null
        if(iterator.isNextToken()) return null
        val n = iterator.peekNode()
        if(n !is StarNode<*>) return null
        if(n.reifiedT != T::class) return null
        iterator.next()
        @Suppress("UNCHECKED_CAST")
        return n as StarAst<T>
    }
}

class SequenceBuilder(override val iterator: ParserIterator): AstBuilder() {
    val nodes = mutableListOf<Ast>()
    override fun add(node: Ast) {
        nodes.add(node)
    }

    fun getSequence() = nodes

    fun consumeWhitespace() {
        while(iterator.hasNext()) {
            when(val token = peekToken()) {
                is WhitespaceToken, is NewLineToken -> add(token)
                else -> break
            }

            iterator.next()
        }
    }

    inline fun <reified T: Token>consume(): T? {
        if(!iterator.hasNext()) return null

        while(true) {
            if(iterator.isNextToken()) {
                val token = peekToken()
                if(token is T) {
                    add(token)
                    iterator.next()
                    consumeWhitespace()
                    return token
                }
            } else {
                //this covers cases where the next element in the iterator is an Ast node
                //but the next token is the token we're looking for
                //the Ast node can't be reused so inject its elements into the iterator
                //and continue using them.
                //this is in a while loop so that PersistentVectors get unrolled all
                //the way down the left subtree
                val node = iterator.peekNode()
                val firstToken = node.firstTokenOrNull
                if(firstToken is T) {
                    iterator.next()
                    iterator.inject(node.nodes)
                    continue
                }
            }
            return null
        }
    }
}

inline fun <reified T: Ast>buildAst(iterator: ParserIterator, f: SequenceBuilder.() -> T): T {
    val builder = SequenceBuilder(iterator)
    val node = builder.reuse<T>()
    if(node != null) return node
    return builder.f()
}

inline fun <reified T: Ast>buildAst(builder: AstBuilder, f: SequenceBuilder.() -> T): T {
    val node = buildAst(builder.iterator, f)
    builder.add(node)
    return node
}

enum class ReuseInstructions {
    dontProgress, inject, reuse
}

inline fun <reified T: Ast>buildAst(iterator: ParserIterator, r: (T) -> ReuseInstructions, f: SequenceBuilder.() -> T): T {
    val builder = SequenceBuilder(iterator)
    if(iterator.hasNext() && !iterator.isNextToken()) {
        val node = iterator.peekNode()
        if(node is T) {
            when(r(node)) {
                ReuseInstructions.inject -> {
                    iterator.next()
                    iterator.inject(node.nodes)
                }
                ReuseInstructions.dontProgress -> {}
                ReuseInstructions.reuse -> {
                    iterator.next()
                    return node
                }
            }
        }
    }
    return builder.f()
}

inline fun <reified T: Ast>buildAst(builder: AstBuilder, r: (T) -> ReuseInstructions, f: SequenceBuilder.() -> T): T {
    val node = buildAst(builder.iterator, r, f)
    builder.add(node)
    return node
}

interface StarBuildingInstruction<out T: Ast>
object StopBuildingStar: StarBuildingInstruction<Nothing>
data class KeepBuildingStar<T: Ast>(val node: T): StarBuildingInstruction<T>

inline fun <reified T: Ast>buildStarAst(iterator: ParserIterator, parseNode: StarBuilder.() -> StarBuildingInstruction<T>): StarAst<T> {
    val nodes = mutableListOf<T>()
    val starBuilder = StarBuilder(iterator)
    var acc = starOf<T>(emptyList())
    while(iterator.hasNext()) {
        val r = starBuilder.reuseStar<T>()
        if(r != null) {
            acc += starOf(nodes) + r
            nodes.clear()
            continue
        }

        val rn = starBuilder.reuse<T>()
        if(rn != null) {
            nodes.add(rn)
            continue
        }

        when(val instruction = starBuilder.parseNode()) {
            is KeepBuildingStar -> nodes.add(instruction.node)
            else -> break
        }
    }
    return acc + starOf(nodes)
}

inline fun <reified T: Ast>buildStarAst(builder: AstBuilder, parseNode: StarBuilder.() -> StarBuildingInstruction<T>): StarAst<T> {
    val node = buildStarAst<T>(builder.iterator, parseNode)
    builder.add(node)
    return node
}

sealed class AstBuilder {
    abstract val iterator: ParserIterator
    abstract fun add(node: Ast)
    fun getBuilder() = this

    inline fun <reified T: Ast>reuse(): T? {
        if(!iterator.hasNext()) return null
        if(iterator.isNextToken()) return null
        val n = iterator.peekNode()
        if(n !is T) return null
        iterator.next()
        add(n)
        return n
    }

    fun peekToken(): Token? {
        if(!iterator.hasNext()) return null
        return iterator.peekToken()
    }
}
