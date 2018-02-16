import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.impl.reportgenerator.XmlToHtmlWithXsltReportGenerator;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.model.ReportServiceException;

public class TestXmlToHtmlReportGenerator extends XmlToHtmlWithXsltReportGenerator {
    @Autowired
    private CollectionsService collectionsService;

    @Override
    public InputStream getXmlData(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> parameters) {
        try {

            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select * from person");

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("persons");
            doc.appendChild(rootElement);
            
            for (IdentifiableObject identifiableObject : collection) {
                Element person = doc.createElement("person");
                rootElement.appendChild(person);

                Attr attr = doc.createAttribute("id");
                attr.setValue(identifiableObject.getId().toStringRepresentation());
                person.setAttributeNode(attr);

                attr = doc.createAttribute("login");
                attr.setValue(identifiableObject.getString("login"));
                person.setAttributeNode(attr);

                attr = doc.createAttribute("firstname");
                attr.setValue(identifiableObject.getString("firstname"));
                person.setAttributeNode(attr);

                attr = doc.createAttribute("lastname");
                attr.setValue(identifiableObject.getString("lastname"));
                person.setAttributeNode(attr);

                attr = doc.createAttribute("email");
                attr.setValue(identifiableObject.getString("email"));
                person.setAttributeNode(attr);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(resultStream);
            transformer.transform(source, result);

            return new ByteArrayInputStream(resultStream.toByteArray());
        } catch (Exception ex) {
            throw new ReportServiceException("Error create xml with report data", ex);
        }
    }

}
