package platform.model.factory;

import java.util.Collection;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;

public interface IRelationFactory
        extends IFactory<IRelation> {
    
    <T extends IRelation> T create(Descriptor<T> type, String id, Collection<Attribute> attributes, INode source, INode target);
}
