import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class LNCLTN extends Similarity {
	
	double a = 0.0;
	double d = 0.0;
	public LNCLTN(double d) throws IOException {
		//this.a = a;
		this.d = d;
	}

	/*@Override
	protected float score(BasicStats stats, float freq, float docLen) {
		// TODO Auto-generated method stub
		double d_t = freq;
		double d_c = a;
		return (float) (d_t*d_c*d);
	}*/

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public long computeNorm(FieldInvertState state) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
		// TODO Auto-generated method stub
		return new TFiDF(d);
	}

}

final class TFiDF extends SimScorer{
	
	IndexReader reader = null;
	double d = 0.0;
	public TFiDF(double d) throws IOException {
		this.d = d;
		Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		reader = DirectoryReader.open(indexDir);
	}
	
	double norm = 0.0;
	@Override
	public float score(int doc, float freq) throws IOException {
		// TODO Auto-generated method stub
		Document doccc = reader.document(doc);
		int idd = Integer.parseInt(doccc.getField("id").stringValue());
		for(int i = 0;i < reader.maxDoc();i++) {
			Document docc = reader.document(i);
			int id = Integer.parseInt(docc.getField("id").stringValue());
			//System.out.println("----------------------------------idd: "+idd);
			//System.out.println("----------------------------------id: "+id);
			if(idd == id) {
				String text = docc.getField("content").stringValue();
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
			}
			
			else {
				continue;
			}
			
		}
		return (float) ((freq * norm) * d);
	}

	@Override
	public float computeSlopFactor(int distance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
