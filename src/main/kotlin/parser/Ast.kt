package parser

interface Ast {
    val offset: Offset
    val firstTokenOrNull: Token?
    val nodes: List<Ast>

    fun shouldKeep(start: Offset, end: Offset) = end < Offset.zero || offset < start
    fun shouldDelete(start: Offset, end: Offset) = offset == Offset.zero || start <= Offset.zero && offset <= end
    fun addToIterator(change: String, start: Offset, end: Offset, iter: ParserIterator, next: Token?)
    fun forceReparse(iter: ParserIterator)
    fun keepOrDelete(start: Offset, end: Offset, iter: ParserIterator, next: Token?) =
        if(shouldDelete(start, end)) true
        else if(shouldKeep(start, end)) {
            if(next?.shouldKeep(start - offset, end - offset) != false) iter.add(this) //the token following this node is kept; don't force reparse
            else forceReparse(iter) // the next element has changed so need to reparse the last element of this vector
            true
        } else false
}

interface TopLevelAst: Ast
class FunctionAst(sequence: List<Ast>, val name: NameToken?, val parameters: StarAst<ParameterAst>?, val block: StarAst<BodyAst>?): SequenceAst(sequence), TopLevelAst
class ParameterAst(sequence: List<Ast>, val name: NameToken, val type: TypeToken?): SequenceAst(sequence)

interface BodyAst: Ast
class LetAst(sequence: List<Ast>, val name: NameToken?, val expression: Expression?): SequenceAst(sequence), BodyAst
class ReturnAst(sequence: List<Ast>, val expression: Expression?): SequenceAst(sequence), BodyAst
class IfAst(sequence: List<Ast>, val condition: Expression?, val block: StarAst<BodyAst>?): SequenceAst(sequence), BodyAst

class GarbageAst(sequence: List<Ast>): SequenceAst(sequence), TopLevelAst, BodyAst {
    //todo improve
    override fun addToIterator(change: String, start: Offset, end: Offset, iter: ParserIterator, next: Token?) {
        var s = start
        var e = end
        for (i in nodes.indices) {
            val eNext =
                if(i + 1 in nodes.indices) nodes[i + 1]
                else next
            nodes[i].addToIterator(change, s, e, iter, eNext?.firstTokenOrNull)

            s -= nodes[i].offset
            e -= nodes[i].offset
        }
    }
}
