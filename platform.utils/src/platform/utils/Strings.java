package platform.utils;

public class Strings {
    
    public static final String NEW_LINE = System.getProperty("line.separator"); //$NON-NLS-1$
    
    public static final String UTF8     = "UTF-8";                              //$NON-NLS-1$
    
    public static final String TYPE     = "type";                               //$NON-NLS-1$
    public static final String ID       = "id";                                 //$NON-NLS-1$
    
    public static final String EMPTY    = "";                                   //$NON-NLS-1$
    public static final String SLASH    = "/";                                  //$NON-NLS-1$
    public static final String SPACE    = " ";                                  //$NON-NLS-1$
    public static final String COMMA    = ",";                                  //$NON-NLS-1$
    public static final String DOT      = ".";                                  //$NON-NLS-1$
    public static final String SHARP    = "#";                                  //$NON-NLS-1$
    
    public static boolean isNullEmptyOrBlank(final String... strings) {
        for (final String string : strings) {
            if (string == null || string.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    public static double parseDouble(final String input, final double errorValue) {
        if (input == null || input.length() == 0) {
            return errorValue;
        }
        try {
            return Double.parseDouble(input);
        } catch (final Exception e) {
            e.printStackTrace();
            return errorValue;
        }
    }
    
    public static int parseInt(final String input, final int errorValue) {
        if (input == null || input.length() == 0) {
            return errorValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (final Exception e) {
            e.printStackTrace();
            return errorValue;
        }
    }
    
}
