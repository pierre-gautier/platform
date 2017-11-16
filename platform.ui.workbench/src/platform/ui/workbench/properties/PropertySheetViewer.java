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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The PropertySheetViewer displays the properties of objects. The model for the
 * viewer consists of a hierarchy of <code>IPropertySheetEntry</code>.
 * <p>
 * This viewer also supports the optional catogorization of the first level
 * <code>IPropertySheetEntry</code> s by using instances of
 * <code>PropertySheetCategory</code>.
 */
/* package */
public class PropertySheetViewer
        extends Viewer {
    
    private static final String[] COLUMN_LABELS               = { "Property", "Value" };                  //$NON-NLS-1$//$NON-NLS-2$
    
    private static final String   FILTER_ID_EXPERT            = "org.eclipse.ui.views.properties.expert"; //$NON-NLS-1$
    private static final String   MISCELLANEOUS_CATEGORY_NAME = "Misc";                                   //$NON-NLS-1$
    private static final int      COLUMN_TO_EDIT              = 1;
    
    private static TreeItem[] getChildItems(final Widget widget) {
        if (widget instanceof Tree) {
            return ((Tree) widget).getItems();
        } else if (widget instanceof TreeItem) {
            return ((TreeItem) widget).getItems();
        }
        // shouldn't happen
        return new TreeItem[0];
    }
    
    // The input objects for the viewer
    private Object[]                    input;
    
    // The root entry of the viewer
    private IPropertySheetEntry         rootEntry;
    
    // The current categories
    private PropertySheetCategory[]     categories;
    
    // SWT widgets
    private final Tree                  tree;
    
    /**
     * Maintain a map from the PropertySheet entry to its
     * corresponding TreeItem. This is used in 'findItem' to
     * greatly increase the performance.
     */
    private final Map<Object, TreeItem> entryToItemMap            = new HashMap<>();
    private final TreeEditor            treeEditor;
    private final MenuItem              menuItem;
    
    private CellEditor                  cellEditor;
    private IPropertySheetEntryListener entryListener;
    private ICellEditorListener         editorListener;
    
    // Flag to indicate if categories (if any) should be shown
    private boolean                     isShowingCategories       = true;
    // Flag to indicate expert properties should be shown
    private boolean                     isShowingExpertProperties = false;
    // The status line manager for showing messages
    private IStatusLineManager          statusLineManager;
    // the property sheet sorter
    private PropertySheetSorter         sorter                    = new PropertySheetSorter();
    
    /**
     * Creates a property sheet viewer on a newly-created tree control
     * under the given parent. The viewer has no input, and no root entry.
     *
     * @param parent
     *            the parent control
     */
    public PropertySheetViewer(final Composite parent) {
        this.tree = new Tree(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.HIDE_SELECTION);
        
        // configure the widget
        this.tree.setLinesVisible(true);
        this.tree.setHeaderVisible(true);
        
        final Menu menu = new Menu(this.tree);
        this.menuItem = new MenuItem(menu, SWT.NONE);
        this.menuItem.setText("Copy"); //$NON-NLS-1$
        this.menuItem.setAccelerator(SWT.MOD1 | 'C');
        this.menuItem.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                PropertySheetViewer.this.copySelection();
            }
        });
        
        menu.setDefaultItem(this.menuItem);
        this.tree.setMenu(menu);
        
        // configure the columns
        this.addColumns();
        
        // add our listeners to the widget
        this.hookControl();
        
        // create a new tree editor
        this.treeEditor = new TreeEditor(this.tree);
        
        // create the entry and editor listener
        this.createEntryListener();
        this.createEditorListener();
    }
    
    /**
     * Returns the active cell editor of this property sheet viewer or
     * <code>null</code> if no cell editor is active.
     *
     * @return the active cell editor
     */
    public CellEditor getActiveCellEditor() {
        return this.cellEditor;
    }
    
    /*
     * (non-Javadoc) Method declared on Viewer.
     */
    @Override
    public Control getControl() {
        return this.tree;
    }
    
    /**
     * The <code>PropertySheetViewer</code> implementation of this method
     * declared on <code>IInputProvider</code> returns the objects for which
     * the viewer is currently showing properties. It returns an
     * <code>Object[]</code> or <code>null</code>.
     */
    @Override
    public Object getInput() {
        return this.input;
    }
    
    /**
     * Returns the root entry for this property sheet viewer. The root entry is
     * not visible in the viewer.
     *
     * @return the root entry or <code>null</code>.
     */
    public IPropertySheetEntry getRootEntry() {
        return this.rootEntry;
    }
    
    /**
     * The <code>PropertySheetViewer</code> implementation of this
     * <code>ISelectionProvider</code> method returns the result as a
     * <code>StructuredSelection</code>.
     * <p>
     * Note that this method only includes <code>IPropertySheetEntry</code> in
     * the selection (no categories).
     * </p>
     */
    @Override
    public ISelection getSelection() {
        if (this.tree.getSelectionCount() == 0) {
            return StructuredSelection.EMPTY;
        }
        final TreeItem[] sel = this.tree.getSelection();
        final List<Object> entries = new ArrayList<>(sel.length);
        for (final TreeItem ti : sel) {
            final Object data = ti.getData();
            if (data instanceof IPropertySheetEntry) {
                entries.add(data);
            }
        }
        return new StructuredSelection(entries);
    }
    
    /**
     * Updates all of the items in the tree.
     * <p>
     * Note that this means ensuring that the tree items reflect the state of
     * the model (entry tree) it does not mean telling the model to update
     * itself.
     * </p>
     */
    @Override
    public void refresh() {
        if (this.rootEntry != null) {
            this.updateChildrenOf(this.rootEntry, this.tree);
        }
    }
    
    /**
     * The <code>PropertySheetViewer</code> implementation of this method
     * declared on <code>Viewer</code> method sets the objects for which the
     * viewer is currently showing properties.
     * <p>
     * The input must be an <code>Object[]</code> or <code>null</code>.
     * </p>
     *
     * @param newInput
     *            the input of this viewer, or <code>null</code> if none
     */
    @Override
    public void setInput(final Object newInput) {
        // need to save any changed value when user clicks elsewhere
        this.applyEditorValue();
        // deactivate our cell editor
        this.deactivateCellEditor();
        
        // set the new input to the root entry
        this.input = (Object[]) newInput;
        if (this.input == null) {
            this.input = new Object[0];
        }
        
        if (this.rootEntry != null) {
            this.rootEntry.setValues(this.input);
            // ensure first level children are visible
            this.updateChildrenOf(this.rootEntry, this.tree);
        }
        
        final Rectangle area = PropertySheetViewer.this.tree.getClientArea();
        this.tree.getColumns()[0].pack();
        this.tree.getColumns()[1].setWidth(area.width - 20 - this.tree.getColumns()[0].getWidth());
        
        // Clear any previous StatusLine messages
        this.updateStatusLine(null);
    }
    
    /**
     * Sets the root entry for this property sheet viewer. The root entry is not
     * visible in the viewer.
     *
     * @param root
     *            the root entry
     */
    public void setRootEntry(final IPropertySheetEntry root) {
        // If we have a root entry, remove our entry listener
        if (this.rootEntry != null) {
            this.rootEntry.removePropertySheetEntryListener(this.entryListener);
        }
        
        this.rootEntry = root;
        
        // Set the root as user data on the tree
        this.tree.setData(this.rootEntry);
        
        // Add an IPropertySheetEntryListener to listen for entry change
        // notifications
        this.rootEntry.addPropertySheetEntryListener(this.entryListener);
        
        // Pass our input to the root, this will trigger entry change
        // callbacks to update this viewer
        this.setInput(this.input);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
     */
    @Override
    public void setSelection(final ISelection selection, final boolean reveal) {
        // Do nothing by default
    }
    
    /**
     * Sets the sorter for this viewer.
     * <p>
     * The default sorter sorts categories and entries alphabetically.
     * A viewer update needs to be triggered after the sorter has changed.
     * </p>
     *
     * @param sorter
     *            the sorter to set (<code>null</code> will reset to the
     *            default sorter)
     * @since 3.1
     */
    public void setSorter(final PropertySheetSorter sorter) {
        this.sorter = sorter;
        if (this.sorter == null) {
            this.sorter = new PropertySheetSorter();
        }
    }
    
    /**
     * Sets the status line manager this view will use to show messages.
     *
     * @param manager
     *            the status line manager
     */
    public void setStatusLineManager(final IStatusLineManager manager) {
        this.statusLineManager = manager;
    }
    
    /**
     * Update the status line based on the data of item.
     *
     * @param item
     */
    protected void updateStatusLine(final Widget item) {
        this.setMessage(null);
        this.setErrorMessage(null);
        
        // Update the status line
        if (item != null) {
            if (item.getData() instanceof PropertySheetEntry) {
                final PropertySheetEntry psEntry = (PropertySheetEntry) item.getData();
                
                // For entries, show the description if any, else show the label
                final String desc = psEntry.getDescription();
                if (desc != null && desc.length() > 0) {
                    this.setMessage(psEntry.getDescription());
                } else {
                    this.setMessage(psEntry.getDisplayName());
                }
            } else if (item.getData() instanceof PropertySheetCategory) {
                final PropertySheetCategory psCat = (PropertySheetCategory) item.getData();
                this.setMessage(psCat.getCategoryName());
            }
        }
    }
    
    /**
     * Deactivate the currently active cell editor.
     */
    /* package */
    void deactivateCellEditor() {
        this.treeEditor.setEditor(null, null, PropertySheetViewer.COLUMN_TO_EDIT);
        if (this.cellEditor != null) {
            this.cellEditor.deactivate();
            this.cellEditor.removeListener(this.editorListener);
            this.cellEditor = null;
        }
        // clear any error message from the editor
        this.setErrorMessage(null);
    }
    
    /**
     * Hides the categories.
     */
    /* package */
    void hideCategories() {
        this.isShowingCategories = false;
        this.categories = null;
        this.refresh();
    }
    
    /**
     * Hides the expert properties.
     */
    /* package */
    void hideExpert() {
        this.isShowingExpertProperties = false;
        this.refresh();
    }
    
    /**
     * Shows the categories.
     */
    /* package */
    void showCategories() {
        this.isShowingCategories = true;
        this.refresh();
    }
    
    /**
     * Shows the expert properties.
     */
    /* package */
    void showExpert() {
        this.isShowingExpertProperties = true;
        this.refresh();
    }
    
    /**
     * Activate a cell editor for the given selected tree item.
     *
     * @param item
     *            the selected tree item
     */
    private void activateCellEditor(final TreeItem item) {
        // ensure the cell editor is visible
        this.tree.showSelection();
        
        // Get the entry for this item
        final IPropertySheetEntry activeEntry = (IPropertySheetEntry) item.getData();
        
        // Get the cell editor for the entry.
        // Note that the editor parent must be the Tree control
        this.cellEditor = activeEntry.getEditor(this.tree);
        
        if (this.cellEditor == null) {
            // unable to create the editor
            return;
        }
        
        // activate the cell editor
        this.cellEditor.activate();
        
        // if the cell editor has no control we can stop now
        final Control control = this.cellEditor.getControl();
        if (control == null) {
            this.cellEditor.deactivate();
            this.cellEditor = null;
            return;
        }
        
        // add our editor listener
        this.cellEditor.addListener(this.editorListener);
        
        // set the layout of the tree editor to match the cell editor
        final CellEditor.LayoutData layout = this.cellEditor.getLayoutData();
        this.treeEditor.horizontalAlignment = layout.horizontalAlignment;
        this.treeEditor.grabHorizontal = layout.grabHorizontal;
        this.treeEditor.minimumWidth = layout.minimumWidth;
        this.treeEditor.setEditor(control, item, PropertySheetViewer.COLUMN_TO_EDIT);
        
        // set the error text from the cel editor
        this.setErrorMessage(this.cellEditor.getErrorMessage());
        
        // give focus to the cell editor
        this.cellEditor.setFocus();
        
    }
    
    /**
     * Add columns to the tree and set up the layout manager accordingly.
     */
    private void addColumns() {
        for (final String string : PropertySheetViewer.COLUMN_LABELS) {
            final TreeColumn column = new TreeColumn(this.tree, 0);
            column.setText(string);
        }
    }
    
    /**
     * Asks the entry currently being edited to apply its current cell editor
     * value.
     */
    private void applyEditorValue() {
        final TreeItem treeItem = this.treeEditor.getItem();
        // treeItem can be null when view is opened
        if (treeItem == null || treeItem.isDisposed()) {
            return;
        }
        final IPropertySheetEntry entry = (IPropertySheetEntry) treeItem.getData();
        entry.applyEditorValue();
    }
    
    private void copySelection() {
        final TreeItem[] items = this.tree.getSelection();
        if (items.length == 1) {
            final TreeItem item = items[0];
            final String itemValue = item.getText(1);
            if (itemValue == null || itemValue.trim().isEmpty()) {
                return;
            }
            final Display display = Display.getCurrent();
            final Clipboard clipboard = new Clipboard(display);
            clipboard.setContents(new Object[] { itemValue }, new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        }
    }
    
    /**
     * Creates the child items for the given widget (item or tree). This
     * method is called when the item is expanded for the first time or when an
     * item is assigned as the root of the tree.
     *
     * @param widget
     *            TreeItem or Tree to create the children in.
     */
    private void createChildren(final Widget widget) {
        // get the current child items
        final TreeItem[] childItems = PropertySheetViewer.getChildItems(widget);
        
        if (childItems.length > 0) {
            final Object data = childItems[0].getData();
            if (data != null) {
                // children already there!
                return;
            }
            // remove the dummy
            childItems[0].dispose();
        }
        
        // get the children and create their tree items
        final Object node = widget.getData();
        final List<?> children = this.getChildren(node);
        if (children.isEmpty()) {
            // this item does't actually have any children
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            // create a new tree item
            this.createItem(children.get(i), widget, i);
        }
    }
    
    /**
     * Creates a new cell editor listener.
     */
    private void createEditorListener() {
        this.editorListener = new ICellEditorListener() {
            
            @Override
            public void applyEditorValue() {
                // Do nothing
            }
            
            @Override
            public void cancelEditor() {
                PropertySheetViewer.this.deactivateCellEditor();
            }
            
            @Override
            public void editorValueChanged(final boolean oldValidState,
                    final boolean newValidState) {
                // Do nothing
            }
        };
    }
    
    /**
     * Creates a new property sheet entry listener.
     */
    private void createEntryListener() {
        this.entryListener = new IPropertySheetEntryListener() {
            
            @Override
            public void childEntriesChanged(final IPropertySheetEntry entry) {
                // update the children of the given entry
                if (entry == PropertySheetViewer.this.rootEntry) {
                    PropertySheetViewer.this.updateChildrenOf(entry, PropertySheetViewer.this.tree);
                } else {
                    final TreeItem item = PropertySheetViewer.this.findItem(entry);
                    if (item != null) {
                        PropertySheetViewer.this.updateChildrenOf(entry, item);
                    }
                }
            }
            
            @Override
            public void errorMessageChanged(final IPropertySheetEntry entry) {
                // update the error message
                PropertySheetViewer.this.setErrorMessage(entry.getErrorText());
            }
            
            @Override
            public void valueChanged(final IPropertySheetEntry entry) {
                // update the given entry
                final TreeItem item = PropertySheetViewer.this.findItem(entry);
                if (item != null) {
                    PropertySheetViewer.this.updateEntry(entry, item);
                }
            }
        };
    }
    
    /**
     * Creates a new tree item, sets the given entry or category (node)in
     * its user data field, and adds a listener to the node if it is an entry.
     *
     * @param node
     *            the entry or category associated with this item
     * @param parent
     *            the parent widget
     * @param index
     *            indicates the position to insert the item into its parent
     */
    private void createItem(final Object node, final Widget parent, final int index) {
        // create the item
        TreeItem item;
        if (parent instanceof TreeItem) {
            item = new TreeItem((TreeItem) parent, SWT.NONE, index);
        } else {
            item = new TreeItem((Tree) parent, SWT.NONE, index);
        }
        
        // set the user data field
        item.setData(node);
        
        // Cache the entry <-> tree item relationship
        this.entryToItemMap.put(node, item);
        
        // Always ensure that if the tree item goes away that it's
        // removed from the cache
        item.addDisposeListener(e -> {
            final Object possibleEntry = e.widget.getData();
            if (possibleEntry != null) {
                PropertySheetViewer.this.entryToItemMap.remove(possibleEntry);
            }
        });
        
        // add our listener
        if (node instanceof IPropertySheetEntry) {
            ((IPropertySheetEntry) node)
                    .addPropertySheetEntryListener(this.entryListener);
        }
        
        // update the visual presentation
        if (node instanceof IPropertySheetEntry) {
            this.updateEntry((IPropertySheetEntry) node, item);
        } else {
            this.updateCategory((PropertySheetCategory) node, item);
        }
    }
    
    /**
     * Sends out a selection changed event for the entry tree to all registered
     * listeners.
     */
    private void entrySelectionChanged() {
        this.fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }
    
    /**
     * Return a tree item in the property sheet that has the same entry in
     * its user data field as the supplied entry. Return <code>null</code> if
     * there is no such item.
     *
     * @param entry
     *            the entry to serach for
     * @return the TreeItem for the entry or <code>null</code> if
     *         there isn't one.
     */
    private TreeItem findItem(final IPropertySheetEntry entry) {
        // Iterate through treeItems to find item
        final TreeItem[] items = this.tree.getItems();
        for (final TreeItem item : items) {
            final TreeItem findItem = this.findItem(entry, item);
            if (findItem != null) {
                return findItem;
            }
        }
        return null;
    }
    
    /**
     * Return a tree item in the property sheet that has the same entry in
     * its user data field as the supplied entry. Return <code>null</code> if
     * there is no such item.
     *
     * @param entry
     *            the entry to search for
     * @param item
     *            the item look in
     * @return the TreeItem for the entry or <code>null</code> if
     *         there isn't one.
     */
    private TreeItem findItem(final IPropertySheetEntry entry, final TreeItem item) {
        // If we can find the TreeItem in the cache, just return it
        final TreeItem mapItem = this.entryToItemMap.get(entry);
        if (mapItem != null) {
            return mapItem;
        }
        
        // compare with current item
        if (entry == item.getData()) {
            return item;
        }
        
        // recurse over children
        final TreeItem[] items = item.getItems();
        for (final TreeItem childItem : items) {
            final TreeItem findItem = this.findItem(entry, childItem);
            if (findItem != null) {
                return findItem;
            }
        }
        return null;
    }
    
    /**
     * Returns the child entries of the given entry
     *
     * @param entry
     *            The entry to search
     * @return the children of the given entry (element type
     *         <code>IPropertySheetEntry</code>)
     */
    private List<?> getChildren(final IPropertySheetEntry entry) {
        // if the entry is the root and we are showing categories, and we have
        // more than the
        // defualt category, return the categories
        if (entry == this.rootEntry
                && this.isShowingCategories
                && (this.categories.length > 1 || this.categories.length == 1
                        && !this.categories[0].getCategoryName().equals(PropertySheetViewer.MISCELLANEOUS_CATEGORY_NAME))) {
            return Arrays.asList(this.categories);
        }
        
        // return the sorted & filtered child entries
        return this.getSortedEntries(this.getFilteredEntries(entry.getChildEntries()));
    }
    
    /**
     * Returns the sorted children of the given category or entry
     *
     * @param node
     *            a category or entry
     * @return the children of the given category or entry
     *         (element type <code>IPropertySheetEntry</code> or
     *         <code>PropertySheetCategory</code>)
     */
    private List<?> getChildren(final Object node) {
        // cast the entry or category
        IPropertySheetEntry entry = null;
        PropertySheetCategory category = null;
        if (node instanceof IPropertySheetEntry) {
            entry = (IPropertySheetEntry) node;
        } else {
            category = (PropertySheetCategory) node;
        }
        
        // get the child entries or categories
        List<?> children;
        if (category == null) {
            children = this.getChildren(entry);
        } else {
            children = this.getChildren(category);
        }
        
        return children;
    }
    
    /**
     * Returns the child entries of the given category
     *
     * @param category
     *            The category to search
     * @return the children of the given category (element type
     *         <code>IPropertySheetEntry</code>)
     */
    private List<?> getChildren(final PropertySheetCategory category) {
        return this.getSortedEntries(this.getFilteredEntries(category.getChildEntries()));
    }
    
    /**
     * Returns the entries which match the current filter.
     *
     * @param entries
     *            the entries to filter
     * @return the entries which match the current filter
     *         (element type <code>IPropertySheetEntry</code>)
     */
    private List<IPropertySheetEntry> getFilteredEntries(final IPropertySheetEntry[] entries) {
        // if no filter just return all entries
        if (this.isShowingExpertProperties) {
            return Arrays.asList(entries);
        }
        
        // check each entry for the filter
        final List<IPropertySheetEntry> filteredEntries = new ArrayList<>(entries.length);
        for (final IPropertySheetEntry entry : entries) {
            if (entry != null) {
                final String[] filters = entry.getFilters();
                boolean expert = false;
                if (filters != null) {
                    for (final String filter : filters) {
                        if (filter.equals(PropertySheetViewer.FILTER_ID_EXPERT)) {
                            expert = true;
                            break;
                        }
                    }
                }
                if (!expert) {
                    filteredEntries.add(entry);
                }
            }
        }
        return filteredEntries;
    }
    
    /**
     * Returns a sorted list of <code>IPropertySheetEntry</code> entries.
     *
     * @param unsortedEntries
     *            unsorted list of <code>IPropertySheetEntry</code>
     * @return a sorted list of the specified entries
     */
    private List<?> getSortedEntries(final List<IPropertySheetEntry> unsortedEntries) {
        final IPropertySheetEntry[] propertySheetEntries = unsortedEntries
                .toArray(new IPropertySheetEntry[unsortedEntries.size()]);
        this.sorter.sort(propertySheetEntries);
        return Arrays.asList(propertySheetEntries);
    }
    
    /**
     * Selection in the viewer occurred. Check if there is an active cell
     * editor. If yes, deactivate it and check if a new cell editor must be
     * activated.
     *
     * @param selection
     *            the TreeItem that is selected
     */
    private void handleSelect(final TreeItem selection) {
        // deactivate the current cell editor
        if (this.cellEditor != null) {
            this.applyEditorValue();
            this.deactivateCellEditor();
        }
        
        if (selection == null) {
            this.setMessage(null);
            this.setErrorMessage(null);
        } else {
            final Object object = selection.getData();
            if (object instanceof IPropertySheetEntry) {
                // get the entry for this item
                final IPropertySheetEntry activeEntry = (IPropertySheetEntry) object;
                
                // display the description for the item
                this.setMessage(activeEntry.getDescription());
                
                // activate a cell editor on the selection
                this.activateCellEditor(selection);
            }
        }
        this.entrySelectionChanged();
    }
    
    /**
     * The expand icon for a node in this viewer has been selected to collapse a
     * subtree. Deactivate the cell editor
     *
     * @param event
     *            the SWT tree event
     */
    private void handleTreeCollapse() {
        if (this.cellEditor != null) {
            this.applyEditorValue();
            this.deactivateCellEditor();
        }
    }
    
    /**
     * The expand icon for a node in this viewer has been selected to expand the
     * subtree. Create the children 1 level deep.
     * <p>
     * Note that we use a "dummy" item (no user data) to show a "+" icon beside
     * an item which has children before the item is expanded now that it is
     * being expanded we have to create the real child items
     * </p>
     *
     * @param event
     *            the SWT tree event
     */
    private void handleTreeExpand(final TreeEvent event) {
        this.createChildren(event.item);
    }
    
    /**
     * Establish this viewer as a listener on the control
     */
    private void hookControl() {
        // Handle selections in the Tree
        // Part1: Double click only (allow traversal via keyboard without
        // activation
        this.tree.addSelectionListener(new SelectionAdapter() {
            
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                if (e.item instanceof TreeItem) {
                    PropertySheetViewer.this.handleSelect((TreeItem) e.item);
                }
            }
            
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e) {
                // The viewer only owns the status line when there is
                // no 'active' cell editor
                if (PropertySheetViewer.this.cellEditor == null || !PropertySheetViewer.this.cellEditor.isActivated()) {
                    PropertySheetViewer.this.updateStatusLine(e.item);
                }
            }
        });
        // Part2: handle single click activation of cell editor
        this.tree.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseDown(final MouseEvent event) {
                // only activate if there is a cell editor
                final Point pt = new Point(event.x, event.y);
                final TreeItem item = PropertySheetViewer.this.tree.getItem(pt);
                if (item != null) {
                    PropertySheetViewer.this.handleSelect(item);
                }
                if (PropertySheetViewer.this.menuItem != null) {
                    PropertySheetViewer.this.menuItem.setEnabled(item != null && item.getText(1) != null && !item.getText(1).trim().isEmpty());
                }
            }
        });
        
        // Add a tree listener to expand and collapse which
        // allows for lazy creation of children
        this.tree.addTreeListener(new TreeListener() {
            
            @Override
            public void treeCollapsed(final TreeEvent event) {
                PropertySheetViewer.this.handleTreeCollapse();
            }
            
            @Override
            public void treeExpanded(final TreeEvent event) {
                PropertySheetViewer.this.handleTreeExpand(event);
            }
        });
        
        this.tree.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(final KeyEvent e) {
                if ((e.stateMask & SWT.MOD1) == SWT.MOD1 && e.keyCode == 'c') {
                    PropertySheetViewer.this.copySelection();
                }
            }
            
            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.character == SWT.ESC) {
                    PropertySheetViewer.this.deactivateCellEditor();
                } else if (e.keyCode == SWT.F5) {
                    // The following will simulate a reselect
                    PropertySheetViewer.this.setInput(PropertySheetViewer.this.getInput());
                }
            }
        });
    }
    
    /**
     * Remove the given item from the tree. Remove our listener if the
     * item's user data is a an entry then set the user data to null
     *
     * @param item
     *            the item to remove
     */
    private void removeItem(final TreeItem item) {
        final Object data = item.getData();
        if (data instanceof IPropertySheetEntry) {
            ((IPropertySheetEntry) data).removePropertySheetEntryListener(this.entryListener);
        }
        item.setData(null);
        
        // We explicitly remove the entry from the map since it's data has been null'd
        this.entryToItemMap.remove(data);
        
        item.dispose();
    }
    
    /**
     * Sets the error message to be displayed in the status line.
     *
     * @param errorMessage
     *            the message to be displayed, or <code>null</code>
     */
    private void setErrorMessage(final String errorMessage) {
        // show the error message
        if (this.statusLineManager != null) {
            this.statusLineManager.setErrorMessage(errorMessage);
        }
    }
    
    /**
     * Sets the message to be displayed in the status line. This message is
     * displayed when there is no error message.
     *
     * @param message
     *            the message to be displayed, or <code>null</code>
     */
    private void setMessage(final String message) {
        // show the message
        if (this.statusLineManager != null) {
            this.statusLineManager.setMessage(message);
        }
    }
    
    /**
     * Updates the categories. Reuses old categories if possible.
     */
    private void updateCategories() {
        // lazy initialize
        if (this.categories == null) {
            this.categories = new PropertySheetCategory[0];
        }
        
        // get all the filtered child entries of the root
        final List<IPropertySheetEntry> childEntries = this.getFilteredEntries(this.rootEntry.getChildEntries());
        
        // if the list is empty, just set an empty categories array
        if (childEntries.isEmpty()) {
            this.categories = new PropertySheetCategory[0];
            return;
        }
        
        // cache old categories by their descriptor name
        final Map<String, PropertySheetCategory> categoryCache = new HashMap<>(this.categories.length * 2 + 1);
        for (final PropertySheetCategory categorie : this.categories) {
            categorie.removeAllEntries();
            categoryCache.put(categorie.getCategoryName(), categorie);
        }
        
        // create a list of categories to get rid of
        final List<?> categoriesToRemove = new ArrayList<Object>(Arrays.asList(this.categories));
        
        // Determine the categories
        PropertySheetCategory misc = categoryCache.get(PropertySheetViewer.MISCELLANEOUS_CATEGORY_NAME);
        if (misc == null) {
            misc = new PropertySheetCategory(PropertySheetViewer.MISCELLANEOUS_CATEGORY_NAME);
        }
        boolean addMisc = false;
        
        for (int i = 0; i < childEntries.size(); i++) {
            final IPropertySheetEntry childEntry = childEntries.get(i);
            final String categoryName = childEntry.getCategory();
            if (categoryName == null) {
                misc.addEntry(childEntry);
                addMisc = true;
                categoriesToRemove.remove(misc);
            } else {
                PropertySheetCategory category = categoryCache.get(categoryName);
                if (category == null) {
                    category = new PropertySheetCategory(categoryName);
                    categoryCache.put(categoryName, category);
                } else {
                    categoriesToRemove.remove(category);
                }
                category.addEntry(childEntry);
            }
        }
        
        // Add the PSE_MISC category if it has entries
        if (addMisc) {
            categoryCache.put(PropertySheetViewer.MISCELLANEOUS_CATEGORY_NAME, misc);
        }
        
        // Sort the categories.
        // Rather than just sorting categoryCache.values(), we'd like the original order to be preserved
        // (with misc added at the end, if needed) before passing to the sorter.
        final List<PropertySheetCategory> categoryList = new ArrayList<>();
        final Set<String> seen = new HashSet<>(childEntries.size());
        for (int i = 0; i < childEntries.size(); i++) {
            final IPropertySheetEntry childEntry = childEntries.get(i);
            final String categoryName = childEntry.getCategory();
            if (categoryName != null && !seen.contains(categoryName)) {
                seen.add(categoryName);
                final PropertySheetCategory category = categoryCache.get(categoryName);
                if (category != null) {
                    categoryList.add(category);
                }
            }
        }
        if (addMisc && !seen.contains(PropertySheetViewer.MISCELLANEOUS_CATEGORY_NAME)) {
            categoryList.add(misc);
        }
        
        final PropertySheetCategory[] categoryArray = categoryList
                .toArray(new PropertySheetCategory[categoryList.size()]);
        this.sorter.sort(categoryArray);
        this.categories = categoryArray;
    }
    
    /**
     * Update the category (but not its parent or children).
     *
     * @param category
     *            the category to update
     * @param item
     *            the tree item for the given entry
     */
    private void updateCategory(final PropertySheetCategory category,
            final TreeItem item) {
        // ensure that backpointer is correct
        item.setData(category);
        
        // Update the map accordingly
        this.entryToItemMap.put(category, item);
        
        // Update the name and value columns
        item.setText(0, category.getCategoryName());
        item.setText(1, ""); //$NON-NLS-1$
        
        // update the "+" icon
        if (category.getAutoExpand()) {
            // we auto expand categories when they first appear
            this.createChildren(item);
            item.setExpanded(true);
            category.setAutoExpand(false);
        } else {
            // we do not want to auto expand categories if the user has
            // collpased them
            this.updatePlus(category, item);
        }
    }
    
    /**
     * Update the child entries or categories of the given entry or category. If
     * the given node is the root entry and we are showing categories then the
     * child entries are categories, otherwise they are entries.
     *
     * @param node
     *            the entry or category whose children we will update
     * @param widget
     *            the widget for the given entry, either a
     *            <code>TableTree</code> if the node is the root node or a
     *            <code>TreeItem</code> otherwise.
     */
    private void updateChildrenOf(final Object node, final Widget widget) {
        // cast the entry or category
        IPropertySheetEntry entry = null;
        PropertySheetCategory category = null;
        if (node instanceof IPropertySheetEntry) {
            entry = (IPropertySheetEntry) node;
        } else {
            category = (PropertySheetCategory) node;
        }
        
        // get the current child tree items
        TreeItem[] childItems = PropertySheetViewer.getChildItems(widget);
        
        // optimization! prune collapsed subtrees
        TreeItem item = null;
        if (widget instanceof TreeItem) {
            item = (TreeItem) widget;
        }
        if (item != null && !item.getExpanded()) {
            // remove all children
            for (final TreeItem childItem : childItems) {
                if (childItem.getData() != null) {
                    this.removeItem(childItem);
                }
            }
            
            // append a dummy if necessary
            if (category != null || entry != null && entry.hasChildEntries()) {
                // may already have a dummy
                // It is either a category (which always has at least one child)
                // or an entry with chidren.
                // Note that this test is not perfect, if we have filtering on
                // then there in fact may be no entires to show when the user
                // presses the "+" expand icon. But this is an acceptable
                // compromise.
                childItems = PropertySheetViewer.getChildItems(widget);
                if (childItems.length == 0) {
                    new TreeItem(item, SWT.NULL);
                }
            }
            return;
        }
        
        // get the child entries or categories
        if (node == this.rootEntry && this.isShowingCategories) {
            // update the categories
            this.updateCategories();
        }
        final List<?> children = this.getChildren(node);
        
        // remove items
        final Set<Object> set = new HashSet<>(childItems.length * 2 + 1);
        
        for (final TreeItem childItem : childItems) {
            final Object data = childItem.getData();
            if (data != null) {
                final Object e = data;
                final int ix = children.indexOf(e);
                if (ix < 0) { // not found
                    this.removeItem(childItem);
                } else { // found
                    set.add(e);
                }
            } else { // the dummy
                childItem.dispose();
            }
        }
        
        // WORKAROUND
        int oldCnt = -1;
        if (widget == this.tree) {
            oldCnt = this.tree.getItemCount();
        }
        
        // add new items
        final int newSize = children.size();
        for (int i = 0; i < newSize; i++) {
            final Object el = children.get(i);
            if (!set.contains(el)) {
                this.createItem(el, widget, i);
            }
        }
        
        // WORKAROUND
        if (widget == this.tree && oldCnt == 0 && this.tree.getItemCount() == 1) {
            this.tree.setRedraw(false);
            this.tree.setRedraw(true);
        }
        
        // get the child tree items after our changes
        childItems = PropertySheetViewer.getChildItems(widget);
        
        // update the child items
        // This ensures that the children are in the correct order
        // are showing the correct values.
        for (int i = 0; i < newSize; i++) {
            final Object el = children.get(i);
            if (el instanceof IPropertySheetEntry) {
                this.updateEntry((IPropertySheetEntry) el, childItems[i]);
            } else {
                this.updateCategory((PropertySheetCategory) el, childItems[i]);
                this.updateChildrenOf(el, childItems[i]);
            }
        }
        // The tree's original selection may no longer apply after the update,
        // so fire the selection changed event.
        this.entrySelectionChanged();
    }
    
    /**
     * Update the given entry (but not its children or parent)
     *
     * @param entry
     *            the entry we will update
     * @param item
     *            the tree item for the given entry
     */
    private void updateEntry(final IPropertySheetEntry entry, final TreeItem item) {
        // ensure that backpointer is correct
        item.setData(entry);
        
        // update the map accordingly
        this.entryToItemMap.put(entry, item);
        
        // update the name and value columns
        item.setText(0, entry.getDisplayName());
        item.setText(1, entry.getValueAsString());
        final Image image = entry.getImage();
        if (item.getImage(1) != image) {
            item.setImage(1, image);
        }
        
        if (entry instanceof PropertySheetEntry) {
            final PropertySheetEntry entry2 = (PropertySheetEntry) entry;
            
            Color color = entry2.getForeground();
            if (item.getForeground() != color) {
                item.setForeground(color);
            }
            
            color = entry2.getBackground();
            if (item.getBackground() != color) {
                item.setBackground(1, color);
            }
            
            final Font font = entry2.getFont();
            if (item.getFont() != font) {
                item.setFont(font);
            }
        }
        
        // update the "+" icon
        this.updatePlus(entry, item);
    }
    
    /**
     * Updates the "+"/"-" icon of the tree item from the given entry
     * or category.
     *
     * @param node
     *            the entry or category
     * @param item
     *            the tree item being updated
     */
    private void updatePlus(final Object node, final TreeItem item) {
        // cast the entry or category
        IPropertySheetEntry entry = null;
        PropertySheetCategory category = null;
        if (node instanceof IPropertySheetEntry) {
            entry = (IPropertySheetEntry) node;
        } else {
            category = (PropertySheetCategory) node;
        }
        
        final boolean hasPlus = item.getItemCount() > 0;
        final boolean needsPlus = category != null || entry != null && entry.hasChildEntries();
        boolean removeAll = false;
        boolean addDummy = false;
        
        if (hasPlus != needsPlus) {
            if (needsPlus) {
                addDummy = true;
            } else {
                removeAll = true;
            }
        }
        if (removeAll) {
            // remove all children
            final TreeItem[] items = item.getItems();
            for (final TreeItem item2 : items) {
                this.removeItem(item2);
            }
        }
        
        if (addDummy) {
            new TreeItem(item, SWT.NULL); // append a dummy to create the
            // plus sign
        }
    }
}
