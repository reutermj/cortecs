package codegen

import ir.*
import typechecker.*

sealed interface TypeLowered
data class BasicType(val name: String): TypeLowered {
    override fun toString() = name
}
data class PointerType(val type: TypeLowered): TypeLowered {
    override fun toString() = "$type*"
}

fun lower(type: Type): TypeLowered {
    return when(type) {
        is ClosedComponentType -> BasicType("${type.name}_t")
        is ClosedEntityType -> BasicType("ecs_entity_t")
        is OpenEntityType -> BasicType("ecs_entity_t")
        is IntType -> BasicType("int32_t")
        is FloatType -> BasicType("float")
        is StringType -> PointerType(BasicType("char"))
        is CharType -> BasicType("char")
        is UnitType -> BasicType("void")
        else -> TODO()
    }
}
data class ComponentDefinition(val label: String, val value: String)
data class ComponentLowered(val type: TypeLowered, val definitions: List<ComponentDefinition>)
data class FnArgLowered(val name: String, val type: TypeLowered)
data class FnLowered(val name: String, val returnType: TypeLowered, val args: List<FnArgLowered>, val body: List<Lowered>)
sealed interface Lowered
data class ReturnLowered(val name: String): Lowered
data class FnCallLowered(val name: String, val type: TypeLowered, val fnName: String, val args: List<String>): Lowered
data class ComponentConstructorLowered(val name: String, val type: TypeLowered, val definitions: List<ComponentDefinition>): Lowered
data class ComponentSelectionLowered(val name: String, val type: TypeLowered, val component: String, val label: String): Lowered
data class GetComponentLowered(val name: String, val type: TypeLowered, val entity: String): Lowered
data class SetComponentLowered(val entity: String, val component: TypeLowered, val value: String): Lowered
data class NewEntityLowered(val name: String, val type: TypeLowered): Lowered
fun lower(fn: FunctionMfir): FnLowered {
    return FnLowered(fn.name,
                     lower(fn.returnType),
                     fn.args.map { FnArgLowered(it.name, lower(it.type))},
                     fn.body.flatMap { lower(it) })
}
fun lower(componentMfir: ComponentMfir): ComponentLowered {
    return ComponentLowered(lower(componentMfir.type), componentMfir.type.labels.map { ComponentDefinition(it.key, lower(it.value).toString()) })
}
fun lower(body: BodyMfir): List<Lowered> {
    return when(body) {
        is ReturnIr -> listOf(ReturnLowered(body.name))
        is DefinitionIr -> emptyList()
        is FunctionCallIr -> listOf(FnCallLowered(body.name, lower(body.type), body.fn.name, body.args.map { it.name }))
        is ComponentConstructorIr -> listOf(ComponentConstructorLowered(body.name, lower(body.type), body.arguments.map { ComponentDefinition(it.name, it.name)/*todo hmm*/ }))
        is ComponentSelectionIr -> listOf(ComponentSelectionLowered(body.name, lower(body.type), body.component.name, body.label))
        is EntityDefinitionIr -> {
            val lowered = mutableListOf<Lowered>()
            lowered.add(NewEntityLowered(body.name, lower(body.type)))
            val processedComponents = mutableSetOf<String>()
            var i = 0
            for(def in body.expressions) {
                if(def.type.kind is ComponentKind) {
                    val defType = lower(def.type)
                    if(!processedComponents.contains(defType.toString())) {
                        processedComponents.add(defType.toString())
                        lowered.add(SetComponentLowered(body.name, defType, def.name))
                    }
                } else {
                    for(component in (def.type as ClosedEntityType).components) {
                        val defType = lower(component)
                        if(processedComponents.contains(defType.toString())) continue
                        processedComponents.add(defType.toString())
                        val defName = "${def.name}_$i"
                        i++
                        lowered.add(GetComponentLowered(defName, defType, def.name))
                        lowered.add(SetComponentLowered(body.name, defType, defName))
                    }
                }
            }
            lowered
        }
        is EntityRestrictionIr -> {
            val lowered = mutableListOf<Lowered>()
            lowered.add(NewEntityLowered(body.name, lower(body.type)))
            var i = 0
            for(component in (body.entity.type as ClosedEntityType).components) {
                if(component != body.component) {
                    val defType = lower(component)
                    val defName = "${body.name}_$i"
                    i++
                    lowered.add(GetComponentLowered(defName, defType, body.entity.name))
                    lowered.add(SetComponentLowered(body.name, defType, defName))
                }
            }
            lowered
        }
        is EntitySelectionIr -> listOf(GetComponentLowered(body.name, lower(body.type), body.entity.name))
        is IntConstantIr -> emptyList()
        is FloatConstantIr -> emptyList()
        is StringConstantIr -> emptyList()
        is CharConstantIr -> emptyList()
    }
}

fun generateCode(fn: FnLowered) {
    println("${fn.returnType} ${fn.name}(${fn.args.map { "${it.type} ${it.name}" }.joinToString(", ")}) {")
    for(body in fn.body) generateCode(body, 1)
    println("}")
}

fun generateCode(component: ComponentLowered) {
    println("typedef struct {")
    for(def in component.definitions) {
        println("  ${def.value} ${def.label};")
    }
    println("} ${component.type};")
}
fun generateCode(lowered: Lowered, depth: Int) {
    print("  ".repeat(depth))
    when(lowered) {
        is ReturnLowered -> println("return ${lowered.name};")
        is FnCallLowered -> println("${lowered.type} ${lowered.name} = ${lowered.fnName}(${lowered.args.joinToString(", ")});")
        is ComponentSelectionLowered -> println("${lowered.type} ${lowered.name} = ${lowered.component}->${lowered.label};")
        is GetComponentLowered -> println("${lowered.type} ${lowered.name} = ecs_get(world, ${lowered.entity}, ${lowered.type});")
        is SetComponentLowered -> println("ecs_set_id(world, ${lowered.entity}, ecs_id(${lowered.component}), sizeof(${lowered.component}), &${lowered.value});")
        is NewEntityLowered -> println("${lowered.type} ${lowered.name} = ecs_new_id(world);")
        is ComponentConstructorLowered -> {
            println("${lowered.type} ${lowered.name} = (${lowered.type}) {")
            for(def in lowered.definitions) {
                print("  ".repeat(depth + 1))
                println(".${def.label} = ${def.value},")
            }
            print("  ".repeat(depth))
            println("};")
        }
    }
}
