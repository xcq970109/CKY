package edu.berkeley.nlp.assignments.parsing.student;

import java.util.List;

import java.util.ArrayList;
import java.util.Collections;

import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;


public class CKYParser implements Parser{
	
	BinaryChartElem[][][] 	binaryChart;
	UnaryChartElem[][][]  	unaryChart;
	SimpleLexicon 		  	simpleLexicon;
	Grammar               	grammar;
	Indexer<String>		  	indexer;
	UnaryClosure			unaryClosure;
	int						numOfTags;
	int                     sentLen;
	List<String>            currSentence;
	
	public CKYParser(List<Tree<String>> trainTrees){
		List<Tree<String>> binarizedTrees = new ArrayList<Tree<String>>();
		
		for(Tree<String> tree : trainTrees) {
			binarizedTrees.add(Markov.annotateTreeMarkovBinarizationWithLeft(tree));
//			binarizedTrees.add(Markov.annotateTreeLosslessBinarization(tree));
		}
		
		simpleLexicon = new SimpleLexicon(binarizedTrees);
		grammar	= Grammar.generativeGrammarFromTrees(binarizedTrees);
		indexer = grammar.getLabelIndexer();
		numOfTags = indexer.size();
		unaryClosure = new UnaryClosure(indexer,grammar.getUnaryRules());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Tree<String> getBestParse(java.util.List<String> sentence) {
		sentLen = sentence.size();
		currSentence = sentence;
		double maxScore,ruleScore;
		

		
		binaryChart = new BinaryChartElem[sentLen][][];
		unaryChart = new UnaryChartElem[sentLen][][];
		
		for(int diff = 0; diff < sentLen; ++diff) {
			binaryChart[diff] = new BinaryChartElem[sentLen-diff][];
			unaryChart[diff] = new UnaryChartElem[sentLen-diff][];
			
			for(int left = 0; left < sentLen-diff; ++left) {
				binaryChart[diff][left] = new BinaryChartElem[numOfTags];
				unaryChart[diff][left] = new UnaryChartElem[numOfTags];
				
				for(int tagIdx = 0; tagIdx < numOfTags; ++tagIdx) {
					binaryChart[diff][left][tagIdx] = new BinaryChartElem();
					unaryChart[diff][left][tagIdx] = new UnaryChartElem();
				}
			}
		}
		
		for(int diff = 1; diff <= sentLen; ++diff) {
			
			for(int left = 0; left <= (sentLen-diff); ++left) {
				
				int right = left+diff;
				int parent;
				double leftScore;
				double rightScore;
				int pos;
				
				if(diff == 1) {
					for(int tagIdx = 0; tagIdx < numOfTags; ++tagIdx) {
						String tagString = indexer.get(tagIdx);						
						double score = simpleLexicon.scoreTagging(sentence.get(left), tagString);
						binaryChart[0][left][tagIdx].score = Double.isNaN(score) ? Double.NEGATIVE_INFINITY : score;
					}
				}
				else
				{
					for(int mid = left+1; mid < right; ++mid) {
						maxScore = Double.NEGATIVE_INFINITY;						
			
						for(int tagIdx = 0; tagIdx < numOfTags; ++tagIdx){
							leftScore = unaryChart[mid-left-1][left][tagIdx].score;
							
							if( leftScore == Double.NEGATIVE_INFINITY) 
								continue;
							
							for(BinaryRule rule : grammar.getBinaryRulesByLeftChild(tagIdx)) {
								rightScore = unaryChart[right-mid-1][mid][rule.getRightChild()].score;
								if( rightScore == Double.NEGATIVE_INFINITY)
									continue;
								
								ruleScore = rule.getScore();
								ruleScore += leftScore;
								ruleScore += rightScore;								
								parent = rule.getParent();
								pos = diff-1;
								
								if(ruleScore > binaryChart[pos][left][parent].score) {
									binaryChart[pos][left][parent].score = ruleScore;
									binaryChart[pos][left][parent].mid = mid;
									binaryChart[pos][left][parent].binaryRule = rule;
								}
							}
						}																	
					}
				}
				
				
				
				
				for(int tagIdx = 0; tagIdx < numOfTags; ++tagIdx) {
					maxScore = Double.NEGATIVE_INFINITY;
					boolean foundReflexive = false;
					
					for(UnaryRule rule : unaryClosure.getClosedUnaryRulesByParent(tagIdx)) {
						int child = rule.getChild();
						
						if(child == tagIdx)
							foundReflexive = true;
						
						ruleScore = rule.getScore();
						ruleScore += binaryChart[diff-1][left][child].score;
						
						if(ruleScore > maxScore) {
							maxScore = ruleScore;
							unaryChart[diff-1][left][tagIdx].unaryRule = rule;
						}
					}
					
					if(foundReflexive) {
						if(maxScore < binaryChart[diff-1][left][tagIdx].score) {
							maxScore = binaryChart[diff-1][left][tagIdx].score;
							unaryChart[diff-1][left][tagIdx].unaryRule = null;
						}
					}
					unaryChart[diff-1][left][tagIdx].score = maxScore;
				}
			}
		}

		Tree<String> ret;
		if(unaryChart[sentLen-1][0][0].score == Double.NEGATIVE_INFINITY) {
			ret = new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
		}else {
			ret = decodeUnaryTreeFrom(0,0,sentLen);
			
		}
		// TODO Auto-generated method stub
		return TreeAnnotations.unAnnotateTree(ret);
	}
	
	public Tree<String> decodeUnaryTreeFrom(int tagIdx, int left, int diff){
		Tree<String> resTree;
		UnaryRule rule = unaryChart[diff-1][left][tagIdx].unaryRule;
		int child = (rule == null) ? tagIdx : rule.getChild();
		
		if(diff == 1) {
			List<Tree<String>> word = Collections.singletonList(new Tree<String>(currSentence.get(left)));
			resTree = new Tree<String>(indexer.get(child),word);
		}
		else {
			resTree = decodeBinaryTreeFrom(child,left,diff);
		}
		
		if(child == tagIdx) {
			return resTree;
		}
		
		List<Integer> unaryPath = unaryClosure.getPath(rule);
		
        for (int step = unaryPath.size()-2; step >= 0; --step) {
            int stepTag = unaryPath.get(step);
            resTree = new Tree<String>(indexer.get(stepTag), Collections.singletonList(resTree));
        }
		
		return resTree;
	}
	
	public Tree<String> decodeBinaryTreeFrom(int tagIdx, int left, int diff){
		BinaryRule rule = binaryChart[diff-1][left][tagIdx].binaryRule;
		int mid = binaryChart[diff-1][left][tagIdx].mid;
		int right = left + diff;
		ArrayList<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(decodeUnaryTreeFrom(rule.getLeftChild(), left, mid-left));
		children.add(decodeUnaryTreeFrom(rule.getRightChild(), mid, right-mid));
		return new Tree<String>(indexer.get(tagIdx),children);
	}
}
