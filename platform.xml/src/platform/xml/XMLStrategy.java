package platform.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import platform.model.AStrategy;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.factory.NodeFactories;
import platform.model.factory.RelationFactories;
import platform.model.io.Serializer;
import platform.model.utils.NodeUtils;
import platform.utils.Strings;

public class XMLStrategy
        extends AStrategy {
    
    private static final String RELATION = "relation"; //$NON-NLS-1$
    private static final String ROOT     = "root";     //$NON-NLS-1$
    private static final String NODE     = "node";     //$NON-NLS-1$
    
    private static INode readNode(final XMLStreamReader reader, final IRoot root)
            throws XMLStreamException {
        final String id = reader.getAttributeValue(null, Strings.ID);
        final Descriptor<? extends INode> type = Descriptor.getDescriptor(reader.getAttributeValue(null, Strings.TYPE));
        if (Strings.isNullEmptyOrBlank(id) || type == null) {
            return null;
        }
        final Collection<Attribute> attributes = new ArrayList<>(reader.getAttributeCount() - 2);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String name = reader.getAttributeLocalName(i);
            final Attribute attribute = Serializer.deserialize(name, reader.getAttributeValue(i));
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        final INode node = NodeFactories.INSTANCE.create(type, id, attributes, root);
        Collection<IRelation> relations = null;
        while (reader.hasNext()
                && reader.nextTag() == XMLStreamConstants.START_ELEMENT
                && reader.getLocalName().equals(XMLStrategy.RELATION)) {
            final IRelation relation = XMLStrategy.readRelation(reader, node);
            if (relation != null) {
                if (relations == null) {
                    relations = new LinkedList<>();
                }
                relations.add(relation);
            }
            if (reader.hasNext()
                    && reader.getEventType() == XMLStreamConstants.END_ELEMENT
                    && reader.getLocalName().equals(XMLStrategy.NODE)) {
                reader.nextTag();
            }
        }
        if (relations != null) {
            node.addRelations(relations);
        }
        return node;
    }
    
    private static IRelation readRelation(final XMLStreamReader reader, final INode source)
            throws XMLStreamException {
        final String id = reader.getAttributeValue(null, Strings.ID);
        final Descriptor<? extends IRelation> type = Descriptor.getDescriptor(reader.getAttributeValue(null, Strings.TYPE));
        if (Strings.isNullEmptyOrBlank(id) || type == null) {
            return null;
        }
        final Collection<Attribute> attributes = new ArrayList<>(reader.getAttributeCount() - 2);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String name = reader.getAttributeLocalName(i);
            final Attribute attribute = Serializer.deserialize(name, reader.getAttributeValue(i));
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        INode target = null;
        if (reader.hasNext()
                && reader.nextTag() == XMLStreamConstants.START_ELEMENT
                && reader.getLocalName().equals(XMLStrategy.NODE)) {
            target = XMLStrategy.readNode(reader, source.getRoot());
        }
        if (target == null) {
            return null;
        }
        return RelationFactories.INSTANCE.create(type, id, attributes, source, target);
    }
    
    private static void writeNode(final XMLStreamWriter writer, final INode node)
            throws XMLStreamException {
        writer.writeStartElement(node instanceof IRoot ? XMLStrategy.ROOT : XMLStrategy.NODE);
        XMLStrategy.writeObject(writer, node);
        for (final IRelation relation : node.getRelations()) {
            XMLStrategy.writeRelation(writer, relation);
        }
        writer.writeEndElement();
    }
    
    private static void writeObject(final XMLStreamWriter writer, final IObject object)
            throws XMLStreamException {
        writer.writeAttribute(Strings.ID, object.getId());
        writer.writeAttribute(Strings.TYPE, object.getType().getId());
        for (final Attribute attribute : object.getAttributes()) {
            final String value = Serializer.serialize(attribute);
            if (!Strings.isNullEmptyOrBlank(value)) {
                writer.writeAttribute(attribute.getDescriptor().getId(), value);
            }
        }
    }
    
    private static void writeRelation(final XMLStreamWriter writer, final IRelation relation)
            throws XMLStreamException {
        writer.writeStartElement(XMLStrategy.RELATION);
        XMLStrategy.writeObject(writer, relation);
        XMLStrategy.writeNode(writer, relation.getTarget());
        writer.writeEndElement();
    }
    
    private final File    file;
    private final IRoot   root;
    private final boolean autoSave;
    
    public XMLStrategy(final IRoot root, final File file, final boolean autoSave) {
        this.autoSave = autoSave;
        this.root = root;
        this.file = file;
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        if (this.autoSave) {
            this.save();
        }
    }
    
    public void indent()
            throws TransformerException, IOException {
        final File tmp = File.createTempFile(UUID.randomUUID().toString(), null);
        try (Reader in = new FileReader(this.file); Writer out = new FileWriter(tmp)) {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1"); //$NON-NLS-1$ //$NON-NLS-2$
            transformer.transform(new StreamSource(in), new StreamResult(out));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        try (OutputStream out = new FileOutputStream(this.file)) {
            Files.copy(tmp.toPath(), out);
        }
        tmp.delete();
    }
    
    public void load() {
        if (!this.file.exists()) {
            return;
        }
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setXMLReporter((message, typeErreur, source, location) -> System.out.println("Error type : " + typeErreur + ", message : " + message)); //$NON-NLS-1$ //$NON-NLS-2$
        try (final FileReader fileReader = new FileReader(this.file)) {
            final XMLStreamReader reader = factory.createXMLStreamReader(fileReader);
            while (reader.hasNext()) {
                if (reader.nextTag() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(XMLStrategy.ROOT)) {
                    final INode loadedRoot = XMLStrategy.readNode(reader, this.root);
                    if (loadedRoot != null) {
                        NodeUtils.merge(this.root, loadedRoot);
                    }
                    break;
                }
            }
            reader.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        if (this.autoSave) {
            this.save();
        }
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        if (this.autoSave) {
            this.save();
        }
    }
    
    public void save() {
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try (final FileOutputStream output = new FileOutputStream(this.file)) {
            final XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output, Strings.UTF8);
            writer.writeStartDocument(Strings.UTF8, "1.0"); //$NON-NLS-1$
            XMLStrategy.writeNode(writer, this.root);
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            this.indent();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
}
