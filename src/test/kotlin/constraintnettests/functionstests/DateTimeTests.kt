package constraintnettests.functionstests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import kotlin.test.Test
import kotlin.test.Ignore
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.math.*
import kotlin.test.*

class DateTimeTests {

        @Test
        fun dateTimeZFormatTest() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = DateTime("2026-07-13T13:10:24Z");
            """)
            solver.propagate()
            assertEquals(1783948224.0, solver.getVariable("dt")!!.min(), 0.0001)
            assertNoIssues()
        }

        @Test
        fun dateTime_negative_unix() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = DateTime("1969-12-31T23:59:59Z");
            """)
            solver.propagate()
            assertEquals(-1.0, solver.getVariable("dt")!!.min(), 0.0001)
            assertNoIssues()
        }

        @Test
        fun dateTime_invalid_format() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = DateTime("invalid-date");
            """)
            solver.propagate()
            assertTrue(status.issues.isNotEmpty(), "Expected datetime formatting issue to be reported")
        }

        @Test
        fun dateFunctionTest() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = Date("2022-10-10");
            """)
            solver.propagate()
            assertNoIssues()
            // unix timestamp for 2022-10-10T00:00:00Z is 1665360000.0
            assertEquals(1665360000.0, solver.getVariable("dt")!!.min(), 0.0001)
        }

        @Test
        fun monthFunctionTest() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = Month("2022-10");
            """)
            solver.propagate()
            assertNoIssues()
            // unix timestamp for 2022-10-01T00:00:00Z is 1664582400.0
            assertEquals(1664582400.0, solver.getVariable("dt")!!.min(), 0.0001)
        }

        @Test
        fun yearFunctionTest() = testSession("ISQ") {
            loadKerML("""
                feature dt: ISQ::TimeValue = Year("2022");
            """)
            solver.propagate()
            assertNoIssues()
            // unix timestamp for 2022-01-01T00:00:00Z is 1640995200.0
            assertEquals(1640995200.0, solver.getVariable("dt")!!.min(), 0.0001)
        }
}
