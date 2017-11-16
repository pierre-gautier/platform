package platform.ui.workbench.properties.editors;

import javax.xml.ws.Holder;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import platform.model.commons.Pos;
import platform.ui.fields.Form;
import platform.ui.fields.IForm;
import platform.ui.fields.IntegerTextField;
import platform.ui.fields.validators.ControlValidators;
import platform.ui.fields.validators.ControlValidators.MinMaxProvider;
import platform.ui.messages.Messages;
import platform.ui.swt.MoveShellListener;
import platform.ui.swt.SWTUtils;

public class PosCellEditor
        extends DialogCellEditor {
    
    private IntegerTextField xField;
    private IntegerTextField yField;
    private final String     yLabel;
    private final String     xLabel;
    private final int        max;
    private final int        min;
    
    public PosCellEditor(final Composite parent, final String xLabel, final String yLabel, final int min, final int max) {
        super(parent);
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.min = min;
        this.max = max;
    }
    
    @Override
    protected Object openDialogBox(final Control cellEditorWindow) {
        
        final Display display = SWTUtils.getDisplay();
        
        final Shell shell = SWTUtils.createShellTool(display, 1, true, false, true, 5, true);
        shell.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_ESCAPE) {
                shell.close();
            }
        });
        
        final Composite composite = SWTUtils.createComposite(shell, 2, true, 5, 5);
        
        final MinMaxProvider<Integer> minMaxProvider = new MinMaxProvider<Integer>() {
            
            @Override
            public Integer max() {
                return PosCellEditor.this.max;
            }
            
            @Override
            public Integer min() {
                return PosCellEditor.this.min;
            }
        };
        
        final IForm form = new Form();
        
        final Pos value = (Pos) this.getValue();
        final Holder<Pos> result = new Holder<>(value);
        
        this.xField = new IntegerTextField(SWTUtils.createText(composite), this.xLabel, value.x.intValue(), form, ControlValidators.createIntValidator(minMaxProvider));
        this.yField = new IntegerTextField(SWTUtils.createText(composite), this.yLabel, value.y.intValue(), form, ControlValidators.createIntValidator(minMaxProvider));
        
        form.registerValidationTrackingControl(SWTUtils.createButtonDefault(composite, SWT.END, SWT.CENTER, Messages.keywordOk, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                result.value = Pos.create(PosCellEditor.this.xField.getValue(), PosCellEditor.this.yField.getValue());
                shell.close();
            }
        }));
        
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
        shell.forceActive();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        return result.value;
        
    }
    
}
