import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class EvaluateNbayes {
	
	File file = new File("Tweet files/pos_tweets.txt");
	File file1 = new File("Tweet files/neg_tweets.txt");
	File file2 = new File("Tweet files/nue_tweets.txt");
	
	File file3 = new File("Tweet files/npos_tweets.txt");
	File file4 = new File("Tweet files/nneg_tweets.txt");
	File file5 = new File("Tweet files/nnue_tweets.txt");
	
	public void calc_t_p() throws FileNotFoundException {
		int count = 0;
		Scanner scan = new Scanner(file);
		while(scan.hasNextLine()) {
			String text = scan.nextLine();
			Scanner scann = new Scanner(file3);
			while(scann.hasNextLine()) {
				String textt = scann.nextLine();
				if(text.equals(textt)) {
					count++;
				}
			}
		}
		
		scan = new Scanner(file1);
		while(scan.hasNextLine()) {
			String text = scan.nextLine();
		   Scanner scann = new Scanner(file4);
			while(scann.hasNextLine()) {
				String textt = scann.nextLine();
				if(text.equals(textt)) {
					count++;
				}
			}
		}
		scan = new Scanner(file2);
		while(scan.hasNextLine()) {
			String text = scan.nextLine();
			Scanner scann = new Scanner(file5);
			while(scann.hasNextLine()) {
				String textt = scann.nextLine();
				if(text.equals(textt)) {
					count++;
				}
			}
		}
		System.out.println("TP: "+count);
	}
	
	public void calc_f_p() throws IOException {
		int count = 0;
		Path path = Paths.get("Tweet files/npos_tweets.txt");
		long lineCount = Files.lines(path).count();
		//System.out.println(lineCount);
		count = (int) lineCount;
		//System.out.println(count);
		Path pathh = Paths.get("Tweet files/nneg_tweets.txt");
		long lineCountt = Files.lines(pathh).count();
		count += (int) lineCountt;
		//System.out.println(count);
		Path pathhh = Paths.get("Tweet files/nnue_tweets.txt");
		long lineCounttt = Files.lines(pathhh).count();
		count += (int) lineCounttt;
		//System.out.println(count);
		System.out.println("FP: "+(count-19));
	}
	
	public void calc_f_n() throws IOException {
		int count = 0;
		Path path = Paths.get("Tweet files/pos_tweets.txt");
		long lineCount = Files.lines(path).count();
		//System.out.println(lineCount);
		count = (int) lineCount;
		//System.out.println(count);
		Path pathh = Paths.get("Tweet files/neg_tweets.txt");
		long lineCountt = Files.lines(pathh).count();
		count += (int) lineCountt;
		//System.out.println(count);
		Path pathhh = Paths.get("Tweet files/nue_tweets.txt");
		long lineCounttt = Files.lines(pathhh).count();
		count += (int) lineCounttt;
		//System.out.println(count);
		System.out.println("FN: "+(count-19));
	}

}
