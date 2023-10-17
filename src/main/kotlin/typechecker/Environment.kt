package typechecker

import parser.*
import utilities.*

interface Environment {
    val substitution: Substitution
    val bindings: Map<BindableToken, TypeScheme>
    val requirements: Map<BindableToken, List<Type>>
    operator fun plus(other: Environment): Environment
    fun copy(substitution: Substitution = this.substitution, bindings: Map<BindableToken, TypeScheme> = this.bindings, requirements: Map<BindableToken, List<Type>> = this.requirements): Environment
    fun addRequirement(token: BindableToken, type: Type): Environment = copy(requirements = requirements + (token to ((requirements[token] ?: emptyList()) + type)))
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

    fun merge(substitution: Substitution, requirements: Map<BindableToken, List<Type>>, bindings: Map<BindableToken, TypeScheme>,
              otherRequirements: Map<BindableToken, List<Type>>, outRequirements: MutableMap<BindableToken, List<Type>>): Substitution =
        otherRequirements.fold(substitution) { init, token, otherTypeVars ->
            val typeScheme = bindings[token]
            if(typeScheme != null)
                otherTypeVars.fold(init) { acc, typeVar ->
                    val (instantiated, instSubstitution) = instantiate(typeScheme, acc)
                    instSubstitution.unify(typeVar, instantiated)
                }
            else if(outRequirements.containsKey(token)) init
            else {
                val typeVars = requirements[token]
                if(typeVars == null) outRequirements[token] = otherTypeVars
                else outRequirements[token] = typeVars + otherTypeVars
                init
            }
        }
}

class TopLevelEnvironment(override val substitution: Substitution, override val bindings: Map<BindableToken, TypeScheme>, override val requirements: Map<BindableToken, List<Type>>): Environment {
    companion object {
        val base =
            mapOf<BindableToken, TypeScheme>(
                OperatorToken("==") to TypeScheme(emptyList(), ArrowType(ProductType(listOf(I32Type, I32Type)), BooleanType)),
                OperatorToken("+") to TypeScheme(emptyList(), ArrowType(ProductType(listOf(I32Type, I32Type)), I32Type)),
                OperatorToken("-") to TypeScheme(emptyList(), ArrowType(ProductType(listOf(I32Type, I32Type)), I32Type)),
                OperatorToken("*") to TypeScheme(emptyList(), ArrowType(ProductType(listOf(I32Type, I32Type)), I32Type)),
                OperatorToken("/") to TypeScheme(emptyList(), ArrowType(ProductType(listOf(I32Type, I32Type)), I32Type)),
            ).let { TopLevelEnvironment(Substitution.empty, it, emptyMap()) }
    }

    override fun copy(substitution: Substitution, bindings: Map<BindableToken, TypeScheme>, requirements: Map<BindableToken, List<Type>>): TopLevelEnvironment = TopLevelEnvironment(substitution, bindings, requirements)

    override fun plus(other: Environment): TopLevelEnvironment {
        when(other) {
            is EmptyEnvironment -> return this
            is BlockEnvironment -> throw Exception()
        }

        val outRequirements = mutableMapOf<BindableToken, List<Type>>()
        val outBindings = bindings + other.bindings
        val outSubstitution =
            (substitution + other.substitution).let {
                merge(it, requirements, bindings, other.requirements, outRequirements)
            }.let {
                merge(it, other.requirements, other.bindings, requirements, outRequirements)
            }
        return TopLevelEnvironment(outSubstitution, outBindings, outRequirements)
    }
}

class BlockEnvironment(override val substitution: Substitution, override val bindings: Map<BindableToken, TypeScheme>, override val requirements: Map<BindableToken, List<Type>>, val freeUserDefinedTypeVariables: Set<UserDefinedTypeVariable>): Environment {
    override fun copy(substitution: Substitution, bindings: Map<BindableToken, TypeScheme>, requirements: Map<BindableToken, List<Type>>): BlockEnvironment = BlockEnvironment(substitution, bindings, requirements, freeUserDefinedTypeVariables)

    override fun plus(other: Environment): BlockEnvironment {
        if(other is EmptyEnvironment) return this
        if(other !is BlockEnvironment) throw Exception()
        val outBindings = bindings + other.bindings
        val outRequirements = mutableMapOf<BindableToken, List<Type>>()
        val outSubstitution = merge(substitution + other.substitution, requirements, bindings, other.requirements, outRequirements)
        for((token, typeVars) in requirements) if(!outRequirements.containsKey(token)) outRequirements[token] = typeVars
        return BlockEnvironment(outSubstitution, outBindings, outRequirements, freeUserDefinedTypeVariables + other.freeUserDefinedTypeVariables)
    }
}

object EmptyEnvironment: Environment {
    override val substitution = Substitution.empty
    override val bindings: Map<BindableToken, TypeScheme> = emptyMap()
    override val requirements: Map<BindableToken, List<Type>> = emptyMap()
    override fun plus(other: Environment) = other
    override fun copy(substitution: Substitution, bindings: Map<BindableToken, TypeScheme>, requirements: Map<BindableToken, List<Type>>) = BlockEnvironment(substitution, bindings, requirements, emptySet())
}