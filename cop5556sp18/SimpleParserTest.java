 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.SimpleParser.SyntaxException;
import cop5556sp18.SimpleParser.UnSupportedOperationException;
import cop5556sp18.Scanner.LexicalException;

 public class SimpleParserTest {

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
	private SimpleParser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String input = "";  //The input is the empty string.  
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String input = "b{}";  
		SimpleParser parser = makeParser(input);
		parser.parse();
	}	

	@Test
	public void testDec0() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String inp = "b{image c;}";
		SimpleParser parser = makeParser(inp);
		parser.parse();
	}

	@Test
	public void testInvalidStartOfBlock() throws LexicalException, SyntaxException, UnSupportedOperationException {

		String inp = "false { image tr; }";
		thrown.expect(SyntaxException.class);
		try {
			SimpleParser simpleParser = makeParser(inp);
			simpleParser.parse();
		}
		catch (SyntaxException e){
			show(e);
			throw e;
		}
	}

	@Test
	 public void testImageExpressionDeclaration() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String inp ;
		SimpleParser parser;
		inp = "falsed { \n image img /* Some Comment */ [ 256, 256 ];\n}";
		parser = makeParser(inp);
		parser.parse();
		inp = "falsed { \n image img /* Some Comment */ [ a+e, b+c ];\n}";
		parser = makeParser(inp);
		parser.parse();
	}

	@Test
	 public void testStatements() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String inp ;
		SimpleParser parser;
		inp = "trued { write x to y;  }";
		parser = makeParser(inp);
		parser.parse();

	}

	@Test
	public void testInvalidStatement() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String inp;
		SimpleParser parser;
		inp = "image some_image;inpu hold fom @0;show yolo;";
		thrown.expect(SyntaxException.class);
		try {
			SimpleParser simpleParser = makeParser(inp);
			simpleParser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}

	 @Test
	 public void testAnotherInvalidStatement() throws LexicalException, SyntaxException, UnSupportedOperationException {
		String inp = "something{int x; int x:=1.2.3..}";
		thrown.expect(SyntaxException.class);
		try {
			SimpleParser simpleParser = makeParser(inp);
			simpleParser.parse();
		}
		catch (SyntaxException e){
			show(e);
			throw e;
		}
	}
	 @Test
	 public void testRotateImageValidCode() throws LexicalException, SyntaxException, UnSupportedOperationException {
		 String input = "rotateImage{" +
				 			"image h;" +
				 			"input h from @0;" +
				 			"show h;" +
				 			"sleep(4000); " +
				 			"image g[width(h),height(h)];" +
				 			"int x;" +
				 			"x := 0;" +
				 			"while(x<width(g)){" +
				 				"int y;" +
				 				"y := 0;" +
				 				"while(y<height(g)){" +
				 					"g[x,y] := h[y,x];" +
				 					"y := y + 1;" +
				 				"};" +
				 				"x := x + 1;" +
				 			"};" +
				 			"show g;" +
				 			"sleep(4000);" +
				 		"}";
		 SimpleParser parser = makeParser(input);
		 parser.parse();
	 }

	 @Test
	 public void testTurnImageGreenAndBlueValidCode() throws LexicalException, SyntaxException, UnSupportedOperationException {
		 String input =
				 "invertColors{" +
				 	"image img[256,256];" +
				 	"int x;" +
				 	"int y;" +
				 	"x:=0;" +
				 	"y:=0;" +
				 	"while(x<width(img)) {" +
				 		"y:=0;" +
				 		"while(y<height(img)) {" +
				 			"im[x,y]:=<<0,255,255,0>>;" +
				 			"y:=y+1;" +
						 "};" +
				 		"x:=x+1;" +
				 	"};" +
				 	"show img;" +
				 "}";
		 SimpleParser parser = makeParser(input);
		 parser.parse();
	 }

	 @Test
	 public void testPolarRotationValidCode() throws LexicalException, SyntaxException, UnSupportedOperationException {
		 String input = "PolarR2{" +
				 			"image img[2048,2048];" +
				 			"int x;" +
				 			"x := 0;" +
				 			"while(x < width(img)) {" +
				 				"int y;" +
				 				"y := 0;" +
				 				"while( y < height(img)) {" +
				 					"float p;" +
				 					"p := polar_r[x,y];" +
				 					"int r;" +
				 					"r := int(p)%Z;" +
				 					"img[x,y] := <<Z,0,0,r>>;" +
				 					"y := y+1;" +
				 				"};" +
				 				"x:=x+1;" +
				 			"};" +
				 			"show img;" +
				 		"}";
		 SimpleParser parser = makeParser(input);
		 parser.parse();
	 }

	 @Test
	 public void testInvertColorsValidCode() throws LexicalException, SyntaxException, UnSupportedOperationException {
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
				 				"while( y< height(img2)) {" +
				 					"blue(img2[x,y]) := red(img[x,y]);" +
				 					"green(img2[x,y]) := blue(img[x,y]);" +
				 					"red(img2[x,y]) := green(img[x,y]);" +
				 					"alpha(img2[x,y]) := Z;" +
				 "					y := y + 1;" +
				 				"};" +
				 				"x:= x + 1;" +
				 			"};" +
				 			"show img2;" +
				 			"sleep(4000);" +
				 		"}";
		 SimpleParser parser = makeParser(input);
		 parser.parse();
	 }

}
	
