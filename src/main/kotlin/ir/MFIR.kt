package ir

import parser.*
import typechecker.*

data class FunctionMfir(val name: String, val returnType: Type, val args: List<DefinitionIr>, val body: MutableList<BodyMfir>)
data class ComponentMfir(val type: ClosedComponentType)
sealed interface BodyMfir {
    val name: String
    val type: Type
}
data class ReturnIr(val retVal: BodyMfir): BodyMfir {
    override val name: String
        get() = "ret_val"
    override val type: Type
        get() = retVal.type
}
data class DefinitionIr(override val name: String, override val type: Type): BodyMfir
data class FunctionCallIr(override val name: String, override val type: Type, val fn: FunctionMfir, val args: List<BodyMfir>): BodyMfir
data class ComponentConstructorIr(override val name: String, override val type: ClosedComponentType, val arguments: List<BodyMfir>): BodyMfir
data class ComponentSelectionIr(override val name: String, override val type: Type, val component: BodyMfir, val label: String): BodyMfir
data class EntityDefinitionIr(override val name: String, override val type: ClosedEntityType, val expressions: List<BodyMfir>): BodyMfir
data class EntityRestrictionIr(override val name: String, override val type: ClosedEntityType, val entity: BodyMfir, val component: ClosedComponentType): BodyMfir
data class EntitySelectionIr(override val name: String, override val type: ClosedComponentType, val entity: BodyMfir): BodyMfir
data class IntConstantIr(override val name: String, val value: String): BodyMfir {
    override val type: Type
        get() = IntType
}
data class FloatConstantIr(override val name: String, val value: String): BodyMfir {
    override val type: Type
        get() = FloatType
}
data class StringConstantIr(override val name: String, val value: String): BodyMfir {
    override val type: Type
        get() = StringType
}
data class CharConstantIr(override val name: String, val value: String): BodyMfir {
    override val type: Type
        get() = CharType
}

fun constructMfir(component: ComponentAst, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>, components: MutableList<ComponentMfir>) {
    val fnType = component._fnType as FunctionType
    val componentType = component.type as ClosedComponentType
    val defs =
        when(component.valueDefs.size) {
            1 -> listOf(DefinitionIr(component.valueDefs.first().name.value, fnType.lhs))
            else -> {
                component.valueDefs.zip((fnType.lhs as SumType).types) { vd, t -> DefinitionIr(vd.name.value, t) }
            }
        }
    val ctor = ComponentConstructorIr("ret_val", componentType, defs)
    val ret = ReturnIr(ctor)
    val fnMfir = FunctionMfir(component.name.value, componentType, defs, mutableListOf(ctor, ret))
    nameToFn[Pair(component.name.value, fnType)] = fnMfir
    components.add(ComponentMfir(componentType))
}

fun constructMfir(fn: FnAst, nameToAst: Map<String, FnAst>, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>): FunctionMfir? {
    if(fn.type is TypeScheme) return null
    return nameToFn[Pair(fn.name.value, fn.type)] ?: run {
        val fnType = fn.type as FunctionType
        val args =
            when(fn.parameters.size) {
                0 -> emptyList()
                1 -> listOf(DefinitionIr(fn.parameters.first().value, fnType.lhs))
                else -> {
                    fn.parameters.zip((fnType.lhs as SumType).types) { p, t -> DefinitionIr(p.value, t) }
                }
            }
        val nameToIr = args.associateBy { it.name }.toMutableMap<String, BodyMfir>()
        val fnMfir = FunctionMfir(fn.name.value, fnType.rhs, args, mutableListOf())
        nameToFn[Pair(fn.name.value, fnType)] = fnMfir
        for(body in fn.body) constructMfir(body, mapOf(), fnMfir.body, nameToAst, nameToIr, nameToFn)
        fnMfir
    }
}

fun constructMfir(fn: FnAst, substitutions: Map<Type, Type>, nameToAst: Map<String, FnAst>, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>): FunctionMfir {
    val fnType = applySubstitutions((fn.type as TypeScheme).body, substitutions) as FunctionType
    val args =
        when(fn.parameters.size) {
            0 -> emptyList()
            1 -> listOf(DefinitionIr(fn.parameters.first().value, fnType.lhs))
            else -> {
                fn.parameters.zip((fnType.lhs as SumType).types) { p, t -> DefinitionIr(p.value, t) }
            }
        }
    val nameToIr = args.associateBy { it.name }.toMutableMap<String, BodyMfir>()
    val fnMfir = FunctionMfir(fn.name.value, fnType.rhs, args, mutableListOf())
    nameToFn[Pair(fn.name.value, fnType)] = fnMfir
    for(body in fn.body) constructMfir(body, substitutions, fnMfir.body, nameToAst, nameToIr, nameToFn)
    return fnMfir
}

fun constructMfir(fnName: String, type: Type, nameToAst: Map<String, FnAst>, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>): FunctionMfir {
    return nameToFn[Pair(fnName, type)] ?: run {
        val fn = nameToAst[fnName]!!
        val fnMfir =
            if(fn.type is FunctionType) constructMfir(fn, nameToAst, nameToFn)!!
            else {
                val fnType = (fn.type as TypeScheme).body
                val substitutions = unify(mutableListOf(Constraint(type, fnType)))
                constructMfir(fn, substitutions, nameToAst, nameToFn)
            }
        nameToFn[Pair(fnName, type)] = fnMfir
        fnMfir
    }
}

fun constructMfir(fnBody: FnBodyAst, substitutions: Map<Type, Type>, bodyIr: MutableList<BodyMfir>, nameToAst: Map<String, FnAst>, nameToIr: MutableMap<String, BodyMfir>, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>) {
    when(fnBody) {
        is ReturnAst -> bodyIr.add(ReturnIr(constructMfir(fnBody.expression, "ret_val", substitutions, bodyIr, nameToAst, nameToIr, nameToFn)))
        is LetAst -> constructMfir(fnBody.expression, fnBody.name.value, substitutions, bodyIr, nameToAst, nameToIr, nameToFn)
    }
}

fun constructMfir(expression: Expression, name: String, substitutions: Map<Type, Type>, bodyIr: MutableList<BodyMfir>, nameToAst: Map<String, FnAst>, nameToIr: MutableMap<String, BodyMfir>, nameToFn: MutableMap<Pair<String, Type>, FunctionMfir>): BodyMfir {
    return when(expression) {
        is FnCallAst -> {
            val dependencies = mutableListOf<BodyMfir>()
            for((i, arg) in expression.arguments.withIndex()) {
                val dependency = constructMfir(arg, "${name}_$i", substitutions, bodyIr, nameToAst, nameToIr, nameToFn)
                dependencies.add(dependency)
            }
            val fnName = expression.fn as NameAst
            val fnMfir = constructMfir(fnName.name.value, applySubstitutions(expression.fn.type, substitutions), nameToAst, nameToFn)
            val nir = FunctionCallIr(name,  applySubstitutions(expression.type, substitutions), fnMfir, dependencies)
            nameToIr[name] = nir
            bodyIr.add(nir)
            nir
        }

        is ComponentSelectionAst -> {
            val component = constructMfir(expression.component, "${name}_0", substitutions, bodyIr, nameToAst, nameToIr, nameToFn)
            val nir = ComponentSelectionIr(name, applySubstitutions(expression.type, substitutions), component, expression.label.value)
            nameToIr[name] = nir
            bodyIr.add(nir)
            component
        }
        is EntitySelectionAst -> {
            val entity = constructMfir(expression.entity, "${name}_0", substitutions, bodyIr, nameToAst, nameToIr, nameToFn)
            val nir = EntitySelectionIr(name, applySubstitutions(expression.type, substitutions) as ClosedComponentType, entity)
            nameToIr[name] = nir
            bodyIr.add(nir)
            nir
        }
        is EntityRestrictionAst -> {
            val entity = constructMfir(expression.entity, "${name}_0", substitutions, bodyIr, nameToAst, nameToIr, nameToFn)
            //todo oh god how is this the only way to get the component type
            val resType = applySubstitutions(expression.type, substitutions) as ClosedEntityType
            val entityType = (applySubstitutions(expression.entity.type, substitutions) as ClosedEntityType)
            val t = (entityType.components - resType.components).first()
            val nir = EntityRestrictionIr(name, resType, entity, t as ClosedComponentType)
            nameToIr[name] = nir
            bodyIr.add(nir)
            nir
        }
        is EntityDefinitionAst -> {
            val dependencies = expression.expressions.mapIndexed { i, exp -> constructMfir(exp, "${name}_$i", substitutions, bodyIr, nameToAst, nameToIr, nameToFn) }
            val nir = EntityDefinitionIr(name, applySubstitutions(expression.type, substitutions) as ClosedEntityType, dependencies)
            nameToIr[name] = nir
            bodyIr.add(nir)
            nir
        }

        is NameAst -> nameToIr[expression.name.value]!!
        is IntConstantAst -> IntConstantIr(name, expression.value.value)
        is FloatConstantAst -> FloatConstantIr(name, expression.value.value)
        is CharConstantAst -> CharConstantIr(name, expression.value.value)
        is StringConstantAst -> StringConstantIr(name, expression.value.value)
    }
}