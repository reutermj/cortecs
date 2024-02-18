package typechecker

import parser.*

var unificationVarNumber: Long = 0
fun freshUnificationVariable() = UnificationTypeVariable("${unificationVarNumber++}")

fun typesToType(types: List<Type>) = //todo find better name for this
    when(types.size) {
        0 -> UnitType  //should probably be empty type
        1 -> types.first()
        else -> ProductType(types)
    }

fun generateBlockEnvironment(block: BlockAst): BlockEnvironment {
    if(block.nodes.isEmpty()) return BlockEnvironment.empty
    val first = when(val node = block.nodes.first()) {
        is BodyAst -> node.environment
        is BlockAst -> node.environment
        else -> throw Exception()
    }

    var outBindings = first.bindings
    var outRequirements = first.requirements
    var outSubstitution = first.substitution
    var outFreeUserDefinedTypeVariable = first.freeUserDefinedTypeVariables
    val outSubordinates = mutableListOf(first)

    for(node in block.nodes.drop(1)) {
        val environment =
            when(node) {
                is BodyAst -> node.environment
                is BlockAst -> node.environment
                else -> throw Exception()
            }

        outBindings += environment.bindings
        outSubstitution += environment.substitution
        outFreeUserDefinedTypeVariable += environment.freeUserDefinedTypeVariables
        val or = mutableMapOf<BindableToken, List<Type>>()
        outSubstitution = merge(outSubstitution, outRequirements, outBindings, environment.requirements, or)
        for((token, typeVars) in outRequirements.requirements) if(!or.containsKey(token)) or[token] = typeVars
        outRequirements = Requirements(or)
        outSubordinates.add(environment)
    }

    return BlockEnvironment(outBindings, outSubstitution, outRequirements, outFreeUserDefinedTypeVariable, outSubordinates)
}

fun generateIfEnvironment(ifAst: IfAst): BlockEnvironment {
    if(ifAst.conditionIndex == -1) return BlockEnvironment.empty
    if(ifAst.blockIndex == -1) return BlockEnvironment.empty

    val cEnvironment = ifAst.condition().environment
    val bEnvironment = generateBlockEnvironment(ifAst.block())

    val requirements = cEnvironment.requirements + bEnvironment.requirements
    //todo handle unification error
    val substitution = (cEnvironment.substitution + bEnvironment.substitution).unify(cEnvironment.type, BooleanType)

    return BlockEnvironment(Bindings.empty, substitution, requirements, bEnvironment.freeUserDefinedTypeVariables, listOf(cEnvironment, bEnvironment))
}

fun generateLetEnvironment(let: LetAst): BlockEnvironment {
    if(let.nameIndex == -1) return BlockEnvironment.empty
    if(let.expressionIndex == -1) return BlockEnvironment.empty
    val environment = let.expression().environment
    val freeUserDefinedTypeVariable = mutableSetOf<UserDefinedTypeVariable>()
    val (type, substitution) =
        if(let.typeAnnotationIndex == -1) generalize(environment.type, environment.substitution)
        else {
            val annotation = tokenToType(let.typeAnnotation())
            if(annotation is UserDefinedTypeVariable) freeUserDefinedTypeVariable.add(annotation)
            //todo handle unification error
            Pair(TypeScheme(emptyList(), annotation), environment.substitution.unify(annotation, environment.type))
        }

    return BlockEnvironment(Bindings.empty.addBinding(let.name(), type), substitution, environment.requirements, freeUserDefinedTypeVariable, listOf(environment))
}

fun generateReturnEnvironment(returnAst: ReturnAst): BlockEnvironment {
    if(returnAst.expressionIndex == -1) return BlockEnvironment.empty
    val environment = returnAst.expression().environment
    val requirements = environment.requirements.addRequirement(ReturnTypeToken, environment.type)
    return BlockEnvironment(Bindings.empty, environment.substitution, requirements, emptySet(), listOf(environment))
}

fun generateFunctionCallExpressionEnvironment(fnCall: FunctionCallExpression): ExpressionEnvironment {
    if(fnCall.argumentsIndex == -1) return ExpressionEnvironment.empty
    val fEnvironment = fnCall.function().environment

    val argTypes = mutableListOf<Type>()
    var substitution = fEnvironment.substitution
    var requirements = fEnvironment.requirements
    val subordinates = mutableListOf<ExpressionEnvironment>()

    fnCall.arguments().inOrder {
        val environment = it.expression().environment
        substitution += environment.substitution
        requirements += environment.requirements
        argTypes.add(environment.type)
        subordinates.add(environment)
    }

    val retType = freshUnificationVariable()
    //todo, start handling unification errors
    substitution = substitution.unify(fEnvironment.type, ArrowType(typesToType(argTypes), retType))
    //todo, the function name requirement needs to be updated with to the arrow type
    return ExpressionEnvironment(retType, substitution, requirements, subordinates)
}

fun generateBinaryExpressionEnvironment(binaryExpression: BinaryExpression): ExpressionEnvironment {
    if(binaryExpression.rhsIndex == -1) return ExpressionEnvironment.empty
    val lEnvironment = binaryExpression.lhs().environment
    val rEnvironment = binaryExpression.rhs().environment

    val retType = freshUnificationVariable()
    val opType = ArrowType(ProductType(listOf(lEnvironment.type, rEnvironment.type)), retType)
    val requirements = (lEnvironment.requirements + rEnvironment.requirements).addRequirement(binaryExpression.op(), opType)
    val substitution = lEnvironment.substitution + rEnvironment.substitution
    return ExpressionEnvironment(retType, substitution, requirements, listOf(lEnvironment, rEnvironment))
}

fun generateUnaryExpressionEnvironment(unaryExpression: UnaryExpression): ExpressionEnvironment {
    if(unaryExpression.expressionIndex == -1) return ExpressionEnvironment.empty
    val environment = unaryExpression.expression().environment

    val retType = freshUnificationVariable()
    val requirements = environment.requirements.addRequirement(unaryExpression.op(), ArrowType(environment.type, retType))
    return ExpressionEnvironment(retType, environment.substitution, requirements, listOf(environment))
}

fun generateGroupingExpressionEnvironment(groupingExpression: GroupingExpression) =
    if(groupingExpression.expressionIndex == -1) ExpressionEnvironment.empty
    else {
        val environment = groupingExpression.expression().environment
        environment.copy(subordinates = listOf(environment))
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
        is CharToken -> ExpressionEnvironment.empty.copy(type = CharacterType)
        is StringToken -> ExpressionEnvironment.empty.copy(type = StringType)
        is BadCharToken -> ExpressionEnvironment.empty
        is BadStringToken -> ExpressionEnvironment.empty
    }

fun getIntType(i: IntToken): Type {
    val isUnsigned = i.value.contains("u", true)
    return when(i.value.last()) {
        'b', 'B' -> if(isUnsigned) U8Type else I8Type
        's', 'S' -> if(isUnsigned) U16Type else I16Type
        'l', 'L' -> if(isUnsigned) U64Type else I64Type
        else -> if(isUnsigned) U32Type else I32Type
    }
}

fun getFloatType(i: FloatToken) =
    when(i.value.last()) {
        'd', 'D' -> F64Type
        else -> F32Type
    }

fun tokenToType(t: TypeAnnotationToken): Type =
    when(t) {
        is TypeToken ->
            when(t.value) {
                "U8" -> U8Type
                "U16" -> U16Type
                "U32" -> U32Type
                "U64" -> U64Type
                "I8" -> I8Type
                "I16" -> I16Type
                "I32" -> I32Type
                "I64" -> I64Type
                "F32" -> F32Type
                "F64" -> F64Type
                "String" -> StringType
                "Character" -> CharacterType
                "Boolean" -> BooleanType
                else -> TODO("User defined type")
            }
        is NameToken -> UserDefinedTypeVariable(t.value)
    }

fun generalize(type: Type, substitution: Substitution): Pair<TypeScheme, Substitution> {
    val applied = substitution.apply(type)
    val typeVariables = applied.freeTypeVariables.toList().sortedBy { it.n }
    val outSubstitution = typeVariables.fold(substitution) { acc, typeVar ->
        when(typeVar) {
            is UnificationTypeVariable -> acc.makeCompatibility(typeVar)
            is UserDefinedTypeVariable -> acc
        }
    }
    return Pair(TypeScheme(typeVariables, applied), outSubstitution)
}

fun merge(substitution: Substitution, requirements: Requirements, bindings: Bindings, otherRequirements: Requirements, outRequirements: MutableMap<BindableToken, List<Type>>): Substitution =
    otherRequirements.fold(substitution) { acc, token, otherTypeVars ->
        val typeScheme = bindings[token]
        if(typeScheme != null)
            otherTypeVars.fold(acc) { acc, typeVar ->
                val (instantiated, instSubstitution) = instantiate(typeScheme, acc)
                instSubstitution.unify(typeVar, instantiated)
            }
        else if(outRequirements.containsKey(token)) acc
        else {
            val typeVars = requirements[token]
            if(typeVars == null) outRequirements[token] = otherTypeVars
            else outRequirements[token] = typeVars + otherTypeVars
            acc
        }
    }

fun instantiate(typeScheme: TypeScheme, substitution: Substitution): Pair<Type, Substitution> {
    val outSubstitution = substitution.mapping.toMutableMap()
    val mapping =
        typeScheme.boundVariables.associateWith {
            when(it) {
                is UserDefinedTypeVariable -> freshUnificationVariable()
                is UnificationTypeVariable ->
                    when(val lookup = substitution.find(it)) {
                        is Representative -> throw Exception("error")
                        is TypeMapping -> lookup.type//todo does this need to be recusive?
                        is Compatibility -> {
                            val fresh = freshUnificationVariable()
                            outSubstitution[lookup.typeVar] = lookup.copy(typeVars = lookup.typeVars + fresh)
                            fresh
                        }
                    }
            }
        }

    fun inst(type: Type): Type =
        when(type) {
            is TypeVariable -> mapping[type] ?: throw Exception()
            is ArrowType -> ArrowType(inst(type.lhs), inst(type.rhs))
            is ProductType -> ProductType(type.types.map { inst(it) })
            else -> type
        }

    return Pair(inst(typeScheme.type), Substitution(outSubstitution))
}