package edu.berkeley.nlp.assignments.parsing.student;


import edu.berkeley.nlp.assignments.parsing.BinaryRule;

public class BinaryChartElem {
	double score;
	BinaryRule binaryRule;
	int mid;
	
	public BinaryChartElem() {
		score = Double.NEGATIVE_INFINITY;
		mid = -1;
		binaryRule = null;
	}
}
