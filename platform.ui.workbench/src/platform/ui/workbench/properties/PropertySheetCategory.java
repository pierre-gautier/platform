package platform.ui.workbench.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * A category in a PropertySheet used to group <code>IPropertySheetEntry</code>
 * entries so they are displayed together.
 */
class PropertySheetCategory {
    
    private final List<IPropertySheetEntry> entries          = new ArrayList<>();
    private final String                    categoryName;
    
    private boolean                         shouldAutoExpand = true;
    
    /**
     * Create a PropertySheet category with name.
     *
     * @param name
     */
    public PropertySheetCategory(final String name) {
        this.categoryName = name;
    }
    
    /**
     * Add an <code>IPropertySheetEntry</code> to the list
     * of entries in this category.
     *
     * @param entry
     */
    public void addEntry(final IPropertySheetEntry entry) {
        this.entries.add(entry);
    }
    
    /**
     * Returns <code>true</code> if this category should be automatically
     * expanded. The default value is <code>true</code>.
     *
     * @return <code>true</code> if this category should be automatically
     *         expanded, <code>false</code> otherwise
     */
    public boolean getAutoExpand() {
        return this.shouldAutoExpand;
    }
    
    /**
     * Return the category name.
     *
     * @return the category name
     */
    public String getCategoryName() {
        return this.categoryName;
    }
    
    /**
     * Returns the entries in this category.
     *
     * @return the entries in this category
     */
    public IPropertySheetEntry[] getChildEntries() {
        return this.entries
                .toArray(new IPropertySheetEntry[this.entries.size()]);
    }
    
    /**
     * Removes all of the entries in this category.
     * Doing so allows us to reuse this category entry.
     */
    public void removeAllEntries() {
        this.entries.clear();
    }
    
    /**
     * Sets if this category should be automatically
     * expanded.
     *
     * @param autoExpand
     */
    public void setAutoExpand(final boolean autoExpand) {
        this.shouldAutoExpand = autoExpand;
    }
}
