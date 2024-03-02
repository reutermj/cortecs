package typechecker

import parser.*

fun generateGroupingExpressionEnvironment(expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val subordinates = listOf(Subordinate(expressionSpan, environment))
    return ExpressionEnvironment(environment.type, environment.requirements, subordinates)
}

fun generateAtomicExpressionEnvironment(atom: AtomicExpressionToken) =
    when(atom) {
        is NameToken -> {
            val type = freshUnificationVariable()
            val requirements = Requirements.empty.addRequirement(atom, type)
            ExpressionEnvironment(type, requirements, emptyList())
        }
        is IntToken -> ExpressionEnvironment(getIntType(atom), Requirements.empty, emptyList())
        is FloatToken -> ExpressionEnvironment(getFloatType(atom), Requirements.empty, emptyList())
        is CharToken -> ExpressionEnvironment(CharacterType(getNextId()), Requirements.empty, emptyList())
        is StringToken -> ExpressionEnvironment(StringType(getNextId()), Requirements.empty, emptyList())
        is BadCharToken -> ExpressionEnvironment(CharacterType(getNextId()), Requirements.empty, emptyList()) //todo should I??
        is BadStringToken -> ExpressionEnvironment(StringType(getNextId()), Requirements.empty, emptyList()) //todo should I??
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