package platform.whatsup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import platform.model.AStrategy;
import platform.model.Attribute;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;

public class WhatsupStrategy
        extends AStrategy {
    
    private final int                size;
    private final List<WhatsupEvent> list;
    
    private int                      modifier;
    
    public WhatsupStrategy(final int size) {
        this.size = size;
        this.list = new ArrayList<>(size);
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        this.check();
        this.list.add(new WhatsupEvent(object.getType().getId(), object.getId(), "attributesChanged")); //$NON-NLS-1$
    }
    
    public int getCurrentIndex() {
        return this.modifier + this.list.size();
    }
    
    public List<WhatsupEvent> getEventsFrom(final int index) {
        if (index < this.modifier && index > this.modifier + this.size) {
            return Collections.emptyList();
        }
        return this.list.subList(index - this.modifier, this.list.size());
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        this.check();
        this.list.add(new WhatsupEvent(node.getType().getId(), node.getId(), "relationsAdded")); //$NON-NLS-1$
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        this.check();
        this.list.add(new WhatsupEvent(node.getType().getId(), node.getId(), "relationsRemoved")); //$NON-NLS-1$
    }
    
    private void check() {
        if (this.list.size() == this.size) {
            this.modifier += this.list.size();
            this.list.clear();
        }
    }
}
