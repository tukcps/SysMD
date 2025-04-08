package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue


class ActionTest {

    @Ignore
    @Test
    fun testAction() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences") {
        loadSysMLv2(
            """
                package 'Action Decomposition' {
                
                    part def Scene;
                    part def Image;
                    part def Picture;
                    
                    action def Focus { in scene : Scene; out image : Image; }
                    action def Shoot { in image: Image; out picture : Picture; }	
                    action def TakePicture { in scene : Scene; out picture : Picture; }
                        
                    action takePicture : TakePicture {
                    
                        in item scene;
                        out item picture;
                        
                        action focus : Focus {
                            in item scene = takePicture::scene; 
                            out item image;
                        }
                        
                        flow from focus.image to shoot.image;
                
                        action shoot : Shoot {
                            in item; 
                            out item picture = takePicture::picture;
                        }
                        
                    }
                    
                }           
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

    }
}