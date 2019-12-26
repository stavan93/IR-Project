import org.apache.lucene.search.similarities.*;

public class UnigramDir extends SimilarityBase {

		double vocabsize;
		
		public UnigramDir(double vocabsize) {
		// TODO Auto-generated constructor stub
			this.vocabsize = vocabsize;
	}
		@Override
		protected float score(BasicStats stats, float freq, float docLen) {
			// TODO Auto-generated method stub
			
				float pt = (float) (stats.getTotalTermFreq()/vocabsize);
			
			return (float)((freq + (1000 * pt))/(docLen + 1000));
		
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "team8-UnigramDirichletSmoothing";
		}
		
	}

