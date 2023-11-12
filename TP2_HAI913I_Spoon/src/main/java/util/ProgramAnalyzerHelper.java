package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import util.graph.Edge;
import util.graph.Graph;
import util.graph.Vertex;
import util.graph.WeightedEdge;
import util.tree.Node;

public class ProgramAnalyzerHelper {
	
	public static List<Node> singletonChildren;
	public static Graph callGraph;
	public static Graph couplingGraph;
	public static Node clusteringTree;
	
	public static void run(String projectName) throws IOException, InterruptedException {
		ParserSpoon parser = new ParserSpoon();
		parser.initialize();
		parser.configure(projectName);
		parser.parseAllFiles();
		callGraph = parser.buildCallGraph();
		buildCouplingGraph();
		Set<Edge> edgesWithoutDuplicates = removeDuplicateCouplings(couplingGraph.getEdges());
		couplingGraph.setEdges(edgesWithoutDuplicates);
		singletonChildren = new ArrayList<>();
        buildClusteringTree();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while(true) {
        	showOptions();
    		String s = br.readLine();
    		try {
    			int choice = Integer.valueOf(s);    			
	    		System.out.println();
	    		if (choice == 5) {
	    			for (int i = 1; i < 5; i++) {
	    				showResult(i);
					}
	    		}
	    		else {
	    			showResult(choice);    			
	    		}
	    		Thread.sleep(2000);
    		}
    		catch(Exception e) {
    			System.err.println("Wrong number format");
    			System.exit(0);
    		}
        }
	}
	
	public static double getCouplingBetweenAB(String classA, String classB) {
        if (callGraph.getEdges().size() == 0 
        		|| !classIsInGraph(callGraph, classA)
        		|| !classIsInGraph(callGraph, classB)) {
            return 0.0 ;
        }

        int calls = 0;
        for (Edge e : callGraph.getEdges()) {
            String callerClass = e.getV1().getName().split("\\.")[0] ;
            String calledClass = e.getV2().getName().split("\\.")[0] ;

            if (callerClass.equals(classA) && calledClass.equals(classB) 
            		|| (callerClass.equals(classB) && calledClass.equals(classA))) {
                calls++;
            }
        }
        return calls / (double) callGraph.getEdges().size() ;
    }
	
	
	public static Set<Edge> removeDuplicateCouplings(Set<Edge> couplings) {
		Set<Edge> newCouplings = new HashSet<>();
		
		newCouplings.addAll(couplings);
		
		for (Edge coupling1 : couplings) {
			for (Edge coupling2 : couplings) {
				if (coupling1.getV1().getName().equals(coupling2.getV2().getName()) &&
						coupling1.getV2().getName().equals(coupling2.getV1().getName()) &&
						newCouplings.contains(coupling1) && newCouplings.contains(coupling2))  {
					newCouplings.remove(coupling2);
					
				}
			}
		}
		return newCouplings;
	}
	
	public static boolean classIsInGraph(Graph g, String className) {
        for (Vertex v : g.getVertexes()) {
            if (v.getName().split("\\.")[0].equals(className)) {
                return true;
            }
        }
        return false;
    }
	
	public static List<String> getAllClassesOfGraph(Graph graph) {
		List<String> classesNames = new ArrayList<>();
		for (Vertex v : graph.getVertexes()) {
			classesNames.add(v.getName().split("\\.")[0]);
		}
		classesNames = classesNames.stream()
				.distinct()
				.collect(Collectors.toList());
		return classesNames;
	}
	
	public static void buildCouplingGraph() {
		couplingGraph = new Graph();
		
		List<String> classes = getAllClassesOfGraph(callGraph);
		
		for (String class1: classes) {
			for (String class2 : classes) {
				double couplingValue = getCouplingBetweenAB(class1, class2); 
				if (couplingValue > 0.) {
					Vertex v1 = new Vertex(class1);
					Vertex v2 = new Vertex(class2);
					Edge e = new WeightedEdge(v1, v2, couplingValue);
					couplingGraph.addVertex(v1);
					couplingGraph.addVertex(v2);
					couplingGraph.addEdge(e);
				}
			}
		}
	}
	
	public static void buildClusteringTree() {
		clusteringTree = new Node();
		
		List<Vertex> classes = new ArrayList<>();
		List<Edge> relations = new ArrayList<>();
		
		for (Edge e: couplingGraph.getEdges()) {
			relations.add(new WeightedEdge(e.getV1(), e.getV2(), e.getWeight()));
		}
		for (Vertex v: couplingGraph.getVertexes()) {
			classes.add(new Vertex(v.getName()));
		}
		
		List<Node> nodes = new ArrayList<>();
		
		while (classes.size() > 1 && relations.size() > 0){
			Edge mostCoupled = getMostCoupled(relations);
			relations.remove(mostCoupled);
			
			Vertex c3 = new Vertex("(" + mostCoupled.getV1().getName() + " " + mostCoupled.getV2().getName() + ")") ;
			
			relations = clusterify(mostCoupled.getV1(), mostCoupled.getV2(), c3, relations) ;
			classes.remove(mostCoupled.getV1());
			classes.remove(mostCoupled.getV2());
			classes.add(c3) ;

            Node node1 = new Node(mostCoupled.getV1().getName()) ;
            Node node2 = new Node(mostCoupled.getV2().getName()) ;
            Node parentNode = new Node(c3.getName()) ;

            // remove old coupled nodes
            for (Node node : nodes) {
                if (node.toString().equalsIgnoreCase(mostCoupled.getV1().getName())) { node1 = node; }
                if (node.toString().equalsIgnoreCase(mostCoupled.getV2().getName())) { node2 = node; }
            }
            if (nodes.contains(node1)) { nodes.remove(node1); }
            if (nodes.contains(node2)) { nodes.remove(node2); }

            parentNode.addChild(node1); parentNode.addChild(node2);
            parentNode.setWeight(calculateWeight(parentNode));
            nodes.add(parentNode) ;
            
        }
	    for (Node node : nodes) { clusteringTree.addChild(node); }
	}
	
	public static List<Edge> clusterify(Vertex v1, Vertex v2, Vertex v3, List<Edge> edges) {
        List<Edge> newEdges = new ArrayList<>() ;

        for (Edge e : edges) {
            if (e.getV1().getName().equals(v1.getName()) ||
	           e.getV1().getName().equals(v2.getName()))
            {
            	e.setV1(v3);
            }

            if (!newEdges.contains(e)) {newEdges.add(e);}
        }
        return newEdges;
    }
	
	public static Edge getMostCoupled(List<Edge> edges) {
		return edges.stream()
				.max(Comparator.comparingDouble(edge -> edge.getWeight()))
				.orElse(null);
	}
		
	public static List<Node> identifyModules(double cp, Node currentNode) {
        List<Node> modules = new ArrayList<>() ;
        // equivalent to ceilDiv
        int nbClasses = -Math.floorDiv(-couplingGraph.getVertexes().size(),2);
      
        for (Node child : currentNode.getChildren()) {
        	singletonChildren.clear();
        	getAllSingletonChildren(child);
            if (averageCoupling(singletonChildren) >= cp && singletonChildren.size() <= nbClasses)  {
                modules.add(child) ;
            }else {
                List<Node> subModules = identifyModules(cp, child);
                if (modules.size() + subModules.size() < nbClasses) { modules.addAll(subModules) ; }
            }
        }
        return modules;
    }
	
	public static double averageCoupling(List<Node> nodes) {
        double sum = 0.0 ;
        int nbCouples = 0;
        for (Node node1 : nodes) {
            for (Node node2 : nodes) {
                if (! node1.equals(node2)) {
                    sum += getCouplingBetweenAB(node1.toString(), node2.toString()) ;
                    nbCouples++;
                }
            }
        }
        return sum / nbCouples;
    }
	
	public static void getAllSingletonChildren(Node node) {
		if (node.isLeaf()) {
    		singletonChildren.add(node);
    	}
    	else {
    		for (Node child : node.getChildren()) {
    			getAllSingletonChildren(child);
    		}
    	}
	}
	
	public static double calculateWeight(Node node) {
	    	
		singletonChildren.clear();
		getAllSingletonChildren(node);
    	double weight = 0.;
    	for (Node child : singletonChildren) {
    		for (Node child2 : singletonChildren) {
    			for (Edge e : couplingGraph.getEdges()) {
    				if (e.getV1().getName().equals(child.getName()) && e.getV2().getName().equals(child2.getName())) {
    					weight+= e.getWeight();
    				}
    			}
    		}
    	}
    	return weight;
    }
	
	public static void showOptions() {
        System.out.println("What do you want to see ? :");
        System.out.println("1 - Call graph (generated with Spoon)");
        System.out.println("2 - Coupling graph");
        System.out.println("3 - Clustering");
        System.out.println("4 - Identified modules");
        System.out.println("5 - All of the above");
        System.out.print("Your choice : ");
    }
	
	public static void showGraph(Graph graph) {
		for (Vertex v : graph.getVertexes()) {
			if (graph.getEdgesGivenVertex(v).size() != 0)
				System.out.println("for " + v.toString() + " :");
			
			for (Edge e : graph.getEdgesGivenVertex(v)) {
				System.out.println("  " + e.toString());
			}
		}
		System.out.println();
	}
	
	public static void showResult(int choice) {
		switch(choice) {
		case 1: {
			System.out.println("Call graph :\n");
			showGraph(callGraph);
	        break;
		}
		case 2: {
			System.out.println("Coupling graph :\n");
			showGraph(couplingGraph);
			break;
		}
		case 3: {
			System.out.println("Clustering Tree :\n");
			System.out.println(clusteringTree + "\n");
			break;
		}
		case 4: {
			System.out.println("Modules (with 0.1 cp) :\n");
			System.out.println(identifyModules(0.1, clusteringTree) + "\n");
			System.out.println("Modules (with 0.2 cp) :\n");
			System.out.println(identifyModules(0.2, clusteringTree) + "\n");
			break;
		}
		default: {
			System.exit(0);
		}
	}
	}
}
