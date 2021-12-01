import java.io.*;

public class Ex1 {

    public static void main(String[] args) {
        try {
            BufferedReader r = new BufferedReader(new FileReader("input.txt"));
            String line;
            line = r.readLine();
            Net net = new Net(line);

            BufferedWriter w = new BufferedWriter(new FileWriter("output.txt"));
            while ((line = r.readLine()) != null){
                w.write(net.compute(line) + "\n");
            }

            w.close();
            r.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //test
    //turn in
}
