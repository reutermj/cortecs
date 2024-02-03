package typechecker

import parser.*
import kotlin.test.*

class BodySubstitutionTests {
    @Test
    fun test001() {
        val s = "let y = x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())
        val xTypeVars = xType.freeTypeVariables.toList().sortedBy { it.n }

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(xTypeVars, yType.boundVariables)
        assertEquals(xType, yType.type)
    }

    @Test
    fun test002() {
        val s = "let y = +x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        assertEquals(xType, plusType.lhs)
        val plusRhs = plusType.rhs
        val plusRhsTypeVars = plusRhs.freeTypeVariables.toList().sortedBy { it.n }

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(plusRhsTypeVars, yType.boundVariables)
        assertEquals(plusRhs, yType.type)
    }

    @Test
    fun test003() {
        val s = "let y = x + z"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertEquals(zType, plusLhs.types[1])
        val plusRhs = plusType.rhs
        val plusRhsTypeVars = plusRhs.freeTypeVariables.toList().sortedBy { it.n }

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(plusRhsTypeVars, yType.boundVariables)
        assertEquals(plusRhs, yType.type)
    }

    @Test
    fun test004() {
        val s = "let y = 1"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<I32Type>(yType)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test005() {
        val s = "return 1"
        val iterator = ParserIterator()
        iterator.add(s)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment

        val returnTypes = environment.requirements[ReturnTypeToken]
        assertIs<List<Type>>(returnTypes)
        assertEquals(1, returnTypes.size)
        val returnType = returnTypes.first()
        assertIs<I32Type>(returnType)
    }

    @Test
    fun test006() {
        val s = "return x"
        val iterator = ParserIterator()
        iterator.add(s)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val returnTypes = environment.requirements[ReturnTypeToken]
        assertIs<List<Type>>(returnTypes)
        assertEquals(1, returnTypes.size)
        val returnType = returnTypes.first()
        assertEquals(xType, returnType)
    }

    @Test
    fun test007() {
        val s = "return +x"
        val iterator = ParserIterator()
        iterator.add(s)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        assertEquals(xType, plusType.lhs)

        val returnTypes = environment.requirements[ReturnTypeToken]
        assertIs<List<Type>>(returnTypes)
        assertEquals(1, returnTypes.size)
        val returnType = returnTypes.first()
        assertEquals(plusType.rhs, returnType)
    }

    @Test
    fun test008() {
        val s = "return x + z"
        val iterator = ParserIterator()
        iterator.add(s)
        val returnAst = parseReturn(iterator)
        val environment = returnAst.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertEquals(zType, plusLhs.types[1])

        val returnTypes = environment.requirements[ReturnTypeToken]
        assertIs<List<Type>>(returnTypes)
        assertEquals(1, returnTypes.size)
        val returnType = returnTypes.first()
        assertEquals(plusType.rhs, returnType)
    }

    @Test
    fun test009() {
        val s = "if(x) {}"
        val iterator = ParserIterator()
        iterator.add(s)
        val ifAst = parseIf(iterator)
        val environment = ifAst.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())
        assertIs<BooleanType>(xType)
    }

    @Test
    fun test010() {
        val s = "if(x == 1) {}"
        val iterator = ParserIterator()
        iterator.add(s)
        val ifAst = parseIf(iterator)
        val environment = ifAst.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val equalsTypes = environment.requirements[OperatorToken("==")]
        assertIs<List<Type>>(equalsTypes)
        assertEquals(1, equalsTypes.size)
        val equalsType = environment.substitution.apply(equalsTypes.first())
        assertIs<ArrowType>(equalsType)
        val equalsLhs = equalsType.lhs
        assertIs<ProductType>(equalsLhs)
        assertEquals(2, equalsLhs.types.size)
        assertEquals(xType, equalsLhs.types[0])
        assertIs<I32Type>(equalsLhs.types[1])
        assertIs<BooleanType>(equalsType.rhs)
    }

    @Test
    fun test011() {
        val s = "let y: U32 = x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())
        assertIs<U32Type>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<U32Type>(yType)
    }

    @Test
    fun test012() {
        val s = "let y: U32 = +x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        assertEquals(xType, plusType.lhs)
        assertIs<U32Type>(plusType.rhs)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<U32Type>(yType)
    }

    @Test
    fun test013() {
        val s = "let y: t = x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())
        assertIs<UserDefinedTypeVariable>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<UserDefinedTypeVariable>(yType)
        assertEquals(xType, yType)
    }

    @Test
    fun test014() {
        val s = "let y: t = +x"
        val iterator = ParserIterator()
        iterator.add(s)
        val let = parseLet(iterator)
        val environment = let.environment

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        assertEquals(xType, plusType.lhs)
        assertIs<UserDefinedTypeVariable>(plusType.rhs)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<UserDefinedTypeVariable>(yType)
        assertEquals(plusType.rhs, yType)
    }

    @Test
    fun test015() {

    }

    @Test
    fun test016() {

    }

    @Test
    fun test017() {

    }

    @Test
    fun test018() {

    }

    @Test
    fun test019() {
    }

    @Test
    fun test020() {
    }

    @Test
    fun test021() {
    }

    @Test
    fun test022() {

    }

    @Test
    fun test023() {

    }

    @Test
    fun test024() {

    }

    @Test
    fun test025() {

    }

    @Test
    fun test026() {

    }

    @Test
    fun test027() {

    }

    @Test
    fun test028() {

    }

    @Test
    fun test029() {

    }

    @Test
    fun test030() {

    }
}