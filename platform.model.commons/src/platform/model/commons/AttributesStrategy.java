package platform.model.commons;

import platform.model.AStrategy;
import platform.model.Descriptor;
import platform.model.IObject;

public class AttributesStrategy
        extends AStrategy {
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(final IObject object, final Descriptor<T> descriptor, final Object value) {
        if (descriptor == Descriptors.ID) {
            return (T) object.getId();
        }
        if (descriptor == Descriptors.TYPE) {
            return (T) object.getType();
        }
        return null;
    }
    
}
