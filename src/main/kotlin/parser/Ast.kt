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

sealed class FnBody: Ast()
data class Let(val name: Name, val expression: Expression): FnBody()
data class Return(val expression: Expression): FnBody()

sealed class Expression: Ast()
data class Name(val name: NameToken): Expression()
data class FnCall(val fn: Expression, val arguments: List<Expression>): Expression()

data class RecordSelection(val record: Expression, val label: NameToken): Expression()
data class RecordLabel(val label: NameToken, val expression: Expression): Expression()
data class RecordDefinition(val labels: List<RecordLabel>, val row: Expression?): Expression()
data class RecordRestriction(val record: Expression, val label: NameToken): Expression()

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
                1 -> print("${ast.parameters.first()}: ${arrow.lhs}")
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
                printWithTypes(e, depth)
            }
            print(")")
        }

        is RecordSelection -> {
            printWithTypes(ast.record, depth)
            print(".${ast.label.value}")
        }
        is RecordLabel -> {
            print("${ast.label.value}: ${ast.expression.type} = ")
            printWithTypes(ast.expression, depth)
        }
        is RecordDefinition -> {
            print("{")
            if(ast.labels.any()) printWithTypes(ast.labels.first(), depth)
            for(def in ast.labels.drop(1)) {
                print(", ")
                printWithTypes(def, depth)
            }
            if(ast.row != null) {
                if(ast.labels.any()) print(", ")
                printWithTypes(ast.row, depth)
            }
            print("}")
        }
        is RecordRestriction -> {
            printWithTypes(ast.record, depth)
            print("\\${ast.label}")
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
        is RecordSelection -> {
            ast.updateType(substitutions)
            updateTypes(ast.record, substitutions)
        }
        is RecordRestriction -> {
            ast.updateType(substitutions)
            updateTypes(ast.record, substitutions)
        }
        is RecordLabel -> updateTypes(ast.expression, substitutions)
        is RecordDefinition -> {
            ast.updateType(substitutions)
            for(l in ast.labels) updateTypes(l, substitutions)
            if(ast.row != null) updateTypes(ast.row, substitutions)
        }

        is Name -> ast.updateType(substitutions)
    }
}