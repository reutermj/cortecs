package typechecker

import parser.*

var unificationVarNumber: Long = 0
fun nextId() = "${unificationVarNumber++}"
fun freshUnificationVariable() = UnificationTypeVariable(nextId())

fun typesToType(types: List<Type>) = //todo find better name for this
    when(types.size) {
        0 -> UnitType(nextId())  //should probably be empty type
        1 -> types.first()
        else -> ProductType(nextId(), types)
    }

fun generateBlockEnvironment(block: BlockAst): BlockEnvironment {
    if(block.nodes.isEmpty()) return BlockEnvironment.empty

    var span = Span.zero

    val outSubordinates = block.nodes.map {
        val subordinate = when(it) {
            is BodyAst -> Subordinate(span, it.environment)
            is BlockAst -> Subordinate(span, it.environment)
            else -> throw Exception()
        }
        span += it.span
        subordinate
    }

    val outFreeUserDefinedTypeVariable =
        outSubordinates.fold(emptyList<UserDefinedTypeVariable>()) { acc, subordinate ->
            acc + subordinate.environment.freeUserDefinedTypeVariables
        }.toSet()

    var substitution = Substitution.empty
    var outBindings = Bindings.empty
    var outRequirements = Requirements.empty
    var outCompatibilities = Compatibilities.empty
    for(subordinate in outSubordinates) {
        val environment = subordinate.environment
        for((token, types) in environment.requirements.requirements) {
            val typeScheme = outBindings[token]
            if(typeScheme == null) {
                outCompatibilities += environment.compatibilities
                continue
            }

            for(type in types) {
                val (instantiated, compatibilities) = instantiate(typeScheme, environment.compatibilities)
                outCompatibilities += compatibilities
                substitution = substitution.unify(type, instantiated)
            }
        }

        outBindings += environment.bindings
        outRequirements += environment.requirements.filter { token, _ -> !outBindings.contains(token) }
    }

    outRequirements = outRequirements.applySubstitution(substitution)
    outBindings = outBindings.applySubstitution(substitution)
    outCompatibilities = outCompatibilities.applySubstitution(substitution)

    return BlockEnvironment(outBindings, outRequirements, outCompatibilities, outFreeUserDefinedTypeVariable, outSubordinates)
}

fun generateIfEnvironment(ifAst: IfAst): BlockEnvironment {
    if(ifAst.conditionIndex == -1) return BlockEnvironment.empty
    if(ifAst.blockIndex == -1) return BlockEnvironment.empty

    val cEnvironment = ifAst.condition().environment
    val bEnvironment = generateBlockEnvironment(ifAst.block())

    val substitution = Substitution.empty.unify(cEnvironment.type, BooleanType(nextId()))
    val mapping = mutableMapOf<String, Type>()
    val requirements = cEnvironment.requirements.map { substitution.apply(it, mapping) } + bEnvironment.requirements

    return BlockEnvironment(Bindings.empty, requirements, bEnvironment.compatibilities, bEnvironment.freeUserDefinedTypeVariables, listOf(Subordinate(ifAst.conditionSpan, cEnvironment), Subordinate(ifAst.blockSpan, bEnvironment)))
}

fun generateLetEnvironment(let: LetAst): BlockEnvironment {
    if(let.nameIndex == -1) return BlockEnvironment.empty
    if(let.expressionIndex == -1) return BlockEnvironment.empty
    val environment = let.expression().environment
    val freeUserDefinedTypeVariable = mutableSetOf<UserDefinedTypeVariable>()
    val requirements: Requirements
    val type: TypeScheme
    val compatibilities: Compatibilities
    val mapping = mutableMapOf<String, Type>()

    if(let.typeAnnotationIndex == -1) {
        requirements = environment.requirements
        val (t, c) = generalize(environment.type)
        type = t
        compatibilities = c
    } else {
        val annotation = tokenToType(let.typeAnnotation())
        if(annotation is UserDefinedTypeVariable) freeUserDefinedTypeVariable.add(annotation)
        val substitution = Substitution.empty.unify(annotation, environment.type)
        requirements = environment.requirements.map { substitution.apply(it, mapping) }
        type = TypeScheme(nextId(), emptyList(), annotation)
        compatibilities = Compatibilities.empty
    }

    return BlockEnvironment(Bindings.empty.addBinding(let.name(), type), requirements, compatibilities, freeUserDefinedTypeVariable, listOf(Subordinate(let.expressionSpan, environment)))
}

fun generateReturnEnvironment(returnAst: ReturnAst): BlockEnvironment {
    if(returnAst.expressionIndex == -1) return BlockEnvironment.empty
    val environment = returnAst.expression().environment
    val requirements = environment.requirements.addRequirement(ReturnTypeToken, environment.type)
    return BlockEnvironment(Bindings.empty, requirements, Compatibilities.empty, emptySet(), listOf(Subordinate(returnAst.expressionSpan, environment)))
}

fun generateFunctionCallExpressionEnvironment(fnCall: FunctionCallExpression): ExpressionEnvironment {
    if(fnCall.argumentsIndex == -1) return ExpressionEnvironment.empty
    val fEnvironment = fnCall.function().environment

    val argTypes = mutableListOf<Type>()
    var requirements = Requirements.empty
    val subordinates = mutableListOf(Subordinate(Span.zero, fEnvironment))

    var argumentSpan = fnCall.argumentsSpan
    fnCall.arguments().inOrder {
        val environment = it.expression().environment
        requirements += environment.requirements
        argTypes.add(environment.type)
        subordinates.add(Subordinate(argumentSpan, environment))
        argumentSpan += it.span
    }

    val retType = freshUnificationVariable()
    //todo, start handling unification errors
    val substitution = Substitution.empty.unify(fEnvironment.type, ArrowType(nextId(), typesToType(argTypes), retType))
    val mappings = mutableMapOf<String, Type>()
    requirements += fEnvironment.requirements.map { substitution.apply(it, mappings) }

    return ExpressionEnvironment(retType, requirements, mappings, subordinates)
}

fun generateBinaryExpressionEnvironment(binaryExpression: BinaryExpression): ExpressionEnvironment {
    if(binaryExpression.rhsIndex == -1) return ExpressionEnvironment.empty
    val lEnvironment = binaryExpression.lhs().environment
    val rEnvironment = binaryExpression.rhs().environment

    val retType = freshUnificationVariable()
    val opType = ArrowType(nextId(), ProductType(nextId(), listOf(lEnvironment.type, rEnvironment.type)), retType)
    val requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(binaryExpression.op(), opType)
    return ExpressionEnvironment(retType, requirements, emptyMap(), listOf(Subordinate(Span.zero, lEnvironment), Subordinate(binaryExpression.rhsSpan, rEnvironment)))
}

fun generateUnaryExpressionEnvironment(unaryExpression: UnaryExpression): ExpressionEnvironment {
    if(unaryExpression.expressionIndex == -1) return ExpressionEnvironment.empty
    val environment = unaryExpression.expression().environment

    val retType = freshUnificationVariable()
    val requirements = environment.requirements.addRequirement(unaryExpression.op(), ArrowType(nextId(), environment.type, retType))
    return ExpressionEnvironment(retType, requirements, emptyMap(), listOf(Subordinate(unaryExpression.expressionSpan, environment)))
}

fun generateGroupingExpressionEnvironment(groupingExpression: GroupingExpression) =
    if(groupingExpression.expressionIndex == -1) ExpressionEnvironment.empty
    else {
        val environment = groupingExpression.expression().environment
        ExpressionEnvironment(environment.type, environment.requirements, emptyMap(), listOf(Subordinate(groupingExpression.expressionSpan, environment)))
    }

fun generateAtomicExpressionEnvironment(a: AtomicExpression) =
    when(val atom = a.atom()) {
        is NameToken -> {
            val type = freshUnificationVariable()
            val requirements = Requirements.empty.addRequirement(atom, type)
            ExpressionEnvironment.empty.copy(type = type, requirements = requirements)
        }
        is IntToken -> ExpressionEnvironment.empty.copy(type = getIntType(atom))
        is FloatToken -> ExpressionEnvironment.empty.copy(type = getFloatType(atom))
        is CharToken -> ExpressionEnvironment.empty.copy(type = CharacterType(nextId()))
        is StringToken -> ExpressionEnvironment.empty.copy(type = StringType(nextId()))
        is BadCharToken -> ExpressionEnvironment.empty
        is BadStringToken -> ExpressionEnvironment.empty
    }

fun getIntType(i: IntToken): Type {
    val isUnsigned = i.value.contains("u", true)
    return when(i.value.last()) {
        'b', 'B' -> if(isUnsigned) U8Type(nextId()) else I8Type(nextId())
        's', 'S' -> if(isUnsigned) U16Type(nextId()) else I16Type(nextId())
        'l', 'L' -> if(isUnsigned) U64Type(nextId()) else I64Type(nextId())
        else -> if(isUnsigned) U32Type(nextId()) else I32Type(nextId())
    }
}

fun getFloatType(i: FloatToken) =
    when(i.value.last()) {
        'd', 'D' -> F64Type(nextId())
        else -> F32Type(nextId())
    }

fun tokenToType(t: TypeAnnotationToken): Type =
    when(t) {
        is TypeToken ->
            when(t.value) {
                "U8" -> U8Type(nextId())
                "U16" -> U16Type(nextId())
                "U32" -> U32Type(nextId())
                "U64" -> U64Type(nextId())
                "I8" -> I8Type(nextId())
                "I16" -> I16Type(nextId())
                "I32" -> I32Type(nextId())
                "I64" -> I64Type(nextId())
                "F32" -> F32Type(nextId())
                "F64" -> F64Type(nextId())
                "String" -> StringType(nextId())
                "Character" -> CharacterType(nextId())
                "Boolean" -> BooleanType(nextId())
                else -> TODO("User defined type")
            }
        is NameToken -> UserDefinedTypeVariable(t.value)
    }

fun generalize(type: Type): Pair<TypeScheme, Compatibilities> {
    val typeVariables = type.freeTypeVariables.toList().sortedBy { it.id }
    val compatibilities = Compatibilities.empty.addCompatibilities(typeVariables.filterIsInstance<UnificationTypeVariable>())
    return Pair(TypeScheme(nextId(), typeVariables, type), compatibilities)
}

fun instantiate(typeScheme: TypeScheme, compatibilities: Compatibilities): Pair<Type, Compatibilities> {
    //todo really test compatibilities
    var outCompatibilities = compatibilities
    val mapping =
        typeScheme.boundVariables.associateWith {
            val fresh = freshUnificationVariable()
            if(it is UnificationTypeVariable) outCompatibilities = outCompatibilities.makeCompatible(it, fresh)
            fresh
        }

    fun inst(type: Type): Type =
        when(type) {
            is TypeVariable -> mapping[type] ?: throw Exception()
            is ArrowType -> ArrowType(nextId(), inst(type.lhs), inst(type.rhs))
            is ProductType -> ProductType(nextId(), type.types.map { inst(it) })
            else -> type
        }

    return Pair(inst(typeScheme.type), outCompatibilities)
}