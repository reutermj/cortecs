package typechecker

import errors.CortecsError
import errors.CortecsErrors
import parser.*

fun generateBlockEnvironment(nodes: List<Ast>): BlockEnvironment {
    if(nodes.isEmpty()) return EmptyBlockEnvironmnt

    var subordinateOffset = Span.zero
    val outSubordinates = nodes.map {
        val subordinate: Subordinate<BlockEnvironment> = when(it) {
            is BodyAst -> Subordinate(subordinateOffset, it.environment)
            is BlockAst -> Subordinate(subordinateOffset, it.environment)
            else -> throw Exception()
        }
        subordinateOffset += it.span
        subordinate
    }

    val errors = mutableListOf<CortecsError>()
    var substitution = Substitution.empty
    var outBindings = Bindings.empty
    var outRequirements = Requirements.empty
    for(subordinate in outSubordinates) {
        val environment = subordinate.environment
        for((token, requirements) in environment.requirements.requirements) {
            val binding = outBindings[token] ?: continue
            for(requirement in requirements) {
                when(val result = substitution.unify(binding, requirement)) {
                    is UnificationSuccess -> substitution = result.substitution
                    is UnificationError -> {
                        val spans = environment.getSpansForType(result.rType)
                        for(span in spans) {
                            errors.add(CortecsError("Unification error", span, Span.zero))
                        }
                    }
                }
            }
        }

        outBindings += environment.bindings
        outRequirements += environment.requirements.filter { token, _ -> !outBindings.contains(token) }
    }

    val mappings = mutableMapOf<Long, Type>()
    outBindings = outBindings.applySubstitution(substitution, mappings)
    outRequirements = outRequirements.applySubstitution(substitution, mappings)

    return BlockEnv(outSubordinates, substitution, mappings, outBindings, outRequirements, CortecsErrors(null, errors))
}

fun generateLetEnvironment(
    name: NameToken?,
    annotation: TypeAnnotationToken?,
    annotationSpan: Span,
    expression: Expression?,
    expressionSpan: Span
): LetEnvironment {
    if(name == null) {
        return LetEnvironment(
            Subordinate(expressionSpan, EmptyExpressionEnvironment),
            Substitution.empty,
            emptyMap(),
            Bindings.empty,
            Requirements.empty,
            CortecsErrors.empty
        )
    }

    if(expression == null) {
        val bindings = Bindings.empty.addBinding(name, Invalid(getNextId()))
        return LetEnvironment(
            Subordinate(expressionSpan, EmptyExpressionEnvironment),
            Substitution.empty,
            emptyMap(),
            bindings,
            Requirements.empty,
            CortecsErrors.empty
        )
    }

    val environment = expression.environment
    val subordinate = Subordinate(expressionSpan, environment)
    val errors = environment.errors.addOffset(expressionSpan)
    if(annotation != null) {
        val expressionType = tokenToType(annotation, getNextId())
        val bindings = Bindings.empty.addBinding(name, expressionType)
        when(val result = Substitution.empty.unify(environment.expressionType, expressionType)) {
            is UnificationSuccess -> {
                val mapping = mutableMapOf<Long, Type>()
                val requirements = environment.requirements.applySubstitution(result.substitution, mapping)
                return LetEnvironment(
                    subordinate, result.substitution, mapping, bindings, requirements, errors
                )
            }

            is UnificationError -> {
                val unificationErrors = environment.getSpansForType(result.rType).map {
                    CortecsError("Unification error", expressionSpan + it, Span.zero)
                }
                val outErrors = errors + CortecsErrors(null, unificationErrors)
                return LetEnvironment(
                    subordinate, Substitution.empty, emptyMap(), bindings, environment.requirements, outErrors
                )
            }
        }
    } else {
        val bindings = Bindings.empty.addBinding(name, environment.expressionType)
        return LetEnvironment(
            subordinate, Substitution.empty, emptyMap(), bindings, environment.requirements, errors
        )
    }
}

fun generateReturnEnvironment(expression: Expression?, expressionSpan: Span): ReturnEnvironment {
    if(expression == null) return ReturnEnvironment(
        Subordinate(expressionSpan, EmptyExpressionEnvironment), Requirements.empty, CortecsErrors.empty
    )

    val environment = expression.environment
    val errors = environment.errors.addOffset(expressionSpan)
    val requirements = if(environment.expressionType is Invalid) environment.requirements
    else environment.requirements.addRequirement(ReturnTypeToken, environment.expressionType)
    return ReturnEnvironment(Subordinate(expressionSpan, environment), requirements, errors)
}

sealed interface FunctionCallArgumentsResult {
    val requirements: Requirements
    val subordinates: List<Subordinate<ExpressionEnvironment>>
    val errors: CortecsErrors
}

data class FunctionCallArgumentsGood(
    val argumentsTypes: List<Type>,
    override val requirements: Requirements,
    override val subordinates: List<Subordinate<ExpressionEnvironment>>,
    override val errors: CortecsErrors
): FunctionCallArgumentsResult

data class FunctionCallArgumentsBad(
    override val requirements: Requirements,
    override val subordinates: List<Subordinate<ExpressionEnvironment>>,
    override val errors: CortecsErrors
): FunctionCallArgumentsResult

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
    else FunctionCallArgumentsGood(argumentTypes, requirements, subordinates, errors)
}

fun processGoodFunctionCall(
    fEnvironment: ExpressionEnvironment, arguments: FunctionCallArgumentsGood
): ExpressionEnvironment {
    val returnType = freshUnificationVariable()
    val lhsType = typesToType(arguments.argumentsTypes, returnType.id)
    val arrowType = ArrowType(returnType.id, lhsType, returnType)

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
            val spans = fEnvironment.getSpansForType(fEnvironment.expressionType)
            for(span in spans) {
                errors += CortecsError("Unification error", span, Span.zero)
            }
            substitution = Substitution.empty
            outType = Invalid(returnType.id)
            outArrow = Invalid(arrowType.id)
        }
    }

    return FunctionCallExpressionEnvironment(
        outType,
        outArrow,
        requirements,
        substitution,
        Subordinate(Span.zero, fEnvironment),
        arguments.subordinates,
        errors
    )
}

fun processBadFunctionCall(
    fEnvironment: ExpressionEnvironment, arguments: FunctionCallArgumentsResult
): ExpressionEnvironment {
    val outType = Invalid(getNextId())
    val outArrow = Invalid(getNextId())
    val requirements = fEnvironment.requirements + arguments.requirements
    val errors = fEnvironment.errors + arguments.errors
    return FunctionCallExpressionEnvironment(
        outType,
        outArrow,
        requirements,
        Substitution.empty,
        Subordinate(Span.zero, fEnvironment),
        arguments.subordinates,
        errors
    )
}

fun generateFunctionCallExpressionEnvironment(
    function: Expression, arguments: ArgumentsAst, argumentsSpan: Span
): ExpressionEnvironment {
    val fEnvironment = function.environment
    return when(val result = processFunctionCallArguments(arguments, argumentsSpan)) {
        is FunctionCallArgumentsGood -> if(fEnvironment.expressionType is Invalid) processBadFunctionCall(
            fEnvironment, result
        )
        else processGoodFunctionCall(fEnvironment, result)

        is FunctionCallArgumentsBad -> processBadFunctionCall(fEnvironment, result)
    }
}

fun generateGroupingExpressionEnvironment(
    expression: Expression, expressionSpan: Span
): ExpressionEnvironment {
    val environment = expression.environment
    val subordinate = Subordinate(expressionSpan, environment)
    val errors = environment.errors.addOffset(expressionSpan)
    return GroupingExpressionEnvironment(
        environment.expressionType, environment.requirements, subordinate, errors
    )
}

fun generateUnaryExpressionEnvironment(
    op: OperatorToken, expression: Expression?, expressionSpan: Span
): ExpressionEnvironment {
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

fun generateBinaryExpressionEnvironment(
    lhs: Expression, op: OperatorToken, opSpan: Span, rhs: Expression?, rhsSpan: Span
): ExpressionEnvironment {
    val lEnvironment = lhs.environment
    val rEnvironment = rhs?.environment
    if(rEnvironment == null) {
        val retType = Invalid(getNextId())
        val opType = Invalid(getNextId())
        val lSubordinate = Subordinate(Span.zero, lEnvironment)
        return BinaryExpressionEnvironment(
            retType, opType, opSpan, lEnvironment.requirements, lSubordinate, null, lEnvironment.errors
        )
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
        val productType =
            ProductType(typeId, listOf(lEnvironment.expressionType, rEnvironment.expressionType))
        opType = ArrowType(typeId, productType, retType)
        requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(op, opType)
    }

    val lSubordinate = Subordinate(Span.zero, lEnvironment)
    val rSubordinate = Subordinate(rhsSpan, rEnvironment)
    val errors = lEnvironment.errors + rEnvironment.errors.addOffset(rhsSpan)
    return BinaryExpressionEnvironment(
        retType, opType, opSpan, requirements, lSubordinate, rSubordinate, errors
    )
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
    is BadCharToken -> AtomicExpressionEnvironment(
        CharacterType(getNextId()), Requirements.empty
    ) //todo should I??
    is BadStringToken -> AtomicExpressionEnvironment(
        StringType(getNextId()), Requirements.empty
    ) //todo should I??
}

var typeId: Long = 0
fun getNextId() = typeId++
fun freshUnificationVariable() = UnificationTypeVariable(getNextId())

fun tokenToType(t: TypeAnnotationToken, id: Long): Type = when(t) {
    is TypeToken -> when(t.value) {
        "U8" -> U8Type(id)
        "U16" -> U16Type(id)
        "U32" -> U32Type(id)
        "U64" -> U64Type(id)
        "I8" -> I8Type(id)
        "I16" -> I16Type(id)
        "I32" -> I32Type(id)
        "I64" -> I64Type(id)
        "F32" -> F32Type(id)
        "F64" -> F64Type(id)
        "String" -> StringType(id)
        "Character" -> CharacterType(id)
        "Boolean" -> BooleanType(id)
        else -> TODO("User defined type")
    }

    is NameToken -> UserDefinedTypeVariable(id) //todo
}

fun typesToType(types: List<Type>, id: Long) = //todo find better name for this
    when(types.size) {
        0 -> UnitType(id)
        1 -> types.first()
        else -> ProductType(id, types)
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