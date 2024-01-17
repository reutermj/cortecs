package typechecker

import errors.*
import parser.*

fun typeCheckFunction(function: FunctionAst): FunctionEnvironment {
    val functionName = function.name() ?: return FunctionEnvironment(Requirements(), CortecsErrors.empty)
    val parameters = function.parameters() ?: return FunctionEnvironment(Requirements(), CortecsErrors.empty)
    val environment = Environment()

    var typeAnnotationBaseSpan = function.parametersSpan
    val parameterTypes = mutableListOf<Type>()
    parameters.inOrder {
        val name = it.name()
        val typeAnnotationSpan = typeAnnotationBaseSpan + it.typeAnnotationSpan
        val typeAnnotation = it.typeAnnotation()
        val type =
            if(typeAnnotation != null) tokenToType(typeAnnotation, typeAnnotationSpan)
            else Invalid(Span.zero)

        parameterTypes.add(type)
        environment.bindings.add(name, type)
        typeAnnotationBaseSpan += it.span
    }

    val returnTypeAnnotation = function.returnType()
    val returnType =
        if(returnTypeAnnotation != null) tokenToType(returnTypeAnnotation, function.returnTypeSpan)
        else UnitType(function.nameSpan)

    val functionType = ArrowType(function.nameSpan, typesToType(parameterTypes, function.nameSpan), returnType)
    environment.bindings.add(functionName, functionType)

    val block = function.block() ?: return FunctionEnvironment(Requirements(), CortecsErrors.empty)
    typeCheckBlock(block, environment, function.blockSpan)

    val errors = mutableListOf<CortecsError>()
    val substitution = environment.constraints.unify(environment.requirements, errors)
    environment.requirements.apply(substitution)

    return FunctionEnvironment(environment.requirements, CortecsErrors(null, errors))
}

fun typeCheckBlock(block: BlockAst, environment: Environment, blockOffset: Span) {
    var offset = blockOffset
    block.inOrder {
        when(it) {
            is LetAst -> typeCheckLet(it, environment, offset)
            is ReturnAst -> TODO()
            is IfAst -> TODO()
            is GarbageBodyAst -> TODO()
        }
        offset += it.span
    }
}

fun typeCheckLet(letAst: LetAst, environment: Environment, offset: Span) {
    val nameOffset = offset + letAst.nameSpan
    val name = letAst.name() ?: return
    val annotationOffset = offset + letAst.typeAnnotationSpan
    val annotation = letAst.typeAnnotation()?.let { tokenToType(it, annotationOffset) }
    val expressionOffset = offset + letAst.expressionSpan
    val expression = letAst.expression()
    if(expression == null) {
        environment.bindings.add(name, annotation ?: Invalid(nameOffset))
        return
    }

    val expressionType = typeCheckExpression(expression, environment, expressionOffset)
    environment.bindings.add(name, annotation ?: expressionType)
    if(annotation != null) {
        environment.constraints.add(annotation, expressionType)
    }
}

fun typeCheckExpression(expression: Expression, environment: Environment, offset: Span): Type {
    return when(expression) {
        is AtomicExpression -> typeCheckAtomicExpression(expression, environment, offset)
        is FunctionCallExpression -> typeCheckFunctionCall(expression, environment, offset)
        is GroupingExpression -> typeCheckGroupingExpression(expression, environment, offset)
        is UnaryExpression -> typeCheckUnaryExpression(expression, environment, offset)
        is BinaryExpression -> typeCheckBinaryExpression(expression, environment, offset)
    }
}

fun typeCheckFunctionCall(call: FunctionCallExpression, environment: Environment, offset: Span): Type {
    val functionType = typeCheckExpression(call.function(), environment, offset)
    if(functionType is Invalid) return Invalid(offset)

    var argumentOffset = offset + call.argumentsSpan
    val argumentTypes = mutableListOf<Type>()
    call.arguments().inOrder {
        argumentTypes.add(typeCheckExpression(it.expression(), environment, argumentOffset))
        argumentOffset += it.span
    }
    if(argumentTypes.any { it is Invalid }) return Invalid(offset)

    val returnType = freshTypeVariable(offset)
    val arrowType = ArrowType(offset, typesToType(argumentTypes, offset), returnType)
    environment.constraints.add(functionType, arrowType)

    return returnType
}

fun typeCheckUnaryExpression(unary: UnaryExpression, environment: Environment, offset: Span): Type {
    val expression = unary.expression() ?: return Invalid(offset)
    val expressionType = typeCheckExpression(expression, environment, offset + unary.expressionSpan)
    if(expressionType is Invalid) return Invalid(offset)

    val placeholder = freshPlaceholder(offset)
    val returnType = freshTypeVariable(offset)
    val unaryType = ArrowType(offset, expressionType, returnType)
    environment.requirements.add(unary.op(), placeholder)
    environment.requirements.add(placeholder, unaryType)

    return returnType
}

fun typeCheckBinaryExpression(binary: BinaryExpression, environment: Environment, offset: Span): Type {
    val opOffset = offset + binary.opSpan
    val rhs = binary.rhs() ?: return Invalid(opOffset)
    val lhs = binary.lhs()
    val lhsType = typeCheckExpression(lhs, environment, offset)
    if(lhsType is Invalid) return Invalid(opOffset)
    val rhsType = typeCheckExpression(rhs, environment, offset)
    if(rhsType is Invalid) return Invalid(opOffset)

    val placeholder = freshPlaceholder(opOffset)
    val returnType = freshTypeVariable(opOffset)
    val binaryType = ArrowType(opOffset, ProductType(opOffset, listOf(lhsType, rhsType)), returnType)
    environment.requirements.add(binary.op(), placeholder)
    environment.requirements.add(placeholder, binaryType)

    return returnType
}

fun typeCheckGroupingExpression(grouping: GroupingExpression, environment: Environment, offset: Span) =
    when(val expression = grouping.expression()) {
        null -> Invalid(offset)
        else -> typeCheckExpression(expression, environment, offset + grouping.expressionSpan)
    }

fun typeCheckAtomicExpression(atom: AtomicExpression, environment: Environment, offset: Span) =
    when(val token = atom.atom()) {
        is NameToken -> {
            val type = environment.bindings[token]?.updateOffset(offset)
            if(type != null) type
            else {
                val placeholder = freshPlaceholder(offset)
                environment.requirements.add(token, placeholder)
                placeholder
            }
        }
        is IntToken -> getIntType(token, offset)
        is FloatToken -> getFloatType(token, offset)
        is CharToken -> CharacterType(offset)
        is StringToken -> StringType(offset)
        is BadCharToken -> CharacterType(offset)
        is BadStringToken -> StringType(offset)
    }

var typeVariableId: Long = 0
fun freshTypeVariable(offset: Span) = TypeVariable(offset, typeVariableId++)

var placeholderId: Long = 0
fun freshPlaceholder(offset: Span) = Placeholder(offset, placeholderId++)

fun typesToType(types: List<Type>, offset: Span) = //todo find better name for this
    when(types.size) {
        0 -> UnitType(offset)
        1 -> types.first()
        else -> ProductType(offset, types)
    }

fun tokenToType(t: TypeAnnotationToken, offset: Span): Type = when(t) {
    is TypeToken -> when(t.value) {
        "U8" -> U8Type(offset)
        "U16" -> U16Type(offset)
        "U32" -> U32Type(offset)
        "U64" -> U64Type(offset)
        "I8" -> I8Type(offset)
        "I16" -> I16Type(offset)
        "I32" -> I32Type(offset)
        "I64" -> I64Type(offset)
        "F32" -> F32Type(offset)
        "F64" -> F64Type(offset)
        "String" -> StringType(offset)
        "Character" -> CharacterType(offset)
        "Boolean" -> BooleanType(offset)
        else -> TODO("User defined type")
    }

    is NameToken -> TODO()
}

fun getIntType(i: IntToken, offset: Span): Type {
    val isUnsigned = i.value.contains("u", true)
    return when(i.value.last()) {
        'b', 'B' -> if(isUnsigned) U8Type(offset) else I8Type(offset)
        's', 'S' -> if(isUnsigned) U16Type(offset) else I16Type(offset)
        'l', 'L' -> if(isUnsigned) U64Type(offset) else I64Type(offset)
        else -> if(isUnsigned) U32Type(offset) else I32Type(offset)
    }
}

fun getFloatType(i: FloatToken, offset: Span) = when(i.value.last()) {
    'd', 'D' -> F64Type(offset)
    else -> F32Type(offset)
}