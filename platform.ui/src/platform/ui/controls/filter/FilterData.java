package platform.ui.controls.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;

import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.io.Formatter;
import platform.utils.collections.CollectionsUtils;

public final class FilterData {
    
    public final String                      value;
    public final boolean                     caseSensitive;
    public final boolean                     useRegex;
    public final Collection<Descriptor<?>>   descriptors;
    
    private final Pattern                    pattern;
    private final String                     actualValue;
    private final Set<Descriptor<IRelation>> relationTypes;
    private final Collection<Descriptor<?>>  exclude;
    
    public FilterData(final String value, final boolean caseSensitive, final boolean useRegex, final Collection<Descriptor<?>> descriptors) {
        this(value, caseSensitive, useRegex, descriptors, null, null);
    }
    
    public FilterData(final String value, final boolean caseSensitive, final boolean useRegex, final Collection<Descriptor<?>> descriptors,
            final Collection<Descriptor<?>> exclude, final Set<Descriptor<IRelation>> relationTypes) {
        Assert.isNotNull(value);
        Assert.isTrue(!CollectionsUtils.isNullOrEmpty(descriptors));
        this.caseSensitive = caseSensitive;
        this.value = value;
        this.actualValue = caseSensitive ? value : value.toLowerCase();
        this.useRegex = useRegex;
        this.pattern = useRegex ? Pattern.compile(value, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE) : null;
        this.descriptors = new ArrayList<>(descriptors);
        this.relationTypes = relationTypes;
        this.exclude = CollectionsUtils.isNullOrEmpty(descriptors) ? Collections.<Descriptor<?>>emptyList() : exclude;
    }
    
    public FilterData(final String value, final FilterData parameters) {
        this(value, parameters.caseSensitive, parameters.useRegex, parameters.descriptors, parameters.exclude, parameters.relationTypes);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final FilterData other = (FilterData) obj;
        if (!this.value.equals(other.value)) {
            return false;
        }
        return true;
    }
    
    /**
     * @param node
     * @return true if node has to be filtered out
     */
    @SuppressWarnings("unchecked")
    public boolean filter(final INode node) {
        final StringBuilder text = new StringBuilder();
        for (final Descriptor<?> pd : this.descriptors) {
            final Object v = node.getAttribute(pd);
            if (v != null) {
                final Formatter<Object> pf = (Formatter<Object>) Formatter.getFormatter(pd.getClazz());
                if (pf != null) {
                    text.append(pf.toString(v));
                } else {
                    text.append(v.toString());
                }
            }
        }
        final String source = this.caseSensitive ? text.toString() : text.toString().toLowerCase();
        if (this.useRegex && this.pattern.matcher(source).find() || source.contains(this.actualValue)) {
            return false;
        }
        return true;
    }
    
    /**
     * @param node
     * @return true if relation has to be filtered out
     */
    
    public boolean filter(final IRelation relation) {
        final INode node = relation.getTarget();
        if (!this.filter(node)) {
            return false;
        }
        if (this.exclude.contains(node.getType()) && !this.filter(relation.getSource())) {
            return false;
        }
        for (final IRelation child : node.getRelations(this.relationTypes)) {
            if (!this.filter(child)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.value.hashCode();
        return result;
    }
    
    public boolean needFilterTo(final FilterData other) {
        if (this.useRegex != other.useRegex) {
            return true;
        }
        if (!this.caseSensitive && other.caseSensitive) {
            return true;
        }
        if (!this.value.startsWith(other.value)) {
            return true;
        }
        if (!this.descriptors.containsAll(other.descriptors)) {
            return true;
        }
        return false;
    }
    
    public boolean needUnfilterTo(final FilterData other) {
        if (this.useRegex != other.useRegex) {
            return true;
        }
        if (this.caseSensitive && !other.caseSensitive) {
            return true;
        }
        if (!other.value.startsWith(this.value)) {
            return true;
        }
        if (!(other.descriptors.containsAll(this.descriptors) && this.descriptors.containsAll(other.descriptors))) {
            return true;
        }
        return false;
    }
    
}
