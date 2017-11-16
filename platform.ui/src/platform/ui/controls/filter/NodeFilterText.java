package platform.ui.controls.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IAttributeListener;
import platform.model.IObject;
import platform.ui.controls.ANodeControl;
import platform.ui.messages.Messages;
import platform.ui.swt.MoveShellListener;
import platform.ui.swt.SWTUtils;

public final class NodeFilterText
        extends ANodeControl<Text>
        implements IAttributeListener,
        IContentProposalListener,
        SelectionListener {
    
    private static final String      FILE_IMAGE_PATH   = "";                                                 //$NON-NLS-1$
    private static final String      SEARCH_IMAGE_PATH = "platform:/plugin/platform.ui/icons/filter_ps.png"; //$NON-NLS-1$
    
    private final Set<Descriptor<?>> properties        = new HashSet<>(8);
    private final IFilter            filter;
    
    private Button                   okButton;
    private Button                   filterButton;
    
    public NodeFilterText(final Composite parent, final boolean displayButton, final IFilter filter) {
        super(parent, null, null);
        Assert.isNotNull(filter, "filter must not be null"); //$NON-NLS-1$
        this.filter = filter;
        this.filter.registerPropertiesListener(this);
        this.getControl().addDisposeListener(this);
        this.initControl();
        if (!displayButton) {
            this.filterButton.dispose();
        }
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        this.initControl();
    }
    
    @Override
    public void proposalAccepted(final IContentProposal proposal) {
        this.filter.setFilterData(new FilterData(this.getControl().getText(), this.filter.getFilterData()));
    }
    
    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
        // do nothing
    }
    
    @Override
    public void widgetDisposed(final DisposeEvent e) {
        super.widgetDisposed(e);
        this.filter.unregisterPropertiesListener(this);
    }
    
    @Override
    public void widgetSelected(final SelectionEvent e) {
        if (e.widget == this.filterButton) {
            this.openFilterShell();
        } else if (e.widget == this.okButton) {
            this.filter();
        }
    }
    
    @Override
    protected final Text createControl(final Composite parent) {
        final Composite composite = SWTUtils.createComposite(parent, 3, false, 0, 0, new GridData(SWT.FILL, SWT.CENTER, false, false));
        final Image imageFilter = SWTUtils.getImageFromUrl(NodeFilterText.FILE_IMAGE_PATH, 16, 16);
        this.filterButton = SWTUtils.createButtonPush(composite, SWT.BEGINNING, SWT.CENTER, imageFilter, this);
        final Text text = SWTUtils.createText(composite);
        text.setMessage(Messages.keywordFilter);
        text.addListener(SWT.Traverse, event -> {
            if (event.detail == SWT.TRAVERSE_RETURN) {
                NodeFilterText.this.filter();
            }
        });
        final Image imageSearch = SWTUtils.getImageFromUrl(NodeFilterText.SEARCH_IMAGE_PATH, 16, 16);
        this.okButton = SWTUtils.createButtonPush(composite, SWT.BEGINNING, SWT.CENTER, imageSearch, this);
        this.filterButton.setBackground(text.getBackground());
        this.okButton.setBackground(text.getBackground());
        composite.setBackground(text.getBackground());
        return text;
    }
    
    @Override
    protected final void initControl() {
        final String value = this.filter.getFilterData().value;
        if (!this.getControl().getText().equals(value)) {
            this.getControl().setText(value);
            this.getControl().setSelection(value.length());
        }
    }
    
    private void filter() {
        BusyIndicator.showWhile(SWTUtils.getDisplay(),
                () -> NodeFilterText.this.filter.setFilterData(new FilterData(NodeFilterText.this.getControl().getText(), NodeFilterText.this.filter.getFilterData())));
    }
    
    private void openFilterShell() {
        
        // create the shell
        final Shell shell = SWTUtils.createShellTool(SWTUtils.getDisplay(), 1, true, false, true, 5, true);
        shell.addListener(SWT.Traverse, event -> {
            if (event.detail == SWT.TRAVERSE_ESCAPE) {
                shell.close();
                event.detail = SWT.TRAVERSE_NONE;
                event.doit = false;
            }
        });
        
        // create a check box for each view descriptor and check it if the view is currently open
        final List<Button> checks = new ArrayList<>(this.filter.getCandidateProperties().size());
        
        if (!this.filter.getCandidateProperties().isEmpty()) {
            
            final Group columnsGroup = SWTUtils.createGroup(shell, "Columns", SWT.DEFAULT, 1, false, SWTUtils.createGridDataFill()); //$NON-NLS-1$
            
            final SelectionListener listener = new SelectionAdapter() {
                
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    if (NodeFilterText.this.okButton == null || NodeFilterText.this.okButton.isDisposed()) {
                        return;
                    }
                    boolean okEnabled = false;
                    for (final Button check : checks) {
                        okEnabled |= check.getSelection();
                    }
                    NodeFilterText.this.okButton.setEnabled(okEnabled);
                }
            };
            
            for (final Descriptor<?> property : this.filter.getCandidateProperties()) {
                final Button check = SWTUtils.createButtonCheck(columnsGroup, property.getLabel());
                check.setData(property);
                check.setSelection(this.filter.getFilterData().descriptors.contains(property));
                check.addSelectionListener(listener);
                checks.add(check);
            }
            
        }
        
        final Group optionsGroup = SWTUtils.createGroup(shell, "Options", SWT.DEFAULT, 1, false, SWTUtils.createGridDataFill()); //$NON-NLS-1$
        
        final Button caseSensitive = SWTUtils.createButtonCheck(optionsGroup, "Case sensitive"); //$NON-NLS-1$
        caseSensitive.setSelection(this.filter.getFilterData().caseSensitive);
        
        final Button useRegex = SWTUtils.createButtonCheck(optionsGroup, "Use regex"); //$NON-NLS-1$
        useRegex.setSelection(this.filter.getFilterData().useRegex);
        
        // create the buttons
        final Composite composite = SWTUtils.createComposite(shell, 2, true, 0, 0);
        
        this.okButton = SWTUtils.createButtonDefault(composite, SWT.END, SWT.CENTER, Messages.keywordOk, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                for (final Button check : checks) {
                    final Descriptor<?> property = (Descriptor<?>) check.getData();
                    if (check.getSelection()) {
                        NodeFilterText.this.properties.add(property);
                    } else {
                        NodeFilterText.this.properties.remove(property);
                    }
                }
                final FilterData parameters = new FilterData(NodeFilterText.this.filter.getFilterData().value, caseSensitive.getSelection(), useRegex.getSelection(), NodeFilterText.this.properties);
                NodeFilterText.this.filter.setFilterData(parameters);
                shell.close();
            }
        });
        
        SWTUtils.createButtonPush(composite, SWT.END, SWT.CENTER, Messages.keywordCancel, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                shell.close();
            }
        });
        
        new MoveShellListener(shell);
        
        final Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final Point pt = SWTUtils.getDisplay().getCursorLocation();
        
        shell.setBounds(pt.x, pt.y, size.x, size.y);
        shell.open();
        
        return;
    }
}
