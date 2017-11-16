package platform.model.commons;

import java.util.Arrays;

import platform.model.ARoot;
import platform.model.Attribute;
import platform.model.IRoot;
import platform.model.ISelectionListener;

public class Master
        extends ARoot
        implements ISelectionListener {
    
    public static final Master INSTANCE = new Master();
    
    protected Master() {
        super(Types.ROOT, Types.ROOT.getId(), Arrays.asList(new Attribute(Descriptors.LABEL, Types.ROOT.getId())), null);
    }
    
    @Override
    public void selectionChanged(final Object source, final IRoot root) {
        this.setSelection(source, root.getSelections());
    }
    
}
