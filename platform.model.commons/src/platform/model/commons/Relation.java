package platform.model.commons;

import java.util.Collection;

import platform.model.ARelation;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;

public class Relation
        extends ARelation {
    
    public Relation(final Descriptor<? extends IRelation> type, final INode source, final INode target) {
        super(type, source, target);
    }
    
    public Relation(final Descriptor<? extends IRelation> type, final String id, final Collection<Attribute> attributes, final INode source, final INode target) {
        super(type, id, attributes, source, target);
    }
    
}
