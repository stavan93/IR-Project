import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ComputeQueryVector {
	
	String query = "";
	IndexReader reader = null;
	private Map<String, Double> queryTermIDF;
	private Map<String,Integer> docFreqMap;
	
	public Map<String, Double> getQueryTermIDF() {
		return queryTermIDF;
	}

	public void setQueryTermIDF(Map<String, Double> queryTermIDF) {
		this.queryTermIDF = queryTermIDF;
	}

	public ComputeQueryVector(String query) throws IOException {
		this.query = query;
		this.queryTermIDF = new HashMap<>();
		Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		reader = DirectoryReader.open(indexDir);
	}
	public double computenorm() throws IOException{
		String[] queryy = query.split(" ");
		double[] arr = new double[queryy.length];
		double[] idf = new double[queryy.length];
		double ans = 0.0;
		for(int j = 0;j < queryy.length;j++) {
			String word = queryy[j];
			int count = 0;
			for(int k = 0;k < queryy.length;k++) {
				if(word.equals(queryy[k])) {
					count++;
				}
			}
			arr[j] = count;
		}
		for(int j = 0;j < idf.length;j++) {
			String word = queryy[j];
			int count = 0;
			for(int k = 0;k < reader.maxDoc();k++) {
				Document doc = reader.document(k);
				String text = doc.getField("content").stringValue();
				if(text.contains(word)) {
					count++;
				}
				else {
					continue;
				}
			}
			
			idf[j] = count;
		}
		for(int j = 0;j < arr.length;j++) {
			arr[j] = 1 + Math.log(arr[j]);
			if(idf[j] == 0.0) {
				arr[j] = 0.0;
			}
			else {
				arr[j] *= Math.log(reader.getDocCount("content")/idf[j]);
				queryTermIDF.put(queryy[j], Math.log(reader.getDocCount("content")/idf[j]));
			}
		}
		for(int j = 0;j < arr.length;j++) {
			ans += arr[j];
		}
		return ans;
	}
	
}
