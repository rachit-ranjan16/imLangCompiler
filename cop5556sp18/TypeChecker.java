package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Types.Type;

public class TypeChecker implements ASTVisitor {

	private SymbolTable symbolTable;
	TypeChecker() {
		symbolTable = new SymbolTable();
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for(ASTNode n: block.decsOrStatements) {
			if(n instanceof Declaration || n instanceof Statement)
				n.visit(this, arg);
			else
				error(block.firstToken);
		}
		symbolTable.leaveScope();
	return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		if(!symbolTable.insert(declaration.name, declaration))
			error(declaration.firstToken);
		if(declaration.height != null || declaration.width != null) {
			if (Types.getType(declaration.type) != Type.IMAGE) error(declaration.firstToken);
			if (declaration.width != null) {
				declaration.width.visit(this, arg);
				if (declaration.width.type != Type.INTEGER) error(declaration.firstToken);
			} else if (declaration.height != null) {
				declaration.height.visit(this, arg);
				if (declaration.height.type != Type.INTEGER) error(declaration.firstToken);
			}
		}
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		statementWrite.sourceDec = symbolTable.lookup(statementWrite.sourceName);
		if(statementWrite.sourceDec == null) error(statementWrite.firstToken);
		statementWrite.destDec = symbolTable.lookup(statementWrite.destName);
		if(statementWrite.destDec == null) error(statementWrite.firstToken);
		if(Types.getType(statementWrite.sourceDec.type) != Type.IMAGE) error(statementWrite.firstToken);
		if(Types.getType(statementWrite.destDec.type) != Type.FILE) error(statementWrite.firstToken);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.dec = symbolTable.lookup(statementInput.destName);
		if(statementInput.dec == null) error(statementInput.firstToken);
		statementInput.e.visit(this,arg);
		if(statementInput.e.type != Type.INTEGER) error(statementInput.firstToken);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		pixelSelector.ex	.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		if(pixelSelector.ex.type != pixelSelector.ey.type) error(pixelSelector.firstToken);
		if(pixelSelector.ex.type != Type.FLOAT && pixelSelector.ex.type != Type.INTEGER) error(pixelSelector.firstToken);
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.guard.visit(this, arg);
		if(expressionConditional.guard.type != Type.BOOLEAN) error(expressionConditional.firstToken);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if(expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) error(expressionConditional.firstToken);
		expressionConditional.type = expressionConditional.trueExpression.type;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		expressionBinary.type = getBinaryExpInferredType(expressionBinary.leftExpression,expressionBinary.op,expressionBinary.rightExpression);
		return null;
	}

	public Type getBinaryExpInferredType(Expression l, Kind op, Expression r) {
		if (l.type == Type.INTEGER && r.type == Type.INTEGER)
			switch (op) {
				case OP_PLUS:
				case OP_MINUS:
				case OP_DIV:
				case OP_TIMES:
				case OP_MOD:
				case OP_POWER:
				case OP_AND:
				case OP_OR:
					return Type.INTEGER;
				case OP_EQ:
				case OP_NEQ:
				case OP_GT:
				case OP_GE:
				case OP_LT:
				case OP_LE:
					return Type.BOOLEAN;
				default:
					return Type.NONE;
			}
		else if(l.type == Type.FLOAT && r.type == Type.FLOAT)
			switch(op) {
				case OP_PLUS:
				case OP_MINUS:
				case OP_TIMES:
				case OP_DIV:
				case OP_POWER:
					return Type.FLOAT;
				case OP_EQ:
				case OP_NEQ:
				case OP_GT:
				case OP_GE:
				case OP_LT:
				case OP_LE:
					return Type.BOOLEAN;
				default:
					return Type.NONE;
			}
		else if((l.type == Type.FLOAT && r.type == Type.INTEGER) ||
				(l.type == Type.INTEGER && r.type == Type.FLOAT))
			switch(op) {
				case OP_PLUS:
				case OP_MINUS:
				case OP_TIMES:
				case OP_DIV:
				case OP_POWER:
					return Type.FLOAT;
				default:
					return Type.NONE;
			}
		else if((l.type == Type.BOOLEAN && r.type == Type.INTEGER) ||
				(l.type == Type.INTEGER && r.type == Type.BOOLEAN))
			switch(op) {
				case OP_AND:
				case OP_OR:
					return Type.BOOLEAN;
				default:
					return Type.NONE;
			}
		else if(l.type == Type.BOOLEAN && r.type == Type.BOOLEAN)
			switch(op) {
				case OP_EQ:
				case OP_NEQ:
				case OP_GT:
				case OP_GE:
				case OP_LT:
				case OP_LE:
					return Type.BOOLEAN;
				default:
					return Type.NONE;
			}
		return Type.NONE;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.type = Type.INTEGER;
		return null;

	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = Type.FLOAT;
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		expressionFunctionAppWithExpressionArg.type = getInferredTypeFunctionApp(expressionFunctionAppWithExpressionArg.function,
				expressionFunctionAppWithExpressionArg.e.type);
		return null;
	}

	private Type getInferredTypeFunctionApp(Kind functionName, Type expType) {
		if(expType == Type.INTEGER)
			switch (functionName) {
				case KW_abs:
				case KW_green:
				case KW_red:
				case KW_blue:
				case KW_int:
				case KW_alpha:
					return Type.INTEGER;
				case KW_float:
					return Type.FLOAT;
				default:
					return Type.NONE;
			}
		else if(expType == Type.FLOAT)
			switch (functionName) {
				case KW_abs:
				case KW_sin:
				case KW_cos:
				case KW_atan:
				case KW_float:
				case KW_log:
					return Type.FLOAT;
				case KW_int:
					return Type.INTEGER;
				default:
					return Type.NONE;
			}
		else if(expType == Type.IMAGE)
			switch (functionName) {
				case KW_width:
				case KW_height:
					return Type.INTEGER;
				default:
					return Type.NONE;
			}
		return Type.NONE;
	}
	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		switch(expressionFunctionAppWithPixel.name) {
			case KW_cart_x:
			case KW_cart_y:
				if (expressionFunctionAppWithPixel.e0.type != Type.FLOAT ||
						expressionFunctionAppWithPixel.e1.type != Type.FLOAT)
					error(expressionFunctionAppWithPixel.firstToken);
				expressionFunctionAppWithPixel.type = Type.INTEGER;
				break;
			case KW_polar_a:
			case KW_polar_r:
				if (expressionFunctionAppWithPixel.e0.type != Type.INTEGER ||
						expressionFunctionAppWithPixel.e1.type != Type.INTEGER)
					error(expressionFunctionAppWithPixel.firstToken);
				expressionFunctionAppWithPixel.type = Type.FLOAT;
				break;
			default:
				error(expressionFunctionAppWithPixel.firstToken);
		}
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this, arg);
		if(expressionPixelConstructor.alpha.type != Type.INTEGER) error(expressionPixelConstructor.firstToken);
		expressionPixelConstructor.red.visit(this, arg);
		if(expressionPixelConstructor.red.type != Type.INTEGER) error(expressionPixelConstructor.firstToken);
		expressionPixelConstructor.green.visit(this, arg);
		if(expressionPixelConstructor.green.type != Type.INTEGER) error(expressionPixelConstructor.firstToken);
		expressionPixelConstructor.blue.visit(this, arg);
		if(expressionPixelConstructor.red.type != Type.INTEGER) error(expressionPixelConstructor.firstToken);
		expressionPixelConstructor.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);
		if(statementAssign.lhs.type != statementAssign.e.type) error(statementAssign.firstToken);
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this,arg);
		switch (statementShow.e.type) {
			case INTEGER:
			case BOOLEAN:
			case FLOAT:
			case IMAGE: break;
			default: error(statementShow.firstToken);
		}
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);
		if(expressionPixel.dec == null) error(expressionPixel.firstToken);
		if(Types.getType(expressionPixel.dec.type) != Type.IMAGE) error(expressionPixel.firstToken);
		expressionPixel.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if(expressionIdent.dec == null) error(expressionIdent.firstToken);
		expressionIdent.type = Types.getType(expressionIdent.dec.type);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.dec = symbolTable.lookup(lhsSample.name);
		if(lhsSample.dec == null) error(lhsSample.firstToken);
		if(Types.getType(lhsSample.dec.type) != Type.IMAGE) error(lhsSample.firstToken);
		lhsSample.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		lhsPixel.pixelSelector.visit(this, arg);
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		if(lhsPixel.dec == null) error(lhsPixel.firstToken);
		if(Types.getType(lhsPixel.dec.type) != Type.IMAGE) error(lhsPixel.firstToken);
		lhsPixel.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		lhsIdent.dec = symbolTable.lookup(lhsIdent.name);
		if(lhsIdent.dec == null) error(lhsIdent.firstToken);
		lhsIdent.type = Types.getType(lhsIdent.dec.type);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		statementIf.guard.visit(this, arg);
		statementIf.b.visit(this, arg);
		if(statementIf.guard.type != Type.BOOLEAN) error(statementIf.firstToken);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		statementWhile.guard.visit(this, arg);
		statementWhile.b.visit(this, arg);
		if(statementWhile.guard.type != Type.BOOLEAN) error(statementWhile.firstToken);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		statementSleep.duration.visit(this, arg);
		if(statementSleep.duration.type != Type.INTEGER) error(statementSleep.firstToken);
		return null;
	}


	private void error(Token t) throws SemanticException {
		throw new SemanticException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
	}
}
