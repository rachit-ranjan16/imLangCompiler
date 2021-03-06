package cop5556sp18;

import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		@SuppressWarnings("unused")
		Program p = parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.progName);
		assertEquals(0, p.block.decsOrStatements.size());
	}	
	
	
	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	
	/** This test illustrates how you can test specific grammar elements by themselves by
	 * calling the corresponding parser method directly, instead of calling parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}

	@Test
	public void testFailedCase() throws LexicalException, SyntaxException {
		String input = "sin x";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		try {
			parser.expression();  //call expression here instead of parse
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Test
	public void testAnotherFailedCase() throws LexicalException, SyntaxException {
		String input = "prog{int var1[500, 1];}";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		try {
			Program p = parser.parse();  //call expression here instead of parse
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	@Test
	public void testValidProgram() throws LexicalException, SyntaxException {
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
		Parser parser = makeParser(input);
		Program p = parser.parse();
		assertEquals(p.progName, "sample");
		assertEquals(p.block.decsOrStatements.size(), 10);
		assertEquals(p.block.decsOrStatements.get(4).firstToken.getKind(), IDENTIFIER);
		System.out.println("Done");
	}


}
	

