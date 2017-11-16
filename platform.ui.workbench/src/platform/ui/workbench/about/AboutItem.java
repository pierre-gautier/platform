package platform.ui.workbench.about;

import java.util.Arrays;

/**
 * Holds the information for text appearing in the about dialog
 */
public class AboutItem {
    
    private final String   text;
    
    private final int[][]  linkRanges;
    
    private final String[] hrefs;
    
    /**
     * Creates a new about item
     */
    public AboutItem(final String text, final int[][] linkRanges, final String[] hrefs) {
        this.text = text;
        this.linkRanges = Arrays.copyOf(linkRanges, linkRanges.length);
        this.hrefs = Arrays.copyOf(hrefs, hrefs.length);
    }
    
    /**
     * Returns the link at the given offset (if there is one),
     * otherwise returns <code>null</code>.
     */
    public String getLinkAt(final int offset) {
        // Check if there is a link at the offset
        for (int i = 0; i < this.linkRanges.length; i++) {
            if (offset >= this.linkRanges[i][0]
                    && offset < this.linkRanges[i][0] + this.linkRanges[i][1]) {
                return this.hrefs[i];
            }
        }
        return null;
    }
    
    /**
     * Returns the link ranges (character locations)
     */
    public int[][] getLinkRanges() {
        return this.linkRanges;
    }
    
    /**
     * Returns the text to display
     */
    public String getText() {
        return this.text;
    }
    
    /**
     * Returns true if a link is present at the given character location
     */
    public boolean isLinkAt(final int offset) {
        // Check if there is a link at the offset
        for (final int[] linkRange : this.linkRanges) {
            if (offset >= linkRange[0]
                    && offset < linkRange[0] + linkRange[1]) {
                return true;
            }
        }
        return false;
    }
}
