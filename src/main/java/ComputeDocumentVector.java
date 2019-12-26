import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ComputeDocumentVector {
	IndexReader reader = null;
	public ComputeDocumentVector() throws IOException {
		Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		reader = DirectoryReader.open(indexDir);
	}
	
	ArrayList<Double> a = new ArrayList<Double>();
	double norm = 0.0;
	public double computenorm() throws IOException {
		
		for(int i = 0;i < reader.maxDoc();i++) {
			Document doc = reader.document(i);
			String text = doc.getField("content").stringValue();
			String[] textt = text.split(" ");
			double[] arr = new double[textt.length];
			
			double ans = 0.0;
			for(int j = 0;j < textt.length;j++) {
				String word = textt[j];
				int count = 0;
				for(int k = 0;k < textt.length;k++) {
					if(word.equals(textt[k])) {
						count++;
					}
				}
				arr[j] = count;
			}
			for(int j = 0;j < arr.length;j++) {
				arr[j] = 1 + Math.log(arr[j]);
				norm += Math.pow((arr[j]),2);
			}
			
			norm = 1/(Math.sqrt(norm));
			/*for(int j = 0;j < arr.length;j++) {
				arr[j] = arr[j] * norm;
			}
			for(int j = 0;j < arr.length;j++) {
				ans += arr[j];
			}
			a.add(ans);*/
		}
		return norm;
	}

}
