public class TestNet {

    static String Path = "C:/Users/ישראל/DS/Algo_for_AI/Esighnment/alarm_net.xml";
    static String Path2 = "C:/Users/ישראל/DS/Algo_for_AI/Esighnment/big_net.xml";

    public static void main(String[] args) throws Exception {
        Net net = new Net(Path);
        System.out.println(net);

        System.out.println(net.BaiseBall("B-E|A=T"));//should return true
        System.out.println(net.BaiseBall("B-E|"));//false
        System.out.println(net.BaiseBall("J-M|B=T,E=F"));//true
        System.out.println(net.BaiseBall("J-M|A=T"));//false
        System.out.println(net.BaiseBall("J-E|B=F"));//true
        System.out.println(net.BaiseBall("J-E|J=F"));//false
        System.out.println(net.BaiseBall("M-B|J=T,A=T,E=T"));//false


        net = new Net(Path2);
        System.out.println(net);

        System.out.println(net.BaiseBall("D1-D2|A3=T"));

    }
}
