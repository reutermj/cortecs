package typechecker

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.plus
import parser.*
import kotlin.test.*

class ExpressionSubstitutionTests {
//    @Test
//    fun testSerialization001() {
//        val format = Json { allowStructuredMapKeys = true
//            serializersModule = atomicExpressionTokenModule + typeAnnotationTokenModule + bodyKeywordModule +
//                    topLevelKeywordModule + keywordModule + bindableTokenModule + tokenModule }
//        val data = NameToken("helloworld")
//        assertEquals("{\"value\":\"helloworld\"}", format.encodeToString(data))
//    }
//
//    @Test
//    fun testSerialization002() {
//        val format = Json { allowStructuredMapKeys = true
//            serializersModule = atomicExpressionTokenModule + typeAnnotationTokenModule + bodyKeywordModule +
//                    topLevelKeywordModule + keywordModule + bindableTokenModule + tokenModule }
//        val data: Token = NameToken("helloworld")
//        assertEquals("{\"type\":\"parser.NameToken\",\"value\":\"helloworld\"}", format.encodeToString(data))
//    }
//
//    @Test
//    fun testSerialization003() {
//        val format = Json { allowStructuredMapKeys = true
//            serializersModule = atomicExpressionTokenModule + typeAnnotationTokenModule + bodyKeywordModule +
//                    topLevelKeywordModule + keywordModule + bindableTokenModule + tokenModule }
//
//        val s = "x + x"
//        val iterator = ParserIterator()
//        iterator.add(s)
//        val builder = SequenceBuilder(iterator)
//        val expression = parseExpression(builder)!!
//
//        assertEquals("{\"type\":\"parser.BinaryExpression\",\"nodes\":[{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}},{\"type\":\"parser.OperatorToken\",\"value\":\"+\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"},{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}}],\"lhs\":{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}},\"op\":{\"value\":\"+\"},\"rhs\":{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}}}", format.encodeToString(expression))
//    }
//
//    @Test
//    fun testSerialization004() {
//        val format = Json { allowStructuredMapKeys = true
//            serializersModule = atomicExpressionTokenModule + typeAnnotationTokenModule + bodyKeywordModule +
//                    topLevelKeywordModule + keywordModule + bindableTokenModule + tokenModule }
//
//        val s = "x + x"
//        val iterator = ParserIterator()
//        iterator.add(s)
//        val builder = SequenceBuilder(iterator)
//        val expression: Ast = parseExpression(builder)!!
//
//        assertEquals("{\"type\":\"parser.BinaryExpression\",\"nodes\":[{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}},{\"type\":\"parser.OperatorToken\",\"value\":\"+\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"},{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}}],\"lhs\":{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"},{\"type\":\"parser.WhitespaceToken\",\"value\":\" \"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}},\"op\":{\"value\":\"+\"},\"rhs\":{\"type\":\"parser.AtomicExpression\",\"nodes\":[{\"type\":\"parser.NameToken\",\"value\":\"x\"}],\"atom\":{\"type\":\"parser.NameToken\",\"value\":\"x\"}}}", format.encodeToString(expression))
//    }

    @Test
    fun test001() {
        val s = "x"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())
        assertEquals(type, xType)
    }

    @Test
    fun test002() {
        val s = "x + x"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(2, xTypes.size)
        assertNotEquals(xTypes[0], xTypes[1])

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertNotEquals(plusLhs.types[0], plusLhs.types[1])
        for(argType in plusLhs.types) assertContains(xTypes, argType)
    }

    @Test
    fun test003() {
        val s = "x + y"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertEquals(yType, plusLhs.types[1])
    }

    @Test
    fun test004() {
        val s = "+x"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertEquals(xType, plusLhs)
    }

    @Test
    fun test005() {
        val s = "f(x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertEquals(xType, fLhs)
    }

    @Test
    fun test006() {
        val s = "f(x, x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(2, xTypes.size)
        assertNotEquals(xTypes[0], xTypes[1])

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(2, fLhs.types.size)
        assertNotEquals(fLhs.types[0], fLhs.types[1])
        for(argType in fLhs.types) assertContains(xTypes, argType)
    }

    @Test
    fun test007() {
        val s = "f(x, x, x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(3, xTypes.size)
        assertNotEquals(xTypes[0], xTypes[1])
        assertNotEquals(xTypes[0], xTypes[2])
        assertNotEquals(xTypes[1], xTypes[2])

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(3, fLhs.types.size)
        assertNotEquals(fLhs.types[0], fLhs.types[1])
        assertNotEquals(fLhs.types[0], fLhs.types[2])
        assertNotEquals(fLhs.types[1], fLhs.types[2])
        for(argType in fLhs.types) assertContains(xTypes, argType)
    }

    @Test
    fun test008() {
        val s = "f(x, y)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(2, fLhs.types.size)
        assertEquals(xType, fLhs.types[0])
        assertEquals(yType, fLhs.types[1])
    }

    @Test
    fun test009() {
        val s = "f(x, y, z)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(3, fLhs.types.size)
        assertEquals(xType, fLhs.types[0])
        assertEquals(yType, fLhs.types[1])
        assertEquals(zType, fLhs.types[2])
    }

    @Test
    fun test010() {
        val s = "f(+x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        val plusLhs = plusType.lhs
        assertEquals(xType, plusLhs)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertEquals(plusRhs, fLhs)
    }

    @Test
    fun test011() {
        val s = "f(x + x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(2, xTypes.size)
        assertNotEquals(xTypes[0], xTypes[1])

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertNotEquals(plusLhs.types[0], plusLhs.types[1])
        for(argType in plusLhs.types) assertContains(xTypes, argType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertEquals(plusRhs, fLhs)
    }

    @Test
    fun test012() {
        val s = "f(x + y)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertEquals(yType, plusLhs.types[1])

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertEquals(plusRhs, fLhs)
    }

    @Test
    fun test013() {
        val s = "f(x + y, z)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertEquals(yType, plusLhs.types[1])

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(plusRhs, fLhs.types[0])
        assertEquals(zType, fLhs.types[1])
    }

    @Test
    fun test014() {
        val s = "f(x + y, z + w)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val yTypes = environment.requirements[NameToken("y")]
        assertIs<List<Type>>(yTypes)
        assertEquals(1, yTypes.size)
        val yType = environment.substitution.apply(yTypes.first())

        val zTypes = environment.requirements[NameToken("z")]
        assertIs<List<Type>>(zTypes)
        assertEquals(1, zTypes.size)
        val zType = environment.substitution.apply(zTypes.first())

        val wTypes = environment.requirements[NameToken("w")]
        assertIs<List<Type>>(wTypes)
        assertEquals(1, wTypes.size)
        val wType = environment.substitution.apply(wTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(2, plusTypes.size)
        val plusReturnTypes = Array<Type?>(2) { null }
        for(plusType in plusTypes) {
            assertIs<ArrowType>(plusType)
            val plusRhs = plusType.rhs
            val plusLhs = plusType.lhs
            assertIs<ProductType>(plusLhs)
            assertEquals(2, plusLhs.types.size)
            when(plusLhs.types[0]) {
                xType -> {
                    plusReturnTypes[0] = plusRhs
                    assertEquals(yType, plusLhs.types[1])
                }
                zType -> {
                    plusReturnTypes[1] = plusRhs
                    assertEquals(wType, plusLhs.types[1])
                }
                else -> assert(false)
            }
        }

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(plusReturnTypes[0], fLhs.types[0])
        assertEquals(plusReturnTypes[1], fLhs.types[1])
    }

    @Test
    fun test015() {
        val s = "f(x + x, x + x)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(4, xTypes.size)

        val plusRetTypes = mutableSetOf<Type>()

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(2, plusTypes.size)
        for(plusType in plusTypes) {
            assertIs<ArrowType>(plusType)
            val plusLhs = plusType.lhs
            assertIs<ProductType>(plusLhs)
            assertEquals(2, plusLhs.types.size)
            assertContains(xTypes, plusLhs.types[0])
            assertContains(xTypes, plusLhs.types[1])
            plusRetTypes.add(plusType.rhs)
        }

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertContains(plusRetTypes, fLhs.types[0])
        assertContains(plusRetTypes, fLhs.types[1])
    }

    @Test
    fun test016() {
        val s = "1b"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<I8Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test017() {
        val s = "1s"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<I16Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test018() {
        val s = "1"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<I32Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test019() {
        val s = "1l"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<I64Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test020() {
        val s = "1ub"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<U8Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test021() {
        val s = "1us"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<U16Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test022() {
        val s = "1u"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<U32Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test023() {
        val s = "1ul"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<U64Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test024() {
        val s = "1.0"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<F32Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test025() {
        val s = "1.0d"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        assertIs<F64Type>(type)
        assertEquals(Substitution.empty, environment.substitution)
        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test026() {
        val s = "+1"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<I32Type>(plusLhs)
    }

    @Test
    fun test027() {
        val s = "x + 1"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertEquals(xType, plusLhs.types[0])
        assertIs<I32Type>(plusLhs.types[1])
    }

    @Test
    fun test028() {
        val s = "1 + x"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val xTypes = environment.requirements[NameToken("x")]
        assertIs<List<Type>>(xTypes)
        assertEquals(1, xTypes.size)
        val xType = environment.substitution.apply(xTypes.first())

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertIs<I32Type>(plusLhs.types[0])
        assertEquals(xType, plusLhs.types[1])
    }

    @Test
    fun test029() {
        val s = "1 + 1"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertIs<I32Type>(plusLhs.types[0])
        assertIs<I32Type>(plusLhs.types[1])
    }

    @Test
    fun test030() {
        val s = "1 + 1.0"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val plusTypes = environment.requirements[OperatorToken("+")]
        assertIs<List<Type>>(plusTypes)
        assertEquals(1, plusTypes.size)
        val plusType = environment.substitution.apply(plusTypes.first())
        assertIs<ArrowType>(plusType)
        val plusRhs = plusType.rhs
        assertEquals(type, plusRhs)
        val plusLhs = plusType.lhs
        assertIs<ProductType>(plusLhs)
        assertEquals(2, plusLhs.types.size)
        assertIs<I32Type>(plusLhs.types[0])
        assertIs<F32Type>(plusLhs.types[1])
    }

    @Test
    fun test031() {
        val s = "f(1)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<I32Type>(fLhs)
    }

    @Test
    fun test032() {
        val s = "f(1, 1)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(2, fLhs.types.size)
        assertIs<I32Type>(fLhs.types[0])
        assertIs<I32Type>(fLhs.types[1])
    }

    @Test
    fun test033() {
        val s = "f(1, 1.0)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(2, fLhs.types.size)
        assertIs<I32Type>(fLhs.types[0])
        assertIs<F32Type>(fLhs.types[1])
    }

    @Test
    fun test034() {
        val s = "f(1, 1, 1)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(3, fLhs.types.size)
        assertIs<I32Type>(fLhs.types[0])
        assertIs<I32Type>(fLhs.types[1])
        assertIs<I32Type>(fLhs.types[2])
    }

    @Test
    fun test035() {
        val s = "f(1, 1.0, 1l)"
        val iterator = ParserIterator()
        iterator.add(s)
        val builder = SequenceBuilder(iterator)
        val expression = parseExpression(builder)!!
        val environment = expression.environment
        val type = environment.substitution.apply(expression.expressionType)

        val fTypes = environment.requirements[NameToken("f")]
        assertIs<List<Type>>(fTypes)
        assertEquals(1, fTypes.size)
        val fType = environment.substitution.apply(fTypes.first())
        assertIs<ArrowType>(fType)
        val fRhs = fType.rhs
        assertEquals(type, fRhs)
        val fLhs = fType.lhs
        assertIs<ProductType>(fLhs)
        assertEquals(3, fLhs.types.size)
        assertIs<I32Type>(fLhs.types[0])
        assertIs<F32Type>(fLhs.types[1])
        assertIs<I64Type>(fLhs.types[2])
    }

    @Test
    fun test036() {

    }

    @Test
    fun test037() {

    }

    @Test
    fun test038() {

    }

    @Test
    fun test039() {

    }

    @Test
    fun test040() {

    }
}