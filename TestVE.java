import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestVE {

    static String Path = "C:/Users/ישראל/DS/Algo_for_AI/Esighnment/alarm_net.xml";

    public static void main(String[] args) throws Exception {
        Net net = new Net(Path);
        System.out.println(net.compute("P(B=T|J=T,M=T)"));
        System.out.println(net.compute("P(J=T|B=T)"));
    }
}
