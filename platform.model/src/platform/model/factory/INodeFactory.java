package platform.model.factory;

import java.util.Collection;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRoot;

public interface INodeFactory
        extends IFactory<INode> {
    
    <T extends INode> T copy(T from);
    
    <T extends INode> T create(Descriptor<T> type, String id, Collection<Attribute> attributes, IRoot root);
    
}
