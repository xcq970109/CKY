package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.assignments.parsing.UnaryRule;

public class UnaryChartElem {
	double score;
	UnaryRule unaryRule;
	
	public UnaryChartElem() {
		score = Double.NEGATIVE_INFINITY;
		unaryRule = null;
	}
}
