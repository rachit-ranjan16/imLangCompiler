 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testParens() throws LexicalException {
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
	}

	@Test
	public void testAllBracketsBalanced() throws LexicalException {
		String inp = "[{()}]";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, LSQUARE, 0, 1, 1,1);
		checkNext(scanner, LBRACE, 1, 1, 1,2);
		checkNext(scanner, LPAREN, 2, 1, 1,3);
		checkNext(scanner, RPAREN, 3, 1, 1,4);
		checkNext(scanner, RBRACE, 4, 1, 1,5);
		checkNext(scanner, RSQUARE, 5, 1, 1,6);
	}

	@Test
	public void testAllBracketsUnbalanced() throws LexicalException {
		String inp = "]})\n({[";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, RSQUARE, 0, 1, 1,1);
		checkNext(scanner, RBRACE, 1, 1, 1,2);
		checkNext(scanner, RPAREN, 2, 1, 1,3);
		checkNext(scanner, LPAREN, 4, 1, 2,1);
		checkNext(scanner, LBRACE, 5, 1, 2,2);
		checkNext(scanner, LSQUARE, 6, 1, 2,3);
	}

	@Test
	public void testSeparators() throws LexicalException {
		String inp = "{\n ;\n;\n[  ];\n(,) \n(.);\n}";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, LBRACE,0,1,1,1);
		checkNext(scanner, SEMI,3,1,2,2);
		checkNext(scanner, SEMI,5,1,3,1);
		checkNext(scanner, LSQUARE,7,1,4,1);
		checkNext(scanner, RSQUARE,10,1,4,4);
		checkNext(scanner, SEMI,11,1,4,5);
		checkNext(scanner, LPAREN,13,1,5,1);
		checkNext(scanner, COMMA,14,1,5,2);
		checkNext(scanner, RPAREN,15,1,5,3);
		checkNext(scanner, LPAREN,18,1,6,1);
		checkNext(scanner, DOT,19,1,6,2);
		checkNext(scanner, RPAREN,20,1,6,3);
		checkNext(scanner, SEMI,21,1,6,4);
		checkNext(scanner, RBRACE,23,1,7,1);
	}

	@Test
	public void testLTGTVariations() throws LexicalException {
		String inp = "<<\n>=\n<";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, LPIXEL,0,2,1,1);
		checkNext(scanner, OP_GE,3,2,2,1);
		checkNext(scanner, OP_LT,6,1,3,1);
	}

	@Test
	public void testMultiCharOperators() throws LexicalException {
		String inp = "+-!!=::=***@";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, OP_PLUS,0,1,1,1);
		checkNext(scanner, OP_MINUS,1,1,1,2);
		checkNext(scanner, OP_EXCLAMATION,2,1,1,3);
		checkNext(scanner, OP_NEQ,3,2,1,4);
		checkNext(scanner, OP_COLON,5,1,1,6);
		checkNext(scanner, OP_ASSIGN,6,2,1,7);
		checkNext(scanner, OP_POWER,8,2,1,9);
		checkNext(scanner, OP_TIMES,10,1,1,11);
		checkNext(scanner, OP_AT,11,1,1,12);
	}

	@Test
	public void testIllegalOperator() throws LexicalException {
		String inp = ">=|=";
		show(inp);
		thrown.expect(LexicalException.class);
		try {
			show(new Scanner(inp).scan());
		} catch (LexicalException e) {
			show(e);
			assertEquals(3,e.getPos());
			throw e;
		}
	}

	@Test
	public void testValidComment() throws LexicalException {
		String inp = ">/* Some Comment */<<";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, OP_GT,0,1,1,1);
		checkNext(scanner, LPIXEL,19,2,1,20);
	}


	@Test
	public void testBoolean() throws LexicalException {
		String inp = "true!=false.";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL,0,4,1,1);
		checkNext(scanner, OP_NEQ,4,2,1,5);
		checkNext(scanner, BOOLEAN_LITERAL,6,5,1,7);
		checkNext(scanner, DOT,11,1,1,12);
	}

	@Test
	public void testIntegerExpression() throws LexicalException {
		String inp = "32*23==23*32";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL,0,2,1,1);
		checkNext(scanner, OP_TIMES,2,1,1,3);
		checkNext(scanner, INTEGER_LITERAL,3,2,1,4);
		checkNext(scanner, OP_EQ,5,2,1,6);
		checkNext(scanner, INTEGER_LITERAL,7,2,1,8);
		checkNext(scanner, OP_TIMES,9,1,1,10);
		checkNext(scanner, INTEGER_LITERAL,10,2,1,11);
	}

	@Test
	public void testFloatingPointExpression() throws LexicalException {
		String inp = "3.2*2.3==2.3*3.2";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL,0,3,1,1);
		checkNext(scanner, OP_TIMES,3,1,1,4);
		checkNext(scanner, FLOAT_LITERAL,4,3,1,5);
		checkNext(scanner, OP_EQ,7,2,1,8);
		checkNext(scanner, FLOAT_LITERAL,9,3,1,10);
		checkNext(scanner, OP_TIMES,12,1,1,13);
		checkNext(scanner, FLOAT_LITERAL,13,3,1,14);
		inp = ".23 + .345 + 0.11";
		scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL,0,3,1,1);
		checkNext(scanner, OP_PLUS,4,1,1,5);
		checkNext(scanner, FLOAT_LITERAL,6,4,1,7);
		checkNext(scanner, OP_PLUS,11,1,1,12);
		checkNext(scanner, FLOAT_LITERAL,13,4,1,14);
	}

	@Test
	public void testKeyWords() throws LexicalException {
		String inp = "default_width:=blue+cart_x**polar_a<<Z";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, KW_default_width,0,13,1,1);
		checkNext(scanner, OP_ASSIGN,13,2,1,14);
		checkNext(scanner, KW_blue,15,4,1,16);
		checkNext(scanner, OP_PLUS,19,1,1,20);
		checkNext(scanner, KW_cart_x,20,6,1,21);
		checkNext(scanner, OP_POWER,26,2,1,27);
		checkNext(scanner, KW_polar_a,28,7,1,29);
		checkNext(scanner, LPIXEL,35,2,1,36);
		checkNext(scanner, KW_Z,37,1,1,38);
	}

	@Test
	public void testLineOfCode() throws LexicalException {
		String inp = "default_width := blue_not$kw + cart_x ** pola2r_Bb <<  Z\n\talpha :=\tawesome ** sin .234 + atan 66";
		Scanner scanner = new Scanner(inp).scan();
		show(inp);
		show(scanner);
		checkNext(scanner, KW_default_width,0,13,1,1);
		checkNext(scanner, OP_ASSIGN,14,2,1,15);
		checkNext(scanner, IDENTIFIER,17,11,1,18);
		checkNext(scanner, OP_PLUS,29,1,1,30);
		checkNext(scanner, KW_cart_x,31,6,1,32);
		checkNext(scanner, OP_POWER,38,2,1,39);
		checkNext(scanner, IDENTIFIER,41,9,1,42);
		checkNext(scanner, LPIXEL,51,2,1,52);
		checkNext(scanner, KW_Z,55,1,1,56);
		checkNext(scanner, KW_alpha,58,5,2,2);
		checkNext(scanner, OP_ASSIGN,64,2,2,8);
		checkNext(scanner, IDENTIFIER,67,7,2,11);
		checkNext(scanner, OP_POWER,75,2,2,19);
		checkNext(scanner, KW_sin,78,3,2,22);
		checkNext(scanner, FLOAT_LITERAL,82,4,2,26);
		checkNext(scanner, OP_PLUS,87,1,2,31);
		checkNext(scanner, KW_atan,89,4,2,33);
		checkNext(scanner, INTEGER_LITERAL,94,2,2,38);
	}

	@Test
	public void testInvalidIdentifier() throws LexicalException {
		String inp = "apl~ha = 3 ** 4";
		show(inp);
		thrown.expect(LexicalException.class);
		try {
			show(new Scanner(inp).scan());
		} catch (LexicalException e) {
			show(e);
			assertEquals(3,e.getPos());
			throw e;
		}
	}
}
	

