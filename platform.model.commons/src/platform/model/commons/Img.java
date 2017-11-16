package platform.model.commons;

import platform.utils.Strings;

public class Img {
    
    public static final Img NULL = new Img(Strings.EMPTY);
    
    private final String    path;
    
    public Img(final String path) {
        super();
        this.path = path;
    }
    
    public String getPath() {
        return this.path;
    }
    
    @Override
    public String toString() {
        return this.getPath();
    }
    
}
