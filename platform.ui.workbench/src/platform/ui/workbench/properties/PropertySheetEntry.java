/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Gunnar Wagenknecht - fix for bug 21756 [PropertiesView] property view sorting
 * Kevin Milburn - [Bug 423214] [PropertiesView] add support for IColorProvider and IFontProvider
 *******************************************************************************/

package platform.ui.workbench.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>PropertySheetEntry</code> is an implementation of
 * <code>IPropertySheetEntry</code> which uses <code>IPropertySource</code>
 * and <code>IPropertyDescriptor</code> to interact with domain model objects.
 * <p>
 * Every property sheet entry has a single descriptor (except the root entry
 * which has none). This descriptor determines what property of its objects it
 * will display/edit.
 * </p>
 * <p>
 * Entries do not listen for changes in their objects. Since there is no
 * restriction on properties being independent, a change in one property may
 * affect other properties. The value of a parent's property may also change. As
 * a result we are forced to refresh the entire entry tree when a property
 * changes value.
 * </p>
 *
 * @since 3.0 (was previously internal)
 */
public class PropertySheetEntry
        extends EventManager
        implements IPropertySheetEntry {
    
    /**
     * Returns an map of property descritptors (keyed on id) for the given
     * property source.
     *
     * @param source
     *            a property source for which to obtain descriptors
     * @return a table of decriptors keyed on their id
     */
    private static Map<Object, IPropertyDescriptor> computePropertyDescriptorsFor(final IPropertySource source) {
        final IPropertyDescriptor[] descriptors = source.getPropertyDescriptors();
        final Map<Object, IPropertyDescriptor> result = new HashMap<>(descriptors.length * 2 + 1);
        for (final IPropertyDescriptor descriptor2 : descriptors) {
            result.put(descriptor2.getId(), descriptor2);
        }
        return result;
    }
    
    /**
     * The values we are displaying/editing. These objects repesent the value of
     * one of the properties of the values of our parent entry. Except for the
     * root entry where they represent the input (selected) objects.
     */
    private Object[]                     values             = new Object[0];
    
    /**
     * The property sources for the values we are displaying/editing.
     */
    private Map<Object, IPropertySource> sources            = new HashMap<>(0);
    
    /**
     * The value of this entry is defined as the the first object in its value
     * array or, if that object is an <code>IPropertySource</code>, the value
     * it returns when sent <code>getEditableValue</code>
     */
    private Object                       editValue;
    private PropertySheetEntry           parent;
    private IPropertyDescriptor          descriptor;
    private CellEditor                   editor;
    private String                       errorText;
    private PropertySheetEntry[]         childEntries;
    
    /**
     * Create the CellEditorListener for this entry. It listens for value
     * changes in the CellEditor, and cancel and finish requests.
     */
    private final ICellEditorListener    cellEditorListener = new ICellEditorListener() {
                                                                
                                                                @Override
                                                                public void applyEditorValue() {
                                                                    PropertySheetEntry.this.applyEditorValue();
                                                                }
                                                                
                                                                @Override
                                                                public void cancelEditor() {
                                                                    PropertySheetEntry.this.setErrorText(null);
                                                                }
                                                                
                                                                @Override
                                                                public void editorValueChanged(final boolean oldValidState,
                                                                        final boolean newValidState) {
                                                                    if (!newValidState) {
                                                                        // currently not valid so show an error message
                                                                        PropertySheetEntry.this.setErrorText(PropertySheetEntry.this.editor.getErrorMessage());
                                                                    } else {
                                                                        // currently valid
                                                                        PropertySheetEntry.this.setErrorText(null);
                                                                    }
                                                                }
                                                            };
    
    @Override
    public void addPropertySheetEntryListener(final IPropertySheetEntryListener listener) {
        this.addListenerObject(listener);
    }
    
    @Override
    public void applyEditorValue() {
        if (this.editor == null) {
            return;
        }
        
        // Check if editor has a valid value
        if (!this.editor.isValueValid()) {
            this.setErrorText(this.editor.getErrorMessage());
            return;
        }
        
        this.setErrorText(null);
        
        // See if the value changed and if so update
        final Object newValue = this.editor.getValue();
        boolean changed = false;
        if (this.values.length > 1) {
            changed = true;
        } else if (this.editValue == null) {
            if (newValue != null) {
                changed = true;
            }
        } else if (!this.editValue.equals(newValue)) {
            changed = true;
        }
        
        // Set the editor value
        if (changed) {
            this.setValue(newValue);
        }
    }
    
    @Override
    public void dispose() {
        if (this.editor != null) {
            this.editor.dispose();
            this.editor = null;
        }
        // recursive call to dispose children
        final PropertySheetEntry[] entriesToDispose = this.childEntries;
        this.childEntries = null;
        if (entriesToDispose != null) {
            for (final PropertySheetEntry element : entriesToDispose) {
                // an error in a property source may cause refreshChildEntries
                // to fail. Since the Workbench handles such errors we
                // can be left in a state where a child entry is null.
                if (element != null) {
                    element.dispose();
                }
            }
        }
    }
    
    @Override
    public String getCategory() {
        return this.descriptor.getCategory();
    }
    
    @Override
    public IPropertySheetEntry[] getChildEntries() {
        if (this.childEntries == null) {
            this.createChildEntries();
        }
        return this.childEntries;
    }
    
    @Override
    public String getDescription() {
        return this.descriptor.getDescription();
    }
    
    @Override
    public String getDisplayName() {
        return this.descriptor.getDisplayName();
    }
    
    @Override
    public CellEditor getEditor(final Composite composite) {
        if (this.editor == null) {
            this.editor = this.descriptor.createPropertyEditor(composite);
            if (this.editor != null) {
                this.editor.addListener(this.cellEditorListener);
            }
        }
        if (this.editor != null) {
            this.editor.setValue(this.editValue);
            this.setErrorText(this.editor.getErrorMessage());
        }
        return this.editor;
    }
    
    @Override
    public String getErrorText() {
        return this.errorText;
    }
    
    @Override
    public String[] getFilters() {
        return this.descriptor.getFilterFlags();
    }
    
    @Override
    public String getId() {
        return this.descriptor.getId();
    }
    
    @Override
    public Image getImage() {
        final ILabelProvider provider = this.descriptor.getLabelProvider();
        if (provider == null) {
            return null;
        }
        return provider.getImage(this.editValue);
    }
    
    @Override
    public String getValueAsString() {
        if (this.editValue == null) {
            return "";//$NON-NLS-1$
        }
        final ILabelProvider provider = this.descriptor.getLabelProvider();
        if (provider == null) {
            return this.editValue.toString();
        }
        final String text = provider.getText(this.editValue);
        if (text == null) {
            return "";//$NON-NLS-1$
        }
        return text;
    }
    
    /**
     * Returns the value objects of this entry.
     *
     * @return the value objects of this entry
     * @since 3.1 (was previously private)
     */
    public Object[] getValues() {
        return this.values;
    }
    
    @Override
    public boolean hasChildEntries() {
        if (this.childEntries != null && this.childEntries.length > 0) {
            return true;
        }
        // see if we could have entires if we were asked
        return !this.computeMergedPropertyDescriptors().isEmpty();
    }
    
    @Override
    public void removePropertySheetEntryListener(final IPropertySheetEntryListener listener) {
        this.removeListenerObject(listener);
    }
    
    /**
     * The <code>PropertySheetEntry</code> implmentation of this method
     * declared on<code>IPropertySheetEntry</code> will obtain an editable
     * value for the given objects and update the child entries.
     * <p>
     * Updating the child entries will typically call this method on the child
     * entries and thus the entire entry tree is updated
     * </p>
     *
     * @param objects
     *            the new values for this entry
     */
    @Override
    public void setValues(final Object[] objects) {
        this.values = Arrays.copyOf(objects, objects.length);
        this.sources = new HashMap<>(this.values.length * 2 + 1);
        
        if (this.values.length == 0) {
            this.editValue = null;
        } else {
            // set the first value object as the entry's value
            Object newValue = this.values[0];
            
            // see if we should convert the value to an editable value
            final IPropertySource source = this.getPropertySource(newValue);
            if (source != null) {
                newValue = source.getEditableValue();
            }
            this.editValue = newValue;
        }
        
        // update our child entries
        this.refreshChildEntries();
        
        // inform listeners that our value changed
        this.fireValueChanged();
    }
    
    /**
     * Factory method to create a new child <code>PropertySheetEntry</code>
     * instance.
     * <p>
     * Subclasses may overwrite to create new instances of their own class.
     * </p>
     *
     * @return a new <code>PropertySheetEntry</code> instance for the
     *         descriptor passed in
     * @since 3.1
     */
    protected PropertySheetEntry createChildEntry() {
        return new PropertySheetEntry();
    }
    
    /**
     * Returns the background color for the entry.
     *
     * @return the background color for the entry, or <code>null</code> to use the default
     *         background color
     * @since 3.7
     */
    protected Color getBackground() {
        final ILabelProvider provider = this.descriptor.getLabelProvider();
        if (provider instanceof IColorProvider) {
            return ((IColorProvider) provider).getBackground(this.editValue);
        }
        return null;
    }
    
    /**
     * Returns the descriptor for this entry.
     *
     * @return the descriptor for this entry
     * @since 3.1 (was previously private)
     */
    protected IPropertyDescriptor getDescriptor() {
        return this.descriptor;
    }
    
    /**
     * Returns the edit value for the object at the given index.
     *
     * @param index
     *            the value object index
     * @return the edit value for the object at the given index
     */
    protected Object getEditValue(final int index) {
        Object value = this.values[index];
        final IPropertySource source = this.getPropertySource(value);
        if (source != null) {
            value = source.getEditableValue();
        }
        return value;
    }
    
    /**
     * Returns the font for the entry.
     *
     * @return the font for the entry, or <code>null</code> to use the default font
     * @since 3.7
     */
    protected Font getFont() {
        final ILabelProvider provider = this.descriptor.getLabelProvider();
        if (provider instanceof IFontProvider) {
            return ((IFontProvider) provider).getFont(this);
        }
        return null;
    }
    
    /**
     * Returns the foreground color for the entry.
     *
     * @return the foreground color for the entry, or <code>null</code> to use the default
     *         foreground color
     * @since 3.7
     */
    protected Color getForeground() {
        final ILabelProvider provider = this.descriptor.getLabelProvider();
        if (provider instanceof IColorProvider) {
            return ((IColorProvider) provider).getForeground(this);
        }
        return null;
    }
    
    /**
     * Returns the parent of this entry.
     *
     * @return the parent entry, or <code>null</code> if it has no parent
     * @since 3.1
     */
    protected PropertySheetEntry getParent() {
        return this.parent;
    }
    
    /**
     * Returns an property source for the given object.
     *
     * @param object
     *            an object for which to obtain a property source or
     *            <code>null</code> if a property source is not available
     * @return an property source for the given object
     * @since 3.1 (was previously private)
     */
    protected IPropertySource getPropertySource(final Object object) {
        if (this.sources.containsKey(object)) {
            return this.sources.get(object);
        }
        final IPropertySource result = object instanceof IPropertySource ? (IPropertySource) object : null;
        this.sources.put(object, result);
        return result;
    }
    
    /**
     * Refresh the entry tree from the root down.
     *
     * @since 3.1 (was previously private)
     */
    protected void refreshFromRoot() {
        if (this.parent == null) {
            this.refreshChildEntries();
        } else {
            this.parent.refreshFromRoot();
        }
    }
    
    /**
     * The value of the given child entry has changed. Therefore we must set
     * this change into our value objects.
     * <p>
     * We must inform our parent so that it can update its value objects
     * </p>
     * <p>
     * Subclasses may override to set the property value in some custom way.
     * </p>
     *
     * @param child
     *            the child entry that changed its value
     */
    protected void valueChanged(final PropertySheetEntry child) {
        for (int i = 0; i < this.values.length; i++) {
            final IPropertySource source = this.getPropertySource(this.values[i]);
            source.setPropertyValue(child.getDescriptor().getId(), child.getEditValue(i));
        }
        
        // inform our parent
        if (this.parent != null) {
            this.parent.valueChanged(this);
        }
    }
    
    /**
     * Return the unsorted intersection of all the
     * <code>IPropertyDescriptor</code>s for the objects.
     *
     * @return List
     */
    private List<IPropertyDescriptor> computeMergedPropertyDescriptors() {
        if (this.values.length == 0) {
            return new ArrayList<>(0);
        }
        
        final IPropertySource firstSource = this.getPropertySource(this.values[0]);
        if (firstSource == null) {
            return new ArrayList<>(0);
        }
        
        if (this.values.length == 1) {
            return Arrays.asList(firstSource.getPropertyDescriptors());
        }
        
        // get all descriptors from each object
        final Map<?, ?>[] propertyDescriptorMaps = new Map[this.values.length];
        for (int i = 0; i < this.values.length; i++) {
            final Object object = this.values[i];
            final IPropertySource source = this.getPropertySource(object);
            if (source == null) {
                // if one of the selected items is not a property source
                // then we show no properties
                return new ArrayList<>(0);
            }
            // get the property descriptors keyed by id
            propertyDescriptorMaps[i] = PropertySheetEntry.computePropertyDescriptorsFor(source);
        }
        
        // intersect
        final Map<?, ?> intersection = propertyDescriptorMaps[0];
        for (int i = 1; i < propertyDescriptorMaps.length; i++) {
            // get the current ids
            final Object[] ids = intersection.keySet().toArray();
            for (int j = 0; j < ids.length; j++) {
                final Object object = propertyDescriptorMaps[i].get(ids[j]);
                if (object == null ||
                // see if the descriptors (which have the same id) are
                // compatible
                        !((IPropertyDescriptor) intersection.get(ids[j])).isCompatibleWith((IPropertyDescriptor) object)) {
                    intersection.remove(ids[j]);
                }
            }
        }
        
        // sorting is handled in the PropertySheetViewer, return unsorted (in the original order)
        final List<IPropertyDescriptor> result = new ArrayList<>(intersection.size());
        final IPropertyDescriptor[] firstDescs = firstSource.getPropertyDescriptors();
        for (final IPropertyDescriptor desc : firstDescs) {
            if (intersection.containsKey(desc.getId())) {
                result.add(desc);
            }
        }
        return result;
    }
    
    /**
     * Create our child entries.
     */
    private void createChildEntries() {
        // get the current descriptors
        final List<IPropertyDescriptor> descriptors = this.computeMergedPropertyDescriptors();
        
        // rebuild child entries using old when possible
        final PropertySheetEntry[] newEntries = new PropertySheetEntry[descriptors.size()];
        for (int i = 0; i < descriptors.size(); i++) {
            final IPropertyDescriptor d = descriptors.get(i);
            // create new entry
            final PropertySheetEntry entry = this.createChildEntry();
            entry.setDescriptor(d);
            entry.setParent(this);
            entry.refreshValues();
            newEntries[i] = entry;
        }
        // only assign if successful
        this.childEntries = newEntries;
    }
    
    /**
     * The child entries of this entry have changed (children added or removed).
     * Notify all listeners of the change.
     */
    private void fireChildEntriesChanged() {
        final Object[] array = this.getListeners();
        for (final Object element : array) {
            final IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
            listener.childEntriesChanged(this);
        }
    }
    
    /**
     * The error message of this entry has changed. Notify all listeners of the
     * change.
     */
    private void fireErrorMessageChanged() {
        final Object[] array = this.getListeners();
        for (final Object element : array) {
            final IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
            listener.errorMessageChanged(this);
        }
    }
    
    /**
     * The values of this entry have changed. Notify all listeners of the
     * change.
     */
    private void fireValueChanged() {
        final Object[] array = this.getListeners();
        for (final Object element : array) {
            final IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
            listener.valueChanged(this);
        }
    }
    
    /**
     * Update our child entries. This implementation tries to reuse child
     * entries if possible (if the id of the new descriptor matches the
     * descriptor id of the old entry).
     */
    private void refreshChildEntries() {
        if (this.childEntries == null) {
            // no children to refresh
            return;
        }
        
        // get the current descriptors
        final List<IPropertyDescriptor> descriptors = this.computeMergedPropertyDescriptors();
        
        // cache old entries by their descriptor id
        final Map<Object, PropertySheetEntry> entryCache = new HashMap<>(this.childEntries.length * 2 + 1);
        for (final PropertySheetEntry childEntry : this.childEntries) {
            if (childEntry != null) {
                entryCache.put(childEntry.getDescriptor().getId(), childEntry);
            }
        }
        
        // create a list of entries to dispose
        final List<?> entriesToDispose = new ArrayList<Object>(Arrays.asList(this.childEntries));
        
        // clear the old entries
        this.childEntries = null;
        
        // rebuild child entries using old when possible
        final PropertySheetEntry[] newEntries = new PropertySheetEntry[descriptors.size()];
        boolean entriesChanged = descriptors.size() != entryCache.size();
        for (int i = 0; i < descriptors.size(); i++) {
            final IPropertyDescriptor d = descriptors.get(i);
            // see if we have an entry matching this descriptor
            PropertySheetEntry entry = entryCache.get(d.getId());
            if (entry != null) {
                // reuse old entry
                entry.setDescriptor(d);
                entriesToDispose.remove(entry);
            } else {
                // create new entry
                entry = this.createChildEntry();
                entry.setDescriptor(d);
                entry.setParent(this);
                entriesChanged = true;
            }
            entry.refreshValues();
            newEntries[i] = entry;
        }
        
        // only assign if successful
        this.childEntries = newEntries;
        
        if (entriesChanged) {
            this.fireChildEntriesChanged();
        }
        
        // Dispose of entries which are no longer needed
        for (int i = 0; i < entriesToDispose.size(); i++) {
            ((IPropertySheetEntry) entriesToDispose.get(i)).dispose();
        }
    }
    
    /**
     * Update our value objects. We ask our parent for the property values based
     * on our descriptor.
     */
    private void refreshValues() {
        // get our parent's value objects
        final Object[] currentSources = this.parent.getValues();
        
        // loop through the objects getting our property value from each
        final Object[] newValues = new Object[currentSources.length];
        for (int i = 0; i < currentSources.length; i++) {
            final IPropertySource source = this.parent.getPropertySource(currentSources[i]);
            newValues[i] = source.getPropertyValue(this.descriptor.getId());
        }
        
        // set our new values
        this.setValues(newValues);
    }
    
    /**
     * Set the descriptor.
     *
     * @param newDescriptor
     */
    private void setDescriptor(final IPropertyDescriptor newDescriptor) {
        // if our descriptor is changing, we have to get rid of our current editor if there is one
        if (this.descriptor != newDescriptor && this.editor != null) {
            this.editor.dispose();
            this.editor = null;
        }
        this.descriptor = newDescriptor;
    }
    
    /**
     * Set the error text. This should be set to null when the current value is
     * valid, otherwise it should be set to a error string
     */
    private void setErrorText(final String newErrorText) {
        this.errorText = newErrorText;
        this.fireErrorMessageChanged();
    }
    
    /**
     * Sets the parent of the entry to be propertySheetEntry.
     *
     * @param propertySheetEntry
     */
    private void setParent(final PropertySheetEntry propertySheetEntry) {
        this.parent = propertySheetEntry;
    }
    
    /**
     * Set the value for this entry.
     * <p>
     * We set the given value as the value for all our value objects. We then
     * call our parent to update the property we represent with the given value.
     * We then trigger a model refresh.
     * <p>
     *
     * @param newValue
     *            the new value
     */
    private void setValue(final Object newValue) {
        // Set the value
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = newValue;
        }
        
        // Inform our parent
        this.parent.valueChanged(this);
        
        // Refresh the model
        this.refreshFromRoot();
    }
}
