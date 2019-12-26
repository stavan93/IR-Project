import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class countToken {

	public Set<String> uniqueTerms;
		
	public Set<String> getUniqueTerms() {
		return uniqueTerms;
	}

	public void setUniqueTerms(Set<String> uniqueTerms) {
		this.uniqueTerms = uniqueTerms;
	}

	public double tokenCounter() throws IOException {
	Demo d = new Demo();
	HashMap<String, String> dmap = new HashMap<String, String>();
	String corpus = "";
	dmap = d.XMLParser();
	for (Map.Entry<String, String> entry : d.getData().entrySet()) {	
		
		corpus = corpus + entry.getValue() + " ";
	}//for
	
	Analyzer a = new StandardAnalyzer();
	TokenStream ts = a.tokenStream("text", corpus);
	ts.reset();
	List<String> corp = new ArrayList<String>();
	while(ts.incrementToken()) {
		corp.add(ts.getAttribute(CharTermAttribute.class).toString());
	}//while
	uniqueTerms = new HashSet<String>(corp);

	return uniqueTerms.size();

	}//tokenCounter
		
}//countToken
