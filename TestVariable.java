import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestVariable {

    static Variable subject;
    static String Path = "C:/Users/ישראל/DS/Algo_for_AI/Esighnment/alarm_net.xml";


    public static void main(String args[]) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(Path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList var = doc.getElementsByTagName("VARIABLE");
        NodeList cpts = doc.getElementsByTagName("DEFINITION");

        Node node = var.item(2);
        Element eElement = (Element) node;
        String name = eElement.getElementsByTagName("NAME").item(0).getTextContent();
        subject = new Variable(name);
        System.out.println(subject);

        node = var.item(0);
        eElement = (Element) node;
        name = eElement.getElementsByTagName("NAME").item(0).getTextContent();
        Variable father = new Variable(name);

        node = var.item(1);
        eElement = (Element) node;
        name = eElement.getElementsByTagName("NAME").item(0).getTextContent();
        Variable mother = new Variable(name);

        String[] values = {"T", "F"};
        father.values = values;
        values = new String[]{"T", "F"};
        mother.values = values;
        values = new String[]{"T", "F"};
        subject.values = values;
        subject.parents = new LinkedList<Variable>();
        subject.parents.add(mother);
        subject.parents.add(father);

        System.out.println("Parents:");
        System.out.println(subject.parents.get(0));
        System.out.println(subject.parents.get(1));

        node = cpts.item(2);
        eElement = (Element) node;
        NodeList probabilties = eElement.getElementsByTagName("TABLE");
        subject.setCPT(probabilties.item(0).getTextContent());
        System.out.println(subject);

    }
}
