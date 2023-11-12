package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.graph.Edge;
import util.graph.Graph;
import util.graph.Vertex;
import visitors.ClassDeclarationVisitor;
import visitors.MethodInvocationVisitor;

public class Parser {
	
	private static String projectPath;
	private static String projectSourcePath;
	private String sep;
	private List<CompilationUnit> cus;
	
	// create AST
	private CompilationUnit parse(char[] classSource) {
		ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 
		parser.setBindingsRecovery(true);
 
		Map<?, ?> options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
 
		parser.setUnitName("");
 
		String[] sources = { projectSourcePath }; 
		String[] classpath = { System.getProperty("java.home") };
 
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
		parser.setSource(classSource);
		
		return (CompilationUnit) parser.createAST(null); // create and parse
	}
	
	public void parseAllFiles() throws IOException {
		cus = new ArrayList<>();
		// read java files
		final File folder = new File(projectSourcePath);
		ArrayList<File> javaFiles = listJavaFilesForFolder(folder);
		for (File fileEntry : javaFiles) {
			String content = FileUtils.readFileToString(fileEntry);
			CompilationUnit cu = parse(content.toCharArray());
			cus.add(cu);
		}
	}
	
	
	// read all java files from specific folder
	public static ArrayList<File> listJavaFilesForFolder(final File folder) {
		ArrayList<File> javaFiles = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				javaFiles.addAll(listJavaFilesForFolder(fileEntry));
			} else if (fileEntry.getName().contains(".java")) {
				javaFiles.add(fileEntry);
			}
		}

		return javaFiles;
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
		projectSourcePath = projectPath + sep + "src";
	}
	
	public Graph buildCallGraph() {
        Graph callGraph = new Graph();
        
        for (CompilationUnit cu : cus) {
        	ClassDeclarationVisitor classVisitor = new ClassDeclarationVisitor();
        	cu.accept(classVisitor);
        	for (TypeDeclaration callerClass : classVisitor.getClasses()){
        		
        		for(MethodDeclaration methodDecl : callerClass.getMethods()) {
        			String className = callerClass.getName().toString()+ "." + methodDecl.getName().getFullyQualifiedName() ;
        			Vertex callerClassV = new Vertex(className);
        			callGraph.addVertex(callerClassV);
        			MethodInvocationVisitor methodInvVisitor = new MethodInvocationVisitor();
        			methodDecl.accept(methodInvVisitor);
        		
        			for (MethodInvocation i : methodInvVisitor.getMethods()) {
        				if (i.getExpression() == null) { // method has void signature
        					break;
        				}        				
        				ITypeBinding binding = i.getExpression().resolveTypeBinding();
        				if (binding != null && !(binding.toString().contains("MISSING")) && !(binding.toString().contains("WARNING"))) {
        					Vertex calledClassV = new Vertex(binding.getName() + "." + i.getName().getFullyQualifiedName()) ;
        					callGraph.addVertex(calledClassV);
        					callGraph.addEdge(new Edge(callerClassV, calledClassV));        					
        				}
                    }
        			
        			for (SuperMethodInvocation i : methodInvVisitor.getSuperMethod()) {
    					Vertex calledClassV = new Vertex(i.getName() + "." + i.getName().getFullyQualifiedName()) ;
    					callGraph.addVertex(calledClassV);
    					callGraph.addEdge(new Edge(callerClassV, calledClassV));        					
                    }
        		}
        	}
        }
        return callGraph;
    }
	
}
