import java.util.Arrays;
import java.util.List;

public class Variable {
    String name;
    String[] values;
    String[][] CPT_cases;
    double[] CPT_values;
    List<Variable> parents;

    public Variable(String name){
        this.name = name;
        this.values = null;
        this.CPT_values = null;
        this.CPT_cases= null;
    }

    public void setCPT(String val){
        this.setCPT_caces();
        this.setCPT_values(val);
    }

    private void setCPT_caces(){
        int i,j, size = this.values.length; //size of the CPT
        for (i = 0; i < this.parents.size(); i++) {
            Variable parent = this.parents.get(i);
            size *= parent.values.length;
        }
        this.CPT_cases = new String[size][this.parents.size() +1];

        int rotationSize = size;
        for (i = 0; i < this.parents.size(); i++){
            Variable parent = this.parents.get(i);
            rotationSize /= parent.values.length;
            int row = 0;
            while (row < this.CPT_cases.length){
                for(j = 0; j < parent.values.length; j++){
                    String value = parent.values[j];
                    for (int I = row; I < row + rotationSize; I++){
                        this.CPT_cases[I][i] = parent.name + " = " + value;
                    }
                    row += rotationSize;
                }
            }
        }
        rotationSize /= this.values.length;  //last iteration to include the variable's possible values
        int row = 0;
        while (row < this.CPT_cases.length){
            for(j = 0; j < this.values.length; j++){
                String value = this.values[j];
                for (int I = row; I < row + rotationSize; I++){
                    this.CPT_cases[I][i] = this.name + " = " + value;
                }
                row += rotationSize;
            }
        }
    }

    private void setCPT_values(String val){
        this.CPT_values = new double[this.CPT_cases.length];
        int i, j;

        i = 0;
        j = 0;
        while (i < val.length()){
            double[] new_index_and_value = getValue(i,val);
            i = (int) new_index_and_value[0];
            this.CPT_values[j] = new_index_and_value[1];
            j++;
        }
    }

    private double[] getValue(int i, String val){
        if (! (i < val.length())){
            return null;
        }

        int j = i;
        double sum = 0;
        char ch = val.charAt(i);
        while (j < val.length() && ch != ' ' && ch != '.'){
            j++;
            ch = val.charAt(j);
        }
        sum += Integer.parseInt(val.substring(i, j));

        if (ch == '.'){

            int fact = 1;
            while (j < val.length() -1 && ch != ' '){
                j++;
                ch = val.charAt(j);
                if(ch == ' '){
                    break;
                }
                int digit = Integer.parseInt("" +ch);
                sum += (digit) / Math.pow(10, fact);
                fact++;
            }
        }

        double[] ans = {j+1, sum};
        return ans;
    }

    public String toString(){
        String s = this.name + " CPT: \n";
        if(this.CPT_cases != null){
            for (int i = 0; i < CPT_cases.length; i++){
                s+= Arrays.toString(this.CPT_cases[i]) + " " + this.CPT_values[i] + "\n";
            }
        }
        return s;
    }
}
