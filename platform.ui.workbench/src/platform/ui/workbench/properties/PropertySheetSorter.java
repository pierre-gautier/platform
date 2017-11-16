package platform.ui.workbench.properties;

import java.text.Collator; // can't use ICU, in public API
import java.util.Arrays;

/**
 * Class used by {@link org.eclipse.ui.views.properties.PropertySheetPage} to
 * sort properties.
 * <p>
 * The default implementation sorts alphabetically. Subclasses may overwrite to
 * implement custom sorting.
 * </p>
 *
 * @since 3.1
 */
public class PropertySheetSorter {
    
    /**
     * The collator used to sort strings.
     */
    private final Collator collator;
    
    /**
     * Creates a new sorter, which uses the default collator to sort strings.
     */
    public PropertySheetSorter() {
        this(Collator.getInstance());
    }
    
    /**
     * Creates a new sorter, which uses the given collator to sort strings.
     *
     * @param collator
     *            the collator to use to sort strings
     */
    public PropertySheetSorter(final Collator collator) {
        this.collator = collator;
    }
    
    /**
     * Returns a negative, zero, or positive number depending on whether the
     * first element is less than, equal to, or greater than the second element.
     * <p>
     * The default implementation of this method uses the collator to
     * compare the display names. Subclasses may override.
     * </p>
     *
     * @param entryA
     *            the first element
     * @param entryB
     *            the second element
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    public int compare(final IPropertySheetEntry entryA, final IPropertySheetEntry entryB) {
        return this.getCollator().compare(entryA.getDisplayName(), entryB.getDisplayName());
    }
    
    /**
     * Returns a negative, zero, or positive number depending on whether the
     * first element is less than, equal to, or greater than the second element.
     * <p>
     * The default implementation of this method uses the collator to
     * compare the strings. Subclasses may override.
     * </p>
     *
     * @param categoryA
     *            the first element
     * @param categoryB
     *            the second element
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    public int compareCategories(final String categoryA, final String categoryB) {
        return this.getCollator().compare(categoryA, categoryB);
    }
    
    /**
     * Sorts the given elements in-place, modifying the given array.
     * <p>
     * The default implementation of this method uses the java.util.Arrays#sort
     * algorithm on the given array, calling <code>compare</code> to compare
     * elements.
     * </p>
     * <p>
     * Subclasses may reimplement this method to provide a more optimized
     * implementation.
     * </p>
     *
     * @param entries
     *            the elements to sort
     */
    public void sort(final IPropertySheetEntry[] entries) {
        Arrays.sort(entries, (a, b) -> PropertySheetSorter.this.compare(a, b));
    }
    
    /**
     * Returns the collator used to sort strings.
     *
     * @return the collator used to sort strings
     */
    protected Collator getCollator() {
        return this.collator;
    }
    
    /**
     * Sorts the given categories in-place, modifying the given array.
     *
     * @param categories
     *            the categories to sort
     */
    void sort(final PropertySheetCategory[] categories) {
        Arrays.sort(categories, (a, b) -> PropertySheetSorter.this.compareCategories(a.getCategoryName(), b.getCategoryName()));
    }
}
