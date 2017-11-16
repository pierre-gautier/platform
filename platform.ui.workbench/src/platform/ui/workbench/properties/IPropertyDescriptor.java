package platform.ui.workbench.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * A descriptor for a property to be presented by a standard property sheet page
 * (<code>PropertySheetPage</code>). These descriptors originate with property
 * sources (<code>IPropertySource</code>).
 * <p>
 * A property descriptor carries the following information:
 * <ul>
 * <li>property id (required)</li>
 * <li>display name (required)</li>
 * <li>brief description of the property (optional)</li>
 * <li>category for grouping related properties (optional)</li>
 * <li>label provider used to display the property value (optional)</li>
 * <li>cell editor for changing the property value (optional)</li>
 * <li>help context id (optional)</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface to provide specialized property
 * descriptors; however, there are standard implementations declared in
 * this package that take care of the most common cases:
 * <ul>
 * <li><code>PropertyDescriptor - read-only property</li>
 *   <li><code>TextPropertyDescriptor</code> - edits with a
 * <code>TextCellEditor</code></li>
 * <li><code>CheckboxPropertyDescriptor - edits with a
 *      <code>CheckboxCellEditor</code></code></li>
 * <li><code>ComboBoxPropertyDescriptor - edits with a
 *      <code>ComboBoxCellEditor</code></code></li>
 * <li><code>ColorPropertyDescriptor - edits with a
 *      <code>ColorCellEditor</code></code></li>
 * </ul>
 * </p>
 *
 * @see IPropertySource#getPropertyDescriptors
 */
public interface IPropertyDescriptor {
    
    CellEditor createPropertyEditor(Composite parent);
    
    String getCategory();
    
    String getDescription();
    
    String getDisplayName();
    
    String[] getFilterFlags();
    
    String getId();
    
    ILabelProvider getLabelProvider();
    
    boolean isCompatibleWith(IPropertyDescriptor anotherProperty);
}
