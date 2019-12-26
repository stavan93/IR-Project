 import java.io.FileInputStream;
import org.apache.lucene.analysis.TokenStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

public class QueryExpansion{
	

	public String expandQuery(String query , String method) throws JWNLException, ParseException, IOException {
		
		int i = 1 , j=0;
		String finalquery="";
		
		String qterms[] = query.split("\\s+");
		
		//Store the initial query terms
		for(String s : qterms) {
			String[] sp = s.split(":");
			finalquery = finalquery + sp[1] + " ";
			
		}//for
		
		if(method=="qe_thesaurus") {
		System.out.println("query expansion thesaurus");
		//expand initial query
		String[] fqueryarr = finalquery.split("\\s+");
		String fquery1 = thesaurusQE(fqueryarr);	
		
		//combine two words of the expanded query and expand the query again
		//to look for bi-terms for eg. United States or European Union, to get terms in query like U.S/US/EU/E.U
		String[] fqueryarr1 = finalquery.split("\\s+");
		for(String s : fqueryarr1) {
			if(i<=(fqueryarr.length - 1))
			fqueryarr1[i-1] = s + " " + fqueryarr[i];
			i++;
		}//for
		
		//expand the bi-words query
		String fquery2 = thesaurusQE(fqueryarr1);
		
		//finalquery is the query with both the expanded queries from above
		finalquery += fquery1 + " " + fquery2;
		
		finalquery = finalquery.trim();	//to remove any leading , trailing spaces.
		
		//Working on the final expanded query
		String expandedQuery="";
		String[] fqarr = finalquery.split("\\s+");
		//removing duplicate terms from the expanded query.
		for(i=0 ; i<fqarr.length;i++) {
			for(j=i+1 ; j<fqarr.length;j++) {
				if(fqarr[i].equalsIgnoreCase(fqarr[j]) && fqarr[i]!="xxx" ) {
			
					fqarr[j]="xxx";
				}//if
			}//ifor
		}//for
	
		//forming the final expanded query
		for(String s : fqarr) {
			if(s!="xxx")
			expandedQuery = expandedQuery + s + " ";
		}//for
		
		return expandedQuery;
		
		}//if
		
		else {
			System.out.println("Query expansion using relevance feedback");
			QueryParser qp = new QueryParser("content", new StandardAnalyzer());
			Query q = qp.parse(query);
			IndexSearcher s = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get("index-directory"))));
			s.setSimilarity(new BM25Similarity());
			TopDocs td = s.search(q, 3);
			ScoreDoc[] sd = td.scoreDocs;
			String top5 = "";
		
			for(i=0 ; i < sd.length ; i++) {
				Document d = s.doc(sd[i].doc);
				String dcont = d.getField("content").stringValue();
				
				Analyzer a = new StandardAnalyzer();
				TokenStream ts = a.tokenStream("text", dcont);
				ts.reset();
				while(ts.incrementToken()) {
					top5 += ts.getAttribute(CharTermAttribute.class).toString() + " ";
				}//while		
			
			}//for
			
			String[] top5split = top5.split(" ");
			HashMap<String,Integer> hm = new HashMap<String,Integer>();
			int counter=0;
			for(String w : top5split) {
				
				if(!hm.containsKey(w)) {
					for(i=0 ; i<top5split.length ; i++) {
						if(w.equalsIgnoreCase(top5split[i]))
							counter++;
					}
					hm.put(w, counter);
					counter=0;
				}
			}//for
		//	System.out.println(finalquery);
			for (Entry<String, Integer> entry : hm.entrySet()) {
			    
				if(entry.getValue() >= 2) {
					if(!finalquery.contains(entry.getKey()))
					finalquery = finalquery + entry.getKey().toString() + " ";
				}
			}
//			System.out.println(finalquery);
	
			return finalquery;
		}//else
		
		
	}//expandQuery
	

	public String thesaurusQE(String[] fquery) throws JWNLException, FileNotFoundException {
		
		String exfquery= "";
		List<String> synarr = new ArrayList<String>();
		JWNL.initialize(new FileInputStream("properties.xml")); 
		Dictionary dictionary = net.didion.jwnl.dictionary.Dictionary.getInstance();
		
		for(String terms : fquery) {//tagging initial pos as noun
			POS pos = POS.NOUN;
			
		IndexWord iword = dictionary.lookupIndexWord(pos, terms);
		
		if(iword != null) {	//checking if the query term is a noun
			pos = POS.NOUN;
		}//if
		
		else { //checking if the query terms is adverb
			iword = dictionary.lookupIndexWord(POS.ADVERB, terms);
			if(iword !=null) pos = POS.ADVERB;
			
			else {//checking if the query terms is adjective
				iword = dictionary.lookupIndexWord(POS.ADJECTIVE, terms);
				if(iword!=null)	pos = POS.ADJECTIVE;
				
				else {//checking if the query terms is verb
					iword = dictionary.lookupIndexWord(POS.VERB, terms);
					if(iword!=null) pos = POS.VERB;
				}//else3
			}//else2
		}//else1
		
			//once done with the pos , looking for words in the thesaurus(synonyms or abbreviations)
		if(iword != null) {
			
			IndexWord indexWord = dictionary.getIndexWord(pos, terms);
			
			Synset[] synset = indexWord.getSenses();
	
			for(Synset s : synset) {
				
				int i=0;
				for(int j=0; j<s.getWordsSize();j++) {
					String temp = s.getWord(i++).getLemma();	//lemma gives the string value of the word
					if(!temp.equalsIgnoreCase(terms) && !synarr.contains(temp) && !temp.contains("_") && !temp.contains("-")) {
						//adding the newly obtained terms to the array
					//	temp = temp.replace("_", "");
						synarr.add(temp);
					}//if
						
				}//ifor
			}//for
			
			//forming the expanded query with all the newly obtained terms
			for(String s : synarr)
				exfquery = exfquery + " " + s;
			
			synarr.clear(); //clearing the array for future use
		}//if
	}//for
		return exfquery;
	}//thesaurusQE
	
	public String relfeedQE() {
		String exfquery="";
		
		return exfquery;
	}//relfeedQE
}//QueryExpansion
