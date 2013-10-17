package javaparserplugin.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javaparserpllugin.utils.Utils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */


public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */

	private static final String CONSOLE_NAME = "JavaParser Console";
	private static StringBuilder builder = null;
	private String workspacePath;
	private String projectPath;

	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		

		builder = new StringBuilder();

		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspacePath = workspace.getRoot().getLocation().toString();

		for (IProject iProject : allProjects) {
			if(JavaProject.hasJavaNature(iProject)){
				IJavaProject jProj = JavaCore.create(iProject);
				//Utils.writeToConsole(CONSOLE_NAME, workspace.getRoot().getLocation().toString());
				parseJavaProject(jProj);
			}
		}

		Utils.writeToConsole(CONSOLE_NAME, builder.toString());

		return null;
	}

	private void parseJavaProject(IJavaProject jProj){

		IPackageFragment[] fragments = null;

		try {
			fragments = jProj.getPackageFragments();
			for (IPackageFragment iPackageFragmentRoot : fragments) {
				//String location = fragments[0].getResource().getLocation().toString();
				if (iPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
					//Utils.writeToConsole(CONSOLE_NAME, "Package " + mypackage.getElementName());
					printICompilationUnitInfo(iPackageFragmentRoot, jProj);
				}
				//Utils.writeToConsole(CONSOLE_NAME, iPackageFragmentRoot.getUnderlyingResource());
			}

		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void printICompilationUnitInfo(IPackageFragment mypackage, IJavaProject jProj) throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			CompilationUnit cu = createCompilationUnit(workspacePath + unit.getPath().toString(), jProj);
			parseCompilationUnit(cu);

		}
	}

	private void printIMethods(ICompilationUnit unit) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		for (IType type : allTypes) {
			printIMethodDetails(type);
		}
	}

	private void printCompilationUnitDetails(ICompilationUnit unit) throws JavaModelException {
		Utils.writeToConsole(CONSOLE_NAME, "Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());
		Utils.writeToConsole(CONSOLE_NAME, "Has number of lines: " + doc.getNumberOfLines());
		printIMethods(unit);
	}

	private void printIMethodDetails(IType type) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			Utils.writeToConsole(CONSOLE_NAME, "Method name " + method.getElementName());
			Utils.writeToConsole(CONSOLE_NAME, "Signature " + method.getSignature());
			Utils.writeToConsole(CONSOLE_NAME, "Return Type " + method.getReturnType());
			//Utils.writeToConsole(CONSOLE_NAME, "Methond body" + method.get); 
		}
	}

	private void parseCompilationUnit(CompilationUnit cu) {

		//		ASTParser parser = ASTParser.newParser(AST.JLS3);
		//		parser.setSource(str.toCharArray());
		//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		//		parser.setResolveBindings(true);
		//		//StringBuilder builder = new StringBuilder();
		//		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		TypeDeclaration cuType = (TypeDeclaration)cu.types().get(0);
		ArrayList<MethodDeclaration> cuMethodList = new ArrayList<MethodDeclaration>(Arrays.asList(cuType.getMethods()));
		//@SuppressWarnings("unchecked")
		List cuImportsList = cu.imports();
		String cuName = cuType.getName().toString();
		builder.append("Name:\n" + cuName + "\n");
		builder.append("Imports:\n");
		for (Object importDeclaration : cuImportsList) {
			builder.append(importDeclaration);
		}
		builder.append("Methods:\n");
		for(MethodDeclaration methodDeclaration : cuMethodList){
			builder.append("- " + methodDeclaration.getName() + "\n");
			ArrayList<Statement> statementList = new ArrayList<Statement>(methodDeclaration.getBody().statements());
			//builder.append(methodDeclaration.getBody());

			for (Statement statement : statementList) {				
				builder.append("statement type: " + statement.getNodeType() + "\n");
				builder.append("\t" + statement.toString());
				if(statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT){
					//IBinding iBinding = ((VariableDeclarationStatement) statement).getType().resolveBinding();
					
					builder.append("\tvariable declaration, type: " + (((VariableDeclarationStatement) statement).getType().resolveBinding() == null? "null" : "not null" )+ "\n");

				}
			}
		}
		builder.append("\n################################################################################################\n");
	}

	private static CompilationUnit createCompilationUnit(String sourceFile, IJavaProject javaProject) {
	    String source = null;
		try {
			source = Utils.readFileToString(sourceFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ASTParser parser = ASTParser.newParser(AST.JLS3); 
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setSource(source.toCharArray()); // set source
	    parser.setProject(javaProject);
	    parser.setResolveBindings(true); // we need bindings later on
	    return (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
	}
}
