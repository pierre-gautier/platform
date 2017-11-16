package platform.ui.workbench.about;

import java.util.Vector;

/**
 * A string pattern matcher, suppporting "*" and "?" wildcards.
 */
public class StringMatcher {
    
    private static final char SINGLE_WILD_CARD = '\u0000';
    
    private final String      fPattern;
    private final int         fLength;                    // pattern length
    private int               fBound           = 0;       // boundary value beyond which we don't need to search in the text
    private final boolean     fIgnoreWildCards;
    private final boolean     fIgnoreCase;
    private boolean           fHasLeadingStar;
    private boolean           fHasTrailingStar;
    private String[]          fSegments;                  // the given pattern is split into * separated segments
    
    /**
     * StringMatcher constructor takes in a String object that is a simple
     * pattern which may contain '*' for 0 and many characters and
     * '?' for exactly one character.
     * Literal '*' and '?' characters must be escaped in the pattern
     * e.g., "\*" means literal "*", etc.
     * Escaping any other character (including the escape character itself),
     * just results in that character in the pattern.
     * e.g., "\a" means "a" and "\\" means "\"
     * If invoking the StringMatcher with string literals in Java, don't forget
     * escape characters are represented by "\\".
     *
     * @param pattern
     *            the pattern to match text against
     * @param ignoreCase
     *            if true, case is ignored
     * @param ignoreWildCards
     *            if true, wild cards and their escape sequences are ignored
     *            (everything is taken literally).
     */
    public StringMatcher(final String pattern, final boolean ignoreCase, final boolean ignoreWildCards) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        this.fIgnoreCase = ignoreCase;
        this.fIgnoreWildCards = ignoreWildCards;
        this.fPattern = pattern;
        this.fLength = pattern.length();
        
        if (this.fIgnoreWildCards) {
            this.parseNoWildCards();
        } else {
            this.parseWildCards();
        }
    }
    
    /**
     * match the given <code>text</code> with the pattern
     *
     * @return true if matched otherwise false
     * @param text
     *            a String object
     */
    public boolean match(final String text) {
        if (text == null) {
            return false;
        }
        return this.match(text, 0, text.length());
    }
    
    /**
     * Given the starting (inclusive) and the ending (exclusive) positions in the
     * <code>text</code>, determine if the given substring matches with aPattern
     *
     * @return true if the specified portion of the text matches the pattern
     * @param text
     *            a String object that contains the substring to match
     * @param start
     *            marks the starting position (inclusive) of the substring
     * @param end
     *            marks the ending index (exclusive) of the substring
     */
    private boolean match(final String text, final int s, final int e) {
        if (null == text) {
            throw new IllegalArgumentException();
        }
        
        int start = s;
        int end = e;
        
        if (start > end) {
            return false;
        }
        
        if (this.fIgnoreWildCards) {
            return end - start == this.fLength && this.fPattern.regionMatches(this.fIgnoreCase, 0, text, start, this.fLength);
        }
        final int segCount = this.fSegments.length;
        if (segCount == 0 && (this.fHasLeadingStar || this.fHasTrailingStar)) {
            return true;
        }
        if (start == end) {
            return this.fLength == 0;
        }
        if (this.fLength == 0) {
            return start == end;
        }
        
        final int tlen = text.length();
        if (start < 0) {
            start = 0;
        }
        if (end > tlen) {
            end = tlen;
        }
        
        int tCurPos = start;
        final int bound = end - this.fBound;
        if (bound < 0) {
            return false;
        }
        int i = 0;
        String current = this.fSegments[i];
        final int segLength = current.length();
        
        /* process first segment */
        if (!this.fHasLeadingStar) {
            if (!this.regExpRegionMatches(text, start, current, 0, segLength)) {
                return false;
            }
            ++i;
            tCurPos = tCurPos + segLength;
        }
        if (this.fSegments.length == 1 && !this.fHasLeadingStar && !this.fHasTrailingStar) {
            // only one segment to match, no wildcards specified
            return tCurPos == end;
        }
        /* process middle segments */
        while (i < segCount) {
            current = this.fSegments[i];
            int currentMatch;
            final int k = current.indexOf(StringMatcher.SINGLE_WILD_CARD);
            if (k < 0) {
                currentMatch = this.textPosIn(text, tCurPos, end, current);
                if (currentMatch < 0) {
                    return false;
                }
            } else {
                currentMatch = this.regExpPosIn(text, tCurPos, end, current);
                if (currentMatch < 0) {
                    return false;
                }
            }
            tCurPos = currentMatch + current.length();
            i++;
        }
        
        /* process final segment */
        if (!this.fHasTrailingStar && tCurPos != end) {
            final int clen = current.length();
            return this.regExpRegionMatches(text, end - clen, current, 0, clen);
        }
        return i == segCount;
    }
    
    /**
     * This method parses the given pattern into segments seperated by wildcard '*' characters.
     * Since wildcards are not being used in this case, the pattern consists of a single segment.
     */
    private void parseNoWildCards() {
        this.fSegments = new String[1];
        this.fSegments[0] = this.fPattern;
        this.fBound = this.fLength;
    }
    
    /**
     * Parses the given pattern into segments seperated by wildcard '*' characters.
     *
     * @param p,
     *            a String object that is a simple regular expression with '*' and/or '?'
     */
    private void parseWildCards() {
        if (this.fPattern.startsWith("*")) { //$NON-NLS-1$
            this.fHasLeadingStar = true;
        }
        if (this.fPattern.endsWith("*") //$NON-NLS-1$
                && this.fLength > 1
                && this.fPattern.charAt(this.fLength - 2) != '\\') {
            /* make sure it's not an escaped wildcard */
            this.fHasTrailingStar = true;
        }
        
        final Vector<String> temp = new Vector<>();
        
        int pos = 0;
        final StringBuffer buf = new StringBuffer();
        while (pos < this.fLength) {
            final char c = this.fPattern.charAt(pos++);
            switch (c) {
                case '\\':
                    if (pos >= this.fLength) {
                        buf.append(c);
                    } else {
                        final char next = this.fPattern.charAt(pos++);
                        /* if it's an escape sequence */
                        if (next == '*' || next == '?' || next == '\\') {
                            buf.append(next);
                        } else {
                            /* not an escape sequence, just insert literally */
                            buf.append(c);
                            buf.append(next);
                        }
                    }
                    break;
                case '*':
                    if (buf.length() > 0) {
                        /* new segment */
                        temp.addElement(buf.toString());
                        this.fBound += buf.length();
                        buf.setLength(0);
                    }
                    break;
                case '?':
                    /* append special character representing single match wildcard */
                    buf.append(StringMatcher.SINGLE_WILD_CARD);
                    break;
                default:
                    buf.append(c);
            }
        }
        
        /* add last buffer to segment list */
        if (buf.length() > 0) {
            temp.addElement(buf.toString());
            this.fBound += buf.length();
        }
        
        this.fSegments = new String[temp.size()];
        temp.copyInto(this.fSegments);
    }
    
    /**
     * @param text
     *            a simple regular expression that may only contain '?'(s)
     * @param start
     *            the starting index in the text for search, inclusive
     * @param end
     *            the stopping point of search, exclusive
     * @param p
     *            a simple regular expression that may contains '?'
     * @return the starting index in the text of the pattern , or -1 if not found
     */
    private int regExpPosIn(final String text, final int start, final int end, final String p) {
        final int plen = p.length();
        
        final int max = end - plen;
        for (int i = start; i <= max; ++i) {
            if (this.regExpRegionMatches(text, i, p, 0, plen)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * @return boolean
     * @param text
     *            a String to match
     * @param start
     *            int that indicates the starting index of match, inclusive
     * @param end</code>
     *            int that indicates the ending index of match, exclusive
     * @param p
     *            String, String, a simple regular expression that may contain '?'
     * @param ignoreCase
     *            boolean indicating wether code>p</code> is case sensitive
     */
    private boolean regExpRegionMatches(final String text, final int tS, final String p, final int pS, final int pl) {
        
        int tStart = tS;
        int pStart = pS;
        int plen = pl;
        
        while (plen-- > 0) {
            final char tchar = text.charAt(tStart++);
            final char pchar = p.charAt(pStart++);
            
            /* process wild cards */
            if (!this.fIgnoreWildCards
                    && pchar == StringMatcher.SINGLE_WILD_CARD) {
                /* skip single wild cards */
                continue;
            }
            if (pchar == tchar) {
                continue;
            }
            if (this.fIgnoreCase) {
                if (Character.toUpperCase(tchar) == Character.toUpperCase(pchar)) {
                    continue;
                }
                // comparing after converting to upper case doesn't handle all cases
                // also compare after converting to lower case
                if (Character.toLowerCase(tchar) == Character.toLowerCase(pchar)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }
    
    /**
     * @param text
     *            the string to match
     * @param start
     *            the starting index in the text for search, inclusive
     * @param end
     *            the stopping point of search, exclusive
     * @param p
     *            a pattern string that has no wildcard
     * @return the starting index in the text of the pattern , or -1 if not found
     */
    private int textPosIn(final String text, final int start, final int end, final String p) {
        
        final int plen = p.length();
        final int max = end - plen;
        
        if (!this.fIgnoreCase) {
            final int i = text.indexOf(p, start);
            if (i == -1 || i > max) {
                return -1;
            }
            return i;
        }
        
        for (int i = start; i <= max; ++i) {
            if (text.regionMatches(true, i, p, 0, plen)) {
                return i;
            }
        }
        
        return -1;
    }
}
