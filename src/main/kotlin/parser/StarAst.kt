package parser

import errors.CortecsError
import kotlinx.serialization.Serializable
import typechecker.*
import kotlin.math.max
import kotlin.reflect.KClass

// star ast elements are implemented using a persistent AVL tree because
// * easy to implement
// * facilitates O(lgn) finding of the node where the change happens
// * facilitates O(lgn) append operation
@Serializable
sealed class StarAst<out T: Ast>: Ast() {
    abstract val height: Int
    abstract fun inOrder(f: (T) -> Unit)
    fun <A>fold(init: A, f: (A, T) -> A): A {
        var acc = init
        inOrder { acc = f(acc, it) }
        return acc
    }
}
@Serializable
object StarLeaf: StarAst<Nothing>() {
    override fun generateEnvironment() = EmptyEnvironment
    override val height = 0
    override val errors = emptyList<CortecsError>()
    override val span: Span
        get() = Span.zero

    override fun firstTokenOrNull() = null
    override fun forceReparse(iter: ParserIterator) = throw Exception()
    override fun addToIterator(change: Change, iter: ParserIterator, next: TokenImpl?) {}
    override fun inOrder(f: (Nothing) -> Unit) {}
}
@Serializable
class StarNode<T: Ast>(val left: StarAst<T>, val element: T, val right: StarAst<T>): StarAst<T>() {
    val nodes = createList(left, element, right)
    override val errors = run {
        val updatedElementErrors = element.errors.map { it.copy(offset = left.span + it.offset) }
        val updatedRightErrors = right.errors.map { it.copy(offset = left.span + element.span + it.offset) }
        left.errors + updatedElementErrors + updatedRightErrors
    }
    private var _span: Span? = null
    override val span: Span
        get() {
            if(_span == null) _span = nodes.fold(Span.zero) { acc, e -> acc + e.span }
            return _span!!
        }

    private var _firstToken: TokenImpl? = null
    override fun firstTokenOrNull(): TokenImpl? {
        if(_firstToken != null) return _firstToken

        for(node in nodes) {
            val first = node.firstTokenOrNull()
            if(first != null) {
                _firstToken = first
                return first
            }
        }
        return null
    }

    override fun forceReparse(iter: ParserIterator) {
        for(node in nodes.dropLast(1)) iter.add(node)
        nodes.last().forceReparse(iter)
    }

    override fun addToIterator(change: Change, iter: ParserIterator, next: TokenImpl?) {
        if(keepOrDelete(change.start, change.end, iter, next)) return
        var s = change.start
        var e = change.end
        for (i in nodes.indices) {
            val eNext = nodes.drop(i + 1).firstNotNullOfOrNull { it.firstTokenOrNull() } ?: next
            nodes[i].addToIterator(change.copy(start = s, end = e), iter, eNext)

            s -= nodes[i].span
            e -= nodes[i].span
        }
    }

    override fun generateEnvironment() = left.environment + element.environment + right.environment
    override val height: Int = max(left.height, right.height) + 1
    fun copy(left: StarAst<T> = this.left, element: T = this.element, right: StarAst<T> = this.right) = StarNode(left, element, right)

    fun popFirstNode(): Pair<T, StarAst<T>> =
        when(left) {
            is StarLeaf -> Pair(element, right)
            is StarNode -> {
                val (first, remaining) = left.popFirstNode()
                Pair(first, copy(left = remaining).rebalance())
            }
        }

    fun concatToTheRight(other: StarNode<T>): StarAst<T> =
        if(height == other.height || height == other.height - 1) {
            //TODO I'm not sure treating the two cases the same is the best option, but it works for now
            val (first, remaining) = other.popFirstNode()
            StarNode(this, first, remaining).rebalance()
        } else when(right) {
            is StarLeaf -> copy(right = other).rebalance()
            is StarNode -> copy(right = right.concatToTheRight(other)).rebalance()
        }

    fun popLastNode(): Pair<T, StarAst<T>> =
        when(right) {
            is StarLeaf -> Pair(element, left)
            is StarNode -> {
                val (last, remaining) = right.popLastNode()
                Pair(last, copy(right = remaining).rebalance())
            }
        }

    fun concatToTheLeft(other: StarNode<T>): StarAst<T> =
        if(height == other.height || height == other.height - 1) {
            //TODO I'm not sure treating the two cases the same is the best option, but it works for now
            val (last, remaining) = other.popLastNode()
            StarNode(remaining, last, this).rebalance()
        } else when(left) {
            is StarLeaf -> copy(left = other).rebalance()
            is StarNode -> copy(left = left.concatToTheLeft(other)).rebalance()
        }

    override fun inOrder(f: (T) -> Unit) {
        left.inOrder(f)
        f(element)
        right.inOrder(f)
    }

    private fun pushAllLeftNodes(stack: MutableList<StarNode<*>>, node: StarNode<*>): StarNode<*> =
        when(val left = node.left) {
            is StarLeaf -> node
            is StarNode -> {
                stack.add(node)
                pushAllLeftNodes(stack, left)
            }
        }

    override fun equals(other: Any?): Boolean {
        if(other !is StarNode<*>) return false

        val lhsStack = mutableListOf<StarNode<*>>()
        val rhsStack = mutableListOf<StarNode<*>>()

        var lhs = pushAllLeftNodes(lhsStack, this)
        var rhs = pushAllLeftNodes(rhsStack, other)

        while(true) {
            if(lhs.element != rhs.element) return false

            lhs =
                when(val lhsRight = lhs.right) {
                    is StarLeaf -> lhsStack.removeLastOrNull() ?: return rhsStack.isEmpty()
                    is StarNode -> pushAllLeftNodes(lhsStack, lhsRight)
                }

            rhs =
                when(val rhsRight = rhs.right) {
                    is StarLeaf -> rhsStack.removeLastOrNull() ?: return false
                    is StarNode -> pushAllLeftNodes(rhsStack, rhsRight)
                }
        }
    }

    override fun hashCode(): Int {
        var hashCode = 0x3103E8BB
        inOrder { hashCode = hashCode xor it.hashCode() }
        return hashCode
    }

    override fun toString(): String {
        val builder = StringBuilder()
        inOrder { builder.append(it.toString()) }
        return builder.toString()
    }

// AVL Tree rebalancing:
//                  -2,1
//                    a
//                   / \
//                  w   c
//                     / \
//                    b   z
//                   / \
//                  x   y
//
//                    |
//      2,1           v         -2,-1
//       c                        a
//      / \           b          / \
//     b   z        /   \       w   b
//    / \    ->   a      c   <-    / \
//   a   y       / \    / \       x   c
//  / \         w   x  y   z         / \
// w   x                            y   z
//                    ^
//                    |
//
//                   2,-1
//                    c
//                   / \
//                  a   z
//                 / \
//                w   b
//                   / \
//                  x   y
    fun rebuild(a: StarNode<T>, b: StarNode<T>, c: StarNode<T>,
                w: StarAst<T>, x: StarAst<T>,
                y: StarAst<T>, z: StarAst<T>): StarAst<T> {
        val left = StarNode(w, a.element, x)
        val right = StarNode(y, c.element, z)
        return StarNode(left, b.element, right)
    }

    fun rebalance(): StarAst<T> {
        return when(left.height - right.height) {
            0, 1, -1 -> this
            2 -> {
                val llh = (left as StarNode).left.height
                val lrh = left.right.height
                when(llh - lrh) {
                    0, 1 -> rebuild(left.left as StarNode, left, this, left.left.left, left.left.right, left.right, right)
                    -1 -> rebuild(left, left.right as StarNode, this, left.left, left.right.left, left.right.right, right)
                    else -> throw Exception("Programmer Error")
                }
            }
            -2 -> {
                val rlh = (right as StarNode).left.height
                val rrh = right.right.height
                when(rlh - rrh) {
                    1 -> rebuild(this, right.left as StarNode, right, left, right.left.left, right.left.right, right.right)
                    0, -1 -> rebuild(this, right, right.right as StarNode, left, right.left, right.right.left, right.right.right)
                    else -> throw Exception("Programmer Error")
                }
            }
            else -> throw Exception("Programmer Error")
        }
    }
}

fun <T: Ast>bulkLoad(elems: List<T>, l: Int, r: Int): StarAst<T> {
    if(l > r) return StarLeaf

    val m = (l + r) / 2
    val left = bulkLoad(elems, l, m - 1)
    val right = bulkLoad(elems, m + 1, r)
    return StarNode(left, elems[m], right)
}

inline fun <reified T: Ast>starOf(elems: List<T> = emptyList()) = bulkLoad(elems, 0, elems.size - 1)

operator fun <T: Ast>StarAst<T>.plus(other: StarAst<T>): StarAst<T> =
    when(this) {
        is StarLeaf -> other
        is StarNode ->
            when(other) {
                is StarLeaf -> this
                is StarNode ->
                    if(other.height > this.height) other.concatToTheLeft(this)
                    else this.concatToTheRight(other)
            }
    }

fun <T: Ast>createList(left: StarAst<T>, element: T, right: StarAst<T>): List<Ast> {
    val elems = mutableListOf<Ast>()
    if(left is StarNode) elems.add(left)
    elems.add(element)
    if(right is StarNode) elems.add(right)
    return elems
}