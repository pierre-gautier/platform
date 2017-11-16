package platform.model.commons;

import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;

public class Types {
    
    public static final Descriptor<IRoot>     ROOT     = new Descriptor<>("root", "Master", IRoot.class);           //$NON-NLS-1$ //$NON-NLS-2$
    public static final Descriptor<INode>     NODE     = new Descriptor<>("node", "Node", INode.class);             //$NON-NLS-1$ //$NON-NLS-2$
    public static final Descriptor<IRelation> RELATION = new Descriptor<>("relation", "Relation", IRelation.class); //$NON-NLS-1$ //$NON-NLS-2$
    
}
