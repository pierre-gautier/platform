package platform.ui.workbench.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>IPropertySheetEntry</code> describes the model interface for the
 * property sheet.
 * <p>
 * May be implemented when supplying a custom root entry to a property page.
 * </p>
 */
interface IPropertySheetEntry {
    
    void addPropertySheetEntryListener(IPropertySheetEntryListener listener);
    
    void applyEditorValue();
    
    void dispose();
    
    String getCategory();
    
    IPropertySheetEntry[] getChildEntries();
    
    String getDescription();
    
    String getDisplayName();
    
    CellEditor getEditor(Composite parent);
    
    String getErrorText();
    
    String[] getFilters();
    
    String getId();
    
    Image getImage();
    
    String getValueAsString();
    
    boolean hasChildEntries();
    
    void removePropertySheetEntryListener(IPropertySheetEntryListener listener);
    
    void setValues(Object[] values);
}
