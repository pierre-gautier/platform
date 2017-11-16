package platform.ui.swt;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import platform.model.commons.Pos;
import platform.utils.Strings;

public final class SWTUtils {
    
    private static final String             OS                     = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
    private static final boolean            IS_WINDOWS             = SWTUtils.OS.contains("win");                 //$NON-NLS-1$
    
    private static final RGB                RESIZE_TRANSPARENT_RGB = new RGB(255, 255, 255);
    private static final int                DEFAULT_BUTTON_WIDTH   = 100;
    
    private static final Map<String, Image> images                 = new HashMap<>(8);
    
    public static Button createButton(final Composite parent, final int style, final Image image, final GridData data, final SelectionListener listener) {
        final Button button = SWTUtils.createButton(parent, style, Strings.EMPTY, data, listener);
        button.setImage(image);
        return button;
    }
    
    public static Button createButton(final Composite parent, final int style, final String text, final GridData data, final SelectionListener listener) {
        final Button button = new Button(parent, style);
        button.setLayoutData(data);
        button.setText(text);
        if (listener != null) {
            button.addSelectionListener(listener);
        }
        return button;
    }
    
    public static Button createButtonCheck(final Composite parent) {
        return SWTUtils.createButtonCheck(parent, Strings.EMPTY);
    }
    
    public static Button createButtonCheck(final Composite parent, final String label) {
        return SWTUtils.createButton(parent, SWT.CHECK, label, SWTUtils.createGridData(SWT.BEGINNING, SWT.CENTER, SWT.DEFAULT, SWT.DEFAULT), null);
    }
    
    public static Button createButtonDefault(final Composite parent, final int alignH, final int alignV, final String text, final SelectionListener listener) {
        final Button defaultButton = SWTUtils.createButtonPush(parent, alignH, alignV, text, listener);
        parent.getShell().setDefaultButton(defaultButton);
        return defaultButton;
    }
    
    public static Button createButtonPush(final Composite parent, final GridData data, final Image image, final SelectionListener listener) {
        return SWTUtils.createButton(parent, SWT.PUSH, image, data, listener);
    }
    
    public static Button createButtonPush(final Composite parent, final GridData data, final String text, final SelectionListener listener) {
        return SWTUtils.createButton(parent, SWT.PUSH, text, data, listener);
    }
    
    public static Button createButtonPush(final Composite parent, final int alignH, final int alignV, final Image image, final SelectionListener listener) {
        final Button button = SWTUtils.createButton(parent, SWT.PUSH, Strings.EMPTY, SWTUtils.createGridData(alignH, alignV, SWT.DEFAULT, SWT.DEFAULT), listener);
        button.setImage(image);
        return button;
    }
    
    public static Button createButtonPush(final Composite parent, final int alignH, final int alignV, final String text, final SelectionListener listener) {
        return SWTUtils.createButton(parent, SWT.PUSH, text, SWTUtils.createGridData(alignH, alignV, SWTUtils.DEFAULT_BUTTON_WIDTH, SWT.DEFAULT), listener);
    }
    
    public static Combo createCombo(final Composite parent, final int style, final Object[] values, final GridData data, final SelectionListener listener) {
        final Combo combo = new Combo(parent, style | SWT.BORDER);
        combo.setLayoutData(data);
        if (values != null) {
            for (final Object value : values) {
                combo.add(value.toString());
                combo.setData(value.toString(), value);
            }
        }
        if (listener != null) {
            combo.addSelectionListener(listener);
        }
        return combo;
    }
    
    public static Composite createComposite(final Composite parent, final int columns, final boolean equal) {
        return SWTUtils.createComposite(parent, columns, equal, SWT.DEFAULT, SWT.DEFAULT, SWTUtils.createGridDataFill());
    }
    
    public static Composite createComposite(final Composite parent, final int columns, final boolean equal, final int margins, final int spacing) {
        return SWTUtils.createComposite(parent, columns, equal, margins, spacing, SWTUtils.createGridDataFill());
    }
    
    public static Composite createComposite(final Composite parent, final int columns, final boolean equal, final int margins, final int spacing, final GridData data) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(columns, equal, margins, spacing));
        composite.setLayoutData(data);
        return composite;
    }
    
    public static Composite createComposite(final Composite parent, final int columns, final boolean equal, final int margins, final int spacing, final int hspan, final int vspan) {
        final GridData gd = SWTUtils.createGridDataFill();
        gd.horizontalSpan = hspan;
        gd.verticalSpan = vspan;
        return SWTUtils.createComposite(parent, columns, equal, margins, spacing, gd);
    }
    
    public static DateTime createDateTime(final Composite parent, final String label, final Calendar calendar, final int style, final SelectionListener listener) {
        Assert.isTrue(style == SWT.TIME || style == SWT.DATE || style == SWT.CALENDAR);
        if (label != null) {
            SWTUtils.createLabel(parent, label, SWT.BEGINNING, SWT.CENTER, 100, SWT.DEFAULT);
        }
        final DateTime dateTime = new DateTime(parent, SWT.BORDER | SWT.DROP_DOWN | style);
        dateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        dateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dateTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        dateTime.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                calendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds());
            }
        });
        if (listener != null) {
            dateTime.addSelectionListener(listener);
        }
        return dateTime;
    }
    
    public static GridData createGridData(final int alignH, final int alignV, final int width, final int height) {
        final GridData data = new GridData(alignH, alignV, alignH == SWT.FILL, alignV == SWT.FILL);
        data.widthHint = width;
        data.heightHint = height;
        return data;
    }
    
    public static GridData createGridDataFill() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }
    
    public static GridData createGridDataHorizontalFill() {
        return new GridData(SWT.FILL, SWT.CENTER, true, false);
    }
    
    public static GridLayout createGridLayout(final int columns, final boolean equals, final int margins, final int spacing) {
        final GridLayout layout = new GridLayout(columns, equals);
        layout.marginHeight = margins == SWT.DEFAULT ? layout.marginHeight : margins;
        layout.marginWidth = margins == SWT.DEFAULT ? layout.marginWidth : margins;
        layout.horizontalSpacing = spacing == SWT.DEFAULT ? layout.horizontalSpacing : spacing;
        layout.verticalSpacing = spacing == SWT.DEFAULT ? layout.verticalSpacing : spacing;
        return layout;
    }
    
    public static Group createGroup(final Composite parent, final String label, final int margins, final int columnNumber, final boolean equal, final GridData data) {
        final Group group = new Group(parent, SWT.NONE);
        group.setLayout(SWTUtils.createGridLayout(columnNumber, equal, margins, margins));
        group.setLayoutData(data);
        group.setText(label);
        return group;
    }
    
    public static Label createLabel(final Composite parent, final String text) {
        return SWTUtils.createLabel(parent, text, SWT.END);
    }
    
    public static Label createLabel(final Composite parent, final String text, final int hAlign) {
        return SWTUtils.createLabel(parent, text, hAlign, SWT.CENTER, SWT.DEFAULT, SWT.DEFAULT);
    }
    
    public static Label createLabel(final Composite parent, final String text, final int alignH, final int alignV, final int width, final int height) {
        final Label label = new Label(parent, SWT.WRAP | SWT.BEGINNING);
        label.setLayoutData(SWTUtils.createGridData(alignH, alignV, width, height));
        if (text != null) {
            label.setText(text);
        }
        return label;
    }
    
    public static Text createPassword(final Composite parent) {
        return SWTUtils.createText(parent, SWT.PASSWORD | SWT.BORDER, SWT.DEFAULT, SWT.DEFAULT);
    }
    
    public static Button createRadioButton(final Composite parent, final String label) {
        return SWTUtils.createButton(parent, SWT.RADIO, label, SWTUtils.createGridData(SWT.BEGINNING, SWT.CENTER, SWT.DEFAULT, SWT.DEFAULT), null);
    }
    
    public static CustomScale createScale(final Composite parent, final String label, final int value, final int max) {
        if (label != null) {
            SWTUtils.createLabel(parent, label);
        }
        final CustomScale scale = new CustomScale(parent, SWT.NONE);
        scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        scale.setPageIncrement(max / 10);
        scale.setMaximum(max);
        scale.setIncrement(1);
        scale.setSelection(value);
        return scale;
    }
    
    public static Shell createShellDialog() {
        final Shell shell = new Shell(SWTUtils.getDisplay(), SWT.DIALOG_TRIM | SWT.ON_TOP);
        shell.setLayout(new GridLayout());
        return shell;
    }
    
    public static Shell createShellTool(final Display display, final int columns, final boolean modal, final boolean resize, final boolean onTop, final int whites, final boolean equals) {
        final Shell shell = new Shell(display, SWT.TOOL | (onTop ? SWT.ON_TOP : 0) | (modal ? SWT.APPLICATION_MODAL : 0) | (resize ? SWT.RESIZE : 0));
        shell.setLayout(SWTUtils.createGridLayout(columns, equals, whites, whites));
        return shell;
    }
    
    public static Label createSpanner(final Composite parent, final int columns, final int rows) {
        final Label spanner = new Label(parent, SWT.NONE);
        spanner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, columns, rows));
        return spanner;
    }
    
    public static Spinner createSpiner(final Composite composite, final String label, final int value, final int min, final int max, final int inc, final int pageInc) {
        if (label != null) {
            SWTUtils.createLabel(composite, label);
        }
        final Spinner spinner = new Spinner(composite, SWT.BORDER);
        spinner.setLayoutData(SWTUtils.createGridData(SWT.FILL, SWT.CENTER, SWT.DEFAULT, SWT.DEFAULT));
        spinner.setValues(value, min, max, 0, inc, pageInc);
        return spinner;
    }
    
    public static Text createText(final Composite parent) {
        return SWTUtils.createText(parent, SWT.BORDER, SWT.DEFAULT, SWT.DEFAULT);
    }
    
    public static Text createText(final Composite parent, final int style, final int height, final int width) {
        final Text text = new Text(parent, style);
        text.setLayoutData(SWTUtils.createGridData(SWT.FILL, SWT.CENTER, width, height));
        text.addFocusListener(new FocusAdapter() {
            
            @Override
            public void focusGained(final FocusEvent e) {
                text.selectAll();
            }
        });
        return text;
    }
    
    public static Monitor getCursorMonitor() {
        final Display display = SWTUtils.getDisplay();
        final Point cursor = display.getCursorLocation();
        Monitor monitor = null;
        for (final Monitor m : display.getMonitors()) {
            if (m.getBounds().contains(cursor)) {
                monitor = m;
            }
        }
        if (monitor == null) {
            monitor = display.getPrimaryMonitor();
        }
        return monitor;
    }
    
    public static Display getDisplay() {
        final Display d = Display.getCurrent();
        if (d != null) {
            return d;
        }
        return Display.getDefault();
    }
    
    public static Image getImageFromUrl(final String path) {
        return SWTUtils.getImageFromUrl(path, SWT.DEFAULT, SWT.DEFAULT, false);
    }
    
    public static Image getImageFromUrl(final String path, final int width, final int height) {
        return SWTUtils.getImageFromUrl(path, width, height, false);
    }
    
    public static synchronized Image getImageFromUrl(final String path, final int width, final int height, final boolean keepRatio) {
        if (Strings.isNullEmptyOrBlank(path)) {
            return null;
        }
        Pos size = Pos.create(width, height);
        final String key = path + size.toString();
        Image image = SWTUtils.images.get(key);
        if (image != null && !image.isDisposed()) {
            return image;
        }
        try {
            final URL url = new URL(path);
            try (final InputStream stream = url.openStream()) {
                image = new Image(Display.getDefault(), stream);
                final Pos imageSize = Pos.create(image.getImageData().width, image.getImageData().height);
                if (width != SWT.DEFAULT && imageSize.x.intValue() != width
                        || height != SWT.DEFAULT && imageSize.y.intValue() != height) {
                    if (keepRatio) {
                        size = Pos.toRatio(imageSize, Pos.create(width, height));
                    }
                    image = SWTUtils.resizeImage(image, size.x.intValue(), size.y.intValue(), true);
                }
                final Image previous = SWTUtils.images.put(key, image);
                if (previous != null && !previous.isDisposed()) {
                    previous.dispose();
                }
                return image;
            }
        } catch (final Exception e) {
            return null;
        }
    }
    
    public static Color getSystemColor(final int color) {
        return SWTUtils.getDisplay().getSystemColor(color);
    }
    
    public static Image resizeImage(final Image source, final int width, final int height, final boolean disposeSource) {
        final Rectangle bounds = source.getBounds();
        if (bounds.width == width && bounds.height == height
                || width <= 0 || height <= 0) {
            return source;
        }
        final Image resizedImage = new Image(SWTUtils.getDisplay(), width, height);
        final GC gc = new GC(resizedImage);
        gc.setInterpolation(SWT.HIGH);
        gc.setAntialias(SWT.ON);
        gc.setAdvanced(true);
        gc.drawImage(source, 0, 0, bounds.width, bounds.height, 0, 0, width, height);
        gc.dispose();
        
        final ImageData sourceData = source.getImageData();
        if (disposeSource) {
            source.dispose();
        }
        final ImageData resizedData = resizedImage.getImageData();
        if (sourceData.getTransparencyType() == SWT.TRANSPARENCY_PIXEL) {
            final RGB rgb = sourceData.palette.getRGB(sourceData.transparentPixel);
            resizedData.transparentPixel = resizedData.palette.getPixel(rgb);
            resizedImage.dispose();
            return new Image(SWTUtils.getDisplay(), resizedData);
        } else if (sourceData.getTransparencyType() == SWT.TRANSPARENCY_ALPHA) {
            final byte[] alphaData = new byte[resizedData.data.length / 4];
            for (int i = 0; i < resizedData.data.length / 4; i++) {
                if (SWTUtils.IS_WINDOWS) {
                    alphaData[i] = resizedData.data[i * 4 + 3];
                } else {
                    alphaData[i] = (byte) (resizedData.data[i * 4 + 3] == -1 ? 0 : -1);
                }
            }
            resizedData.alphaData = alphaData;
            resizedImage.dispose();
            return new Image(SWTUtils.getDisplay(), resizedData);
        } else if (sourceData.getTransparencyType() == SWT.TRANSPARENCY_MASK) {
            System.out.println(" ----------------------------------- TRANSPARENCY_MASK "); //$NON-NLS-1$
        }
        return resizedImage;
    }
    
    public static Image transparentImage(final Image source, final RGB backgroundColor, final boolean disposeSource) {
        if (source == null) {
            return null;
        }
        final ImageData data = source.getImageData();
        if (backgroundColor == null) {
            return SWTUtils.transparentImage(source, SWTUtils.RESIZE_TRANSPARENT_RGB, disposeSource);
        }
        try {
            data.alpha = -1;
            data.maskData = null;
            data.transparentPixel = data.palette.getPixel(backgroundColor);
            return new Image(SWTUtils.getDisplay(), data);
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            return source;
        } finally {
            if (disposeSource) {
                source.dispose();
            }
        }
    }
    
    private SWTUtils() {
        // hide constructor
    }
    
}
