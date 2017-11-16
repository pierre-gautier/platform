package platform.model.commons;

import java.time.Instant;

import platform.model.Descriptor;
import platform.utils.Strings;

@SuppressWarnings("nls")
public class Descriptors {
    
    @SuppressWarnings("rawtypes")
    public static final Descriptor<Descriptor> TYPE   = new Descriptor<>(Strings.TYPE, "Type", "System", Descriptor.class, null);
    public static final Descriptor<String>     ID     = new Descriptor<>(Strings.ID, "ID", "System", String.class, null);
    
    public static final Descriptor<Instant>    DATE   = new Descriptor<>("date", "Date", "Basic", Instant.class, null);
    
    public static final Descriptor<String>     LABEL  = new Descriptor<>("label", "Label", "Basic", String.class, Strings.EMPTY);
    public static final Descriptor<Boolean>    ACTIVE = new Descriptor<>("active", "Active", "Basic", Boolean.class, Boolean.TRUE);
    
    public static final Descriptor<Col>        COLOR  = new Descriptor<>("color", "Color", "Graphics", Col.class, Col.NULL);
    public static final Descriptor<Img>        IMAGE  = new Descriptor<>("image", "Image", "Graphics", Img.class, Img.NULL);
    public static final Descriptor<Pos>        SIZE   = new Descriptor<>("size", "Size", "Graphics", Pos.class, Pos.NULL);
    
}
