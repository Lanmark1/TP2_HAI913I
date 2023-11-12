package util;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import util.graph.Edge;
import util.graph.Graph;
import util.graph.Vertex;

import java.io.File;
import java.io.IOException;

public class ParserSpoon {

	private static String projectPath;
	private String sep;
    private CtModel model;
    
    public ParserSpoon(){ 
    	
    }

    public void initialize() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			sep = "\\";
		}
		else {
			sep = "/";
		}
	}
	
	public void configure(String projectName) {
		if (projectName.equals("")) { // default project to parse if no input is provided
			projectPath = System.getProperty("user.dir") + sep +"projectsToParse" + sep + "dummyProject";
		}
		else {
			projectPath =  System.getProperty("user.dir") + sep +"projectsToParse" + sep + projectName;						
		}
	}
    
    public void parseAllFiles() throws IOException {
    	final File folder = new File(projectPath);
    	Launcher launcher = new Launcher();
    	launcher.addInputResource(folder.getPath());
    	launcher.getEnvironment().setComplianceLevel(8);
        model = launcher.buildModel();
    }
    
    public Graph buildCallGraph() {
        Graph callGraph = new Graph();
        for (CtType type : model.getAllTypes()) {
            for (Object method : type.getMethods()) {
                CtMethod p = (CtMethod) method;
                String callerClass = type.getSimpleName();
                String callerMethod = p.getSimpleName();

                Vertex caller = new Vertex(callerClass+ "." + callerMethod);
                callGraph.addVertex(caller);

                p.filterChildren(new TypeFilter(CtInvocation.class)).forEach(inv->{
                    CtInvocation<?> invocation = (CtInvocation<?>) inv;

                    String calledClass = invocation.getExecutable().getDeclaringType().getSimpleName();
                    String calledMethod = invocation.getExecutable().getSimpleName();

                    Vertex called = new Vertex(calledClass+ "." + calledMethod);
                    callGraph.addVertex(called);

                    callGraph.addEdge(new Edge(caller, called));
                });
            }
        }

        return callGraph;
    }
}
