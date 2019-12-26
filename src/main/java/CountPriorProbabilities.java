import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class CountPriorProbabilities {
	File file1 = null;
	File file2 = null;
	IndexReader reader = null;
	HashMap<String,Integer> posmap = new HashMap<String,Integer>();
	HashMap<String,Integer> negmap = new HashMap<String,Integer>();
	HashMap<String,Integer> nuemap = new HashMap<String,Integer>();
	BufferedWriter writer = new BufferedWriter(new FileWriter("pos_tweets.txt"));
	BufferedWriter writerr = new BufferedWriter(new FileWriter("neg_tweets.txt"));
	BufferedWriter writerrr = new BufferedWriter(new FileWriter("nue_tweets.txt"));
	public CountPriorProbabilities() throws IOException {
		file1 = new File("positive-words.txt");
		file2 = new File("negative-words.txt");
		Directory indexDir = FSDirectory.open(Paths.get("/home/spa1019/team-8/IR-Project/Candidate Set/index-directory"));
		reader = DirectoryReader.open(indexDir);
	}
	
	public HashMap<String,Integer> countvocabulary() throws IOException {
		String text = "";
		for(int i =0;i < reader.maxDoc();i++) {
			Document doc = reader.document(i);
			text += doc.getField("content").stringValue();
		}
		String[] textt = text.split(" ");
		Pattern p = Pattern.compile("[a-zA-Z]+"); 
        Matcher m = p.matcher(text); 
          
        
        HashMap<String, Integer> vocab = new HashMap<String,Integer>(); 
          
        
        while (m.find())  
        { 
            String word = m.group(); 
              
            
            if(!vocab.containsKey(word)) 
                vocab.put(word, 1); 
            else
                
                vocab.put(word, vocab.get(word) + 1); 
              
        }
		return vocab;
	}
	
	public int classify(String s,File file) throws IOException {
		int prob = 0;
		Scanner scan = new Scanner(file);
		String[] text = s.split(" ");
		int count = 0;
		while(scan.hasNextLine()) {
			String word = scan.nextLine();
			for(int j = 0;j < text.length;j++) {
				text[j].toLowerCase();
				if(word.equals(text[j])) {
					count++;
				}
			}
		}
		prob = count;
		return prob;
	}
	
	public double[] classifier() throws IOException {
	    int count = 0;
		double pos = 0.0;
		double neg = 0.0;
		double nue = 0.0;
		for(int i = 0;i < reader.maxDoc();i++) {
		    count++;
			Document doc = reader.document(i);
			String text = doc.getField("content").stringValue();
			int pos_prob = classify(text,file1);
			//System.out.println("Pos: "+pos_prob);
			int neg_prob = classify(text,file2);
			//System.out.println("Neg: "+neg_prob);
			if(pos_prob > neg_prob) {
			    writer.write(text);
				pos++;
				Pattern p = Pattern.compile("[a-zA-Z]+"); 
		        Matcher m = p.matcher(text); 
		        while (m.find())  
		        { 
		            String word = m.group(); 
		              
		            
		            if(!posmap.containsKey(word)) 
		                posmap.put(word, 1); 
		            else
		                
		                posmap.put(word, posmap.get(word) + 1); 
		              
		        }
		        
			}
			else if(pos_prob < neg_prob) {
			    writerr.write(text);
				neg++;
				Pattern p = Pattern.compile("[a-zA-Z]+"); 
		        Matcher m = p.matcher(text); 
		        while (m.find())  
		        { 
		            String word = m.group(); 
		              
		            
		            if(!negmap.containsKey(word)) 
		                negmap.put(word, 1); 
		            else
		                
		                negmap.put(word, negmap.get(word) + 1); 
		              
		        }
			}
			else if((pos_prob == 0) && (neg_prob == 0) ) {
			    writerrr.write(text);
				nue++;
				Pattern p = Pattern.compile("[a-zA-Z]+"); 
		        Matcher m = p.matcher(text); 
		        while (m.find())  
		        { 
		            String word = m.group(); 
		              
		            
		            if(!nuemap.containsKey(word)) 
		                nuemap.put(word, 1); 
		            else
		                
		                nuemap.put(word, nuemap.get(word) + 1); 
		              
		        }
			}
			else {
			    writer.write(text);
				pos++;
				Pattern p = Pattern.compile("[a-zA-Z]+"); 
		        Matcher m = p.matcher(text); 
		        while (m.find())  
		        { 
		            String word = m.group(); 
		              
		            
		            if(!posmap.containsKey(word)) 
		                posmap.put(word, 1); 
		            else
		                
		                posmap.put(word, posmap.get(word) + 1); 
		              
		        }
			}
		}
		pos = pos / reader.getDocCount("content");
		neg = neg / reader.getDocCount("content");
		nue = nue / reader.getDocCount("content");
		double[] prob = new double[3];
		prob[0] = pos;
		prob[1] = neg;
		prob[2] = nue;
		p_map();
		n_map();
		nu_map();
		writer.close();
		writerr.close();
		writerrr.close();
		System.out.println("----------------------------------------------------------------------------------------------"+count);
		return prob;
	}
	
	public HashMap<String,Integer> p_map(){
		return posmap;
	}
	
	public HashMap<String,Integer> n_map(){
		return negmap;
	}
	
	public HashMap<String,Integer> nu_map(){
		return nuemap;
	}
	
}
