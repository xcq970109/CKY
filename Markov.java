package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.ling.Tree;


public class Markov {
	
	static int h = 2;
	static int v = 2;
	
	public static Tree<String> annotateTreeMarkovBinarizationWithLeft(Tree<String> unAnnotatedTree) {
		return markovBinarizeTreeWithLeft(unAnnotatedTree,"dummy");
	}
	
	public static Tree<String> annotateTreeMarkovBinarizationWithRight(Tree<String> unAnnotatedTree) {
		return markovBinarizeTreeWithRight(unAnnotatedTree,"dummy");
	}

	public static Tree<String> annotateTreeLosslessBinarizationWithLeft(Tree<String> unAnnotatedTree) {
		return binarizeTreeWithLeft(unAnnotatedTree);
	}

	
	private static Tree<String> markovBinarizeTreeWithRight(Tree<String> tree, String parent) {
		String label = tree.getLabel();
		String vLabel = v > 1 ? String.format("%s^%s", label, parent): label;
		
		if (tree.isLeaf()) {
			return new Tree<String>(label);
		}

		if (tree.getChildren().size() == 1) {
			return new Tree<String>(vLabel,Collections.singletonList(markovBinarizeTreeWithRight(tree.getChildren().get(0), label)));
		}
		
		Tree<String> intermediateTree = markovBinarizeTreeHelperWithRight(tree, 0, "@"+vLabel+"->..", label, tree.getChildren().size());
		return new Tree<String>(vLabel, intermediateTree.getChildren());
	}

	private static Tree<String> markovBinarizeTreeHelperWithRight(Tree<String> tree, int numChildrenToBeGenerated, String vLabel, String parentForLowerLevel, int size) {
		Tree<String> leftTree = markovBinarizeTreeWithRight(tree.getChildren().get(numChildrenToBeGenerated), parentForLowerLevel);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(leftTree);
		
		if (numChildrenToBeGenerated < size - 1) {
			Tree<String> rightTree = markovBinarizeTreeHelperWithRight(tree, numChildrenToBeGenerated + 1, vLabel, parentForLowerLevel, size);
			children.add(rightTree);
		}
		else {
			return leftTree;
		}

		for (int i = Math.max(0, numChildrenToBeGenerated - h) ; i < numChildrenToBeGenerated; ++i) {
			vLabel += "_"+tree.getChildren().get(i).getLabel();
		}
		
		return new Tree<String>(vLabel, children);
	}
	
	private static Tree<String> markovBinarizeTreeWithLeft(Tree<String> tree,String parent) {
		String label = tree.getLabel();
		String vLabel = v > 1 ? String.format("%s^%s", label, parent): label;
		
		if (tree.isLeaf()) {
			return new Tree<String>(label);
		}
		
		if (tree.getChildren().size() == 1) {
			return new Tree<String>(vLabel,Collections.singletonList(markovBinarizeTreeWithLeft(tree.getChildren().get(0), label)));
		}
		
		Tree<String> intermediateTree = markovBinarizeTreeHelperWithLeft(tree, tree.getChildren().size() - 1, "@"+vLabel+"->..", label);
		
		return new Tree<String>(vLabel, intermediateTree.getChildren());
	}

	private static Tree<String> markovBinarizeTreeHelperWithLeft(Tree<String> tree,int numChildrenGenerated, String vLabel, String parent) {
		Tree<String> rightTree = markovBinarizeTreeWithLeft(tree.getChildren().get(numChildrenGenerated), parent);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		
		if (numChildrenGenerated > 0) {
			Tree<String> leftTree = markovBinarizeTreeHelperWithLeft(tree, numChildrenGenerated - 1, vLabel, parent);
			children.add(leftTree);
		} 
		else {
			return rightTree;
		}
		
		children.add(rightTree);
		int bound = Math.min(tree.getChildren().size()-1, numChildrenGenerated + h);
		for (int i = numChildrenGenerated + 1 ; i <= bound; ++i) {
			vLabel += "_"+tree.getChildren().get(i).getLabel();
		}
		
		return new Tree<String>(vLabel, children);
	}
	
	private static Tree<String> binarizeTreeWithLeft(Tree<String> tree) {
		String label = tree.getLabel();
		
		if (tree.isLeaf()) 
			return new Tree<String>(label);
		
		if (tree.getChildren().size() == 1) { 
			return new Tree<String>(label, Collections.singletonList(binarizeTreeWithLeft(tree.getChildren().get(0)))); 
		}

		Tree<String> intermediateTree = binarizeTreeHelperWithLeft(tree, tree.getChildren().size()-1, "@" + label + "->");
		
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelperWithLeft(Tree<String> tree, int numChildrenGenerated, String intermediateLabel) {
		Tree<String> rightTree = binarizeTreeWithLeft( tree.getChildren().get(numChildrenGenerated));
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		
		if (numChildrenGenerated > 0) {
			Tree<String> leftTree = binarizeTreeHelperWithLeft(tree, numChildrenGenerated -1, intermediateLabel);
			children.add(leftTree);
		}
		else {
			return rightTree;
		}
		
		children.add(rightTree);
		
		for (int i = 0 ; i <= numChildrenGenerated; ++i) {
			intermediateLabel += "_"+tree.getChildren().get(i).getLabel();
		}
		
		return new Tree<String>(intermediateLabel, children);
	}
}
