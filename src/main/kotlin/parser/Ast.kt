package parser

import tokenizer.*
import typechecker.*

sealed class Ast {
    var _type: Type? = null
    val type: Type
        get() = _type!!

    fun updateType(substitutions: Map<TypeVariable, Type>) {
        val t = _type
        if(t != null) _type = applySubstitutions(t, substitutions)
    }
}

sealed class Program: Ast()
data class Fn(val name: Name, val parameters: List<Name>, val body: List<FnBody>): Program()
data class ComponentValue(val name: Name, val type: Name)
data class Component(val name: Name, val valueDefs: List<ComponentValue>): Program()

sealed class FnBody: Ast()
data class Let(val name: Name, val expression: Expression): FnBody()
data class Return(val expression: Expression): FnBody()

sealed class Expression: Ast()
data class Name(val name: NameToken): Expression()
data class FnCall(val fn: Expression, val arguments: List<Expression>): Expression()

data class ComponentSelection(val component: Expression, val label: Name): Expression()

data class EntityDefinition(val expressions: List<Expression>): Expression()
data class EntityRestriction(val entity: Expression, val label: Name): Expression()
data class EntitySelection(val entity: Expression, val label: Name): Expression()

data class IntConstant(val value: IntToken): Expression() {
    init {
        _type = IntType
    }
}
data class FloatConstant(val value: FloatToken): Expression() {
    init {
        _type = FloatType
    }
}
data class StringConstant(val value: StringToken): Expression() {
    init {
        _type = StringType
    }
}
data class CharConstant(val value: CharToken): Expression() {
    init {
        _type = CharType
    }
}


fun printWithTypes(ast: Ast, depth: Int = 0) {
    when(ast) {
        is Component -> {
            print("\t".repeat(depth))
            print("component ${ast.name.name.value}(")
            when(ast.valueDefs.size) {
                0 -> {}
                1 -> print("${ast.valueDefs.first().name.name.value}: ${ast.valueDefs.first().type.name.value}")
                else -> {
                    for(value in ast.valueDefs.dropLast(1)) {
                        print("${value.name.name.value}: ${value.type.name.value}, ")
                    }
                    print("${ast.valueDefs.last().name.name.value}: ${ast.valueDefs.last().type.name.value}")
                }
            }
            println(")")
        }
        is Fn -> {
            print("\t".repeat(depth))
            print("fn ${ast.name.name.value}")
            val ts = ast.type
            val arrow: Type
            if(ts is TypeScheme) {
                var t = ts
                print("[")
                while(true) {
                    val tp = t
                    if(tp is TypeScheme) {
                        print("${tp.boundVariable}")
                        if(tp.body is TypeScheme) print(", ")
                        t = tp.body
                    } else break
                }
                print("]")
                arrow = t
            } else arrow = ts

            if(arrow !is Arrow) throw Exception()

            print("(")
            when(ast.parameters.size) {
                0 -> {}
                1 -> print("${ast.parameters.first().name.value}: ${arrow.lhs}")
                else -> {
                    var sum = arrow.lhs
                    for(name in ast.parameters.dropLast(1)) {
                        print("${name.name.value}: ${(sum as Sum).lhs}, ")
                        sum = (sum as Sum).rhs
                    }
                    print("${ast.parameters.last().name.value}: $sum")
                }
            }
            println("): ${arrow.rhs} {")

            for(body in ast.body) {
                printWithTypes(body, depth + 1)
            }
            println("}")
        }
        is Let -> {
            print("\t".repeat(depth))
            print("let ${ast.name.name.value}: ${ast.type} = ")
            printWithTypes(ast.expression, depth)
            println()
        }
        is Return -> {
            print("\t".repeat(depth))
            print("return ")
            printWithTypes(ast.expression, depth)
            println()
        }

        is FnCall -> {
            printWithTypes(ast.fn, depth)
            print("(")
            if(ast.arguments.any()) printWithTypes(ast.arguments.first(), depth)
            for(e in ast.arguments.drop(1)) {
                print(", ")
                printWithTypes(e, depth)
            }
            print(")")
        }

        is EntityDefinition -> {
            print("{")
            if(ast.expressions.any()) printWithTypes(ast.expressions.first(), depth)
            for(def in ast.expressions.drop(1)) {
                print(", ")
                printWithTypes(def, depth)
            }
            print("}")
        }

        is EntitySelection -> {
            printWithTypes(ast.entity, depth)
            print(".${ast.label.name.value}")
        }

        is EntityRestriction -> {
            printWithTypes(ast.entity, depth)
            print("\\${ast.label.name.value}")
        }

        is ComponentSelection -> {
            printWithTypes(ast.component, depth)
            print(".${ast.label.name.value}")
        }

        is Name -> print(ast.name.value)
        is IntConstant -> print(ast.value.value)
        is FloatConstant -> print(ast.value.value)
        is StringConstant -> print(ast.value.value)
        is CharConstant -> print(ast.value.value)
    }
}

fun updateTypes(ast: Ast, substitutions: Map<TypeVariable, Type>) {
    when(ast) {
        is Let -> {
            ast.updateType(substitutions)
            updateTypes(ast.expression, substitutions)
        }
        is Return -> {
            ast.updateType(substitutions)
            updateTypes(ast.expression, substitutions)
        }

        is FnCall -> {
            ast.updateType(substitutions)
            updateTypes(ast.fn, substitutions)
            for(e in ast.arguments) updateTypes(e, substitutions)
        }

        is EntityDefinition -> {
            ast.updateType(substitutions)
            for(e in ast.expressions) updateTypes(e, substitutions)
        }
        is EntityRestriction -> {
            ast.updateType(substitutions)
            updateTypes(ast.entity, substitutions)
        }
        is EntitySelection -> {
            ast.updateType(substitutions)
            updateTypes(ast.entity, substitutions)
        }

        is ComponentSelection -> {
            ast.updateType(substitutions)
            updateTypes(ast.component, substitutions)
        }

        is Name -> ast.updateType(substitutions)
    }
}