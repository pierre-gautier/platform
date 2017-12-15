package platform.ui.controls.tree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IAttributeListener;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.IRelationListener;
import platform.model.IRoot;
import platform.model.ISelectionListener;
import platform.model.commons.Descriptors;
import platform.model.utils.NodeUtils;
import platform.ui.controls.tree.Overlay.Position;
import platform.ui.swt.SWTUtils;

public class ModelTreeContentProvider
        implements ISelectionChangedListener,
        ITreeContentProvider,
        IAttributeListener,
        ISelectionListener,
        IRelationListener,
        ILabelProvider,
        IColorProvider {
    
    private class ImageOverlay {
        
        protected Position position;
        protected String   imagePath;
        
        public ImageOverlay(final Position position, final String imagePath) {
            this.position = position;
            this.imagePath = imagePath;
        }
    }
    
    private final IRoot                                  root;
    private final TreeViewer                             viewer;
    private final Set<Descriptor<IRelation>>             types;
    private final Collection<Descriptor<?>>              labelDescriptors = new HashSet<>(1);
    private final Map<Descriptor<Boolean>, ImageOverlay> imageOverlayMap  = new HashMap<>(4);
    private final Collection<WeakReference<INode>>       models           = new ArrayList<>(64);
    
    private boolean                                      updatingSelection;
    
    public ModelTreeContentProvider(final TreeViewer viewer, final IRoot root, final Set<Descriptor<IRelation>> types) {
        this.root = root;
        this.types = types;
        this.viewer = viewer;
        this.labelDescriptors.add(Descriptors.LABEL);
        this.labelDescriptors.add(Descriptors.ACTIVE);
        this.root.registerSelectionListener(this);
    }
    
    public void addLabelDescriptor(final Descriptor<?> descriptor) {
        this.labelDescriptors.add(descriptor);
    }
    
    @Override
    public void addListener(final ILabelProviderListener listener) {
        // do nothing
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        for (final Attribute value : attributes) {
            if (this.labelDescriptors.contains(value.getDescriptor())) {
                this.viewer.update(NodeUtils.findParentRelation(this.root, object.getId()), null);
                break;
            }
        }
        
    }
    
    @Override
    public void dispose() {
        this.root.unregisterSelectionListener(this);
        for (final WeakReference<INode> reference : this.models) {
            final INode model = reference.get();
            if (model != null) {
                model.unregisterRelationListener(this);
                model.unregisterAttributeListener(this);
            }
        }
    }
    
    @Override
    public Color getBackground(final Object element) {
        return null;
    }
    
    @Override
    public Object[] getChildren(final Object parentElement) {
        final IRelation relation = (IRelation) parentElement;
        final Collection<IRelation> relations = relation.getTarget().getRelations(this.types);
        this.register(relations);
        return relations.toArray(new Object[relations.size()]);
    }
    
    @Override
    public Object[] getElements(final Object inputElement) {
        final INode input = (INode) inputElement;
        final Collection<IRelation> relations = input.getRelations(this.types);
        this.register(relations);
        return relations.toArray(new Object[relations.size()]);
    }
    
    @Override
    public Color getForeground(final Object element) {
        final INode target = ((IRelation) element).getTarget();
        if (!target.getAttribute(Descriptors.ACTIVE)) {
            return SWTUtils.getSystemColor(SWT.COLOR_GRAY);
        }
        return null;
    }
    
    @Override
    public Image getImage(final Object element) {
        final INode target = ((IRelation) element).getTarget();
        final Image defaultImage = SWTUtils.getImageFromUrl(target.getAttribute(Descriptors.IMAGE).getPath(), 16, 16);
        
        if (!this.imageOverlayMap.entrySet().parallelStream().anyMatch(e -> target.getAttribute(e.getKey()))) {
            return defaultImage;
        }
        
        final ImageDescriptor id = ImageDescriptor.createFromImage(defaultImage);
        final Overlay oi = new Overlay(id, new Point(16, 16));
        
        for (final Entry<Descriptor<Boolean>, ImageOverlay> entry : this.imageOverlayMap.entrySet()) {
            if (target.getAttribute(entry.getKey())) {
                final ImageDescriptor icon = ImageDescriptor.createFromImage(SWTUtils.getImageFromUrl(entry.getValue().imagePath, 8, 8));
                oi.addOverlayIcon(entry.getValue().position, icon);
            }
        }
        
        return oi.createImage();
    }
    
    @Override
    public Object getParent(final Object element) {
        final INode parentNode = ((IRelation) element).getSource();
        return NodeUtils.findParentRelation(this.root, parentNode.getId());
    }
    
    @Override
    public String getText(final Object element) {
        return ((IRelation) element).getTarget().getAttribute(Descriptors.LABEL);
    }
    
    @Override
    public boolean hasChildren(final Object element) {
        return !((IRelation) element).getTarget().getRelations(this.types).isEmpty();
    }
    
    @Override
    public void inputChanged(final Viewer source, final Object oldInput, final Object newInput) {
        Assert.isTrue(oldInput == null || newInput == null);
        if (oldInput != null) {
            final INode input = (INode) oldInput;
            input.unregisterAttributeListener(this);
            input.unregisterRelationListener(this);
        }
        if (newInput != null) {
            final INode input = (INode) newInput;
            input.registerAttributeListener(this);
            input.registerRelationListener(this);
        }
    }
    
    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return this.labelDescriptors.contains(Descriptor.getDescriptor(property));
    }
    
    public void registerImageOverlay(final Descriptor<Boolean> descriptor, final Position position, final String imagePath) {
        this.addLabelDescriptor(descriptor);
        this.imageOverlayMap.put(descriptor, new ImageOverlay(position, imagePath));
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> relations) {
        this.relationsRemoved(node, relations);
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> relations) {
        final IRelation parentRelation = NodeUtils.findParentRelation(this.root, node.getId());
        this.viewer.refresh(parentRelation);
    }
    
    public boolean removeImageOverlay(final Descriptor<Boolean> pd) {
        this.labelDescriptors.remove(pd);
        return this.imageOverlayMap.remove(pd) != null;
    }
    
    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // do nothing
    }
    
    @Override
    public void selectionChanged(final Object source, final IRoot repository) {
        if (source != this.viewer) {
            final Collection<IRelation> selections = this.root.getSelections();
            if (selections.isEmpty()) {
                this.viewer.setSelection(null);
                return;
            }
            final List<IRelation> actualSelections = new ArrayList<>(selections.size());
            for (final IRelation selection : selections) {
                for (final Descriptor<?> type : this.types) {
                    if (selection.getType().equals(type)) {
                        actualSelections.add(selection);
                        break;
                    }
                }
            }
            this.updatingSelection = true;
            if (!actualSelections.isEmpty()) {
                this.viewer.setSelection(new StructuredSelection(actualSelections));
            }
            this.updatingSelection = false;
        }
    }
    
    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        if (this.updatingSelection) {
            return;
        }
        final StructuredSelection selection = (StructuredSelection) event.getSelection();
        if (selection.isEmpty()) {
            this.root.setSelection(this.viewer, null);
            return;
        }
        final Object[] selectionArray = selection.toArray();
        final Collection<IRelation> relations = new ArrayList<>(selectionArray.length);
        for (final Object object : selectionArray) {
            relations.add((IRelation) object);
        }
        this.root.setSelection(this.viewer, relations);
    }
    
    private void register(final Collection<IRelation> relations) {
        for (final IRelation relation : relations) {
            relation.getTarget().registerAttributeListener(this);
            relation.getTarget().registerRelationListener(this);
            this.models.add(new WeakReference<>(relation.getTarget()));
        }
    }
    
}
