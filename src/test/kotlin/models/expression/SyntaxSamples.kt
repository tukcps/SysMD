package models.expression

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.tokenOf
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.BinaryOperatorInformation
import com.github.tukcps.sysmd.model.kerml.FeatureTyping
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement
import com.github.tukcps.sysmd.model.kerml.UnresolvedType
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkOwnership
import org.junit.jupiter.api.Assertions.assertEquals
import org.opentest4j.AssertionFailedError
import util.assertNoIssues
import util.testSession
import kotlin.test.*


private fun InvocationExpression.assertArgs(vararg argChecks : Expression.() -> Unit)
{
	val argument = argument
	assertEquals(argChecks.size, argument.size) {
		"In `$astString`: Arity mismatch!"
	}

	for((arg, check) in argument zip argChecks)
		check(arg)
}

private fun Expression.operator(op : String, vararg argChecks : Expression.() -> Unit)
{
	val msg = "Got '${astString}'"
	assertIs<OperatorExpression>(this, msg)
	assertEquals(op, operator, msg)
	assertArgs(*argChecks)
}

private fun Expression.operator(op : String, vararg features : String)
= operator(op, *featureRefs(*features))

private fun Expression.typeOperator(op : String, expr : Expression.() -> Unit, type : Type.() -> Unit)
{
	val msg = "Got '${astString}'"
	assertIs<OperatorExpression>(this, msg)
	assertEquals(op, operator, msg)
	val p = parameter
	assertEquals(2, p.size)
	assertIs<Expression>(p[0]).apply(expr)
	assertIsNot<Expression>(p[1])
	assertIs<FeatureTyping>(p[1].ownedRelationship.single()).type.type()
}
private fun Expression.typeOperator(op : String, type : Type.() -> Unit)
{
	val msg = "Got '${astString}'"
	assertIs<OperatorExpression>(this, msg)
	assertEquals(op, operator, msg)
	val t = parameter.single()
	assertIsNot<Expression>(t)
	assertIs<FeatureTyping>(t.ownedRelationship.single()).type.type()
}

private fun Expression.typeOperator(op : String, feature : String, type : String) = typeOperator(op, {
	featureRef(feature)
}, {
	assertIs<UnresolvedType>(this)
	assertEquals(type, relativeName)
})
private fun Expression.typeOperator(op : String, type : String) = typeOperator(op) {
	assertIs<UnresolvedType>(this)
	assertEquals(type, relativeName)
}


private fun Expression.invocation(of : QualifiedName, vararg argChecks : Expression.() -> Unit)
{
	assertIs<InvocationExpression>(this)
	assertEquals(of, functionName)
	assertArgs(*argChecks)
}

private fun Expression.invocation(of : QualifiedName, vararg argChecks : Pair<String, Expression.() -> Unit>)
{
	assertIs<InvocationExpression>(this)
	assertEquals(of, functionName)
	val pars = parameterMembership

	for((entry, p) in argChecks zip pars)
	{
		val (name, check) = entry
		assertEquals(Identification(name), Identification(p.memberShortName, p.memberName))
		// this placeholder is inserted to signal that indices need to be recalculated once function is known
		assertEquals(-1, p.parameterIndex)
		check(assertIs<Expression>(p.ownedMemberParameter))
	}
}

private fun Expression.bodyExpression(vararg args : String, result : Expression.() -> Unit)
{
	assertIs<BodyExpression>(this)
	val arguments = this.arguments
	assertEquals(args.size, arguments.size)

	for((i,a) in arguments.withIndex())
		assertEquals(args[i], a.name, "argument #$i")

	assertNotNull(returnExpression, "no return expression").result()
}

private fun Expression.metadataAccess(of : QualifiedName)
{
	assertIs<MetadataAccessExpression>(this)
	assertIs<UnresolvedElement>(referencedElement).also {
		assertEquals(of, it.relativeName)
	}
}

private fun Expression.index(list : Expression.() -> Unit, index : Expression.() -> Unit)
{
	assertIs<IndexExpression>(this)
	operator("#", list, index)
}

private fun Expression.featureRef(feature : QualifiedName)
{
	assertIs<FeatureReferenceExpression>(this)
	// we cannot resolve here, data isn't loaded yet
	assertEquals(feature, astString)
}

private fun Expression.rawName(name : QualifiedName)
{
	assertIs<RawNameExpression>(this)
	assertEquals(name, rawName)
}

private inline fun Expression.featureChain(chain : String, op : Expression.() -> Unit)
{
	assertIs<FeatureChainExpression>(this)
	assertEquals(chain, targetFeature)
	assertEquals(1, argument.size, "FeatureChainExpression should be unary")
	assertNotNull(source).op()
}

private fun featureRefs(vararg names : String) : Array<Expression.() -> Unit>
= names.map { n ->
	{ x : Expression -> x.featureRef(n) }
}.toTypedArray()

private fun literals(vararg vals : Long) : Array<Expression.() -> Unit>
= vals.map { v ->
	{ x : Expression -> x.literal(v) }
}.toTypedArray()

private fun literals(vararg vals : Double) : Array<Expression.() -> Unit>
		= vals.map { v ->
	{ x : Expression -> x.literal(v) }
}.toTypedArray()

private fun Expression.literal(want : Boolean)
{
	assertIs<LiteralBoolean>(this)
	assertEquals(want, value)
}

private fun Expression.literal(want : Long)
{
	assertIs<LiteralInteger>(this)
	assertEquals(want, value)
}

private fun Expression.literal(want : Double)
{
	assertIs<LiteralRational>(this)
	assertEquals(want, assertNotNull(value), 1e-8)
}

private fun Expression.flatten(op : String = ",") : List<Expression>
= if(this is OperatorExpression && operator == op)
	argument.filter { it !is NullExpression }.flatMap { it.flatten(op) }
else
	listOf(this)

private fun Expression.sequence(vararg checkEntry : Expression.() -> Unit)
{
	val xs = flatten()
	assertEquals(checkEntry.size, xs.size, "Sequence size mismatch")

	for((x, check) in xs zip checkEntry)
		x.check()
}


/** Syntax samples from 7.4.9 */
class SyntaxSamples
{
	private inline fun parseTest(str : String, leaving : String? = null, body : Expression.() -> Unit) = testSession(runlevel = Runlevel.NONE,) {
		val parser = KerML(this)
		parser.input = str
		parser.semantics.initOwningNamespaces("")
		val expr = parser.Expression()
		assertNoIssues()

		when(leaving) {
			null -> assertEquals(EOF, parser.token.kind) {
				"Parser didn't consume input completely, left '${str.substring(parser.token.indices.first)}'"
			}
			else -> {
				assertNotEquals(EOF, parser.token.kind)
				assertEquals(leaving, str.substring(parser.token.indices.first))
			}
		}

		expr.body()
		addOwnedMember(expr, global)

		assertNoIssues()
		checkOwnership()
		assertNoIssues()

		// since features are left unresolved, this export causes errors
		/*val data = export().map { it.payloadElementSnapshot!! }

		testSession {
			settings.initialize = false
			import(data)
			assertProperClone(expr, repo.elements[expr.elementId!!] as Expression)
		}*/

		// creates dangling pointers
		assertProperClone(expr, expr.clone())
	}

	@Test
	fun lexerSupport() = testSession {
		val report = mutableListOf<String>()

		for(op in BinaryOperatorInformation.bySymbol.keys)
		{
			if(op == " ")
				continue // space operator has no special token

			val tk = tokenOf(op)

			if(tk === null)
				report.add("No token for '$op'")
			else if(op !in setOf("&", "|"))
			{
				val g = tk.toString().lowercase()

				if(op != g)
					report.add("Round trip operator mismatch; Got '$g' instead of '$op'")
			}
		}

		if(report.isNotEmpty())
			throw AssertionFailedError(report.joinToString("\n"))
	}

	@Test
	fun conditional1() = parseTest("if x >= 0? x else -x") {
		operator("if", {
			operator(">=", {
				featureRef("x")
			}, {
				literal(0)
			})
		}, {
			featureRef("x")
		}, {
			operator("-", "x")
		})
	}

	@Test
	fun conditional2() = parseTest("if x? true else false") {
		assertEquals("if x ? true else false", astString)
		operator("if", {
			featureRef("x")
		}, {
			literal(true)
		}, {
			literal(false)
		})
	}

	@Test
	fun conditional3() = parseTest("false or if x? true else false") {
		operator("or", {
			literal(false)
		}, {
			operator("if", {
				featureRef("x")
			}, {
				literal(true)
			}, {
				literal(false)
			})
		})
	}

	@Test
	fun conditional4() = parseTest("2 ^ if if y? false else true ? 1 + if z? 2 else 4 else (if q? a else b) * 3") {
		operator("^", {
			literal(2)
		}, {
			operator("if", {
				operator("if", { featureRef("y") }, { literal(false) }, { literal(true) })
			}, {
				operator("+", {
					literal(1)
				}, {
					operator("if", {
						featureRef("z")
					}, {
						literal(2)
					}, {
						literal(4)
					})
				})
			}, {
				operator("*", {
					operator("if", *featureRefs("q", "a", "b"))
				}, {
					literal(3)
				})
			})
		})
	}

	@Test
	fun binary1() = parseTest("x + y") {
		operator("+", "x", "y")
	}

	@Test
	fun binary2() = parseTest("list#(i) ?? fallback") {
		operator("??", {
			index({
				featureRef("list")
			}, {
				featureRef("i")
			})
		}, {
			featureRef("fallback")
		})
	}

	@Test
	fun binary3() = parseTest("sensor == null or sensor.reading > 0") {
		operator("or", {
			operator("==", {
				featureRef("sensor")
			}, {
				assertIs<NullExpression>(this)
			})
		}, {
			operator(">", {
				featureChain("reading", {
					featureRef("sensor")
				})
			}, {
				literal(0)
			})
		})
	}

	@Test
	fun binary4() = parseTest("currentPortion == tripPortion") {
		operator("==", "currentPortion", "tripPortion")
	}

	@Test
	fun binary5() = parseTest("currentPortion === tripPortion") {
		operator("===", "currentPortion", "tripPortion")
	}

	@Test
	fun unary1() = parseTest("-x") {
		operator("-", "x")
	}

	@Test
	fun unary2() = parseTest("not isOutOfRange(sensor)") {
		operator("not", {
			invocation("isOutOfRange", {
				featureRef("sensor")
			})
		})
	}

	@Test
	fun unary3() = parseTest("not completed") {
		operator("not", "completed")
	}

	@Test
	fun classification1() = parseTest("sensors istype ThermalSensor") {
		typeOperator("istype", "sensors", "ThermalSensor")
	}

	@Test
	fun classification2() = parseTest("sensors @ ThermalSensor") {
		typeOperator("@", "sensors", "ThermalSensor")
	}

	@Test
	fun classification3() = parseTest("person hastype Administrator") {
		typeOperator("hastype", "person", "Administrator")
	}

	@Test
	fun classification4() = parseTest("sensors as ThermalSensor") {
		typeOperator("as", "sensors", "ThermalSensor")
	}

	@Test
	fun classification5() = parseTest("istype ThermalSensor") {
		// standard could also allow the implicit argument to be inserted syntactically
		typeOperator("istype", "ThermalSensor")
	}

	@Test @Ignore // TODO: '@' is lexed as 'metadata'?
	fun classification6() = parseTest("@ThermalSensor") {
		typeOperator("@", "ThermalSensor")
	}

	@Test
	fun classification7() = parseTest("hastype Administrator") {
		typeOperator("hastype", "Administrator")
	}

	@Test
	fun classification8() = parseTest("as Supervisor") {
		typeOperator("as", "Supervisor")
	}


	// standard calls this operator a "shorthand"; unclear whether that means it should be expanded syntactically,
	// or implemented by operator semantics
	@Test
	fun metaclassification1() = parseTest("designModel @@ ApprovalAnnotation") {
		typeOperator("@@", "designModel", "ApprovalAnnotation")
	}

	@Test
	fun metaclassification2() = parseTest("designModel.metadata @ ApprovalAnnotation") {
		typeOperator("@", {
			metadataAccess("designModel")
		}, {
			assertIs<UnresolvedType>(this)
			assertEquals("ApprovalAnnotation", relativeName)
		})
	}

	@Test
	fun metaclassification3() = parseTest("sensors meta KerML::Feature") {
		typeOperator("meta", "sensors", "KerML::Feature")
	}

	@Test
	fun metaclassification4() = parseTest("sensors.metadata as KerML::Feature") {
		typeOperator("as", {
			metadataAccess("sensors")
		}, {
			assertIs<UnresolvedType>(this)
			assertEquals("KerML::Feature", relativeName)
		})
	}

	@Test @Ignore // TODO: lexer doesn't recognize 'meta' keyword
	fun metaclassification5() = parseTest("(sensors meta KerML::Feature).name") {
		featureChain("name") {
			typeOperator("meta", "sensors", "KerML::Feature")
		}
	}

	@Test
	fun extent() = parseTest("all Sensor") {
		typeOperator("all", "Sensor")
	}

	private fun Expression.assertPrecedenceExample() = operator("+", {
		operator("+", {
			operator("-", "w")
		}, {
			operator("*", {
				operator("*", "x", "y")
			}, {
				featureRef("z")
			})
		})
	}, {
		operator("^", {
			featureRef("a")
		}, {
			operator("^", "b", "c")
		})
	})

	@Test
	fun precedence1() = parseTest("-w + x * y * z + a ^ b ^ c") {
		assertPrecedenceExample()
	}

	@Test
	fun precedence2() = parseTest("( (-w) + ( (x * y) * z ) ) + ( a ^ (b ^ c) )") {
		assertPrecedenceExample()
	}

	@Test
	fun index1() = parseTest("sensors#(activeSensorIndex)") {
		index({
			featureRef("sensors")
		}, {
			featureRef("activeSensorIndex")
		})
	}

	@Test
	fun index2() = parseTest("detectorArray#(n, m)") {
		index({
			featureRef("detectorArray")
		}, {
			sequence(*featureRefs("n", "m"))
		})
	}

	// standard doesn't specify associativity of ',' (since it isn't a "BinaryOperator")
	// + flattening is implied to happen only at run-time,
	// so the exact order of these is highly

	@Test
	fun sequence1() = parseTest("(temperatureSensor, windSensor, precipitationSensor)") {
		sequence(*featureRefs("temperatureSensor", "windSensor", "precipitationSensor"))
	}
	@Test
	fun sequence2() = parseTest("( 1, 3, 5, 7, 11, 13, )") {
		sequence(*literals(1, 3, 5, 7, 11, 13))
	}

	@Test
	fun sequence3() = parseTest("((1, 2, 3), 4)") {
		sequence(*literals(1, 2, 3, 4))
	}

	@Test
	fun sequence4() = parseTest("(1, (2, 3), 4)") {
		sequence(*literals(1, 2, 3, 4))
	}

	@Test
	fun sequence5() = parseTest("(1, (), (2, 3, 4))") {
		sequence(*literals(1, 2, 3, 4))
	}

	@Test
	fun sequence6() = parseTest("(highValue + lowValue) / 2") {
		operator("/", {
			operator("+", "highValue", "lowValue")
		}, {
			literal(2)
		})
	}

	@Test
	fun sequence7() = parseTest("((((((()))))))") {
		assertIs<NullExpression>(this)
	}

	@Test
	fun featureChain1() = parseTest("getPlatform(id).sensors.isActive") {
		featureChain("sensors.isActive") {
			invocation("getPlatform", {
				featureRef("id")
			})
		}
	}

	@Test
	fun featureChain2() = parseTest("(getPlatform(id).sensors).isActive") {
		featureChain("isActive") {
			featureChain("sensors", {
				invocation("getPlatform", {
					featureRef("id")
				})
			})
		}
	}

	@Test @Ignore // TODO new expressions inside BodyExpression
	fun collect1() = parseTest("sensors.{ in s: Sensor; s.reading }") {
		println(astString)
		assertIs<CollectExpression>(this)
		operator("collect", {
			featureRef("sensors")
		}, {
			bodyExpression("s") {}
		})
	}

	@Test @Ignore // TODO new expressions inside BodyExpression
	fun select1() = parseTest("sensors.?{in s: Sensor; s.reading}") {
		println(astString)
		assertIs<SelectExpression>(this)
		operator("select", {
			featureRef("sensors")
		}, {
			bodyExpression("s") {
				featureRef("s")
			}
		})
	}


	@Test
	fun functionOperation1() = parseTest("sensors -> selectSensorsOver(limit) -> computeCriticalValue()") {
		invocation("computeCriticalValue", {
			invocation("selectSensorsOver", *featureRefs("sensors", "limit"))
		})
	}

	@Test @Ignore // TODO new expressions inside BodyExpression
	fun functionOperation2() = parseTest("sensors -> select {in s: Sensor; s::isActive}") {
		invocation("select", {
			featureRef("sensors")
		}, {
			bodyExpression() {}
		})
	}

	@Test @Ignore // TODO new expressions inside BodyExpression
	fun functionOperation3() = parseTest("members -> reject {in m: Member; not m->isInGoodStanding()}") {
		invocation("reject", {
			featureRef("members")
		}, {
			// TODO
		})
	}

	@Test
	fun functionOperation4() = parseTest("factors -> reduce {in x: Real; in y: Real; x * y}") {
		invocation("reduce", {
			featureRef("factors")
		}, {
			assertEquals("x * y", this.expression)
			// TODO: once OwnedExpression is updated to actually set ownership
			/*bodyExpression("x", "y") {
				operator("*", "x", "y")
			}*/
		})
	}

	@Test
	fun functionOperation5() = parseTest("factors -> reduce RealFunctions::'*'") {
		invocation("reduce", *featureRefs("factors", "RealFunctions::*"))
	}


	@Test
	fun null1() = parseTest("null") {
		assertIs<NullExpression>(this)
	}

	@Test
	fun null2() = parseTest("()") {
		assertIs<NullExpression>(this)
	}

	@Test
	fun invocation1() = parseTest("IntegerFunctions::'+'(i, j)") {
		// modulo quoting
		invocation("IntegerFunctions::+", *featureRefs("i", "j"))
	}

	@Test
	fun invocation2() = parseTest("isInGoodStanding(members#(n))") {
		invocation("isInGoodStanding", {
			index({
				featureRef("members")
			}, {
				featureRef("n")
			})
		})
	}

	@Test
	fun invocation3() = parseTest("AddMember(org, who)") {
		invocation("AddMember", *featureRefs("org", "who"))
	}

	@Test
	fun invocation4() = parseTest("AddMember(newMember = who, organization = org)") {
		invocation("AddMember",
			"newMember" to {
				featureRef("who")
			},
			"organization" to {
				featureRef("org")
			}
		)
	}

	@Test @Ignore // TODO
	fun invocation5() = parseTest("myStats.avg()") {}

	@Test @Ignore
	fun constructor1() = parseTest("new Member(\"Jane\", \"Doe\", 1234, null)") {
		// TODO
	}

	@Test @Ignore
	fun constructor2() = parseTest("new Member(\n" +
			"firstName = \"John\", lastName = \"Doe\", sponsor = thisMember,\n" +
			"memberNumber = thisMember.memberNumber + 1)") {
		// TODO
	}

	// standard gives "member" as an example of a simple name, but that is a reserved keyword...
	@Test
	fun featureRef1() = parseTest("members") { featureRef("members") }

	@Test
	fun featureRef2() = parseTest("spacecraft::mainAssembly::sensors") {
		featureRef("spacecraft::mainAssembly::sensors")
	}

	@Test
	fun featureRef3() = parseTest("sensor::isActive") { featureRef("sensor::isActive") }


	@Test @Ignore // TODO: new expressions inside BodyExpression
	fun body1() = parseTest("apply({in x; if x istype Integer? (x as Integer) + 1 else 0}, 1)") {
		invocation("apply", {
			// TODO
		}, {
			literal(1)
		})
	}

	@Test
	fun metadataAccess1() = parseTest("SecureSystem.metadata") {
		metadataAccess("SecureSystem")
	}


	@Test
	fun literal1() = parseTest("\"This is a string literal.\"") {
		assertIs<LiteralString>(this)
		assertEquals("This is a string literal.", value)
	}

	@Test
	fun literal2() = parseTest("0") { literal(0) }

	@Test
	fun literal3() = parseTest("1234") { literal(1234) }

	@Test
	fun literal4() = parseTest("3.14") {
		assertIs<LiteralRational>(this)
		assertTrue(assertNotNull(value) in 3.13999 .. 3.14001, value.toString())
	}

	@Test @Ignore // TODO: fix lexer
	fun literal5() = parseTest(".5") {
		assertIs<LiteralRational>(this)
		assertTrue(assertNotNull(value) in 0.4999 .. 0.5001, value.toString())
	}

	@Test
	fun literal6() = parseTest("2.5E-10") {
		assertIs<LiteralRational>(this)
		assertTrue(assertNotNull(value) in 2.4999E-10 .. 2.5001E-10, value.toString())
	}

	@Test
	fun literal7() = parseTest("1E+3") {
		assertIs<LiteralRational>(this)
		assertTrue(assertNotNull(value) in 999.99 .. 1000.01, value.toString())
	}

	@Test
	fun literal8() = parseTest("*") {
		assertIs<LiteralInfinity>(this)
	}

	@Test
	fun rangeWithUnits() = parseTest("8.25 .. 60 [kg / m]") {
		operator("..", {
			literal(8.25)
		}, {
			operator("[", {
				literal(60)
			}, {
				operator("/", {
					rawName("kg")
				}, {
					rawName("m")
				})
			})
		})
	}

	@Test
	fun brackets1() = parseTest("x[1]") {
		assertEquals("x[1]", astString)
		operator("[", {
			featureRef("x")
		}, {
			literal(1)
		})
	}

	@Test
	fun brackets2() = parseTest("x[1,2,3]") {
		assertEquals("x[1, 2, 3]", astString)
		operator("[", {
			featureRef("x")
		}, {
			sequence(*literals(1,2,3))
		})
	}


	// SysML Extensions
	@Test
	fun trailingDot() = parseTest("100.", ".") {
		literal(100)
	}

	@Test
	fun bracketedRange() = parseTest("[-2.0 .. 1.0]") {
		// bracket disappears? Implicit .. or custom LiteralRange subtype ?
		operator("..", *literals(-2.0, 1.0)) // parser generates negative literals
	}

	@Test
	fun inverseBrackets() = parseTest("[10.0 .. 100.0] kW") {
		// should this be parsed C-style? or with space operator?
		operator(" ", {
			operator("..", *literals(10.0, 100.0))
		}, {
			rawName("kW")
		})
	}

	@Test
	fun doubleBrackets() = parseTest("[10.0 .. 100.0] [kW]") {
		operator("[", {
			operator("..", *literals(10.0, 100.0))
		}, {
			rawName("kW")
		})
	}

	@Test
	fun literalWithUnit() = parseTest("1.0 m") {
		operator(" ", {
			literal(1.0)
		}, {
			rawName("m")
		})
	}

	@Test
	fun vectorWithUnit() = parseTest("(0.5,1.5) kg") {
		operator(" ", {
			sequence(*literals(0.5, 1.5))
		}, {
			rawName("kg")
		})
	}

	@Test
	fun implicitUnitMultiplication() = parseTest("100 [N m]") {
		operator("[", {
			literal(100)
		}, {
			operator(" ", { rawName("N") }, { rawName("m") },)
		})
	}

	@Test
	fun implicitUnitMultiplication2() = parseTest("20 [kg m^2 / s^3 A ]") {
		operator("[", {
			literal(20)
		}, {
			println(astString)
			operator("/", {
				operator(" ", {
					rawName("kg")
				}, {
					operator("^", { rawName("m") }, { literal(2) })
				})
			}, {
				operator(" ", {
					operator("^", { rawName("s") }, { literal(3) })
				}, {
					rawName("A")
				})
			})
		})
	}

	@Test
	fun symbolicUnit() = parseTest("10.0 [%]") {
		operator("[", {
			literal(10.0)
		}, {
			rawName("%")
		})
	}

	@Test @Ignore // legacy parser cannot parse this due to bug
	fun percentSuffix() = parseTest("20%") {
		operator("[", {
			literal(20)
		}, {
			rawName("%")
		})
	}

	@Test
	fun infixCrossProduct() = parseTest("a cross b") {
		operator("cross", *featureRefs("a", "b"))
	}

	/** Some legacy SysMD extensions are handled by preserving raw name literals
	 * (at some point they may be rewritten to appropriate SysMLv2 expressions instead)
	 */
	@Test
	fun legacyFunction1() = parseTest("sumOverSubclasses(2 + x)") {
		invocation("sumOverSubclasses", {
			operator("+", {
				literal(2)
			}, {
				assertIs<RawNameExpression>(this)
				assertEquals("x", rawName)
			})
		})
	}
}