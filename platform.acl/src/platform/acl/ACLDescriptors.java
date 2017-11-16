package platform.acl;

import platform.model.Descriptor;

public class ACLDescriptors {
    
    public static final Descriptor<Boolean> CREATE   = new Descriptor<>("create", "Create", Boolean.class);     //$NON-NLS-1$ //$NON-NLS-2$
    public static final Descriptor<Boolean> RETRIEVE = new Descriptor<>("retrieve", "Retrieve", Boolean.class); //$NON-NLS-1$ //$NON-NLS-2$
    public static final Descriptor<Boolean> UPDATE   = new Descriptor<>("update", "Update", Boolean.class);     //$NON-NLS-1$ //$NON-NLS-2$
    public static final Descriptor<Boolean> DELETE   = new Descriptor<>("delete", "Delete", Boolean.class);     //$NON-NLS-1$ //$NON-NLS-2$
    
}
