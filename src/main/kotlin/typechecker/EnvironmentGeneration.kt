package typechecker

import parser.*

fun generateGroupingExpressionEnvironment(expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val subordinate = Subordinate(expressionSpan, environment)
    return GroupingExpressionEnvironment(environment.type, environment.requirements, subordinate)
}

fun generateUnaryExpressionEnvironment(op: OperatorToken, expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val retType = freshUnificationVariable()
    val opType = ArrowType(getNextId(), environment.type, retType)
    val requirements = environment.requirements.addRequirement(op, opType)
    val subordinate = Subordinate(expressionSpan, environment)
    return UnaryExpressionEnvironment(retType, opType, requirements, subordinate)
}

fun generateAtomicExpressionEnvironment(atom: AtomicExpressionToken) =
    when(atom) {
        is NameToken -> {
            val type = freshUnificationVariable()
            val requirements = Requirements.empty.addRequirement(atom, type)
            AtomicExpressionEnvironment(type, requirements)
        }
        is IntToken -> AtomicExpressionEnvironment(getIntType(atom), Requirements.empty)
        is FloatToken -> AtomicExpressionEnvironment(getFloatType(atom), Requirements.empty)
        is CharToken -> AtomicExpressionEnvironment(CharacterType(getNextId()), Requirements.empty)
        is StringToken -> AtomicExpressionEnvironment(StringType(getNextId()), Requirements.empty)
        is BadCharToken -> AtomicExpressionEnvironment(CharacterType(getNextId()), Requirements.empty) //todo should I??
        is BadStringToken -> AtomicExpressionEnvironment(StringType(getNextId()), Requirements.empty) //todo should I??
    }

var typeId: Long = 0
fun getNextId() = typeId++
fun freshUnificationVariable() = UnificationTypeVariable(getNextId())

fun getIntType(i: IntToken): Type {
    val isUnsigned = i.value.contains("u", true)
    return when(i.value.last()) {
        'b', 'B' -> if(isUnsigned) U8Type(getNextId()) else I8Type(getNextId())
        's', 'S' -> if(isUnsigned) U16Type(getNextId()) else I16Type(getNextId())
        'l', 'L' -> if(isUnsigned) U64Type(getNextId()) else I64Type(getNextId())
        else -> if(isUnsigned) U32Type(getNextId()) else I32Type(getNextId())
    }
}

fun getFloatType(i: FloatToken) =
    when(i.value.last()) {
        'd', 'D' -> F64Type(getNextId())
        else -> F32Type(getNextId())
    }