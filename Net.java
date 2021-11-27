import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Net {

    List<Variable>[] AdjList; //Adjacency list to represent the graph

    public Net(String file_name) throws ParserConfigurationException, IOException, SAXException {
        NodeList[] var_and_cpts = extractXML(file_name);
        NodeList var = var_and_cpts[0];
        NodeList cpts = var_and_cpts[1];
        this.AdjList = new List[var.getLength()];
        for (int i = 0; i < AdjList.length; i++){
            this.AdjList[i] = new LinkedList<Variable>();
        }
        buildNET(var, cpts);
    }

    private static NodeList[] extractXML(String file_name) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(file_name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList var = doc.getElementsByTagName("VARIABLE");
        NodeList cpts = doc.getElementsByTagName("DEFINITION");

        NodeList[] ans = {var, cpts};
        return ans;
    }

    private void buildNET(NodeList var, NodeList cpts){

        //initalising the network's nodes/variables
        for (int itr = 0; itr < var.getLength(); itr++) {
            Node node = var.item(itr);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                String name = eElement.getElementsByTagName("NAME").item(0).getTextContent();
                Variable v = new Variable(name);
                this.AdjList[itr].add(v);

                NodeList val = eElement.getElementsByTagName("OUTCOME");
                int i = 0;
                while (val.item(i) != null) {
                    i++;
                }
                v.values = new String[i];
                i = 0;
                while (val.item(i) != null) {
                    v.values[i] = val.item(i).getTextContent();
                    i++;
                }

                v.parents = new LinkedList<Variable>();
            }
        }

            //making sure each node is reachable by their parents
            for (int itr = 0; itr < cpts.getLength(); itr++) {
                Node node = cpts.item(itr);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    String name = eElement.getElementsByTagName("FOR").item(0).getTextContent();
                    Variable curr = this.getNode(name);

                    // adding to the variable's parent's list itself
                    NodeList parents = eElement.getElementsByTagName("GIVEN");
                    int i = 0;
                    while (parents.item(i) != null) {
                        List<Variable> parent = this.AdjList[this.find_var(parents.item(i).getTextContent())]; //the parents list of
                        curr.parents.add(parent.get(0));
                        parent.add(curr);                                                            //reachable nodes(sons)
                        i++;
                    }

                    // updating the PCT
                    NodeList probabilties = eElement.getElementsByTagName("TABLE");
                    curr.setCPT(probabilties.item(0).getTextContent());
                }
            }

    }

    private int find_var(String name){
        for (int i = 0; i < this.AdjList.length; i++){
            if(this.AdjList[i].get(0).name.equals(name)){
                return i;
            }
        }
        return -1;
    }

    public Variable getNode(String name){
        return this.AdjList[find_var(name)].get(0);
    }

    public boolean BaiseBall(String question) {
        String src = "" + question.charAt(0);
        String dest = "" + question.charAt(2);
        List<String> knowns = new LinkedList<String>();

        int i = 4;
        while (i < question.length()){
            String name = "";
            char ch = question.charAt(i);;
            while (ch != '='){
                name += ch;
                i++;
                ch = question.charAt(i);
            }
            while (i < question.length() && ch != ','){
                ch = question.charAt(i);
                i++;
            }i++;
            knowns.add(name);
        }

        boolean[] beenTo = new boolean[this.AdjList.length];
        for (i = 0; i < beenTo.length; i++){
            beenTo[i] = false;
        }

        return baiseball(src, dest, knowns, beenTo, false);
    }

    private boolean baiseball(String src, String dest, List<String> knowns, boolean[] beenTo, boolean fromParent){
        if(src.equals(dest)){
            return true;
        }
        int start_num = this.find_var(src);
        Variable start = this.getNode(src);
        beenTo[start_num] = true;

        boolean ans = false;

        if(fromParent){
            if(knowns.contains(start.name)){
                for (int i = 0; i < start.parents.size(); i++){
                    if (ans){
                        break;
                    }

                    String parent_name = start.parents.get(i).name;
                    if (knowns.contains(start.parents.get(i).name)){
                        continue;
                    }else if(! beenTo[this.find_var(parent_name)]){
                        ans = ans  || baiseball(parent_name, dest, knowns, beenTo, false);
                    }
                }
            }else {
                for (int i = 1; i < this.AdjList[start_num].size(); i++){
                    if (ans){
                        break;
                    }

                    String sons_name = this.AdjList[start_num].get(i).name;
                    if(! beenTo[this.find_var(sons_name)]){
                        ans = ans  || baiseball(sons_name, dest, knowns, beenTo, true);
                    }
                }
            }
        }else if (! knowns.contains(start.name)){
            for (int i = 1; i < this.AdjList[start_num].size(); i++){
                if (ans){
                    break;
                }

                String sons_name = this.AdjList[start_num].get(i).name;
                if(! beenTo[this.find_var(sons_name)]){
                    ans = ans  || baiseball(sons_name, dest, knowns, beenTo, true);
                }
            }
            for (int i = 0; i < start.parents.size(); i++){
                if (ans){
                    break;
                }

                String parent_name = start.parents.get(i).name;
                if (knowns.contains(start.parents.get(i).name)){
                    continue;
                }else if(! beenTo[this.find_var(parent_name)]){
                    ans = ans  || baiseball(parent_name, dest, knowns, beenTo, false);
                }
            }
        }

        return ans;
    }

    public double[][] Variablle_Elimnation(List<double[][]> factors){
        return null;
    }

    public double compute(String question) {
        return 0;
    }

    public String toString(){
        String s = "";
        for (int i = 0; i < this.AdjList.length; i++){
            s += "the sons of element" + " " + this.AdjList[i].get(0).name + " and itself\n";
            s += this.AdjList[i] + "\n";
        }
        return s;
    }
}
