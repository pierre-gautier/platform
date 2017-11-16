package platform.model;

import java.util.Collection;
import java.util.List;

public interface IRoot
        extends INode {
    
    void addStrategy(IStrategy strategy);
    
    IRelation getSelection();
    
    List<IRelation> getSelections();
    
    List<IStrategy> getStrategies();
    
    @Override
    Descriptor<? extends IRoot> getType();
    
    void registerSelectionListener(ISelectionListener listener);
    
    void setSelection(Object source, Collection<IRelation> selection);
    
    void unregisterSelectionListener(ISelectionListener listener);
    
}
