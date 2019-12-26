import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;


public class UnigramJM extends SimilarityBase{

    public double vocabsize;
    
    public UnigramJM(double vocabsize)
    {
    	
        this.vocabsize = vocabsize;
    }

    @Override
    public String toString() {
        return "UnigramJM";
    }
    
    @Override
	protected float score(BasicStats stats, float freq, float docLen) {
		
		return  (float) (((0.9) * (freq/docLen)) + ((1-0.9) * (stats.getTotalTermFreq()/vocabsize)));
		
	}

}
