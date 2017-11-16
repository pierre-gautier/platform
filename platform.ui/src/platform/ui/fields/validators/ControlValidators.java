package platform.ui.fields.validators;

import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import platform.ui.messages.Messages;
import platform.utils.Strings;

public final class ControlValidators {
    
    public abstract static class AControlValidator<C extends Control>
            implements IControlValidator<C> {
        
        @Override
        public Image getImage() {
            return ControlValidators.IMAGE_ERROR;
        }
    }
    
    public interface IControlValidator<C extends Control> {
        
        Image getImage();
        
        String getMessage();
        
        boolean validate(C control);
    }
    
    public interface MinMaxProvider<T extends Number> {
        
        T max();
        
        T min();
    }
    
    public static final Image                    IMAGE_REQUIRED                    = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage();
    public static final Image                    IMAGE_WARNING                     = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
    public static final Image                    IMAGE_ERROR                       = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
    public static final Image                    IMAGE_INFO                        = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage();
    
    public static final IControlValidator<Combo> COMBO_NON_EMPTY_VALIDATOR         = new AControlValidator<Combo>() {
                                                                                       
                                                                                       @Override
                                                                                       public String getMessage() {
                                                                                           return Messages.errorComboEmpty;
                                                                                       }
                                                                                       
                                                                                       @Override
                                                                                       public boolean validate(final Combo combo) {
                                                                                           return combo.getText().length() > 0;
                                                                                       }
                                                                                   };
    
    public static final IControlValidator<Text>  INT_POSITIVE_VALIDATOR            = new AControlValidator<Text>() {
                                                                                       
                                                                                       @Override
                                                                                       public String getMessage() {
                                                                                           return Messages.errorTextNotInt;
                                                                                       }
                                                                                       
                                                                                       @Override
                                                                                       public boolean validate(final Text text) {
                                                                                           try {
                                                                                               if (!text.isEnabled()) {
                                                                                                   return true;
                                                                                               }
                                                                                               final int value = Integer.parseInt(text.getText());
                                                                                               return value > 0;
                                                                                           } catch (final NumberFormatException e) {
                                                                                               return false;
                                                                                           }
                                                                                       }
                                                                                   };
    
    public static final IControlValidator<Text>  DOUBLE_POSITIVE_VALIDATOR         = new AControlValidator<Text>() {
                                                                                       
                                                                                       @Override
                                                                                       public String getMessage() {
                                                                                           return Messages.errorTextNotDouble;
                                                                                       }
                                                                                       
                                                                                       @Override
                                                                                       public boolean validate(final Text text) {
                                                                                           try {
                                                                                               if (!text.isEnabled()) {
                                                                                                   return true;
                                                                                               }
                                                                                               final double value = Double.parseDouble(text.getText());
                                                                                               return value >= 0;
                                                                                           } catch (final NumberFormatException e) {
                                                                                               return false;
                                                                                           }
                                                                                       }
                                                                                   };
    
    public static final IControlValidator<Text>  TEXT_NON_EMPTY_VALIDATOR          = new AControlValidator<Text>() {
                                                                                       
                                                                                       @Override
                                                                                       public String getMessage() {
                                                                                           return Messages.errorTextEmpty;
                                                                                       }
                                                                                       
                                                                                       @Override
                                                                                       public boolean validate(final Text text) {
                                                                                           return !text.isEnabled() || text.getText().length() > 0;
                                                                                       }
                                                                                   };
    
    public static final IControlValidator<Text>  TEXT_NO_SPACE_NON_EMPTY_VALIDATOR = new AControlValidator<Text>() {
                                                                                       
                                                                                       @Override
                                                                                       public String getMessage() {
                                                                                           return Messages.errorTextEmptyOrWithSpace;
                                                                                       }
                                                                                       
                                                                                       @Override
                                                                                       public boolean validate(final Text text) {
                                                                                           return !text.isEnabled() || text.getText().length() > 0 && !text.getText().contains(Strings.SPACE);
                                                                                       }
                                                                                   };
    
    public static IControlValidator<Text> createDoubleValidator(final MinMaxProvider<Double> provider) {
        return new AControlValidator<Text>() {
            
            private String message;
            
            @Override
            public String getMessage() {
                return this.message;
            }
            
            @Override
            public boolean validate(final Text text) {
                try {
                    if (!text.isEnabled()) {
                        return true;
                    }
                    final double value = Double.parseDouble(text.getText());
                    if (value < provider.min() || value > provider.max()) {
                        this.message = NLS.bind(Messages.errorInterval, provider.min(), provider.max());
                        return false;
                    }
                    return true;
                } catch (final NumberFormatException e) {
                    this.message = Messages.errorTextNotDouble;
                    return false;
                }
            }
        };
    }
    
    public static IControlValidator<Text> createIntValidator(final MinMaxProvider<Integer> provider) {
        return new AControlValidator<Text>() {
            
            private String message;
            
            @Override
            public String getMessage() {
                return this.message;
            }
            
            @Override
            public boolean validate(final Text text) {
                try {
                    if (!text.isEnabled()) {
                        return true;
                    }
                    final int value = Integer.parseInt(text.getText());
                    if (value < provider.min() || value > provider.max()) {
                        this.message = NLS.bind(Messages.errorInterval, provider.min(), provider.max());
                        return false;
                    }
                    return true;
                } catch (final NumberFormatException e) {
                    this.message = Messages.errorTextNotInt;
                    return false;
                }
            }
            
        };
    }
    
    private ControlValidators() {
        // hide constructor
    }
    
}
