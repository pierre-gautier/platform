/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package platform.ui.workbench.about;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

/**
 * Manages links in styled text.
 */

public class AboutTextManager {
    
    /**
     * Scan the contents of the about text
     *
     * @param s
     * @return
     */
    public static AboutItem scan(final String s) {
        final List<int[]> linkRanges = new ArrayList<>();
        final List<String> links = new ArrayList<>();
        
        // slightly modified version of jface url detection
        // see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector
        
        int urlSeparatorOffset = s.indexOf("://"); //$NON-NLS-1$
        while (urlSeparatorOffset >= 0) {
            
            boolean startDoubleQuote = false;
            
            // URL protocol (left to "://")
            int urlOffset = urlSeparatorOffset;
            char ch;
            do {
                urlOffset--;
                ch = ' ';
                if (urlOffset > -1) {
                    ch = s.charAt(urlOffset);
                }
                startDoubleQuote = ch == '"';
            } while (Character.isUnicodeIdentifierStart(ch));
            urlOffset++;
            
            // Right to "://"
            final StringTokenizer tokenizer = new StringTokenizer(s.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
            if (!tokenizer.hasMoreTokens()) {
                return null;
            }
            
            int urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffset;
            
            if (startDoubleQuote) {
                int endOffset = -1;
                final int nextDoubleQuote = s.indexOf('"', urlOffset);
                final int nextWhitespace = s.indexOf(' ', urlOffset);
                if (nextDoubleQuote != -1 && nextWhitespace != -1) {
                    endOffset = Math.min(nextDoubleQuote, nextWhitespace);
                } else if (nextDoubleQuote != -1) {
                    endOffset = nextDoubleQuote;
                } else if (nextWhitespace != -1) {
                    endOffset = nextWhitespace;
                }
                if (endOffset != -1) {
                    urlLength = endOffset - urlOffset;
                }
            }
            
            linkRanges.add(new int[] { urlOffset, urlLength });
            links.add(s.substring(urlOffset, urlOffset + urlLength));
            
            urlSeparatorOffset = s.indexOf("://", urlOffset + urlLength + 1); //$NON-NLS-1$
        }
        return new AboutItem(s, linkRanges.toArray(new int[linkRanges.size()][2]), links.toArray(new String[links.size()]));
    }
    
    private final StyledText styledText;
    
    private Cursor           handCursor;
    
    private Cursor           busyCursor;
    
    private boolean          mouseDown = false;
    
    private boolean          dragEvent = false;
    
    private AboutItem        item;
    
    public AboutTextManager(final StyledText text) {
        this.styledText = text;
        this.createCursors();
        this.addListeners();
    }
    
    /**
     * Gets the about item.
     *
     * @return the about item
     */
    public AboutItem getItem() {
        return this.item;
    }
    
    /**
     * Sets the about item.
     *
     * @param item
     *            about item
     */
    public void setItem(final AboutItem item) {
        this.item = item;
        if (item != null) {
            this.styledText.setText(item.getText());
            this.setLinkRanges(item.getLinkRanges());
        }
    }
    
    /**
     * Adds listeners to the given styled text
     */
    private void addListeners() {
        this.styledText.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                AboutTextManager.this.mouseDown = true;
            }
            
            @Override
            public void mouseUp(final MouseEvent e) {
                AboutTextManager.this.mouseDown = false;
                final int offset = AboutTextManager.this.styledText.getCaretOffset();
                if (AboutTextManager.this.dragEvent) {
                    // don't activate a link during a drag/mouse up operation
                    AboutTextManager.this.dragEvent = false;
                    if (AboutTextManager.this.item != null && AboutTextManager.this.item.isLinkAt(offset)) {
                        AboutTextManager.this.styledText.setCursor(AboutTextManager.this.handCursor);
                    }
                } else if (AboutTextManager.this.item != null && AboutTextManager.this.item.isLinkAt(offset)) {
                    AboutTextManager.this.styledText.setCursor(AboutTextManager.this.busyCursor);
                    AboutUtils.openLink(AboutTextManager.this.item.getLinkAt(offset));
                    final StyleRange selectionRange = AboutTextManager.this.getCurrentRange();
                    AboutTextManager.this.styledText.setSelectionRange(selectionRange.start, selectionRange.length);
                    AboutTextManager.this.styledText.setCursor(null);
                }
            }
        });
        
        this.styledText.addMouseMoveListener(e -> {
            // Do not change cursor on drag events
            if (AboutTextManager.this.mouseDown) {
                if (!AboutTextManager.this.dragEvent) {
                    final StyledText text1 = (StyledText) e.widget;
                    text1.setCursor(null);
                }
                AboutTextManager.this.dragEvent = true;
                return;
            }
            final StyledText text2 = (StyledText) e.widget;
            int offset = -1;
            try {
                offset = text2.getOffsetAtLocation(new Point(e.x, e.y));
            } catch (final IllegalArgumentException ex) {
                // leave value as -1
            }
            if (offset == -1) {
                text2.setCursor(null);
            } else if (AboutTextManager.this.item != null && AboutTextManager.this.item.isLinkAt(offset)) {
                text2.setCursor(AboutTextManager.this.handCursor);
            } else {
                text2.setCursor(null);
            }
        });
        
        this.styledText.addTraverseListener(e -> {
            switch (e.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    e.doit = true;
                    break;
                case SWT.TRAVERSE_TAB_NEXT:
                    // Previously traverse out in the backward direction?
                    final Point nextSelection = AboutTextManager.this.styledText.getSelection();
                    final int charCount = AboutTextManager.this.styledText.getCharCount();
                    if (nextSelection.x == charCount
                            && nextSelection.y == charCount) {
                        AboutTextManager.this.styledText.setSelection(0);
                    }
                    final StyleRange nextRange = AboutTextManager.this.findNextRange();
                    if (nextRange == null) {
                        // Next time in start at beginning, also used by
                        // TRAVERSE_TAB_PREVIOUS to indicate we traversed out
                        // in the forward direction
                        AboutTextManager.this.styledText.setSelection(0);
                        e.doit = true;
                    } else {
                        AboutTextManager.this.styledText.setSelectionRange(nextRange.start,
                                nextRange.length);
                        e.doit = true;
                        e.detail = SWT.TRAVERSE_NONE;
                    }
                    break;
                case SWT.TRAVERSE_TAB_PREVIOUS:
                    // Previously traverse out in the forward direction?
                    final Point previousSelection = AboutTextManager.this.styledText.getSelection();
                    if (previousSelection.x == 0
                            && previousSelection.y == 0) {
                        AboutTextManager.this.styledText.setSelection(AboutTextManager.this.styledText.getCharCount());
                    }
                    final StyleRange previousRange = AboutTextManager.this.findPreviousRange();
                    if (previousRange == null) {
                        // Next time in start at the end, also used by
                        // TRAVERSE_TAB_NEXT to indicate we traversed out
                        // in the backward direction
                        AboutTextManager.this.styledText.setSelection(AboutTextManager.this.styledText.getCharCount());
                        e.doit = true;
                    } else {
                        AboutTextManager.this.styledText.setSelectionRange(previousRange.start,
                                previousRange.length);
                        e.doit = true;
                        e.detail = SWT.TRAVERSE_NONE;
                    }
                    break;
                default:
                    break;
            }
        });
        
        // Listen for Tab and Space to allow keyboard navigation
        this.styledText.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(final KeyEvent event) {
                final StyledText text = (StyledText) event.widget;
                if (event.character == ' ' || event.character == SWT.CR) {
                    if (AboutTextManager.this.item != null) {
                        // Be sure we are in the selection
                        final int offset = text.getSelection().x + 1;
                        if (AboutTextManager.this.item.isLinkAt(offset)) {
                            text.setCursor(AboutTextManager.this.busyCursor);
                            AboutUtils.openLink(AboutTextManager.this.item.getLinkAt(offset));
                            final StyleRange selectionRange = AboutTextManager.this.getCurrentRange();
                            text.setSelectionRange(selectionRange.start, selectionRange.length);
                            text.setCursor(null);
                        }
                    }
                    return;
                }
            }
        });
    }
    
    private void createCursors() {
        this.handCursor = new Cursor(this.styledText.getDisplay(), SWT.CURSOR_HAND);
        this.busyCursor = new Cursor(this.styledText.getDisplay(), SWT.CURSOR_WAIT);
        this.styledText.addDisposeListener(e -> {
            AboutTextManager.this.handCursor.dispose();
            AboutTextManager.this.handCursor = null;
            AboutTextManager.this.busyCursor.dispose();
            AboutTextManager.this.busyCursor = null;
        });
    }
    
    /**
     * Find the next range after the current
     * selection.
     */
    private StyleRange findNextRange() {
        final StyleRange[] ranges = this.styledText.getStyleRanges();
        final int currentSelectionEnd = this.styledText.getSelection().y;
        
        for (final StyleRange range : ranges) {
            if (range.start >= currentSelectionEnd) {
                return range;
            }
        }
        return null;
    }
    
    /**
     * Find the previous range before the current selection.
     */
    private StyleRange findPreviousRange() {
        final StyleRange[] ranges = this.styledText.getStyleRanges();
        final int currentSelectionStart = this.styledText.getSelection().x;
        
        for (int i = ranges.length - 1; i > -1; i--) {
            if (ranges[i].start + ranges[i].length - 1 < currentSelectionStart) {
                return ranges[i];
            }
        }
        return null;
    }
    
    /**
     * Find the range of the current selection.
     */
    private StyleRange getCurrentRange() {
        final StyleRange[] ranges = this.styledText.getStyleRanges();
        final int currentSelectionEnd = this.styledText.getSelection().y;
        final int currentSelectionStart = this.styledText.getSelection().x;
        
        for (final StyleRange range : ranges) {
            if (currentSelectionStart >= range.start
                    && currentSelectionEnd <= range.start + range.length) {
                return range;
            }
        }
        return null;
    }
    
    /**
     * Sets the styled text's link (blue) ranges
     */
    private void setLinkRanges(final int[][] linkRanges) {
        final Color fg = JFaceColors.getHyperlinkText(this.styledText.getShell()
                .getDisplay());
        for (final int[] linkRange : linkRanges) {
            final StyleRange r = new StyleRange(linkRange[0], linkRange[1],
                    fg, null);
            this.styledText.setStyleRange(r);
        }
    }
}
