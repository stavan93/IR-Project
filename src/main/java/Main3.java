import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main3 {
	
	public static void calc(String s,String d) throws IOException {
		
		String filePath = s;
	      String input = null;
	      //Instantiating the Scanner class
	      Scanner sc = new Scanner(new File(filePath));
	      //Instantiating the FileWriter class
	      FileWriter writer = new FileWriter(d);
	      //Instantiating the Set class
	      Set set = new HashSet();
	      while (sc.hasNextLine()) {
	         input = sc.nextLine();
	         if(set.add(input)) {
	            writer.append(input+"\n");
	         }
	      }
	      writer.flush();
	      System.out.println("Contents added............");
		
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CountPriorProbabilities cpp = new CountPriorProbabilities();
		cpp.classifier();
		NaiveBayes nb = new NaiveBayes();
		nb.classify();
		
		
		calc("pos_tweets.txt","Tweet files/pos_tweets.txt");
		calc("neg_tweets.txt","Tweet files/neg_tweets.txt");
		calc("nue_tweets.txt","Tweet files/nue_tweets.txt");
		calc("npos_tweets.txt","Tweet files/npos_tweets.txt");
		calc("nneg_tweets.txt","Tweet files/nneg_tweets.txt");
		calc("nnue_tweets.txt","Tweet files/nnue_tweets.txt");
		
		EvaluateNbayes e = new EvaluateNbayes();
		e.calc_f_n();
		e.calc_f_p();
		e.calc_t_p();

	}

}
