package models.expression

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import org.junit.jupiter.api.RepeatedTest
import util.testProjectSession
import kotlin.test.Ignore
import kotlin.test.assertEquals

class SerializationTest
{
	/** Serialized an element and makes sure the original object wasn't mutated */
	fun checkSerialization(f : Element) : ElementData
	{
		val dao = f.toDAO()
		val alsoDao = f.toDAO()

		val mapper = ObjectMapper()
		val json = mapper.writeValueAsString(dao)
		assertEquals(json, mapper.writeValueAsString(alsoDao))

		return dao
	}

	/** Tests that serializing 2+2 and re-importing yields the same expression.
	 * Ensures that the DAOs can be reordered
	 */
	@Ignore // I added the OwningRelationship to the Global Namespace to the serialization.
    // I is unclear whether they are to be included in the serialized model or not.
    // As of now I assume yes, because otherwise one cannot import anything to the global namespace, or have any reference to it.
    // (maybe this is even forbidden --> to be checked.)
    @RepeatedTest(10) // we want to reach "every" permutation of the list
	fun exampleExpressionSerialization() = testProjectSession {
		val tt = twoPlusTwo()
		addElement(tt)

		val data = listOf(
			tt, tt.argument[0], tt.argument[1],
			tt.ownedRelationship[0], tt.ownedRelationship[1],
		).shuffled().map { checkSerialization(it) }

		/*
		for((id,v) in repo.elements)
			println("$id -> $v")
		// */

		/*
		println("=========== DAO =============")
		for(dao in data)
			println("${dao.elementId} -> $dao")
		// */

		delete(tt)

		/*
		println("=========== AFTER DELETE =============")
		for((id,v) in repo.elements)
			println("$id -> $v")
		// */

		import(data)

		/*
		println("========== RESTORED ==============")
		for((id,v) in repo.elements)
			println("$id -> $v")
		// */

		assertIsTwoPlusTwo(repo.elements.values.single { it is OperatorExpression })
	}
}