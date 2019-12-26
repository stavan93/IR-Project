import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class SearchEngine {
	
	IndexSearcher searcher = null;
	private QueryParser parsetext = null;
	Scanner scan = new Scanner(new File("Queries.txt"));
	Map<String, String> queries = new HashMap<String,String>();
	int vocabularySize;
	
	public SearchEngine()throws IOException{
		
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get("index-directory"))));
        parsetext = new QueryParser("content", new StandardAnalyzer());
	}
	//Performs search on the index and returns the search results
	public TopDocs performsearch(String str, int num)throws IOException, ParseException {
		 Query query = parsetext.parse(str);
	     return searcher.search(query, num);
	}
	
	public String IndexElimination(String query) throws IOException {
		ComputeDocumentVector docv = new ComputeDocumentVector();
		ComputeQueryVector qv = new ComputeQueryVector(query);
		double a = docv.computenorm();
		double d = qv.computenorm();
		String newQuery = "";
		Double minIdf = Double.MAX_VALUE;
		Map<String, Double> queryIDF = qv.getQueryTermIDF();
		Double min = 3.0;
		for (Map.Entry<String, Double> entry : queryIDF.entrySet()) {
			if (entry.getValue() < minIdf)
				minIdf = entry.getValue();
		}
		for (Map.Entry<String, Double> entry : queryIDF.entrySet()) {
			if (entry.getValue() != minIdf)
				newQuery = newQuery.concat(entry.getKey() + " ");
		}
		qv = new ComputeQueryVector(newQuery.trim());
		double d1 = qv.computenorm();
		LNCLTNSimilarity(query);
		return newQuery;
	}
	
	public List<String> tokenizeString(String reader) {
		List<String> result = new ArrayList<String>();
		try {
			Analyzer analyzer = new StandardAnalyzer();
			TokenStream stream = analyzer.tokenStream("content", reader);
			stream.reset();
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		return result;

	}

	public Map<String, List<String>> tokenizeDocuments() throws IOException {
		Map<String, List<String>> listTokens = new HashMap<>();
		Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		IndexReader reader = DirectoryReader.open(indexDir);
		Analyzer analyzer = new StandardAnalyzer();
		 Set<String> hSet = new HashSet<String>(); 
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < reader.maxDoc(); i++) {
			List<String> documentTokens = new ArrayList<String>();
			Document doc = reader.document(i);
			String docId = doc.get("id");
			TokenStream stream = analyzer.tokenStream("content", doc.get("content"));
			stream.reset();
			while (stream.incrementToken()) {
				String termString = stream.getAttribute(CharTermAttribute.class).toString();
				documentTokens.add(termString);
				hSet.add(termString);
				result.add(termString);
			}
			stream.close();
			listTokens.put(docId, documentTokens);
			// TokenSources.getTokenStream(doc, "text", new StandardAnalyzer());
		}
		analyzer.close();
		vocabularySize = hSet.size();
		return listTokens;
	}
public Map<String, Map<String, Integer>> calculateDocFrequency() throws IOException {
	Map<String, List<String>> listTokens = new HashMap<>();
	Map<String, Map<String, Integer>> docFrequencyMap = new HashMap<>();
	Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
	IndexReader reader = DirectoryReader.open(indexDir);
	Analyzer analyzer = new StandardAnalyzer();
	// Set<String> hSet = new HashSet<String>();
	// List<String> result = new ArrayList<String>();
	for (int i = 0; i < reader.maxDoc(); i++) {
		List<String> documentTokens = new ArrayList<String>();
		Document doc = reader.document(i);
		String docId = doc.get("id");
		TokenStream stream = analyzer.tokenStream("content", doc.get("content"));
		stream.reset();
		while (stream.incrementToken()) {
			String termString = stream.getAttribute(CharTermAttribute.class).toString();
			documentTokens.add(termString);

			// hSet.add(termString);
			// result.add(termString);
		}
		stream.close();
		listTokens.put(docId, documentTokens);
		for (Map.Entry<String, List<String>> docIdToken : listTokens.entrySet()) {
			Map<String, Integer> docFreq = new HashMap<>();
			for (String token : docIdToken.getValue()) {
				if (!docFreq.containsKey(token)) {
					docFreq.put(token, 0);
				}
				docFreq.put(token, docFreq.get(token) + 1);
			}
			docFrequencyMap.put(docIdToken.getKey(), docFreq);
		}
		// TokenSources.getTokenStream(doc, "text", new StandardAnalyzer());
	}
	analyzer.close();
	// vocabularySize = hSet.size();
	return docFrequencyMap;
}

public Map<String, Double> calculateDocQuality() throws IOException {
        Map<String, Map<String, Integer>> docFrequencyMap = new HashMap<>();
		docFrequencyMap = calculateDocFrequency();
		Map<String, Double> docQualityScore = new HashMap<>();
		for (Map.Entry<String, Map<String, Integer>> docEntry : docFrequencyMap.entrySet()) {
			double count = 0;
			double sum=0;
			for (Map.Entry<String, Integer> entry : docEntry.getValue().entrySet()) {
				if (entry.getValue() < 6)
					count++;
				sum+=entry.getValue();
			}
			docQualityScore.put(docEntry.getKey(), count/sum);
		}
		return docQualityScore;
}
// Calculating scores and ranking for Bigram Model
	public void displayresultsBML(String s) throws ParseException, IOException {
		Map<String, List<String>> listTokens = tokenizeDocuments();
		BufferedWriter writer = new BufferedWriter(new FileWriter(s));
		BigramLM bigram = new BigramLM();
		bigram.setVocabularySize(vocabularySize);
		while (scan.hasNextLine()) {
			String ss = scan.nextLine();
			String[] str = ss.split(",");
			 String queryStr = str[1];
			List<String> queryTokens = tokenizeString(queryStr);
			// documentTokens and the querytokens passed to performSearc1 method for
			// calculating probaibility.
			Map<String, Double> totalScoreBigram = bigram.performSearch1(listTokens, queryTokens);
			Map<String, Double> filteredScoreBigram = totalScoreBigram.entrySet().stream()
					.sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(100).collect(Collectors
							.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			int count = 1;
			for (Map.Entry<String, Double> entry : filteredScoreBigram.entrySet()) {
				System.out.println(str[0] + " " + "Q0 " + entry.getKey() + " " + count + " "
						+ entry.getValue() + " team-8\n");

				writer.append(str[0] + " " + "Q0" + " " + entry.getKey() + " " + count + " "
						+ entry.getValue() + " team-8\n");
				count++;
			}
			System.out.println("----------------------------------------------------------------");
		}
		writer.close();

	}

public void displayresultsChampion() throws ParseException, IOException {
	// Using ScoreDocs we score the top 10 Documents from the search results
	BufferedWriter writer = new BufferedWriter(new FileWriter("Runfile-Champion.run"));
	while (scan.hasNextLine()) {
		String ss = scan.nextLine();
		String[] str = ss.split(",");
		TopDocs topDocs = null;
		System.out.println(str[1]);
		LNCLTNSimilarity(str[1]);
		topDocs = performsearch(str[1], 100);
		Map<String, Double> docQualityScore = calculateDocQuality();
		System.out.println("Total Hits: " + topDocs.totalHits);
		System.out.println("Top 10 Results: ");
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document doc = getDocument(hits[i].doc);
			for (Map.Entry<String, Double> entry : docQualityScore.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(doc.getField("id").stringValue())) {
					System.out.println("Query ID: " + str[0] + " " + (i + 1) + " :" + "Doc ID: "
							+ doc.getField("id").stringValue() + "\nDocument Text: "
							+ doc.getField("content").stringValue() + "\nScore: (" + (hits[i].score + entry.getValue())+ ")");
					writer.write(str[0] + " Q0 " + doc.getField("id").stringValue() + " " + (i + 1) + " "
							+ hits[i].score + " team-8\n");
				}
			}
		}
	}
	writer.close();
}

	
	public void BM25similarity() {
		searcher.setSimilarity(new BM25Similarity());
	}
	public void LNCLTNSimilarity(String query) throws IOException {
		//ComputeDocumentVector docv = new ComputeDocumentVector();
		ComputeQueryVector qv = new ComputeQueryVector(query);
		//double a = docv.computenorm();
		//System.out.println("Doc_v: "+a);
		double d = qv.computenorm();
		searcher.setSimilarity(new LNCLTN(d));
	}
	public Document getDocument(int docId)throws IOException {
		 return searcher.doc(docId);
	}
	public void displayresults()throws ParseException, IOException {
		//Using ScoreDocs we score the top 10 Documents from the search results
		BufferedWriter writer = null;
		boolean flag = true;
		while(scan.hasNextLine()) {
			String ss = scan.nextLine();
			String[] str = ss.split(",");
			System.out.println(str[1]);
			if(flag) {
				String similarity = choosesimilarity(str[1]);
				writer = new BufferedWriter(new FileWriter(similarity));
				flag = false;
			}
			TopDocs topDocs = performsearch(str[1], 100);
			System.out.println("Total Hits: " + topDocs.totalHits);
			System.out.println("Top 100 Results: ");
	        ScoreDoc[] hits = topDocs.scoreDocs;
	        for (int i = 0; i < hits.length; i++) {
	            Document doc = getDocument(hits[i].doc);
	            System.out.println("Query ID: "+str[0]+" "+i+1+" :"+"Doc ID: " + doc.getField("id").stringValue()+"\nDocument Text: " + doc.getField("content").stringValue() + "\nScore: (" + hits[i].score + ")");
	            writer.write(str[0]+" Q0 "+ doc.getField("id").stringValue()+" "+ (i+1)+" " +  hits[i].score + " team-8\n");
	        }
		}
		writer.close();
	}
	
	public void displayresultsIndexElimination()throws ParseException, IOException {
		//Using ScoreDocs we score the top 10 Documents from the search results
		BufferedWriter writer = new BufferedWriter(new FileWriter("Runfile-Index-Elimination.run"));
		boolean flag = true;
		while(scan.hasNextLine()) {
			String ss = scan.nextLine();
			String[] str = ss.split(",");
			System.out.println(str[1]);
			String newquery = IndexElimination(str[1]);
			System.out.println("New Query: "+newquery);
			TopDocs topDocs = null;
			if(newquery!= null && newquery!= "")
			{
				topDocs = performsearch(newquery, 100);	
			}
			else
			{
			topDocs = performsearch(str[1], 100);
			}
			System.out.println("Total Hits: " + topDocs.totalHits);
			System.out.println("Top 100 Results: ");
	        ScoreDoc[] hits = topDocs.scoreDocs;
	        for (int i = 0; i < hits.length; i++) {
	            Document doc = getDocument(hits[i].doc);
	            System.out.println("Query ID: "+str[0]+" "+i+1+" :"+"Doc ID: " + doc.getField("id").stringValue()+"\nDocument Text: " + doc.getField("content").stringValue() + "\nScore: (" + hits[i].score + ")");
	            writer.write(str[0]+" Q0 "+ doc.getField("id").stringValue()+" "+ (i+1)+" " +  hits[i].score + " team-8\n");
	        }
		}
		writer.close();
	}
	
	public String choosesimilarity(String s) throws IOException, ParseException {
		System.out.println("Select a scoring function:");
		 System.out.println("1). BM25 Similarity.");
		 System.out.println("2). LNCLTN Simiraity.");
		 System.out.println("3). LNCLTN Using Champion List.");
		 System.out.println("4). BiGram Simiraity.");
		 System.out.println("5). LNCLTN using Index Elimination.");
		 //System.out.println("3). UnigramDIR");
		 //System.out.println("3). UnigramDIR");
		 Scanner scann = new Scanner(System.in);
		 int x = scann.nextInt();
		 if(x == 1) {
			 BM25similarity();
			 return "Runfile-BM25.run";
		 }
		 else if(x == 2) {
			 LNCLTNSimilarity(s);
			 return "Runfile-LNCLTN.run";
		 }
		 else if(x == 3) {
			 displayresultsChampion();
			 return "Runfile-Champion.run";
		 }
		 else if(x == 4) {
			 displayresultsBML("Runfile-Bigram.run");
			 return "Runfile-ChampionList.run";
		 }
		 else if(x == 5) {
			 displayresultsIndexElimination();
			 return "Runfile-Index-Elimination.run";
		 }
		 /*else if(x == 4) {
			 //unigram_dir();
			 return "Runfile-Bigram_LM";
		 }*/
		 else {
			 System.out.println("You need to choose one.");
			 return "0";
		 }
	}
}


