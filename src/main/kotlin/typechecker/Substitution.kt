package typechecker

import kotlinx.serialization.*

@Serializable
sealed class Lookup: LookupIntermediate()
@Serializable
sealed class LookupIntermediate
@Serializable
data class Representative(val typeVar: TypeVariable): Lookup()
@Serializable
data class TypeMapping(val type: Type): Lookup()
@Serializable
data class Intermediate(val dst: UnificationTypeVariable): LookupIntermediate()
@Serializable
data class Compatibility(val typeVar: TypeVariable, val typeVars: Set<UnificationTypeVariable>): Lookup()
@Serializable
data class Substitution(val mapping: Map<TypeVariable, LookupIntermediate>) {
    companion object {
        val empty = Substitution(emptyMap())
    }

    operator fun plus(other: Substitution) = Substitution(mapping + other.mapping)

    fun unify(lType: Type, rType: Type): Substitution =
        when {
            lType == rType -> this
            lType is UnificationTypeVariable -> unifyUnificationTypeVariable(lType, rType)
            rType is UnificationTypeVariable -> unifyUnificationTypeVariable(rType, lType)
            lType is ArrowType && rType is ArrowType -> unify(lType.lhs, rType.lhs).unify(lType.rhs, rType.rhs)
            lType is ProductType && rType is ProductType -> {
                if(lType.types.size != rType.types.size) TODO()
                lType.types.zip(rType.types).fold(this) { acc, pair -> acc.unify(pair.first, pair.second) }
            }
            else ->
                TODO()
        }

    fun unifyCompatibilityToType(compatibility: Compatibility, type: Type): Substitution =
        compatibility.typeVars.fold(pointAt(compatibility.typeVar, type)) { acc, typeVar -> acc.unify(typeVar, type) }

    fun unifyUnificationTypeVariable(lType: UnificationTypeVariable, rType: Type): Substitution =
        when(val lLookup = find(lType)) {
            is TypeMapping -> unify(rType, lLookup.type)
            is Representative ->
                when(rType) {
                    is UnificationTypeVariable ->
                        when(val rLookup = find(rType)) {
                            is Representative -> pointAt(lLookup.typeVar, rLookup.typeVar)
                            is TypeMapping -> pointAt(lLookup.typeVar, rLookup.type)
                            is Compatibility -> unifyCompatibilityToType(rLookup, lLookup.typeVar)
                        }
                    else -> pointAt(lLookup.typeVar, rType)
                }
            is Compatibility ->
                when(rType) {
                    is UnificationTypeVariable ->
                        when(val rLookup = find(rType)) {
                            is Representative -> unifyCompatibilityToType(lLookup, rLookup.typeVar)
                            is TypeMapping -> unifyCompatibilityToType(lLookup, rLookup.type)
                            is Compatibility -> Substitution(pointAt(lLookup.typeVar, rLookup.typeVar).mapping + (rLookup.typeVar to Compatibility(rLookup.typeVar, rLookup.typeVars + lLookup.typeVars)))
                        }
                    else -> unifyCompatibilityToType(lLookup, rType)
                }
        }

    fun apply(type: Type): Type =
        when(type) {
            is ConcreteType -> type
            is ArrowType -> ArrowType(apply(type.lhs), apply(type.rhs))
            is ProductType -> ProductType(type.types.map { apply(it) })
            is UserDefinedTypeVariable -> type
            is UnificationTypeVariable ->
                when(val result = find(type)) {
                    is Representative -> result.typeVar
                    is TypeMapping -> apply(result.type)
                    is Compatibility -> result.typeVar
                }
            is TypeScheme -> {
                //For co-contextual type inference, TypeSchemes abstract over all type variables from the function
                //Many of the variables only exist due to unresolved references to other functions
                //Once these references are resolved, the type variables can be discharged from the TypeScheme
                val typeVars = type.boundVariables.fold(emptySet<TypeVariable>()) { acc, typeVar ->
                    val applied = apply(typeVar)
                    if(applied is TypeVariable) acc + applied
                    else acc
                }

                if(typeVars.any()) TypeScheme(typeVars.toList().sortedBy { it.n }, apply(type.type))
                else apply(type.type)
            }
            else -> TODO()
        }

    fun find(typeVar: UnificationTypeVariable): Lookup {
        var tv = typeVar
        while(true) {
            when(val result = mapping[tv] ?: Representative(tv)) {
                is Lookup -> return result
                is Intermediate -> tv = result.dst
            }
        }
    }

    fun makeCompatibility(inst: UnificationTypeVariable) =
        when(find(inst)) {
            is Representative -> Substitution(mapping + (inst to Compatibility(inst, emptySet())))
            is Compatibility -> this
            is TypeMapping -> throw Exception()
        }

    fun pointAt(src: TypeVariable, dst: Type) =
        when(dst) {
            src -> this
            is UnificationTypeVariable -> Substitution(mapping + (src to Intermediate(dst)))
            else -> Substitution(mapping + (src to TypeMapping(dst)))
        }
}
