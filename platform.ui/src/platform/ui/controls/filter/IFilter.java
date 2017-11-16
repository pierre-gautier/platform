package platform.ui.controls.filter;

import java.util.Collection;

import platform.model.Descriptor;
import platform.model.IAttributeListener;

public interface IFilter {
    
    Collection<Descriptor<?>> getCandidateProperties();
    
    FilterData getFilterData();
    
    void registerPropertiesListener(IAttributeListener listener);
    
    void setFilterData(FilterData parameters);
    
    void unregisterPropertiesListener(IAttributeListener listener);
    
}
