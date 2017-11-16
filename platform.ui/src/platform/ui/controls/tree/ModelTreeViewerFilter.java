package platform.ui.controls.tree;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import platform.model.Descriptor;
import platform.model.IAttributeListener;
import platform.model.IRelation;
import platform.ui.controls.filter.FilterData;
import platform.ui.controls.filter.IFilter;

public class ModelTreeViewerFilter
        extends ViewerFilter
        implements IFilter {
    
    private FilterData                      filterData;
    private final Collection<Descriptor<?>> candidateProperties;
    private TreeViewer                      viewer;
    
    public ModelTreeViewerFilter(final Collection<Descriptor<?>> candidateProperties, final Collection<Descriptor<?>> exclude, final Set<Descriptor<IRelation>> relationTypes) {
        super();
        Assert.isTrue(candidateProperties != null && !candidateProperties.isEmpty(), "candidate properties must not be null or empty"); //$NON-NLS-1$
        this.candidateProperties = candidateProperties;
        this.filterData = new FilterData("", false, false, candidateProperties, exclude, relationTypes); //$NON-NLS-1$
    }
    
    @Override
    public Collection<Descriptor<?>> getCandidateProperties() {
        return this.candidateProperties;
    }
    
    @Override
    public FilterData getFilterData() {
        return this.filterData;
    }
    
    @Override
    public boolean isFilterProperty(final Object element, final String property) {
        for (final Descriptor<?> pd : this.filterData.descriptors) {
            if (pd.getId().equals(property)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void registerPropertiesListener(final IAttributeListener listener) {
        // do nothing
    }
    
    @Override
    public boolean select(final Viewer source, final Object parentElement, final Object element) {
        return !this.filterData.filter((IRelation) element);
    }
    
    @Override
    public void setFilterData(final FilterData data) {
        final boolean unfilter = this.filterData.needUnfilterTo(data);
        final boolean filter = this.filterData.needFilterTo(data);
        if (unfilter || filter) {
            this.filterData = data;
            this.viewer.collapseAll();
            this.viewer.refresh();
            if (filter) {
                this.viewer.expandToLevel(2);
            }
        }
    }
    
    public void setViewer(final TreeViewer viewer) {
        this.viewer = viewer;
    }
    
    @Override
    public void unregisterPropertiesListener(final IAttributeListener listener) {
        // do nothing
    }
    
}
