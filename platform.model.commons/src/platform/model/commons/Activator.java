package platform.model.commons;

import java.time.Instant;
import java.time.LocalDate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import platform.model.factory.NodeFactories;
import platform.model.factory.NodeFactory;
import platform.model.factory.RelationFactory;
import platform.model.io.Formatter;
import platform.model.io.Serializer;
import platform.utils.Strings;

public final class Activator
        implements BundleActivator {
    
    static {
        new Serializer<String>(String.class) {
            
            @Override
            public String toObject(final String input) {
                if (Strings.isNullEmptyOrBlank(input)) {
                    return Strings.EMPTY;
                }
                return input;
            }
        };
        new Serializer<Long>(Long.class) {
            
            @Override
            public Long toObject(final String input) {
                try {
                    return Long.parseLong(input);
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
        };
        new Serializer<Boolean>(Boolean.class) {
            
            @Override
            public Boolean toObject(final String input) {
                return Boolean.parseBoolean(input);
            }
        };
        new Serializer<Instant>(Instant.class) {
            
            @Override
            public Instant toObject(final String input) {
                try {
                    return Instant.ofEpochSecond(Long.parseLong(input));
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
            
            @Override
            public String toString(final Object input) {
                return String.valueOf(((Instant) input).getEpochSecond());
            }
        };
        new Serializer<LocalDate>(LocalDate.class) {
            
            @Override
            public LocalDate toObject(final String input) {
                try {
                    return LocalDate.ofEpochDay(Long.parseLong(input));
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
            
            @Override
            public String toString(final Object input) {
                return String.valueOf(((LocalDate) input).toEpochDay());
            }
        };
        new Serializer<Col>(Col.class) {
            
            @Override
            public Col toObject(final String input) {
                if (Strings.isNullEmptyOrBlank(input)) {
                    return null;
                }
                final String[] values = input.split(Strings.SHARP);
                if (values.length != 3) {
                    return null;
                }
                try {
                    return new Col(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
            
            @Override
            public String toString(final Object input) {
                final Col col = (Col) input;
                return String.valueOf(col.getRed()) + Strings.SHARP + String.valueOf(col.getGreen()) + Strings.SHARP + String.valueOf(col.getBlue());
            }
        };
        new Serializer<Img>(Img.class) {
            
            @Override
            public Img toObject(final String input) {
                return new Img(input);
            }
            
        };
        new Serializer<Pos>(Pos.class) {
            
            @Override
            public Pos toObject(final String input) {
                if (Strings.isNullEmptyOrBlank(input)) {
                    return null;
                }
                final String[] values = input.split(Strings.COMMA);
                if (values.length != 2) {
                    return null;
                }
                try {
                    return Pos.create(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
        };
    }
    
    static {
        new Formatter<Img>(Img.class) {
            
            @Override
            public String toString(final Img input) {
                return Strings.EMPTY;
            }
        };
        new Formatter<Col>(Col.class) {
            
            @Override
            public String toString(final Col input) {
                return Strings.EMPTY;
            }
        };
    }
    
    @Override
    public void start(final BundleContext bundleContext) {
        RelationFactory.INSTANCE.registerType(Types.RELATION, Relation.class);
        NodeFactory.INSTANCE.registerType(Types.NODE, Node.class);
        NodeFactories.INSTANCE.register(RootFactory.INSTANCE);
        
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
