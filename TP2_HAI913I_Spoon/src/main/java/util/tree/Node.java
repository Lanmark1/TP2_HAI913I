package util.tree;

import java.util.ArrayList; 
import java.util.List;

public class Node {

    ArrayList<Node> children;
    private String name;
    private double weight;
    
    public Node() {
        this.children = new ArrayList<>();
        this.name = "";
        this.weight = 0.;
    }

    public Node(String name) {
		this.children = new ArrayList<>();
    	this.name = name;
    	this.weight = 0.;
    }
    
    public Node(String name, double weight) {
		this.children = new ArrayList<>();
    	this.name = name;
		this.weight = weight;
	}

	@Override
    public String toString() {
		if (children.size() == 0) return name;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Node c : children) {
            sb.append(c.toString()).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    public void addChild(Node child) {
        children.add(child) ;
    }
    public void addAllChild(List<Node> list) {
        children.addAll(list) ;
    }
    public ArrayList<Node> getChildren() {
        return children;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
	public boolean isLeaf() {
		return this.getChildren().size() == 0;
	}
	
    public double getWeight() {
    	return this.weight;
    }
    
    public void setWeight(double weight) {
    	this.weight = weight;
    }
    
}
