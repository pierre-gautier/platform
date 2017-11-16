package platform.model.license;

import java.time.LocalDate;

import platform.model.Descriptor;
import platform.model.INode;
import platform.utils.Strings;

@SuppressWarnings("nls")
public class LicenseDescriptors {
    
    public static final Descriptor<INode>     LICENSE = new Descriptor<>("license", "License", INode.class);                           //$NON-NLS-1$ //$NON-NLS-2$
    
    public static final Descriptor<String>    MAIL    = new Descriptor<>("mail", "Mail", "License", String.class, Strings.EMPTY);
    public static final Descriptor<String>    USER    = new Descriptor<>("user", "User", "License", String.class, Strings.EMPTY);
    public static final Descriptor<String>    TOKEN   = new Descriptor<>("token", "Token", "License", String.class, Strings.EMPTY);
    public static final Descriptor<LocalDate> END     = new Descriptor<>("expiration", "Expiration", "License", LocalDate.class, null);
    
}
