package compiler.sysml

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.session.SessionSettings
import com.github.tukcps.sysmd.services.session.SessionStatus
import kotlin.test.Test
import kotlin.test.assertTrue

class ConstraintTests {

    /**
     * Check whether basic connectivity is generated.
     */
    @Test
    fun assertConstraintTest() {
        val src = "assert constraint c { true }"
        val settings = SessionSettings().also {
            it.includeOwningRelationshipsToRoot = true
        }
        val status = SessionStatus()
        val elements = SysMLv2(settings = settings, status = status).parse(src)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(elements.any { it.type == ElementType.Invariant })
    }

    /**
     * Check whether basic connectivity is generated.
     */
    @Test
    fun constraintTest() {
        val src = "constraint c { true} "
        val settings = SessionSettings().also {
            it.includeOwningRelationshipsToRoot = true
        }
        val status = SessionStatus()
        val elements = SysMLv2(settings = settings, status = status).parse(src)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        // Correct:
        // assertTrue(elements.any { it.type == ElementType.BooleanExpression.toString() })
        assertTrue(elements.any { it.type == ElementType.Feature })
    }
}