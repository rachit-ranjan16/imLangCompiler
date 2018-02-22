package cop5556sp18;
/* *
 * Initial code for Parser for the class project in COP5556 Programming Language Principles
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;
import cop5556sp18.AST.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException{
		Program p = null;
		p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException{
		Block b = null;
		Program p = null;
		Token firstToken = null;
		if(isKind(IDENTIFIER)) {
			firstToken = match(IDENTIFIER);
			b = block();
			p = new Program(firstToken,firstToken,b);
		}
		else error();
		return p;

	}

	private void error() throws SyntaxException {
		throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
	}

	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha};
	Kind[] firstPixelSelector = {LSQUARE};
	Kind[] firstColor = {KW_red, KW_green, KW_blue, KW_alpha};
	Kind[] firstUnaryExpressionNotPlusOrMinus = {OP_EXCLAMATION,INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER,
		KW_sin,KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_width, KW_height, KW_int, KW_float,
		KW_red, KW_green, KW_blue, KW_alpha,
		KW_Z, KW_default_width, KW_default_height,
		LPIXEL};
	Kind[] firstPixelConstructor = {LPIXEL};
	Kind[] firstPredefinedName = {KW_Z, KW_default_width, KW_default_height};
	Kind[] firstFunctionApplication = {KW_sin,KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_width, KW_height, KW_int, KW_float,
			KW_red, KW_green, KW_blue, KW_alpha,};
	Kind[] firstPixelExpression = {IDENTIFIER};
	Kind[] firstPrimary = {INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER,
			KW_sin,KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_width, KW_height, KW_int, KW_float,
			KW_red, KW_green, KW_blue, KW_alpha,
			KW_Z, KW_default_width, KW_default_height,
			LPIXEL
	};

//	Block ::=  { (  (Declaration | Statement) ; )* }
	public Block block() throws SyntaxException{
		List<ASTNode> decsOrStatements = new ArrayList<>();
		Block b = null;
		match(LBRACE);
		Token firstToken = t;
		while (isKind(firstDec)|isKind(firstStatement)) {
			if (isKind(firstDec)) {
				decsOrStatements.add(declaration());
			}
			else if (isKind(firstStatement)) {
				decsOrStatements.add(statement());
			}
			match(SEMI);
		}
		match(RBRACE);
		b = new Block(firstToken, decsOrStatements);
		return b;
	}

//	Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	public Declaration declaration() throws SyntaxException{
			Expression e0 = null;
			Expression e1 = null;
			Token ty = type();
			Token firstToken = match(IDENTIFIER);
			if (t.getKind() == LSQUARE) {
				match(LSQUARE);
				e0 = expression();
				match(COMMA);
				e1 = expression();
				match(RSQUARE);
			}
			return new Declaration(firstToken, ty, firstToken, e0, e1);
	}

	/*
	Statement ::= StatementInput | StatementWrite | StatementAssignment
		| StatementWhile | StatementIf | StatementShow | StatementSleep
	 */
	public Statement statement() throws SyntaxException{
		Statement s = null;
		switch (t.getKind()) {
			case KW_input:
				s = statementInput();
				break;
			case KW_write:
				s = statementWrite();
				break;
			case KW_while:
				s = statementWhile();
				break;
			case KW_if:
				s = statementIf();
				break;
			case KW_show:
				s = statementShow();
				break;
			case KW_sleep:
				s = statementSleep();
				break;
			case IDENTIFIER:
			case KW_red:
			case KW_green:
			case KW_blue:
			case KW_alpha:
				s = statementAssignment();
				break;
		}
		return s;
	}
	// StatementAssignment ::=  LHS := Expression
	public Statement statementAssignment() throws SyntaxException{
		Token firstToken = t;
		LHS l = lhs();
		match(OP_ASSIGN);
		Expression e = expression();
		return new StatementAssign(firstToken, l, e);
	}

	// StatementSleep ::=  sleep Expression
	public Statement statementSleep() throws SyntaxException{
		Token firstToken = t;
		match(KW_sleep);
		Expression e = expression();
		return new StatementSleep(firstToken, e);
	}

	// StatementShow ::=  show Expression
	public Statement statementShow() throws SyntaxException{
		Token firstToken = t;
		match(KW_show);
		Expression e = expression();
		return new StatementShow(firstToken, e);
	}

	// StatementIf ::=  if ( Expression ) Block
	public Statement statementIf() throws SyntaxException{
		Token firstToken = t;
		match(KW_if);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new StatementIf(firstToken, e, b);
	}

	// StatementWhile ::=  while (Expression ) Block
	public Statement statementWhile() throws SyntaxException{
		Token firstToken = t;
		match(KW_while);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new StatementWhile(firstToken, e, b);
	}

	// StatementWrite ::= write IDENTIFIER to IDENTIFIER
	public Statement statementWrite() throws SyntaxException {
		Token firstToken = t;
		match(KW_write);
		Token source = match(IDENTIFIER);
		match(KW_to);
		Token dest = match(IDENTIFIER);
		return new StatementWrite(firstToken,source,dest);
	}

	// StatementInput ::= input IDENTIFIER from @ Expression
	public Statement statementInput() throws SyntaxException{
		Token firstToken = t;
		match(KW_input);
		Token name = match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression e = expression();
		return new StatementInput(firstToken, name, e);
	}


	//	LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector
	public LHS lhs() throws SyntaxException {
		Token firstToken = t;
		LHS l = null;
		if (t.getKind() == IDENTIFIER) {
			if(scanner.peek().getKind() == LSQUARE) {
				Token name = match(IDENTIFIER);
				PixelSelector ps = pixelSelector();
				l = new LHSPixel(firstToken, name, ps);
			}
			else {
				Token name = match(IDENTIFIER);
				l = new LHSIdent(firstToken, name);
			}
		} else if (isKind(firstColor)) {
			Token color = color();
			match(LPAREN);
			Token name = match(IDENTIFIER);
			PixelSelector ps =  pixelSelector();
			match(RPAREN);
			l = new LHSSample(firstToken, name, ps, color);
		}
		else error();
		return l;
	}


//	Color ::= red | green | blue | alpha
	public Token color() throws SyntaxException{
		Token name = null;
		switch (t.getKind()) {
			case KW_red: name =  match(KW_red);break;
			case KW_green: name =  match(KW_green);break;
			case KW_blue: name =  match(KW_blue);break;
			case KW_alpha: name =  match(KW_alpha);break;
		}
		return name;
	}


//	PixelSelector ::= [ Expression , Expression ]
	public PixelSelector pixelSelector() throws SyntaxException{
		Token firstToken = t;
		match(LSQUARE);
		Expression e0 = expression();
		match(COMMA);
		Expression e1 = expression();
		match(RSQUARE);
		return new PixelSelector(firstToken, e0, e1);
	}

	public Token type() throws SyntaxException{
		Token tmp = null;
		switch (t.getKind()) {
			case KW_int:
				 tmp =  match(KW_int);
				break;
			case KW_float:
				tmp =   match(KW_float);
				break;
			case KW_boolean:
				tmp =   match(KW_boolean);
				break;
			case KW_filename:
				tmp =   match(KW_filename);
				break;
			case KW_image:
				tmp =   match(KW_image);
				break;
		}
		return tmp;
	}

//	Expression ::=  OrExpression  ?  Expression  :  Expression
//	               |   OrExpression
	public Expression expression() throws SyntaxException{
			Token firstToken = t;
			Expression e0 = orExpression();
			Expression e1 = null;
			Expression e2 = null;
			if (t.getKind() == OP_QUESTION) {
				match(OP_QUESTION);
				e1 = expression();
				match(OP_COLON);
				e2 = expression();
			}
			return new ExpressionConditional(firstToken,e0,e1,e2);
		}

//	OrExpression  ::=  AndExpression   (  |  AndExpression ) *
//	ExpressionBinary ::= Expression op Expression
	public Expression orExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = andExpression();
		while(t.getKind() == OP_OR) {
			Token op = match(OP_OR);
			Expression e1 = andExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

//	AndExpression ::=  EqExpression ( & EqExpression )*
	//ExpressionBinary ::= Expression op Expression
	public Expression andExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = eqExpression();
		while(t.getKind() == OP_AND) {
			Token op = match(OP_AND);
			Expression e1 = eqExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

// EqExpression ::=  RelExpression  (  (== | != )  RelExpression )*
	//ExpressionBinary ::= Expression op Expression
	public Expression eqExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = relExpression();
		while(t.getKind() == OP_EQ | t.getKind() == OP_NEQ) {
			Token op = null;
			switch (t.getKind()) {
				case OP_EQ:
					op = match(OP_EQ);
					break;
				case OP_NEQ:
					op = match(OP_NEQ);
					break;
			}
			Expression e1 = relExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

//	RelExpression ::= AddExpression (  (<  | > |  <=  | >= )   AddExpression)*
//ExpressionBinary ::= Expression op Expression
	public Expression relExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = addExpression();
		while(t.getKind() == OP_LT |
				t.getKind() == OP_LE |
				t.getKind() == OP_GT |
				t.getKind() == OP_GE) {
			Token op = null;
			switch(t.getKind()) {
				case OP_LT:
					op = match(OP_LT);
					break;
				case OP_LE:
					op = match(OP_LE);
					break;
				case OP_GT:
					op = match(OP_GT);
					break;
				case OP_GE:
					op = match(OP_GE);
					break;
			}
			Expression e1 = addExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

//	AddExpression ::= MultExpression   (  ( + | - ) MultExpression )*
//ExpressionBinary ::= Expression op Expression
	public Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = multExpression();
		while (t.getKind() == OP_PLUS | t.getKind() == OP_MINUS) {
			Token op = null;
			switch (t.getKind()) {
				case OP_PLUS:
					op = match(OP_PLUS);
					break;
				case OP_MINUS:
					op = match(OP_MINUS);
					break;
			}
			Expression e1 = multExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

//	MultExpression := PowerExpression ( ( * | /  | % ) PowerExpression )*
//ExpressionBinary ::= Expression op Expression
	public Expression multExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = powerExpression();
		while(t.getKind() == OP_TIMES | t.getKind() == OP_DIV | t.getKind() == OP_MOD) {
			Token op = null;
			switch (t.getKind()) {
				case OP_TIMES:
					op = match(OP_TIMES);
					break;
				case OP_DIV:
					op = match(OP_DIV);
					break;
				case OP_MOD:
					op = match(OP_MOD);
					break;
			}
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

//	PowerExpression := UnaryExpression  (** PowerExpression | ε)
//	ExpressionBinary ::= Expression op Expression
	public Expression powerExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = unaryExpression();
		if(t.getKind() == OP_POWER) {
			Token op = match(OP_POWER);
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
//	UnaryExpression ::= + UnaryExpression | - UnaryExpression | UnaryExpressionNotPlusMinus
//	ExpressionUnary ::= Op Expression
	public Expression unaryExpression() throws SyntaxException{
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		if(t.getKind() == OP_PLUS | t.getKind() == OP_MINUS) {
			switch (t.getKind()) {
				case OP_PLUS:
					op = match(OP_PLUS);
					break;
				case OP_MINUS:
					op = match(OP_MINUS);
					break;
			}
			Expression e1 = unaryExpression();
			e0 = new ExpressionUnary(firstToken,op,e1);
		}
		else if (isKind(firstUnaryExpressionNotPlusOrMinus))
			e0 = unaryExpressionNotPlusOrMinus();
		else error();
		return e0;
	}

//	UnaryExpressionNotPlusMinus ::=  ! UnaryExpression  | Primary
//	ExpressionUnary ::= Op Expression
	public Expression unaryExpressionNotPlusOrMinus() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		switch (t.getKind()) {
			case OP_EXCLAMATION:
				op = match(OP_EXCLAMATION);
				Expression e1 = unaryExpression();
				e0 = new ExpressionUnary(firstToken,op,e1);
				break;
			default:
				if (isKind(firstPrimary))
					e0 = primary();
		}
		return e0;
	}

//	PixelExpression ::= IDENTIFIER PixelSelector
	public Expression pixelExpression() throws SyntaxException{
		Token firstToken = t;
		Token name = match(IDENTIFIER);
		return new ExpressionPixel(firstToken, name, pixelSelector());
	}

//	FunctionApplication ::= FunctionName ( Expression )  | FunctionName  [ Expression , Expression ]
//	ExpressionFunctionAppWithPixel ::= FunctionName Expression Expression
	public Expression functionApplication() throws SyntaxException{
		Token firstToken = t;
		Token functionName = functionName();
		Expression e0 = null;
		Expression e1 = null;
		switch(t.getKind()) {
			case LPAREN:
				match(LPAREN);
				e0 = expression();
				match(RPAREN);
				break;
			case LSQUARE:
				match(LSQUARE);
				e0 = expression();
				match(COMMA);
				e1 = expression();
				match(RSQUARE);
				break;
		}
		return new ExpressionFunctionAppWithPixel(firstToken, functionName, e0, e1);
	}

//	PredefinedName ::= Z | default_height | default_width
	// ExpressionPredefinedName
	public Expression predefinedName() throws SyntaxException{
		Token firstToken = t;
		Expression e = null;
		switch (t.getKind()) {
			case KW_Z:
				e = new ExpressionPredefinedName(firstToken, match(KW_Z));
				break;
			case KW_default_height:
				e = new ExpressionPredefinedName(firstToken, match(KW_default_height));
				break;
			case KW_default_width:
				e = new ExpressionPredefinedName(firstToken, match(KW_default_width));
				break;
		}
		return e;
	}

//	PixelConstructor ::=  <<  Expression , Expression , Expression , Expression  >>
	public Expression pixelConstructor() throws SyntaxException {
		Token firstToken = t;
		match(LPIXEL);
		Expression e0 = expression();
		match(COMMA);
		Expression e1 = expression();
		match(COMMA);
		Expression e2 = expression();
		match(COMMA);
		Expression e3 = expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(firstToken, e0, e1, e2, e3);
	}

//	Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL |
//			( Expression ) | FunctionApplication  | IDENTIFIER | PixelExpression |
//	PredefinedName | PixelConstructor
	public Expression primary() throws SyntaxException{
		Token firstToken = t;
		Token lit;
		Expression e0 = null;
		switch (t.getKind()) {
			case INTEGER_LITERAL:
				lit = match(INTEGER_LITERAL);
				e0 = new ExpressionIntegerLiteral(firstToken, lit);
				break;
			case BOOLEAN_LITERAL:
				lit = match(BOOLEAN_LITERAL);
				e0 = new ExpressionBooleanLiteral(firstToken, lit);
				break;
			case FLOAT_LITERAL:
				lit = match(FLOAT_LITERAL);
				e0 = new ExpressionFloatLiteral(firstToken, lit);
				break;
			case IDENTIFIER:
				if(scanner.peek().getKind() == LSQUARE) {
					e0 = pixelExpression();
				}
				else
					e0 = new ExpressionIdent(firstToken, match(IDENTIFIER));
				break;
			case LPAREN:
				match(LPAREN);
				e0 = expression();
				match(RPAREN);
				break;
			default:
				if (isKind(firstPixelExpression))
					e0 = pixelExpression();
				else if (isKind(firstFunctionApplication))
					e0 = functionApplication();
				else if (isKind(firstPredefinedName))
					e0 = predefinedName();
				else if (isKind(firstPixelConstructor))
					e0 = pixelConstructor();
		}
		return e0;
	}

//	FunctionName ::= sin | cos | atan | abs | log | cart_x | cart_y | polar_a | polar_r
//	int | float | width | height | Color
	public Token functionName() throws SyntaxException{
		Token name = null;
		switch (t.getKind()) {
			case KW_sin:
				name = match(KW_sin);
				break;
			case KW_cos:
				 name = match(KW_cos);
				break;
			case KW_atan:
				name = match(KW_atan);
				break;
			case KW_abs:
				name = match(KW_abs);
				break;
			case KW_log:
				name = match(KW_log);
				break;
			case KW_cart_x:
				name = match(KW_cart_x);
				break;
			case KW_cart_y:
				name = match(KW_cart_y);
				break;
			case KW_polar_a:
				name = match(KW_polar_a);
				break;
			case KW_polar_r:
				name = match(KW_polar_r);
				break;
			case KW_int:
				name =  match(KW_int);
				break;
			case KW_float:
				name =  match(KW_float);
				break;
			case KW_width:
				name =  match(KW_width);
				break;
			case KW_height:
				name =  match(KW_height);
				break;
			default:
				name = color();
		}
		return name;
	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			error();
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
	}
	

}

