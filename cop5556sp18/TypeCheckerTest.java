package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void testOneDeclaration() throws Exception {
		String input = "emptyProg{image img[256,256];}";
		typeCheck(input);
	}
	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	@Test
	public void testValidProgram() throws Exception {
		String input = "sample{" +
				"image img;" +
				"input img from @0;" +
				"show img;" +
				"sleep(4000);" +
				"image img2[width(img),height(img)];" +
				"int x;" +
				"x := 0;" +
				"while(x<width(img2)) {" +
				"int y;" +
				"y := 0;" +
				"while( y < height(img2)) {" +
				"blue(img2[x,y]) := red(img[x,y]);" +
				"green(img2[x,y]) := blue(img[x,y]);" +
				"red(img2[x,y]) := green(img[x,y]);" +
				"alpha(img2[x,y]) := Z;" +
				"y := y + 1;" +
				"};" +
				"x:= x + 1;" +
				"};" +
				"show img2;" +
				"sleep(4000);" +
				"}";
		typeCheck(input);
	}
}
