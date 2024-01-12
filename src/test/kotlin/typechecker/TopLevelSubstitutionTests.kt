package typechecker

import parser.*
import kotlin.test.*

class TopLevelSubstitutionTests {
    @Test
    fun test001() {
        val s = """fn id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val typeVar = idType.boundVariables.first()
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test002() {
        val s = """fn foo(x) {
                  |return id(x)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(fooType.boundVariables.size, 2)

        val arrowType = fooType.type
        assertIs<ArrowType>(arrowType)
        val arrowLhs = arrowType.lhs
        val arrowRhs = arrowType.rhs
        assertIs<UnificationTypeVariable>(arrowLhs)
        assertIs<UnificationTypeVariable>(arrowRhs)

        val typeVariables = Array<Type?>(2) { null }
        for(typeVar in fooType.boundVariables) {
            if(typeVar == arrowLhs && typeVariables[0] == null) typeVariables[0] = arrowLhs
            else if(typeVar == arrowRhs && typeVariables[1] == null) typeVariables[1] = arrowRhs
            else assert(false)
        }

        val idTypes = environment.requirements[NameToken("id")]
        assertIs<List<Type>>(idTypes)
        assertEquals(idTypes.size, 1)
        val idType = environment.substitution.apply(idTypes.first())
        assertIs<ArrowType>(idType)
        assertEquals(typeVariables[0], idType.lhs)
        assertEquals(typeVariables[1], idType.rhs)
    }

    @Test
    fun test003() {
        val s = """fn foo(x) {
                  |if(x) {}
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<BooleanType>(fooType.lhs)
        assertIs<UnitType>(fooType.rhs)
    }

    @Test
    fun test004() {
        val s = """fn foo(x) {
                  |if(x == 0) {}
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        val arrowType = fooType.type
        assertIs<ArrowType>(arrowType)
        assertIs<UnificationTypeVariable>(arrowType.lhs)
        assertIs<UnitType>(arrowType.rhs)

        val equalsTypes = environment.requirements[OperatorToken("==")]
        assertIs<List<Type>>(equalsTypes)
        assertEquals(equalsTypes.size, 1)
        val equalsType = environment.substitution.apply(equalsTypes.first())
        assertIs<ArrowType>(equalsType)
        val lhsType = equalsType.lhs
        assertIs<ProductType>(lhsType)
        assertEquals(lhsType.types.size, 2)
        assertEquals(arrowType.lhs, lhsType.types[0])
        assertIs<I32Type>(lhsType.types[1])
        assertIs<BooleanType>(equalsType.rhs)
    }

    @Test
    fun test005() {
        val s = """fn foo(x) {
                  |if(x == 0) {}
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment =  TopLevelEnvironment.base  + fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<I32Type>(fooType.lhs)
        assertIs<UnitType>(fooType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test006() {
        val s = """fn fib(x) {
                  |if(x == 0) {
                  |return 0
                  |}
                  |if(x == 1) {
                  |return 1
                  |}
                  |return fib(x - 2) + fib(x - 1)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment =  TopLevelEnvironment.base  + fn.environment

        val fibType = environment.substitution.apply(environment.bindings[NameToken("fib")]!!)
        assertIs<ArrowType>(fibType)
        assertIs<I32Type>(fibType.lhs)
        assertIs<I32Type>(fibType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test007() {
        val s = """fn foo(x) {
                  |let z: U32 = x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment =  TopLevelEnvironment.base  + fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<U32Type>(fooType.lhs)
        assertIs<UnitType>(fooType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test008() {
        val s = """fn id(x: U32) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<ArrowType>(idType)
        assertIs<U32Type>(idType.lhs)
        assertIs<U32Type>(idType.rhs)
    }

    @Test
    fun test009() {
        val s = """fn foo(x: U32) {
                  |return id(x)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(fooType.boundVariables.size, 1)

        val arrowType = fooType.type
        assertIs<ArrowType>(arrowType)
        assertIs<U32Type>(arrowType.lhs)
        assertIs<UnificationTypeVariable>(arrowType.rhs)

        val idTypes = environment.requirements[NameToken("id")]
        assertIs<List<Type>>(idTypes)
        assertEquals(idTypes.size, 1)
        val idType = environment.substitution.apply(idTypes.first())
        assertIs<ArrowType>(idType)
        assertIs<U32Type>(idType.lhs)
        assertEquals(arrowType.rhs, idType.rhs)
    }

    @Test
    fun test010() {
        val s = """fn id(x): U32 {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<ArrowType>(idType)
        assertIs<U32Type>(idType.lhs)
        assertIs<U32Type>(idType.rhs)
    }

    @Test
    fun test011() {
        val s = """fn foo(x): U32 {
                  |return id(x)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(fooType.boundVariables.size, 1)

        val arrowType = fooType.type
        assertIs<ArrowType>(arrowType)
        assertIs<UnificationTypeVariable>(arrowType.lhs)
        assertIs<U32Type>(arrowType.rhs)

        val idTypes = environment.requirements[NameToken("id")]
        assertIs<List<Type>>(idTypes)
        assertEquals(idTypes.size, 1)
        val idType = environment.substitution.apply(idTypes.first())
        assertIs<ArrowType>(idType)
        assertEquals(arrowType.lhs, idType.lhs)
        assertIs<U32Type>(idType.rhs)
    }

    @Test
    fun test012() {
        val s = """fn foo() {
                  |let x = f(1)
                  |let y = f(1.1)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<UnitType>(fooType.rhs)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(2, fTypes.size)
        for(type in fTypes) {
            val applied = environment.substitution.apply(type)
            assertIs<ArrowType>(applied)
            assertTrue { applied.lhs is I32Type || applied.lhs is F32Type }
            assertIs<UnificationTypeVariable>(applied.rhs)
        }
    }

    @Test
    fun test013() {
        val s = """fn id(a) {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val typeVar = idType.boundVariables.first()
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test014() {
        val s = """fn id(a) {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return y
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val typeVar = idType.boundVariables.first()
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test015() {
        val s = """fn id(a) {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return z
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val typeVar = idType.boundVariables.first()
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test016() {
        val s = """fn id(a) {
                         |let x = a
                         |let y: I32 = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<ArrowType>(idType)
        assertEquals(ArrowType(I32Type, I32Type), idType)
    }

    @Test
    fun test017() {
        val s = """fn id(a: I32) {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<ArrowType>(idType)
        assertEquals(ArrowType(I32Type, I32Type), idType)
    }

    @Test
    fun test018() {
        val s = """fn id(a): I32 {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<ArrowType>(idType)
        assertEquals(ArrowType(I32Type, I32Type), idType)
    }

    @Test
    fun test019() {
        val s = """fn id(a: t) {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(1, idType.boundVariables.size)
        val typeVar = idType.boundVariables.first()
        assertIs<UserDefinedTypeVariable>(typeVar)
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test020() {
        val s = """fn id(a): t {
                         |let x = a
                         |let y = a
                         |let z = a
                         |return x
                         |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val fn = parseFn(builder)
        val environment = fn.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(1, idType.boundVariables.size)
        val typeVar = idType.boundVariables.first()
        assertIs<UserDefinedTypeVariable>(typeVar)
        assertEquals(ArrowType(typeVar, typeVar), idType.type)
    }

    @Test
    fun test021() {
        val s = """fn foo() {}
            |fn id""".trimMargin()

        val iterator = ParserIterator()
        iterator.add(s)
        val fn = parseProgram(iterator)
        println()
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