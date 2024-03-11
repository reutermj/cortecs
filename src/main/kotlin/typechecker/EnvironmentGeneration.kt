package typechecker

import errors.CortecsError
import errors.CortecsErrors
import parser.*

sealed interface FunctionCallArgumentsResult {
    val requirements: Requirements
    val subordinates: List<Subordinate<ExpressionEnvironment>>
    val errors: CortecsErrors
}

data class FunctionCallArgumentsGood(val argumentsType: Type, override val requirements: Requirements, override val subordinates: List<Subordinate<ExpressionEnvironment>>, override val errors: CortecsErrors): FunctionCallArgumentsResult
data class FunctionCallArgumentsBad(override val requirements: Requirements, override val subordinates: List<Subordinate<ExpressionEnvironment>>, override val errors: CortecsErrors): FunctionCallArgumentsResult

fun processFunctionCallArguments(arguments: ArgumentsAst, argumentsSpan: Span): FunctionCallArgumentsResult {
    val argumentTypes = mutableListOf<Type>()
    val subordinates = mutableListOf<Subordinate<ExpressionEnvironment>>()
    var argumentSpan = argumentsSpan
    var anyInvalid = false
    var requirements = Requirements.empty
    var errors = CortecsErrors.empty
    arguments.inOrder {
        val environment = it.expression().environment
        if(environment.expressionType is Invalid) anyInvalid = true

        errors += environment.errors.addOffset(argumentSpan)
        argumentTypes.add(environment.expressionType)
        subordinates.add(Subordinate(argumentSpan, environment))
        argumentSpan += it.span
        requirements += environment.requirements
    }

    return if(anyInvalid) FunctionCallArgumentsBad(requirements, subordinates, errors)
    else FunctionCallArgumentsGood(typesToType(argumentTypes), requirements, subordinates, errors)
}

fun processGoodFunctionCall(fEnvironment: ExpressionEnvironment, arguments: FunctionCallArgumentsGood): ExpressionEnvironment {
    val returnType = freshUnificationVariable()
    val rhsType = arguments.argumentsType
    val arrowType = ArrowType(rhsType.id, rhsType, returnType)

    val outType: Type
    val outArrow: Type
    val substitution: Substitution
    var requirements = arguments.requirements
    var errors = fEnvironment.errors + arguments.errors
    val mappings = mutableMapOf<Long, Type>() //todo why is mappings not used
    when(val result = Substitution.empty.unify(fEnvironment.expressionType, arrowType)) {
        is UnificationSuccess -> {
            substitution = result.substitution
            requirements += fEnvironment.requirements.applySubstitution(substitution, mappings)
            outType = returnType
            outArrow = arrowType
        }

        is UnificationError -> {
            val spans = fEnvironment.getSpansForId(fEnvironment.expressionType.id)
            for(span in spans) {
                errors += CortecsError("Unification error", span, Span.zero)
            }
            substitution = Substitution.empty
            outType = Invalid(returnType.id)
            outArrow = Invalid(arrowType.id)
        }
    }

    return FunctionCallExpressionEnvironment(outType, outArrow, requirements, substitution, Subordinate(Span.zero, fEnvironment), arguments.subordinates, errors)
}

fun processBadFunctionCall(fEnvironment: ExpressionEnvironment, arguments: FunctionCallArgumentsResult): ExpressionEnvironment {
    val outType = Invalid(getNextId())
    val outArrow = Invalid(getNextId())
    val requirements = fEnvironment.requirements + arguments.requirements
    val errors = fEnvironment.errors + arguments.errors
    return FunctionCallExpressionEnvironment(outType, outArrow, requirements, Substitution.empty, Subordinate(Span.zero, fEnvironment), arguments.subordinates, errors)
}

fun generateFunctionCallExpressionEnvironment(function: Expression, arguments: ArgumentsAst, argumentsSpan: Span): ExpressionEnvironment {
    val fEnvironment = function.environment
    return when(val result = processFunctionCallArguments(arguments, argumentsSpan)) {
        is FunctionCallArgumentsGood -> if(fEnvironment.expressionType is Invalid) processBadFunctionCall(fEnvironment, result)
        else processGoodFunctionCall(fEnvironment, result)

        is FunctionCallArgumentsBad -> processBadFunctionCall(fEnvironment, result)
    }
}

fun generateGroupingExpressionEnvironment(expression: Expression, expressionSpan: Span): ExpressionEnvironment {
    val environment = expression.environment
    val subordinate = Subordinate(expressionSpan, environment)
    val errors = environment.errors.addOffset(expressionSpan)
    return GroupingExpressionEnvironment(environment.expressionType, environment.requirements, subordinate, errors)
}

fun generateUnaryExpressionEnvironment(op: OperatorToken, expression: Expression?, expressionSpan: Span): ExpressionEnvironment {
    if(expression == null) return EmptyExpressionEnvironment

    val environment = expression.environment
    val retType: Type
    val opType: Type
    val requirements: Requirements
    if(environment.expressionType is Invalid) {
        retType = environment.expressionType
        opType = environment.expressionType
        requirements = environment.requirements
    } else {
        retType = freshUnificationVariable()
        opType = ArrowType(getNextId(), environment.expressionType, retType)
        requirements = environment.requirements.addRequirement(op, opType)
    }
    val errors = environment.errors.addOffset(expressionSpan)

    val subordinate = Subordinate(expressionSpan, environment)
    return UnaryExpressionEnvironment(retType, opType, requirements, subordinate, errors)
}

fun generateBinaryExpressionEnvironment(lhs: Expression, op: OperatorToken, opSpan: Span, rhs: Expression?, rhsSpan: Span): ExpressionEnvironment {
    val lEnvironment = lhs.environment
    val rEnvironment = rhs?.environment
    if(rEnvironment == null) {
        val retType = Invalid(getNextId())
        val opType = Invalid(getNextId())
        val lSubordinate = Subordinate(Span.zero, lEnvironment)
        return BinaryExpressionEnvironment(retType, opType, opSpan, lEnvironment.requirements, lSubordinate, null, lEnvironment.errors)
    }
    val typeId = getNextId()

    val retType: Type
    val opType: Type
    val requirements: Requirements
    if(lEnvironment.expressionType is Invalid || rEnvironment.expressionType is Invalid) {
        retType = Invalid(getNextId())
        opType = Invalid(typeId)
        requirements = lEnvironment.requirements + rEnvironment.requirements
    } else {
        retType = freshUnificationVariable()
        val productType = ProductType(typeId, listOf(lEnvironment.expressionType, rEnvironment.expressionType))
        opType = ArrowType(typeId, productType, retType)
        requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(op, opType)
    }

    val lSubordinate = Subordinate(Span.zero, lEnvironment)
    val rSubordinate = Subordinate(rhsSpan, rEnvironment)
    val errors = lEnvironment.errors + rEnvironment.errors.addOffset(rhsSpan)
    return BinaryExpressionEnvironment(retType, opType, opSpan, requirements, lSubordinate, rSubordinate, errors)
}

fun generateAtomicExpressionEnvironment(atom: AtomicExpressionToken) = when(atom) {
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

fun getFloatType(i: FloatToken) = when(i.value.last()) {
    'd', 'D' -> F64Type(getNextId())
    else -> F32Type(getNextId())
}