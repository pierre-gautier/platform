package platform.model.utils;

import java.util.HashSet;

import platform.model.IObject;

public class TraversalContext
        extends HashSet<IObject> {
    
    private static final long serialVersionUID = 1620197614770176866L;
    private int               level;
    
    public TraversalContext() {
        this(10, -1);
    }
    
    public TraversalContext(final int size, final int level) {
        super(size);
        this.level = level;
    }
    
    public boolean traverseNextLevel() {
        return this.level == -1 || this.level-- > 0;
    }
    
}
