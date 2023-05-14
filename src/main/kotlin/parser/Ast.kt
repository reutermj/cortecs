package parser

import tokenizer.*
import typechecker.*

sealed class Ast {
    var _type: Type? = null
    val type: Type
        get() = _type!!

    fun updateType(substitutions: Map<Type, Type>) {
        val t = _type
        if(t != null) _type = applySubstitutions(t, substitutions)
    }
}

sealed class ProgramAst: Ast()
data class FnAst(val name: NameToken, val parameters: List<NameToken>, val body: List<FnBodyAst>): ProgramAst()
data class ComponentValue(val name: NameToken, val typeName: NameToken)
data class ComponentAst(val name: NameToken, val valueDefs: List<ComponentValue>): ProgramAst() {
    var _fnType: FunctionType? = null
}

sealed class FnBodyAst: Ast()
data class LetAst(val name: NameToken, val expression: Expression): FnBodyAst()
data class ReturnAst(val expression: Expression): FnBodyAst()

sealed class Expression: Ast()
data class NameAst(val name: NameToken): Expression()
data class FnCallAst(val fn: Expression, val arguments: List<Expression>): Expression()

data class ComponentSelectionAst(val component: Expression, val label: NameToken): Expression()

data class EntityDefinitionAst(val expressions: List<Expression>): Expression()
data class EntityRestrictionAst(val entity: Expression, val label: NameToken): Expression()
data class EntitySelectionAst(val entity: Expression, val label: NameToken): Expression()

data class IntConstantAst(val value: IntToken): Expression() {
    init {
        _type = IntType
    }
}
data class FloatConstantAst(val value: FloatToken): Expression() {
    init {
        _type = FloatType
    }
}
data class StringConstantAst(val value: StringToken): Expression() {
    init {
        _type = StringType
    }
}
data class CharConstantAst(val value: CharToken): Expression() {
    init {
        _type = CharType
    }
}


fun printWithTypes(ast: Ast, depth: Int = 0) {
    when(ast) {
        is ComponentAst -> {
            print("\t".repeat(depth))
            print("component ${ast.name.value}(")
            when(ast.valueDefs.size) {
                0 -> {}
                1 -> print("${ast.valueDefs.first().name.value}: ${ast.valueDefs.first().typeName.value}")
                else -> {
                    for(value in ast.valueDefs.dropLast(1)) {
                        print("${value.name.value}: ${value.typeName.value}, ")
                    }
                    print("${ast.valueDefs.last().name.value}: ${ast.valueDefs.last().typeName.value}")
                }
            }
            println(")")
        }
        is FnAst -> {
            print("\t".repeat(depth))
            print("fn ${ast.name.value}")
            val ts = ast.type
            val arrow =
                if(ts is TypeScheme) {
                    print(ts.boundVariables.joinToString(", ", "[", "]"))
                    ts.body
                } else ts
            if(arrow !is FunctionType) throw Exception()

            print("(")
            when(ast.parameters.size) {
                0 -> {}
                1 -> print("${ast.parameters.first().value}: ${arrow.lhs}")
                else -> print(ast.parameters.zip((arrow.lhs as SumType).types) { name, type -> "$name: $type" }.joinToString(", "))
            }
            println("): ${arrow.rhs} {")

            for(body in ast.body) {
                printWithTypes(body, depth + 1)
            }
            println("}")
        }
        is LetAst -> {
            print("\t".repeat(depth))
            print("let ${ast.name.value}: ${ast.type} = ")
            printWithTypes(ast.expression, depth)
            println()
        }
        is ReturnAst -> {
            print("\t".repeat(depth))
            print("return ")
            printWithTypes(ast.expression, depth)
            println()
        }

        is FnCallAst -> {
            printWithTypes(ast.fn, depth)
            print("(")
            if(ast.arguments.any()) printWithTypes(ast.arguments.first(), depth)
            for(e in ast.arguments.drop(1)) {
                print(", ")
                printWithTypes(e, depth)
            }
            print(")")
        }

        is EntityDefinitionAst -> {
            print("{")
            if(ast.expressions.any()) printWithTypes(ast.expressions.first(), depth)
            for(def in ast.expressions.drop(1)) {
                print(", ")
                printWithTypes(def, depth)
            }
            print("}")
        }

        is EntitySelectionAst -> {
            printWithTypes(ast.entity, depth)
            print(".${ast.label.value}")
        }

        is EntityRestrictionAst -> {
            printWithTypes(ast.entity, depth)
            print("\\${ast.label.value}")
        }

        is ComponentSelectionAst -> {
            printWithTypes(ast.component, depth)
            print(".${ast.label.value}")
        }

        is NameAst -> print(ast.name.value)
        is IntConstantAst -> print(ast.value.value)
        is FloatConstantAst -> print(ast.value.value)
        is StringConstantAst -> print(ast.value.value)
        is CharConstantAst -> print(ast.value.value)
    }
}


fun updateTypes(ast: Ast, substitutions: Map<Type, Type>) {
    when(ast) {
        is LetAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.expression, substitutions)
        }
        is ReturnAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.expression, substitutions)
        }

        is FnCallAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.fn, substitutions)
            for(e in ast.arguments) updateTypes(e, substitutions)
        }

        is EntityDefinitionAst -> {
            ast.updateType(substitutions)
            for(e in ast.expressions) updateTypes(e, substitutions)
        }
        is EntityRestrictionAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.entity, substitutions)
        }
        is EntitySelectionAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.entity, substitutions)
        }

        is ComponentSelectionAst -> {
            ast.updateType(substitutions)
            updateTypes(ast.component, substitutions)
        }

        is NameAst -> ast.updateType(substitutions)
    }
}