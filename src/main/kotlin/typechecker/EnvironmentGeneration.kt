package typechecker

import parser.*

fun generateAtomicExpressionEnvironment(atom: AtomicExpressionToken) =
    when(atom) {
        is NameToken -> {
            val type = freshUnificationVariable()
            val requirements = Requirements.empty.addRequirement(atom, type)
            ExpressionEnvironment(type, requirements)
        }
        is IntToken -> ExpressionEnvironment(getIntType(atom), Requirements.empty)
        is FloatToken -> ExpressionEnvironment(getFloatType(atom), Requirements.empty)
        is CharToken -> ExpressionEnvironment(CharacterType(getNextId()), Requirements.empty)
        is StringToken -> ExpressionEnvironment(StringType(getNextId()), Requirements.empty)
        is BadCharToken -> ExpressionEnvironment(CharacterType(getNextId()), Requirements.empty) //todo should I??
        is BadStringToken -> ExpressionEnvironment(StringType(getNextId()), Requirements.empty) //todo should I??
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