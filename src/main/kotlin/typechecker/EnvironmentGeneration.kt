package typechecker

import errors.CortecsError
import errors.CortecsErrors
import parser.*

fun generateFunctionCallExpressionEnvironment(function: Expression, arguments: ArgumentsAst, argumentsSpan: Span): ExpressionEnvironment {
    val argumentTypes = mutableListOf<Type>()
    val argumentSubordinates = mutableListOf<Subordinate<ExpressionEnvironment>>()
    var argumentSpan = argumentsSpan
    var requirements = Requirements.empty
    arguments.inOrder {
        val environment = it.expression().environment
        argumentTypes.add(environment.expressionType)
        argumentSubordinates.add(Subordinate(argumentSpan, environment))
        argumentSpan += it.span
        requirements += environment.requirements
    }

    val errors = mutableListOf<CortecsError>()

    val returnType = freshUnificationVariable()
    val rhsType = typesToType(argumentTypes)
    val arrowType = ArrowType(rhsType.id, rhsType, returnType)
    val substitution =
        when(val result = Substitution.empty.unify(function.environment.expressionType, arrowType)) {
            is UnificationSuccess -> result.substitution
            is UnificationError -> {
                val spans = function.environment.getSpansForId(function.environment.expressionType.id)
                for(span in spans) {
                    errors.add(CortecsError("Unification error", span, Span.zero))
                }
                Substitution.empty
            }
        }

    val mappings = mutableMapOf<Long, Type>()
    requirements += function.environment.requirements.applySubstitution(substitution, mappings)

    val functionSubordinate = Subordinate(Span.zero, function.environment)
    return FunctionCallExpressionEnvironment(returnType, arrowType, requirements, substitution, functionSubordinate, argumentSubordinates, CortecsErrors(null, errors))
}

fun generateGroupingExpressionEnvironment(expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val subordinate = Subordinate(expressionSpan, environment)
    return GroupingExpressionEnvironment(environment.expressionType, environment.requirements, subordinate)
}

fun generateUnaryExpressionEnvironment(op: OperatorToken, expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val retType = freshUnificationVariable()
    val opType = ArrowType(getNextId(), environment.expressionType, retType)
    val requirements = environment.requirements.addRequirement(op, opType)
    val subordinate = Subordinate(expressionSpan, environment)
    return UnaryExpressionEnvironment(retType, opType, requirements, subordinate)
}

fun generateBinaryExpressionEnvironment(lhs: Expression, op: OperatorToken, opSpan: Span, rhs: Expression, rhsSpan: Span): ExpressionEnvironment {
    val lEnvironment = lhs.environment
    val rEnvironment = rhs.environment
    val retType = freshUnificationVariable()
    val typeId = getNextId()
    val productType = ProductType(typeId, listOf(lEnvironment.expressionType, rEnvironment.expressionType))
    val opType = ArrowType(typeId, productType, retType)
    val requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(op, opType)
    val lSubordinate = Subordinate(Span.zero, lEnvironment)
    val rSubordinate = Subordinate(rhsSpan, rEnvironment)
    return BinaryExpressionEnvironment(retType, opType, opSpan, requirements, lSubordinate, rSubordinate)
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

fun typesToType(types: List<Type>) = //todo find better name for this
    when(types.size) {
        0 -> UnitType(getNextId())  //should probably be empty type
        1 -> types.first()
        else -> ProductType(getNextId(), types)
    }

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