package cop5556sp18;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.CodeGenUtils.DynamicClassLoader;
import cop5556sp18.AST.Program;

public class CodeGenTest {

	//determines whether show prints anything
	static boolean doPrint = true;

	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	//determines whether a classfile is created
	static boolean doCreateFile = true;

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	//values passed to CodeGenerator constructor to control grading and debugging output
	private boolean devel = true; //if true, print devel output
	private boolean grade = true; //if true, print grade output

//	private boolean devel = false; 
//	private boolean grade = false; 

	//sets the default width and height of newly created images.  Should be small enough to fit on screen.
	public static final int defaultWidth = 1024;
	public static final int defaultHeight = 1024;


	/**
	 * Generates bytecode for given input.
	 * Throws exceptions for Lexical, Syntax, and Type checking errors
	 *
	 * @param input String containing source code
	 * @return Generated bytecode
	 * @throws Exception
	 */
	byte[] genCode(String input) throws Exception {

		//scan, parse, and type check
		Scanner scanner = new Scanner(input);
		show(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeChecker v = new TypeChecker();
		program.visit(v, null);
		show(program);  //It may be useful useful to show this here if code generation fails

		//generate code
		CodeGenerator cv = new CodeGenerator(devel, grade, null, defaultWidth, defaultHeight);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		show(program); //doing it here shows the values filled in during code gen
		//display the generated bytecode
		show(CodeGenUtils.bytecodeToString(bytecode));

		//write byte code to file
		if (doCreateFile) {
			String name = ((Program) program).progName;
//			String classFileName = "bin/" + name + ".class";
			String classFileName = name + ".class";
			OutputStream output = new FileOutputStream(classFileName);
			output.write(bytecode);
			output.close();
			System.out.println("wrote classfile to " + classFileName);
		}

		//return generated classfile as byte array
		return bytecode;
	}

	/**
	 * Run main method in given class
	 *
	 * @param className
	 * @param bytecode
	 * @param commandLineArgs String array containing command line arguments, empty array if none
	 * @throws +
	 * @throws Throwable
	 */
	void runCode(String className, byte[] bytecode, String[] commandLineArgs) throws Exception {
		RuntimeLog.initLog(); //initialize log used for grading.
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		@SuppressWarnings("rawtypes")
		Class[] argTypes = {commandLineArgs.getClass()};
		Method m = testClass.getMethod("main", argTypes);
		show("Output from " + m + ":");  //print name of method to be executed
		Object passedArgs[] = {commandLineArgs};  //create array containing params, in this case a single array.
		try {
			m.invoke(null, passedArgs);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				Exception ec = (Exception) e.getCause();
				throw ec;
			}
			throw e;
		}
	}


	/**
	 * When invoked from JUnit, Frames containing images will be shown and then immediately deleted.
	 * To prevent this behavior, waitForKey will pause until a key is pressed.
	 *
	 * @throws IOException
	 */
	void waitForKey() throws IOException {
		System.out.println("enter any char to exit");
		System.in.read();
	}

	/**
	 * When invoked from JUnit, Frames containing images will be shown and then immediately deleted.
	 * To prevent this behavior, keepFrame will keep the frame visible for 5000 milliseconds.
	 *
	 * @throws Exception
	 */
	void keepFrame() throws Exception {
		Thread.sleep(5000);
	}


	/**
	 * Since we are not doing any optimization, the compiler will
	 * still create a class with a main method and the JUnit test will
	 * execute it.
	 * <p>
	 * The only thing it will do is append the "entering main" and "leaving main" messages to the log.
	 *
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String prog = "emptyProg";
		String input = prog + "{}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n " + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());
	}


	@Test
	public void integerLit() throws Exception {
		String prog = "intgegerLit";
		String input = prog + "{show 3;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;3;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void floatingLit() throws Exception {
		String prog = "floatingLit";
		String input = prog + "{show 3.5;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;3.5;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void booleanLit() throws Exception {
		String prog = "booleanLit";
		String input = prog + "{show false;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;false;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testExpressionBinaryIntegerSum() throws Exception {
		String prog = "intSum";
		String input = prog + "{show 2+3;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;5;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testExpressionBinaryIntegerPower() throws Exception {
		String prog = "intSum";
		String input = prog + "{show 2**3;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;8;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testExpressionBinaryIntFloatPower() throws Exception {
		String prog = "intSum";
		String input = prog + "{show -2-3;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;-5;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testExpressionUnaryIntegerNegate() throws Exception {
		String prog = "intSum";
		String input = prog + "{show !10;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;-11;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testExpressionUnaryBooleanNegate() throws Exception {
		String prog = "intSum";
		String input = prog + "{show !true;} ";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;false;leaving main;", RuntimeLog.globalLog.toString());
	}

	@Test
	public void testFailedCases() throws Exception {
		String input;
		byte[] bytecode;
		String[] commandLineArgs = {};
		input = "prog{show 9.1 + 4.5;show 9.1 - 4.5;show 9.1 * 4.5;show 8.82 / 4.2;show 9.1 ** 4.1;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;13.6;4.6000004;40.95;2.1;8552.039;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{boolean y; y := true; show y; y := false; show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;true;false;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{int y; y := 55; show y; y := 234; show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;55;234;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{show log(1.0); show atan(0.5);}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;0.0;0.4636476;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{int a; a := int(-3.7); show a; a := int(4); show a;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;-3;4;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{float a; a := float(-3.7); show a; a := float(4); show a;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;-3.7;4.0;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{float y; y := 6.6; show y; y := -0.5; show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;6.6;-0.5;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{boolean y; y := true; show y; y := false; show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;true;false;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{int a; a := 123456789; show alpha(a);\n a := -1; show alpha(a);}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;7;255;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{int a; a := 123456789;\n show red(a); show green(a); show blue(a);}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;91;205;21;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{image y;\n show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{image b[512,256]; show width(b); show height(b);\nimage c; show width(c); show height(c);}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;512;256;1024;1024;leaving main;",RuntimeLog.globalLog.toString());

		input = "prog{image y[512,256];\n show y;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());

		String[] commandLineArgs1 = {"10", "2.0", "true", "false"};
		input = "prog{int x; input x from @ 0 ; show x;\n float y; input y from @ 1; show y;\nboolean z; input z from @ 2; show z;\ninput z from @ 3; show z;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs1);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;10;2.0;true;false;leaving main;", RuntimeLog.globalLog.toString());

		String[] commandLineArgs2 = {"rachit_ranjan_90514445_hw2.jar"};
		input = "prog{filename f1;\n filename f2; \n input f1 from @ 0 ;\n f2 := f1;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs2);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

		String[] commandLineArgs3 = {"1.jpg"};
		input = "prog{image y[256,256]; input y from @ 0 ; show y; sleep 3000;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs3);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

		String[] commandLineArgs4 = {"https://www.argospetinsurance.co.uk/assets/uploads/2017/12/cat-pet-animal-domestic-104827.jpeg"};
		input = "prog{image y[512,512]; input y from @ 0 ; show y; sleep 300;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs4);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

		String[] commandLineArgs5 = {"1.jpg","https://www.argospetinsurance.co.uk/assets/uploads/2017/12/cat-pet-animal-domestic-104827.jpeg"};
		input = "prog{image x[512,512]; input x from @ 1 ; show x; image y; y := x; show y; sleep 3000;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs5);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

		input = "prog{image y[1000,1000]; image copy[1000,1000]; input y from @ 0 ; show y; copy := y; show copy; sleep 3000;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs5);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

		input = "prog{image y[1000,1000]; image copy[1000,1000]; input y from @ 0 ; show y; copy := y; show copy; sleep 3000;}";
		bytecode = genCode(input);
		runCode("prog", bytecode, commandLineArgs5);
		show("Log:\n" + RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;", RuntimeLog.globalLog.toString());

	}


	@Test
	public void testFinalFailedCase() throws Exception {
		String input;
		byte[] bytecode;
		String[] commandLineArgs = {};
		input = "lhssample{ " +
					"image im[512,256]; " +
					"int x;" +
					"\nint y; " +
					"\nx := 0; " +
					"\ny := 0; " +
					"\nwhile (x < width(im)){ " +
						"\ny := 0; " +
				"		while (y < height(im)){" +
							"\nalpha(im[x,y]) := 255;" +
							"\n" +
							"red(im[x,y]) := 0;" +
							"\ngreen(im[x,y]) := x+y;" +
							"\nblue(im[x,y]) := 0; " +
							"\ny := y + 1; " +
						"\n};" +
						"\nx := x + 1;" +
					"};" +
					"\nshow im; sleep 3000;" +
				"\n}";
		bytecode = genCode(input);
		runCode("lhssample", bytecode, commandLineArgs);
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
}