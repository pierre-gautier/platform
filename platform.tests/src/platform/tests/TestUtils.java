package platform.tests;

import java.util.Arrays;

import platform.model.INode;
import platform.model.commons.Types;
import platform.model.factory.NodeFactories;
import platform.model.factory.RelationFactories;

public class TestUtils {
    
    public static String addRelation(final INode parent) {
        return TestUtils.addRelation(parent, NodeFactories.INSTANCE.create(Types.NODE, null, null, parent.getRoot()));
    }
    
    public static String addRelation(final INode parent, final INode child) {
        parent.addRelations(Arrays.asList(RelationFactories.INSTANCE.create(Types.RELATION, null, null, parent, child)));
        return child.getId();
    }
    
}
