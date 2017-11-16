package platform.ui.workbench.properties;

/**
 * customized eclipse interface
 */
public interface IPropertySource {
    
    Object getEditableValue();
    
    IPropertyDescriptor[] getPropertyDescriptors();
    
    Object getPropertyValue(Object id);
    
    boolean isPropertyEditable(Object id);
    
    void setPropertyValue(Object id, Object value);
}
