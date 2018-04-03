/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;


public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	static int slot = 0;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		declaration.setSlot(slot++);
		String name = declaration.name;
		Label startLabel = new Label();
		Label endLabel = new Label();
		mv.visitLabel(startLabel);
		Type type = Types.getType(declaration.type);
		if (type == Type.IMAGE) {
			if (declaration.width != null && declaration.height!=null) {
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
			}
			else {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
					"makeImage", "(II)Ljava/awt/image/BufferedImage", false);
		}
		mv.visitLocalVariable("name","Ljava/awt/image/BufferedImage", null,startLabel,endLabel, slot++);
		mv.visitLabel(startLabel);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		if(expressionBinary.leftExpression.getType() == Type.INTEGER &&
				expressionBinary.rightExpression.getType() == Type.INTEGER)
			switch (expressionBinary.op) {
				case OP_PLUS:
					mv.visitInsn(Opcodes.IADD);
					break;
				case OP_MINUS:
					mv.visitInsn(Opcodes.ISUB);
					break;
				case OP_TIMES:
					mv.visitInsn(Opcodes.IMUL);
					break;
				case OP_DIV:
					mv.visitInsn(Opcodes.IDIV);
					break;
				case OP_MOD:
					mv.visitInsn(Opcodes.IREM);
					break;
				case OP_POWER:
					mv.visitInsn(Opcodes.POP2);
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(Opcodes.I2D);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(Opcodes.I2D);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(Opcodes.D2I);
					break;
				case OP_AND:
					mv.visitInsn(Opcodes.IAND);
					break;
				case OP_OR:
					mv.visitInsn(Opcodes.IOR);
			}
		else if(expressionBinary.leftExpression.getType() == Type.FLOAT &&
				expressionBinary.rightExpression.getType() == Type.FLOAT)
			switch (expressionBinary.op) {
				case OP_PLUS:
					mv.visitInsn(Opcodes.FADD);
					break;
				case OP_MINUS:
					mv.visitInsn(Opcodes.FSUB);
					break;
				case OP_TIMES:
					mv.visitInsn(Opcodes.FMUL);
					break;
				case OP_DIV:
					mv.visitInsn(Opcodes.FDIV);
					break;
				case OP_POWER:
					mv.visitInsn(Opcodes.F2D);
					mv.visitInsn(Opcodes.SWAP);
					mv.visitInsn(Opcodes.F2D);
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math","pow", "(DD)D", false);
					mv.visitInsn(Opcodes.D2F);
					break;
			}
		else if((expressionBinary.leftExpression.getType() == Type.INTEGER &&
				expressionBinary.rightExpression.getType() == Type.FLOAT) ||
				(expressionBinary.leftExpression.getType() == Type.FLOAT &&
				expressionBinary.rightExpression.getType() == Type.INTEGER)) {

			mv.visitInsn(Opcodes.POP2);
			expressionBinary.leftExpression.visit(this, arg);
			if (expressionBinary.leftExpression.getType() == Type.INTEGER)
				mv.visitInsn(Opcodes.I2F);
			expressionBinary.rightExpression.visit(this, arg);
			if (expressionBinary.rightExpression.getType() == Type.INTEGER)
				mv.visitInsn(Opcodes.I2F);
			switch (expressionBinary.op) {
				case OP_PLUS:
					mv.visitInsn(Opcodes.FADD);
					break;
				case OP_MINUS:
					mv.visitInsn(Opcodes.FSUB);
					break;
				case OP_TIMES:
					mv.visitInsn(Opcodes.FMUL);
					break;
				case OP_DIV:
					mv.visitInsn(Opcodes.FDIV);
					break;
				case OP_POWER:
					mv.visitInsn(Opcodes.POP2);
					expressionBinary.leftExpression.visit(this, arg);
					switch(expressionBinary.leftExpression.getType()) {
						case INTEGER:
							mv.visitInsn(Opcodes.I2D);
							break;
						case FLOAT:
							mv.visitInsn(Opcodes.F2D);
					}
					expressionBinary.rightExpression.visit(this, arg);
					switch (expressionBinary.rightExpression.getType()) {
						case INTEGER:
							mv.visitInsn(Opcodes.I2D);
							break;
						case FLOAT:
							mv.visitInsn(Opcodes.F2D);
					}
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math","pow", "(DD)D", false);
					mv.visitInsn(Opcodes.D2F);
					break;
			}
		}
		else if(expressionBinary.leftExpression.getType() == Type.BOOLEAN &&
				expressionBinary.rightExpression.getType() == Type.BOOLEAN)
			switch (expressionBinary.op) {
				case OP_AND:
					mv.visitInsn(Opcodes.IAND);
					break;
				case OP_OR:
					mv.visitInsn(Opcodes.IOR);
					break;
			}
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}


	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		switch (expressionFunctionAppWithExpressionArg.function) {
			case KW_int:
				if (expressionFunctionAppWithExpressionArg.e.getType() == Type.FLOAT)
					mv.visitInsn(Opcodes.F2I);
				break;
			case KW_float:
				if (expressionFunctionAppWithExpressionArg.e.getType() == Type.INTEGER)
					mv.visitInsn(Opcodes.I2F);
				break;
			case KW_sin:
				mv.visitInsn(Opcodes.F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(Opcodes.D2F);
				break;
			case KW_cos:
				mv.visitInsn(Opcodes.F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(Opcodes.D2F);
				break;
			case KW_atan:
				mv.visitInsn(Opcodes.F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(Opcodes.D2F);
				break;
			case KW_log:
				mv.visitInsn(Opcodes.F2D);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(Opcodes.D2F);
				break;
			case KW_abs:
				switch(expressionFunctionAppWithExpressionArg.e.getType()) {
					case INTEGER:
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
						break;
					case FLOAT:
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
						break;

				}
				break;
		}
		Math.abs(-1);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {

		switch (expressionIdent.getType()) {
			case INTEGER:
				mv.visitVarInsn(Opcodes.ILOAD, expressionIdent.dec.getSlot());
				break;
			case FLOAT:
				mv.visitVarInsn(Opcodes.FLOAD, expressionIdent.dec.getSlot());
				break;
			case BOOLEAN:
				mv.visitVarInsn(Opcodes.ILOAD, expressionIdent.dec.getSlot());
				break;
			case IMAGE:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
						"deepCopy", "(Ljava/awt/image/BufferedImage;)LBufferedImage", false);
				mv.visitVarInsn(Opcodes.ALOAD, expressionIdent.dec.getSlot());
				break;
			case FILE:
				mv.visitVarInsn(Opcodes.ALOAD, expressionIdent.dec.getSlot());
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		switch (expressionPredefinedName.name) {
			case KW_Z:
				mv.visitLdcInsn(255);
				break;
			case KW_default_height:
				mv.visitLdcInsn(defaultHeight);
				break;
			case KW_default_width:
				mv.visitLdcInsn(defaultWidth);
				break;

		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);

		switch (expressionUnary.expression.getType()) {
			case BOOLEAN:
				mv.visitLdcInsn(true);
				mv.visitInsn(IXOR);
				break;
			case INTEGER:
				switch (expressionUnary.op) {
					case OP_PLUS:
						break;
					case OP_MINUS:
						mv.visitInsn(Opcodes.INEG);
						break;
					case OP_EXCLAMATION:
						mv.visitInsn(Opcodes.INEG);
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitInsn(Opcodes.ISUB);
				}
				break;
			case FLOAT:
				switch (expressionUnary.op) {
					case OP_PLUS:
						break;
					case OP_MINUS:
						mv.visitInsn(Opcodes.FNEG);
				}
		}
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		switch (lhsIdent.type) {
			case INTEGER:
				mv.visitVarInsn(Opcodes.ILOAD, lhsIdent.dec.getSlot());
				break;
			case FLOAT:
				mv.visitVarInsn(Opcodes.FLOAD, lhsIdent.dec.getSlot());
				break;
			case BOOLEAN:
				mv.visitVarInsn(Opcodes.ILOAD, lhsIdent.dec.getSlot());
				break;
			case IMAGE:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
						"deepCopy", "(Ljava/awt/image/BufferedImage;)LBufferedImage", false);
				mv.visitVarInsn(Opcodes.ALOAD, lhsIdent.dec.getSlot());
				break;
			case FILE:
				mv.visitVarInsn(Opcodes.ALOAD, lhsIdent.dec.getSlot());
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		switch(statementInput.dec.type) {
			case KW_int:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				mv.visitVarInsn(Opcodes.ILOAD, statementInput.dec.getSlot());
				break;
			case KW_float:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
				mv.visitVarInsn(Opcodes.FLOAD, statementInput.dec.getSlot());
				break;
			case KW_image:
				if(statementInput.dec.width == null && statementInput.dec.height == null) {
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
							"makeImage", "(Ljava/lang/String;)Ljava/awt/image/BufferedImage", false);
				}
				else {
					statementInput.dec.width.visit(this, arg);
					statementInput.dec.height.visit(this, arg);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
							"makeImage", "(Ljava/lang/String;II)Ljava/awt/image/BufferedImage", false);
				}
				break;
			case KW_boolean:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitVarInsn(Opcodes.ILOAD, statementInput.dec.getSlot());
				break;
			case KW_filename:
				mv.visitVarInsn(Opcodes.ALOAD, statementInput.dec.getSlot());
				break;
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER :
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);

				break;
			case BOOLEAN :
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);

			 	break;
			case FLOAT :
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);

				 break;
			case IMAGE :
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className,
						"makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame", false);
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(Opcodes.I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread","sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
