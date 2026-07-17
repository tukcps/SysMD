package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoaderTests {
    @Test
    fun loadsKerMLMetamodel() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.KERML_XMI,
            model
        )

        assertTrue(model.allClasses().isNotEmpty())
        assertNotNull(
            model.allClasses()
                .find { it.name == "Element" }
        )
    }
}