// Unigram probability with Laplace Smoothing

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class UnigramLaplace extends SimilarityBase{

	public double vocabsize;
	
	

	public UnigramLaplace(double vocabsize) {
		
		this.vocabsize = vocabsize;
	}

	@Override
	protected float  score(BasicStats stats, float freq, float docLen) {
		// Calculating the probability and perform smoothing with the given alpha.
		float score= (float) ((freq + 1) / (docLen + vocabsize));
		return (float)(score);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "UnigramLaplace";
	}
	
	
}
