package platform.ui.actions.edit;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import platform.model.IRelation;
import platform.utils.collections.CollectionsUtils;

public class Clipboard {
    
    private static final Map<String, Clipboard> clipboards = new HashMap<>(1);
    
    public static Clipboard getClipboard(final String name) {
        Clipboard instance = Clipboard.clipboards.get(name);
        if (instance == null) {
            instance = new Clipboard();
            Clipboard.clipboards.put(name, instance);
        }
        return instance;
    }
    
    private Collection<WeakReference<IRelation>> copied;
    
    private Clipboard() {
        super();
    }
    
    public void copy(final Collection<IRelation> relations) {
        if (CollectionsUtils.isNullOrEmpty(relations)) {
            this.copied = null;
            return;
        }
        this.copied = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            this.copied.add(new WeakReference<>(relation));
        }
    }
    
    public Collection<IRelation> getCopied() {
        if (this.copied == null) {
            return Collections.emptyList();
        }
        final Collection<IRelation> relations = new ArrayList<>(this.copied.size());
        for (final WeakReference<IRelation> reference : this.copied) {
            final IRelation relation = reference.get();
            if (relation != null) {
                relations.add(relation);
            }
        }
        return relations;
    }
    
}
