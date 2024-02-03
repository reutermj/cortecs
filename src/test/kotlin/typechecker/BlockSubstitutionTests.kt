package typechecker

import parser.*
import kotlin.test.*

class BlockSubstitutionTests {
    @Test
    fun test001() {
        val s = """let x = 1
                         |let y = x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        assertNull(environment.requirements[NameToken("x")])
        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<I32Type>(yType)
    }

    @Test
    fun test002() {
        val s = """let x = 1
                         |let y = +x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

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
        val s = """let x = 1
                         |let y = x + z""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

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
        assertIs<I32Type>(plusLhs.types[0])
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
        val s = """let x = 1
                         |let y = f(x)""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        assertIs<I32Type>(fType.lhs)
        val fRhs = fType.rhs
        val fRhsTypeVars = fRhs.freeTypeVariables.toList().sortedBy { it.n }

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(fRhsTypeVars, yType.boundVariables)
        assertEquals(fRhs, yType.type)
    }


    @Test
    fun test005() {
        val s = """let x = 1
                         |let y = f(x, z)""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(2, fLhs.types.size)
        assertIs<I32Type>(fLhs.types[0])
        assertEquals(zType, fLhs.types[1])
        val fRhs = fType.rhs
        val fRhsTypeVars = fRhs.freeTypeVariables.toList().sortedBy { it.n }

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(fRhsTypeVars, yType.boundVariables)
        assertEquals(fRhs, yType.type)
    }


    @Test
    fun test006() {
        val s = """let x = z
                         |let y = x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())
        val zTypeVars = zType.freeTypeVariables.toList().sortedBy { it.n }

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<TypeScheme>(xType)
        assertEquals(zTypeVars, xType.boundVariables)
        assertEquals(zType, xType.type)
        val xTypeVar = xType.boundVariables.first()
        assertIs<UnificationTypeVariable>(xTypeVar)

        assertEquals(1, xType.boundVariables.size)
        val xCompat = environment.substitution.find(xTypeVar)
        assertIs<Compatibility>(xCompat)
        assertEquals(1, xCompat.typeVars.size)
        val typeVar = environment.substitution.apply(xCompat.typeVars.first())

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertEquals(listOf(typeVar), yType.boundVariables)
        assertEquals(typeVar, yType.type)
    }

    @Test
    fun test007() {
        val s = """let z = 1
                         |let x = z
                         |let y = x""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val zType = environment.substitution.apply(environment.bindings[NameToken("z")]!!)
        assertIs<I32Type>(zType)

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<I32Type>(yType)
    }

    @Test
    fun test008() {
        val s = """let x = a
                         |let y = a
                         |let z = a""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val aTypes = environment.requirements[NameToken("a")]
        assertIs<List<Type>>(aTypes)

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<TypeScheme>(xType)
        assertContains(aTypes, xType.type)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<TypeScheme>(yType)
        assertContains(aTypes, yType.type)

        val zType = environment.substitution.apply(environment.bindings[NameToken("z")]!!)
        assertIs<TypeScheme>(zType)
        assertContains(aTypes, zType.type)
    }

    @Test
    fun test009() {
        val s = """let a = 1
                         |let x = a
                         |let y = a
                         |let z = a""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val aType = environment.substitution.apply(environment.bindings[NameToken("a")]!!)
        assertIs<I32Type>(aType)

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<I32Type>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<I32Type>(yType)

        val zType = environment.substitution.apply(environment.bindings[NameToken("z")]!!)
        assertIs<I32Type>(zType)
    }

    @Test
    fun test010() {
        val s = """let x: t = a
                         |let y: t = b
                         |let z: t = c""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val block = parseBlock(iterator)
        val environment = block.environment

        val xType = environment.substitution.apply(environment.bindings[NameToken("x")]!!)
        assertIs<UserDefinedTypeVariable>(xType)

        val yType = environment.substitution.apply(environment.bindings[NameToken("y")]!!)
        assertIs<UserDefinedTypeVariable>(yType)

        val zType = environment.substitution.apply(environment.bindings[NameToken("z")]!!)
        assertIs<UserDefinedTypeVariable>(zType)

        assertEquals(xType, yType)
        assertEquals(xType, zType)
        assertEquals(yType, zType)
    }

    @Test
    fun test011() {

    }

    @Test
    fun test012() {

    }

    @Test
    fun test013() {

    }

    @Test
    fun test014() {

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
}