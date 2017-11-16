package platform.model;

public interface IRelation
        extends IObject {
    
    INode getSource();
    
    INode getTarget();
    
    @Override
    Descriptor<? extends IRelation> getType();
    
}
