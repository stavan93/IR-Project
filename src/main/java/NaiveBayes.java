import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class NaiveBayes {
	double[] prior_prob = new double[3];
	HashMap<String,Integer> posmap = new HashMap<String,Integer>();
	HashMap<String,Integer> negmap = new HashMap<String,Integer>();
	HashMap<String,Integer> nuemap = new HashMap<String,Integer>();
	HashMap<String, Integer> vocab = new HashMap<String,Integer>();
	ArrayList<String> pos_tweets = new ArrayList<String>();
	ArrayList<String> neg_tweets = new ArrayList<String>();
	ArrayList<String> nue_tweets = new ArrayList<String>();
	BufferedWriter writer = new BufferedWriter(new FileWriter("npos_tweets.txt"));
	BufferedWriter writerr = new BufferedWriter(new FileWriter("nneg_tweets.txt"));
	BufferedWriter writerrr = new BufferedWriter(new FileWriter("nnue_tweets.txt"));
	File file = null;
	HashMap<String,ArrayList<String>> runfile_data = new HashMap<String,ArrayList<String>>();
	IndexReader reader = null;
	public NaiveBayes() throws IOException {
		Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		reader = DirectoryReader.open(indexDir);
		CountPriorProbabilities cpp = new CountPriorProbabilities();
		prior_prob = cpp.classifier();
		vocab = cpp.countvocabulary();
		posmap = cpp.p_map();
		negmap = cpp.n_map();
		nuemap = cpp.nu_map();
	}
	
	public long vocab_count() {
		long vocab_count = 0;
		for (HashMap.Entry<String, Integer> entry : vocab.entrySet()) {
		    String key = entry.getKey();
		    vocab_count++;
		}
		return vocab_count;
	}
	
	public long count_nue_tokens() {
		long nue_token_count = 0;
		for (HashMap.Entry<String, Integer> entry : nuemap.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    nue_token_count += value;
		}
		return nue_token_count;
	}
	
	public long count_pos_tokens() {
		long pos_token_count = 0;
		for (HashMap.Entry<String, Integer> entry : posmap.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    pos_token_count += value;
		}
		return pos_token_count;
	}
	
	public long count_neg_tokens() {
		long neg_token_count = 0;
		for (HashMap.Entry<String, Integer> entry : negmap.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    neg_token_count += value;
		}
		return neg_token_count;
	}
	
	public double calc_nue_prob(String s) {
		double score = 0.0;
		for(HashMap.Entry<String, Integer> entry : nuemap.entrySet()) {
			if(s.equals(entry.getKey())) {
				score = entry.getValue();
			}
		}
		return score;
	}
	
	public double calc_pos_prob(String s) {
		double score = 0.0;
		for(HashMap.Entry<String, Integer> entry : posmap.entrySet()) {
			if(s.equals(entry.getKey())) {
				score = entry.getValue();
			}
		}
		return score;
	}
	
	public double calc_neg_prob(String s) {
		double score = 0.0;
		for(HashMap.Entry<String, Integer> entry : negmap.entrySet()) {
			if(s.equals(entry.getKey())) {
				score = entry.getValue();
			}
		}
		return score;
	}
	
	public void select_file() {
		System.out.println("Chosse RunFile: ");
		Scanner scan = new Scanner(System.in);
		String s = scan.next();
		
		Path path = FileSystems.getDefault().getPath(s);
		if(!Files.exists(path)) {
			System.out.println("File does not Exist.");
		}
		file = new File(s);
	}
	
	public void read_runfile() throws FileNotFoundException {
		Scanner scan = new Scanner(file);
		ArrayList<String> tweet = new ArrayList<String>();
		while(scan.hasNextLine()) {
			String text = scan.nextLine();
			String[] textt = text.split(" ");
			if(!runfile_data.containsKey(textt[0])) {
				tweet.clear();
				tweet.add(textt[2]);
			}
			else {
				tweet.add(textt[2]);
			}
			runfile_data.put(textt[0], tweet);
		}
	}
	
	public void display() throws FileNotFoundException {
		select_file();
		read_runfile();
		for (HashMap.Entry<String, ArrayList<String>> entry : runfile_data.entrySet()) {
			System.out.println(entry.getKey());
			String query_id = entry.getKey();
		    String query = "";
		    File file1 = new File("Queries.txt");
		    Scanner scan = new Scanner(file1);
		    while(scan.hasNextLine()) {
		    	String line = scan.nextLine();
		    	if(line.contains(query_id)) {
		    		String[] linee = line.split(",");
		    		query = linee[1];
		    		break;
		    	}
		    }
		    ArrayList<String> value = entry.getValue();
		    System.out.println("Query: "+ query);
		    for(String tweet : value) {
		    	System.out.println(tweet);
		    }
		}
	}
	
	public void classify() throws IOException {
		select_file();
		read_runfile();
		for (HashMap.Entry<String, ArrayList<String>> entry : runfile_data.entrySet()) {
		    String query_id = entry.getKey();
		    String query = "";
		    File file1 = new File("Queries.txt");
		    Scanner scan = new Scanner(file1);
		    while(scan.hasNextLine()) {
		    	String line = scan.nextLine();
		    	if(line.contains(query_id)) {
		    		String[] linee = line.split(",");
		    		query = linee[1];
		    		break;
		    	}
		    }
		    ArrayList<String> value = entry.getValue();
		    System.out.println("Query: "+ query);
		    for(String tweet : value){
		        for(int i = 0;i < reader.maxDoc();i++) {
		        	Document doc = reader.document(i);
		        	String doc_id = doc.getField("id").stringValue();
		        	if(tweet.equals(doc_id)) {
		        		double pos_prob = 1.0;
		        		double neg_prob = 1.0;
		        		double nue_prob = 1.0;
		        		String text = doc.getField("content").stringValue();
		        		String[] textt = text.split(" ");
		        		for(int j = 0;j < textt.length;j++) {
		        			pos_prob *= (calc_pos_prob(textt[j]) + 1)/(count_pos_tokens() + vocab_count());
		        			neg_prob *= (calc_neg_prob(textt[j]) + 1)/(count_neg_tokens() + vocab_count());
		        			nue_prob *= (calc_nue_prob(textt[j]) + 1)/(count_nue_tokens() + vocab_count());
		        		}
		        		pos_prob *= prior_prob[0];
		        		neg_prob *= prior_prob[1];
		        		nue_prob *= prior_prob[2];
		        		if((pos_prob > neg_prob) && (pos_prob > nue_prob)) {
		        			System.out.println("Positive: "+text);
		        			pos_tweets.add(text);
		        			writer.write(text);
		        		}
		        		else if((neg_prob > pos_prob) && (neg_prob > nue_prob)) {
		        			System.out.println("Negative: "+text);
		        			neg_tweets.add(text);
		        			writerr.write(text);
		        		}
		        		else {
		        			System.out.println("Nuetral: "+text);
		        			nue_tweets.add(text);
		        			writerrr.write(text);
		        		}
		        	}
		        }
		    }
		}
		writer.close();
		writerr.close();
		writerrr.close();
	}
}
