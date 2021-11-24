import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;



public class TestXML_conversion {

    static String Path = "C:/Users/ישראל/DS/Algo_for_AI/Esighnment/alarm_net.xml";

    public static void main(String[] args) throws Exception {
        File file = new File(Path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList var = doc.getElementsByTagName("VARIABLE");
        NodeList cpts = doc.getElementsByTagName("DEFINITION");

        System.out.println(var.getLength() + "\n\n");

        for (int itr = 0; itr < var.getLength(); itr++) {
            Node node = var.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                System.out.println("name: " + eElement.getElementsByTagName("NAME").item(0).getTextContent());
                System.out.println("value: " + eElement.getElementsByTagName("OUTCOME").item(0).getTextContent());
                System.out.println("value: " + eElement.getElementsByTagName("OUTCOME").item(1).getTextContent());

            }
        }

        System.out.println("\n\n\n");

        for (int itr = 0; itr < cpts.getLength(); itr++) {
            Node node = cpts.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                System.out.println("name: " + eElement.getElementsByTagName("FOR").item(0).getTextContent());
                NodeList parents = eElement.getElementsByTagName("GIVEN");
                int i = 0;
                while (parents.item(i) != null){
                        System.out.println("parent: " + parents.item(i).getTextContent());
                    i++;
                }

            }
        }

        for (int itr = 0; itr < cpts.getLength(); itr++) {
            Node node = cpts.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                 NodeList probabilties = eElement.getElementsByTagName("TABLE");
                System.out.println("values: " + probabilties.item(0).getTextContent());
            }
        }


    }

}
