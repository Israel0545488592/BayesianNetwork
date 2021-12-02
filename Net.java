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

import java.util.Arrays;
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

        return new NodeList[]{var, cpts};
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

                    // updating the CPT
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
        String src = question.substring(0, question.indexOf('-'));
        String dest = question.substring(question.indexOf('-') +1, question.indexOf('|'));
        List<String> knowns = new LinkedList<>();

        int i = question.indexOf('|') +1;
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
            }
            knowns.add(name);
        }

        List<String> beenTo = new LinkedList<String>();

        return baiseball(src, dest, knowns, beenTo, false);
    }

    private boolean baiseball(String src, String dest, List<String> knowns, List<String> beenTo, boolean fromParent){
        if(src.equals(dest)){
            return true;
        }
        int start_num = this.find_var(src);
        Variable start = this.getNode(src);

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
                    }else if(! beenTo.contains(start.name + parent_name)){
                        beenTo.add(start.name + parent_name);
                        ans = ans  || baiseball(parent_name, dest, knowns, beenTo, false);
                    }
                }
            }else {
                for (int i = 1; i < this.AdjList[start_num].size(); i++){
                    if (ans){
                        break;
                    }

                    String sons_name = this.AdjList[start_num].get(i).name;
                    if(! beenTo.contains(start.name + sons_name)){
                        beenTo.add(start.name +sons_name);
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
                if(! beenTo.contains(start.name + sons_name)){
                    beenTo.add(start.name + sons_name);
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
                }else if(! beenTo.contains(start.name + parent_name)){
                    beenTo.add(start.name + parent_name);
                    ans = ans  || baiseball(parent_name, dest, knowns, beenTo, false);
                }
            }
        }

        return ans;
    }

    public String compute(String question) {
        String ans;

        if (question.charAt(0) == 'P'){
            ans = String.valueOf(this.probabilty(question.substring(2, question.indexOf(')'))));
        }else {
            if (this.BaiseBall(question)){
                ans = "no";
            }else {
                ans = "yes";
            }
        }

        return ans;
    }

    private String probabilty(String question){
        List<String> knowns = new LinkedList<String>();
        int i = 0, j = 0;

        while (i < question.length()){
            while (j < question.length() && ! ("|,".contains("" + question.charAt(j)))){
                j++;
            }
            knowns.add(question.substring(i, j));
            i = j+1;
            j++;
        }

        String vars = "";
        for (i = 0; i < this.AdjList.length; i++){
            vars += AdjList[i].get(0).name;
            vars += ',';
        }
        String[] questions = ChainRule(vars);

        return this.VE(questions, knowns);
    }

    private String[] ChainRule(String questions){
        String[] ans = questions.split(",");

        if(ans.length > 1){

            String[] tmp = Arrays.copyOf(ans, ans.length);
            for (int i = 1; i < ans.length; i++){

                ans[i] += '|';
                int j = i -1;
                while (j >= 0){
                    ans[i] += tmp[j];
                    if (j != 0){
                        ans[i] += ',';
                    }
                    j--;
                }
            }
        }

        return ans;
    }

    public String VE(String[] questions, List<String> knowns){//Variable_Elimination
        questions = getRidOf_IndependentVariables(questions);

        List<String> unknows = new LinkedList<>();
        for (int i = 0; i < this.AdjList.length; i++){
            Variable v = this.AdjList[i].get(0);
            boolean isKnown = false;
            for (int j = 0; j < knowns.size(); j++){
                if (knowns.get(j).contains(v.name)){
                    isKnown = true;
                    break;
                }
            }
            if (! isKnown){
                unknows.add(v.name);
            }
        }

        String[] factorChain = new String[questions.length + unknows.size()];
        factorChain = arrange(factorChain, unknows, questions);

        //freeFactors( dont involve hidden variables)
        for (int i = 0, j = 0; i < questions.length; i++){
            if (questions[i] != null){
                factorChain[j] = questions[i];
                j++;
            }
        }

        //Join factors
        List<String[][]> caces = new LinkedList<>();
        List<double[]> values = new LinkedList<double[]>();
        int additions = 0, multleplictions = 0;

        for (int i = 0; i < factorChain.length; i++){
            if (! factorChain[i].contains("sum")){
                caces.add(this.getNode(getNameBeforeSighn(factorChain[i])).CPT_cases);
                values.add(this.getNode(getNameBeforeSighn(factorChain[i])).CPT_values);
            }
        }

        for (int i = factorChain.length -1; i >= 1; i--){
            if (! factorChain[i].contains("sum")){
                if (! factorChain[i-1].contains("sum")){

                    String common = "";
                    double[] factor1_val = values.get(values.size() -1);
                    double[] factor2_val = values.get(values.size() -2);
                    String[][] factor1_cace = caces.get(caces.size() -1);
                    String[][] factor2_cace = caces.get(caces.size() -2);
                    for (int j = 0; j < factor1_cace[0].length; j++){
                        for (int l = 0; l < factor2_cace[0].length; l++){
                            if (getNameBeforeSighn(factor1_cace[0][j]).equals(getNameBeforeSighn(factor2_cace[0][l]))){
                                common += getNameBeforeSighn(factor1_cace[0][j]) + ',';
                                break;
                            }
                        }
                    }
                    common = common.substring(0, common.length() -1);
                    String[] Common_names = common.split(",");

                    //allocating the memory for the joined factor
                    int width = factor1_cace[0].length + factor2_cace[0].length - Common_names.length;
                    int len = this.getNode(getNameBeforeSighn(factor1_cace[0][0])).values.length;
                    for (int j = 1; j < factor1_cace[0].length; j++){
                        len *= this.getNode(getNameBeforeSighn(factor1_cace[0][j])).values.length;
                    }
                    for (int j = 0; j < factor2_cace[0].length; j++){
                        len *= this.getNode(getNameBeforeSighn(factor2_cace[0][j])).values.length;
                    }
                    for (int j = 0; j < Common_names.length; j++){
                        len /= this.getNode(Common_names[j]).values.length;
                    }

                    String[][] NewFactor_cace = new String[len][width];
                    double[] newFactor_val = new double[len];
                    int loc = 0;

                    for (int j = 0; j < factor1_cace.length; j++){
                        String[] valuesOf_common_variables = new String[Common_names.length];
                        String[] cas = factor1_cace[j];
                        for (int l = 0; l < Common_names.length; l++){
                            String name = Common_names[l];
                            for (int I = 0; I < cas.length; I++){
                                if (getNameBeforeSighn(cas[I]).equals(name)){
                                    valuesOf_common_variables[l] = cas[I];
                                }
                            }
                        }

                        for (int l = 0; l < factor2_val.length; l++){
                            boolean compatable = true;
                            for (String valuesOf_common_variable : valuesOf_common_variables) {
                                boolean flag = false;
                                String val = valuesOf_common_variable;
                                for (int J = 0; J < factor2_cace[l].length; J++) {
                                    if (getNameBeforeSighn(factor2_cace[l][J]).equals(getNameBeforeSighn(val))) {
                                        if (!factor2_cace[l][J].equals(val)) {
                                            compatable = false;
                                            flag = true;
                                            break;
                                        }
                                    }
                                }

                                if (flag) {
                                    break;
                                }
                            }

                            if (compatable) {
                                int loc2 = 0;
                                for (String ca : cas) {
                                    if (!common.contains(getNameBeforeSighn(ca))) {
                                        NewFactor_cace[loc][loc2] = ca;
                                        loc2++;
                                    }
                                }
                                for (int I = 0; I < factor2_cace[l].length; I++) {
                                    if (!common.contains(getNameBeforeSighn(factor2_cace[l][I]))) {
                                        NewFactor_cace[loc][loc2] = factor2_cace[l][I];
                                        loc2++;
                                    }
                                }
                                for (int I = 0; I < cas.length; I++) {
                                    if (common.contains(getNameBeforeSighn(cas[I]))) {
                                        NewFactor_cace[loc][loc2] = cas[I];
                                        loc2++;
                                    }
                                }

                                newFactor_val[loc] = factor1_val[j] * factor2_val[l];
                                multleplictions += 1;
                                loc++;
                            }
                        }
                    }

                    caces.remove(caces.size() -1);
                    caces.remove(caces.size() -1);
                    caces.add(NewFactor_cace);
                    values.remove( values.size() -1);
                    values.remove( values.size() -1);
                    values.add(newFactor_val);
                }else {
                    String over = factorChain[i-1].substring(factorChain[i-1].indexOf('r') +1);
                    Variable v = this.getNode(over);
                    int val_count = v.values.length;

                    double[] factor_val = values.get(values.size() -1);
                    String[][] factor_cace = caces.get(caces.size() -1);

                    String[][] NewFactor_cace = new String[factor_cace.length / val_count][factor_cace[0].length -1];
                    double[] newFactor_val = new double[factor_cace.length / val_count];

                    int loc = 0;
                    List<Integer> beenTheir = new LinkedList<Integer>();

                    for (int j = 0; j < factor_cace.length; j++){
                        if (! beenTheir.contains(j)){
                            String[] cace = factor_cace[j];

                            int l = 0;
                            while (l < cace.length){
                                if (getNameBeforeSighn(cace[l]).equals(over)){
                                    break;
                                }
                                l++;
                            }
                            String val = getValue(cace[l]);

                            String[] NewCase = new String[cace.length -1];
                            l = 0;
                            for (String s : cace) {
                                if (!getNameBeforeSighn(s).equals(over)) {
                                    NewCase[l] = s;
                                    l++;
                                }
                            }

                            double newVal = factor_val[j];
                            for (l = 0; l < factor_cace.length; l++){
                                if (l != j){
                                    String[] cace2 = factor_cace[l];

                                    boolean compatable = true;
                                    for (int I = 0; I < cace2.length; I++){
                                        if (getNameBeforeSighn(cace2[I]).equals(over)){
                                            if ( getValue(cace2[I]).equals(getValue(cace[I]))){
                                                compatable = false;
                                                break;
                                            }
                                        }else {
                                            if (! getValue(cace2[I]).equals(getValue(cace[I]))){
                                                compatable = false;
                                                break;
                                            }
                                        }
                                    }

                                    if (compatable){
                                        beenTheir.add(l);
                                        newVal += factor_val[l];
                                        additions++;
                                    }
                                }
                            }

                            NewFactor_cace[loc] = NewCase;
                            newFactor_val[loc] = newVal;
                            loc++;
                        }
                    }

                    caces.remove(caces.size() -1);
                    caces.add(NewFactor_cace);
                    values.remove( values.size() -1);
                    values.add(newFactor_val);
                }
            }else {
                factorChain[i] = factorChain[i+1];
                i++;
            }
        }

        //final factor
        String[][] Result_caces = caces.get(caces.size() -1);
        double[] Result_values = values.get(values.size() -1);


        Variable target = this.getNode(getNameBeforeSighn(knowns.get(0)));
        String[] vals = target.values;

        //normalizing
        int i = locationInTable(Result_values, Result_caces, knowns);
        double result = Result_values[i];
        double normlizer = 0;
        for (int j = 0; j < vals.length; j++){
            List<String> l = new LinkedList<String>();

            l.add(target.name + "=" + vals[j]);
            for (int I = 1; I < knowns.size(); I++){
                l.add(knowns.get(I));
            }

            normlizer += Result_values[locationInTable(Result_values, Result_caces, l)];
        }
        additions += vals.length;

        String FinalAns = String.valueOf(Result_values[i] / normlizer);
        if (FinalAns.length() > 7){
            FinalAns = FinalAns.substring(0, 7);
        }
        return  FinalAns  + "," + additions + "," + multleplictions;
    }

    private int locationInTable(double[] Result_values, String[][] Result_caces, List<String> knowns){
        int i = 0;

        for ( i = 0; i < Result_values.length; i++){
            String[] cas = Result_caces[i];
            boolean TheOne = true;

            for (int j = 0; j < knowns.size(); j++){
                String known = knowns.get(j);
                known = known.substring(0, known.indexOf('=')) + " = " + known.substring(known.indexOf('=') +1);
                boolean included = true;
                int l;
                for (l = 0; l < cas.length; l++){
                    if (known.equals(cas[l])){
                        break;
                    }
                }
                if (l == cas.length){
                    included = false;
                }
                if (! included){
                    TheOne = false;
                    break;
                }

            }
            if (TheOne){
                break;
            }
        }

        return i;
    }

    private String[] arrange(String[] factorChain, List<String> unknows, String[] questions){

        for (int i = 0, j = 0, l; i < unknows.size(); i++) {
            String unknown = unknows.get(i);
            l = 0;
            while (l < questions.length) {
                if (questions[l] != null){
                    if (questions[l].contains(unknown)) {
                        factorChain[factorChain.length - 1 - j] = questions[l];
                        questions[l] = null;
                        j++;
                    }
                }
                l++;
            }
            factorChain[factorChain.length - 1 - j] = "sumOver" + unknown;
            j++;
        }

        return factorChain;
    }

    private String[] getRidOf_IndependentVariables(String[] questions){//change to private after debug
        for (int i = 1; i < questions.length; i++){
            String question = questions[i];
            String var = question.substring(0, question.indexOf('|'));
            String givens = question.substring(question.indexOf('|') +1);
            String toEliminate = "";

            for (int j = 0; j < givens.length(); j++){
                String name = "";
                while (j < givens.length() && givens.charAt(j) != ','){
                    name += givens.charAt(j);
                    j++;
                }

                String condition = "";
                for (int l = 0; l < givens.length(); l++){
                    String name2 = "";
                    while (l < givens.length() && givens.charAt(l) != ','){
                        name2 += givens.charAt(l);
                        l++;
                    }
                    if (! name2.equals(name)){
                        condition += name2 + "=T,";
                    }
                }
                if (condition.length() != 0){
                    condition = condition.substring(0, condition.length() -1);
                }

                if (! this.BaiseBall(var + "-" + name + "|" + condition)){
                    toEliminate += name;
                }
            }

            questions[i] = Eliminate(question, toEliminate);
        }

        return questions;
    }

    private String Eliminate(String question, String toEliminate){
        int i = question.indexOf('|') +1;
        while (i < question.length()){
            int j = i+1;
            while (j < question.length() && question.charAt(j) != ','){
                j++;
            }
            if(toEliminate.contains(question.substring(i, j))){
                question = question.substring(0, i) + question.substring(j);
            }
            i++;
        }

        while ( "|,".contains("" + question.charAt(question.length() -1))){
            question = question.substring(0, question.length() -1);
        }

        return question;
    }

    private String getName(String s){
        int i = 0;
        while (i < s.length() && !(",|=".contains("" + s.charAt(i)))){
            i++;
        }i--;
        return s.substring(i);
    }

    private String getNameBeforeSighn(String s){
        int i = 0;
        while (i < s.length() && !(",|= ".contains("" + s.charAt(i)))){
            i++;
        }
        return s.substring(0, i);
    }

    private  String getValue(String s){
        return s.substring(getName(s).length());
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
