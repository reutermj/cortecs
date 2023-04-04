package parser

import kotlin.math.max
import kotlin.reflect.KClass

//todo implement an invariant that ensures 8 <= elements.size <= 16 for all but the last node
class StarAst<T: Ast>(val left: StarAst<T>?, val elements: List<T>, val right: StarAst<T>?, val reifiedT: KClass<T>): SequenceAst(createList(left, elements, right)) {
    val height: Int = max(left?.height ?: 0, right?.height ?: 0) + 1
    fun copy(left: StarAst<T>? = this.left, elements: List<T> = this.elements, right: StarAst<T>? = this.right) = StarAst(left, elements, right, reifiedT)

    fun popFirstNode(): Pair<List<T>, StarAst<T>?> =
        if(left == null) Pair(elements, right)
        else {
            val (first, remaining) = left.popFirstNode()
            Pair(first, copy(left = remaining).rebalance())
        }

    fun concatToTheRight(other: StarAst<T>): StarAst<T> =
        if(height == other.height || height == other.height - 1) {
            //TODO I'm not sure treating the two cases the same is the best option, but it works for now
            val (first, remaining) = other.popFirstNode()
            StarAst(this, first, remaining, reifiedT).rebalance()
        } else if(right == null) copy(right = other).rebalance()
        else copy(right = right.concatToTheRight(other)).rebalance()

    fun popLastNode(): Pair<List<T>, StarAst<T>?> =
        if(right == null) Pair(elements, left)
        else {
            val (last, remaining) = right.popLastNode()
            Pair(last, copy(right = remaining).rebalance())
        }

    fun concatToTheLeft(other: StarAst<T>): StarAst<T> =
        if(height == other.height || height == other.height - 1) {
            //TODO I'm not sure treating the two cases the same is the best option, but it works for now
            val (last, remaining) = other.popLastNode()
            StarAst(remaining, last, this, reifiedT).rebalance()
        } else if(left == null) copy(left = other).rebalance()
        else copy(left = left.concatToTheLeft(other)).rebalance()

    operator fun plus(other: StarAst<T>): StarAst<T> =
        if(elements.isEmpty()) other
        else if(other.elements.isEmpty()) this
        else if(other.height > height) other.concatToTheLeft(this)
        else concatToTheRight(other)

    fun inOrder(f: (T) -> Unit) {
        left?.inOrder(f)
        for(elem in elements) f(elem as T)
        right?.inOrder(f)
    }

    private fun pushAllLeftNodes(stack: MutableList<StarAst<*>>, node: StarAst<*>): StarAst<*> =
        if(node.left == null) node
        else {
            stack.add(node)
            pushAllLeftNodes(stack, node.left)
        }

    override fun equals(other: Any?): Boolean {
        if(other !is StarAst<*>) return false

        val lhsStack = mutableListOf<StarAst<*>>()
        val rhsStack = mutableListOf<StarAst<*>>()

        var lhs = pushAllLeftNodes(lhsStack, this)
        var rhs = pushAllLeftNodes(rhsStack, other)

        var i = 0
        var j = 0

        while(true) {
            if(i == lhs.elements.size) {
                lhs =
                    if(lhs.right == null) lhsStack.removeLastOrNull() ?: return j == rhs.elements.size && rhsStack.isEmpty()
                    else pushAllLeftNodes(lhsStack, lhs.right!!)
                i = 0
            }

            if(j == rhs.elements.size) {
                rhs =
                    if(rhs.right == null) rhsStack.removeLastOrNull() ?: return false
                    else pushAllLeftNodes(rhsStack, rhs.right!!)
                j = 0
            }

            if(lhs.elements[i] != rhs.elements[j]) return false

            i++
            j++
        }
    }

    override fun hashCode(): Int {
        var hashCode = 0x3103E8BB
        inOrder { hashCode = hashCode xor it.hashCode() }
        return hashCode
    }

    override fun toString(): String {
        val builder = StringBuilder()
        inOrder {
            builder.append(it.toString())
        }
        return builder.toString()
    }

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
    fun rebuild(a: StarAst<T>, b: StarAst<T>, c: StarAst<T>,
                w: StarAst<T>?, x: StarAst<T>?,
                y: StarAst<T>?, z: StarAst<T>?): StarAst<T> {
        val left = StarAst(w, a.elements, x, reifiedT)
        val right = StarAst(y, c.elements, z, reifiedT)
        return StarAst(left, b.elements, right, reifiedT)
    }

    fun rebalance(): StarAst<T> {
        val lh = left?.height ?: 0
        val rh = right?.height ?: 0
        return when(lh - rh) {
            0, 1, -1 -> this
            2 -> {
                val llh = left!!.left?.height ?: 0
                val lrh = left.right?.height ?: 0
                when(llh - lrh) {
                    0, 1 -> rebuild(left.left!!, left, this, left.left.left, left.left.right, left.right, right)
                    -1 -> rebuild(left, left.right!!, this, left.left, left.right.left, left.right.right, right)
                    else -> throw Exception("Programmer Error")
                }
            }
            -2 -> {
                val rlh = right!!.left?.height ?: 0
                val rrh = right.right?.height ?: 0
                when(rlh - rrh) {
                    1 -> rebuild(this, right.left!!, right, left, right.left.left, right.left.right, right.right)
                    0, -1 -> rebuild(this, right, right.right!!, left, right.left, right.right.left, right.right.right)
                    else -> throw Exception("Programmer Error")
                }
            }
            else -> throw Exception("Programmer Error")
        }
    }
}

const val nodeSize = 16


fun <T: Ast>bulkLoad(elems: List<T>, l: Int, r: Int, elementsPerNode: Int, elementsInLastNode: Int, reifiedT: KClass<T>): StarAst<T>? {
    if(l > r) return null

    val m = (l + r) / 2
    val left = bulkLoad(elems, l, m - 1, elementsPerNode, elementsInLastNode, reifiedT)
    val right = bulkLoad(elems, m + 1, r, elementsPerNode, elementsInLastNode, reifiedT)
    val size =
        if(m == elementsPerNode) {
            if(elementsInLastNode == 0) return null
            elementsInLastNode
        }
        else nodeSize
    val elements = List(size) { elems[m * nodeSize + it] }

    return StarAst(left, elements, right, reifiedT)
}

inline fun <reified T: Ast>starOf(elems: List<T> = emptyList()): StarAst<T> {
    if(elems.size <= nodeSize) {
        val elements = List(elems.size) { elems[it] }
        return StarAst(null, elements, null, T::class)
    }

    val elementsInLastNode = elems.size % nodeSize
    val elementsPerNode = elems.size / nodeSize

    return bulkLoad(elems, 0, elementsPerNode, elementsPerNode, elementsInLastNode, T::class)!!
}

fun <T: Ast>createList(l: StarAst<T>?, e: List<Ast>, r: StarAst<T>?): List<Ast> {
    val out = mutableListOf<Ast>()
    if(l != null) out.add(l)
    out.addAll(e)
    if(r != null) out.add(r)
    return out
}
