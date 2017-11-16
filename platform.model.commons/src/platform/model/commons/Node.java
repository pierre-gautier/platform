package platform.model.commons;

import java.util.Collection;

import platform.model.ANode;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;

public class Node
        extends ANode {
    
    public Node(final Descriptor<? extends INode> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        super(type, id, attributes, root);
    }
    
}
