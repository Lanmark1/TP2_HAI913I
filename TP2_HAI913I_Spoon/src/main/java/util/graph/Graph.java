package util.graph;

import java.util.HashSet;
import java.util.Set;

public class Graph {

    private Set<Vertex> vertexes ;
    private Set<Edge> edges;

    public Graph() {
        vertexes = new HashSet<>() ;
        edges =  new HashSet<>() ;
    }

    public void addVertex(Vertex v) {        
    	vertexes.add(v);    
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public Set<Vertex> getVertexes() {
        return vertexes;
    }
    
    public Set<Edge> getEdgesGivenVertex(Vertex v) {
    	Set<Edge> res = new HashSet<>();
    	for (Edge e : edges) {
    		if (e.getV1().equals(v)) {
    			res.add(e);
    		}
    	}
    	return res;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Set<Edge> edges) {
    	this.edges = edges;
    }
    
    @Override
    public String toString() {
        return "Graph {" +
                "listVertex=" + vertexes +
                ", listEdge=" + edges +
                '}';
    }
}
