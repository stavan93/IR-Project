import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;


public class Main {

	public static void main(String[] args) throws IOException, ParseException {
		
		try {
		    Scanner scann = new Scanner(System.in);
		    Path pathh = FileSystems.getDefault().getPath("qrels_file.qrels");
		    if(!Files.exists(pathh)) {
			    CreateQrelsFile q = new CreateQrelsFile();
			    q.XMLParser();
		    }
			Scanner scan = new Scanner(new File("Queries.txt"));
			Path path = FileSystems.getDefault().getPath("index-directory");
			if(!Files.exists(path)) {
				System.out.println("Indexing File...");
		        IndexFiles  indexer = new IndexFiles();
		        indexer.indexfile();
		        System.out.println("Indexing Done.");
			}
			String x = "";
			do{
				System.out.println("-----------------------------------------");
		        System.out.println("Performing Search...");
	        	SearchEngine search = new SearchEngine();
	        	search.displayresults();
	        		
	        	
	        	
		        
		        System.out.println("Search done");
		        System.out.println("---------------------------------------------------------");
		        System.out.println("Would you like to continue:(Y/N)");
				x = scann.next();
				x.toLowerCase();
			}while(x.equals("y"));
			x = "";
			do{
				System.out.println("Performing Naive Bayes Classification");
				NaiveBayes nb = new NaiveBayes();
				nb.classify();
				System.out.println("Naive Bayes Classification Done.");
				System.out.println("---------------------------------------------------------");
		        System.out.println("Would you like to continue:(Y/N)");
				x = scann.next();
				x.toLowerCase();
			}while(x.equals("y"));
			x = "";
			do{
				System.out.println("Performing K-Means Classification");
			    KMeans km = new KMeans();
			    km.start();
			    System.out.println("K-Means Classification Done.");
			    System.out.println("---------------------------------------------------------");
		        System.out.println("Would you like to continue:(Y/N)");
				x = scann.next();
				x.toLowerCase();
			}while(x.equals("y"));
			
			
			Learningtorank lr = new Learningtorank();
			lr.createfeaturevector();
			Runltor r = new Runltor();
			r.executeCommands();
	}
		
		catch(Exception e) {
			
		}
		
	}
}