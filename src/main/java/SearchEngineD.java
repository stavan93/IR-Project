
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import net.didion.jwnl.JWNLException;

public class SearchEngineD {

	IndexSearcher searcher = null;
	private QueryParser parsetext = null;
	Scanner scan = new Scanner(new File("Queries.txt"));
	Map<String, String> queries = new HashMap<String, String>();

	public SearchEngineD() throws IOException {

		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get("index-directory"))));
		parsetext = new QueryParser("content", new StandardAnalyzer());
	}

	// Performs search on the index and returns the search results
	public TopDocs performsearch(String str, int num , String method , String scorer) throws IOException, ParseException, JWNLException {
		QueryExpansion qex = new QueryExpansion();
		Query iquery = parsetext.parse(str);
		String finalquery = qex.expandQuery(iquery.toString() , method);
		System.out.println("Initial Query: " + str +"\nExpanded Query:" + finalquery.toLowerCase() + "\nMethod: " + method + " "
				+ "Scorer: " + scorer);
		Query query = parsetext.parse(finalquery);
		
		if(method.equalsIgnoreCase("qe_thesaurus"))
		return searcher.search(query, num);
		
		else {
			return searcher.search(query, num);
		}//else
	}

	public Document getDocument(int docId) throws IOException {
		return searcher.doc(docId);
	}

	public void displayresults(String method , String scorer) throws ParseException, IOException, JWNLException {
		// Using ScoreDocs we score the top 10 Documents from the search results
		
		if(scorer.equalsIgnoreCase("bm25")) {
			searcher.setSimilarity(new BM25Similarity());
		}//if
		
		else if(scorer.equalsIgnoreCase("unilap")) {
			countToken ct = new countToken();
			double vocabsize = ct.tokenCounter();
			UnigramLaplace unilap = new UnigramLaplace(vocabsize);
			searcher.setSimilarity(unilap);
		}
		
		else if(scorer.equalsIgnoreCase("unijm")) {
			countToken ct = new countToken();
			double vocabsize = ct.tokenCounter();
			UnigramJM unijm = new UnigramJM(vocabsize);
			searcher.setSimilarity(unijm);
		}
		
		else {
			countToken ct = new countToken();
			double vocabsize = ct.tokenCounter();
			UnigramDir unidir = new UnigramDir(vocabsize);
			searcher.setSimilarity(unidir);
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("Runfile-" + scorer + "-" + method + ".run"));
		while (scan.hasNextLine()) {
			String ss = scan.nextLine();
			String[] str = ss.split(",");
			TopDocs topDocs = performsearch(str[1], 20 , method , scorer);
			System.out.println("Total Hits: " + topDocs.totalHits + "\n");
			ScoreDoc[] hits = topDocs.scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document doc = getDocument(hits[i].doc);
				if((i+1) == 1)System.out.println(doc.getField("content").stringValue());
				//System.out.print("\nQuery ID: " + str[0] + " | Rank: " + (i+1) + " | " + "Doc ID: "
					//	+ doc.getField("id").stringValue() + "\nScore: (" + hits[i].score + ")" + "\nDocument Text: " + doc.getField("content").stringValue() );
				writer.write(str[0] + " Q0 " + doc.getField("id").stringValue() + " " + (i + 1) + " " + hits[i].score
						+ " team-8\n");
			}
		}
		writer.close();
	}


}
