package platform.ui.fields;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import platform.ui.fields.validators.ControlValidators.IControlValidator;
import platform.ui.messages.Messages;
import platform.ui.swt.SWTUtils;

public class FileField
        extends TextWithButtonField {
    
    private final String[] extLabels;
    private final String[] exts;
    private final int      style;
    
    public FileField(final Composite parent, final String label, final String value, final IForm form, final IControlValidator<Text> validator, final int style) {
        this(parent, label, value, form, validator, style, null, null);
    }
    
    public FileField(final Composite parent, final String label, final String value, final IForm form, final IControlValidator<Text> validator, final int style, final String[] exts,
            final String[] extLabels) {
        super(parent, label, Messages.keywordBrowse, value, form, validator);
        Assert.isTrue(exts == null || extLabels != null && exts.length == extLabels.length, "extension labels must contain as much entry as extensions does"); //$NON-NLS-1$
        this.extLabels = extLabels == null ? null : Arrays.copyOf(extLabels, extLabels.length);
        this.exts = exts == null ? null : Arrays.copyOf(exts, exts.length);
        this.style = style;
    }
    
    @Override
    protected String getButtonValue() {
        final FileDialog fileDialog = new FileDialog(SWTUtils.getDisplay().getActiveShell(), this.style);
        fileDialog.setText("Pick a file"); //$NON-NLS-1$
        fileDialog.setFileName(this.value);
        fileDialog.setFilterPath(this.value);
        if (this.exts != null && this.exts.length > 0) {
            fileDialog.setFilterExtensions(this.exts);
            fileDialog.setFilterNames(this.extLabels);
        }
        return fileDialog.open();
    }
    
}
