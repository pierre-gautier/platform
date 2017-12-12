package platform.tests;

import java.util.Random;

import org.junit.Test;

import platform.model.IObject;
import platform.model.IRoot;
import platform.model.commons.Descriptors;
import platform.model.commons.Node;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.whatsup.WhatsupStrategy;

@SuppressWarnings("nls")
public class TestWhatsup {
    
    @Test
    public void testNeverGrow() {
        final IRoot root = new Root("toor");
        root.addStrategy(new WhatsupStrategy(10));
        final IObject object = new Node(Types.NODE, "id", null, root);
        object.addAttribute(Descriptors.LABEL, String.valueOf(new Random().nextDouble()));
        for (int i = 0; i < 100; i++) {
            object.addAttribute(Descriptors.LABEL, String.valueOf(new Random().nextDouble()));
        }
        object.addAttribute(Descriptors.LABEL, String.valueOf(new Random().nextDouble()));
    }
    
}
