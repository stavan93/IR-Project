import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * @author Gayathri
 *
 */
public class BigramLM {

	/**
	 * 
	 */
	private Map<String, Map<String, Integer>> documentUnigramMap = new HashMap<>();
	private Map<String, Map<String, Integer>> documentBigramMap = new HashMap<>();
	private int vocabularySize;
	private Double firstTermProbability ;	
	
	public int getVocabularySize() {
		return vocabularySize;
	}

	public void setVocabularySize(int vocabularySize) {
		this.vocabularySize = vocabularySize;
	}

	public BigramLM() {
		// TODO Auto-generated constructor stub
	}

	public void bigramCount(Map<String, List<String>> documentTokens) {
		int count = 0;
		for (Map.Entry<String, List<String>> docIdToken : documentTokens.entrySet()) {
			Map<String, Integer> unigramMap = new HashMap<>();
			Map<String, Integer> bigramMap = new HashMap<>();
			unigramMap.put("$", 0);
			String prevToken = "$";
			for (String token : docIdToken.getValue()) {
				if (!unigramMap.containsKey(token)) {
					unigramMap.put(token, 0);
				}
				unigramMap.put(token, unigramMap.get(token) + 1);
				String key = prevToken + " " + token;
				if (!bigramMap.containsKey(key)) {
					bigramMap.put(key, 0);
				}
				bigramMap.put(key, bigramMap.get(key) + 1);
				prevToken = token;
			}
			documentUnigramMap.put(docIdToken.getKey(), unigramMap);
			documentBigramMap.put(docIdToken.getKey(), bigramMap);
		}
	}

	public Map<String, Double> calculateScoresForQueryTokens(Map<String, List<String>> documentTokens,
			List<String> queryTokens) {
		Map<String, Map<String, Double>> documentBigramQueryProb = new HashMap<>();
		Map<String, Double> documentScores = new HashMap<>();
		Map<String, Integer> documetFirstTermCountMap = new HashMap<>();
		Double finalValue = 1.0;
		String prevToken = "$";
		String firstToken = "";
		for (String token : queryTokens) {
			firstTermProbability= 1.0;
			String key = prevToken + " " + token;
			if (!documentBigramMap.entrySet().isEmpty()) {
				for (Map.Entry<String, Map<String, Integer>> entry : documentBigramMap.entrySet()) {
					String docId = entry.getKey();
					if (!documentBigramQueryProb.containsKey(docId)) {
						documentBigramQueryProb.put(docId, new HashMap<>());
					}
					if (!entry.getValue().containsKey(key)) {
						entry.getValue().put(key, 0);
					}
					Double bigramCount = (double) entry.getValue().get(key) + 1;
					if (!documentUnigramMap.get(docId).containsKey(prevToken)) {
						documentUnigramMap.get(docId).put(prevToken, 0);
					}
					if (documentTokens.containsKey(docId) && documentTokens.get(docId).size() != 0) {
						Double unigramCount = (double) documentUnigramMap.get(docId).get(prevToken)
									+ getVocabularySize();
						finalValue = (bigramCount / unigramCount);
						documentBigramQueryProb.get(docId).put(key, (finalValue));
					}
					if(prevToken.contains("$"))  //Calculate P(A) 
					{
						firstToken = token;
					}
					if (!documetFirstTermCountMap.containsKey(docId)) {
						documetFirstTermCountMap.put(docId, 0);
					}
					if (token.equals(firstToken)) {
						documetFirstTermCountMap.put(docId, documetFirstTermCountMap.get(docId)+1);
					}
					
				}
				prevToken = token;
			}
		}

		for (Map.Entry<String, Map<String, Double>> entry : documentBigramQueryProb.entrySet()) {
			String docId = entry.getKey();
			prevToken = "$";
			for (String token : queryTokens) {
				String key = prevToken + " " + token;
				if (!documentScores.containsKey(docId)) {
					documentScores.put(docId, 1.0);
				}
				if (!entry.getValue().containsKey(key)) {
					entry.getValue().put(key, 1.0);
				}
				
				documentScores.put(docId, (((double)documetFirstTermCountMap.get(docId))/(double)documentUnigramMap.get(docId).size()) * documentScores.get(docId) * entry.getValue().get(key));
			
			}
		}
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(documentScores.entrySet());
		Collections.sort(list, (obj1, obj2) -> obj2.getValue().compareTo(obj1.getValue()));
		Map<String, Double> resultMap = new LinkedHashMap<>();
		list.forEach(arg0 -> { resultMap.put(arg0.getKey(), arg0.getValue()); });
		/*
		 * resultMap =
		 * documentScores.entrySet().stream().collect(Collectors.toMap(Map.Entry::
		 * getKey, Map.Entry::getValue)) .entrySet().stream().sorted(Map.Entry.<String,
		 * Double>comparingByValue().reversed()).limit(count).collect(Collectors.toMap(
		 * Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		 *//*
							 * .stream() .collect( Collectors.toMap( Map.Entry::getKey, entry ->
							 * entry.getValue().size() ) // get a map where key=original key and value list
							 * size ) // {64=3, 65=4, 2=1, 66=3, 67=2, 4=1} .entrySet() .stream() // sort
							 * map by value - list size .sorted(
							 * Map.Entry.<Integer,Integer>comparingByValue().reversed()) .limit(3) // get
							 * top 3 .map(Map.Entry::getKey) .collect(Collectors.toList());
							 */
		return resultMap;
		/*
		 * return documentScores.entrySet().stream().sorted(Map.Entry.<String,
		 * Double>comparingByValue().reversed()) .limit(count)
		 * .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) ->
		 * e1, LinkedHashMap::new));
		 */
	}

	public Map<String, Double> performSearch1(Map<String, List<String>> listTokens, List<String> queryTokens) {
		// TODO Auto-generated method stub
		bigramCount(listTokens);
		return calculateScoresForQueryTokens(listTokens, queryTokens);
	}
}
