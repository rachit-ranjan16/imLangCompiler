package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


public class SimpleParser {
	
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

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException{
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException{
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
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
	public void block() throws SyntaxException{
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
			if (isKind(firstDec)) {
				declaration();
			}
			else if (isKind(firstStatement)) {
				statement();
			}
			match(SEMI);
		}
		match(RBRACE);

	}

//	Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	public void declaration() throws SyntaxException{
			type();
			match(IDENTIFIER);
			if (t.getKind() == LSQUARE) {
				match(LSQUARE);
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
			}
	}

	/*
	Statement ::= StatementInput | StatementWrite | StatementAssignment
		| StatementWhile | StatementIf | StatementShow | StatementSleep
	 */
	public void statement() throws SyntaxException{
		switch (t.getKind()) {

			case KW_input:
				statementInput();
				break;
			case KW_write:
				statementWrite();
				break;
			case KW_while:
				statementWhile();
				break;
			case KW_if:
				statementIf();
				break;
			case KW_show:
				statementShow();
				break;
			case KW_sleep:
				statementSleep();
				break;
			case IDENTIFIER:
			case KW_red:
			case KW_green:
			case KW_blue:
			case KW_alpha:
				statementAssignment();
				break;
		}
	}
	// StatementAssignment ::=  LHS := Expression
	private void statementAssignment() throws SyntaxException{

		lhs();
		match(OP_ASSIGN);
		expression();
	}

	// StatementSleep ::=  sleep Expression
	private void statementSleep() throws SyntaxException{
		match(KW_sleep);
		expression();
	}

	// StatementShow ::=  show Expression
	private void statementShow() throws SyntaxException{
		match(KW_show);
		expression();
	}

	// StatementIf ::=  if ( Expression ) Block
	private void statementIf() throws SyntaxException{
		match(KW_if);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	// StatementWhile ::=  while (Expression ) Block
	private void statementWhile() throws SyntaxException{
		match(KW_while);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	// StatementWrite ::= write IDENTIFIER to IDENTIFIER
	private void statementWrite() throws SyntaxException {
		match(KW_write);
		match(IDENTIFIER);
		match(KW_to);
		match(IDENTIFIER);
	}

	// StatementInput ::= input IDENTIFIER from @ Expression
	private void statementInput() throws SyntaxException{
		match(KW_input);
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		expression();
	}


	//	LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector
	public void lhs() throws SyntaxException{
			if (t.getKind() == IDENTIFIER) {
				match(IDENTIFIER);
				if (isKind(firstPixelSelector))
					pixelSelector();
			} else if (isKind(firstColor)) {
				color();
				match(LPAREN);
				match(IDENTIFIER);
				pixelSelector();
				match(RPAREN);

			}
			else throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
		}


//	Color ::= red | green | blue | alpha
	public void color() throws SyntaxException{
		switch (t.getKind()) {
			case KW_red: match(KW_red);break;
			case KW_green: match(KW_green);break;
			case KW_blue: match(KW_blue);break;
			case KW_alpha: match(KW_alpha);break;
		}
	}


//	PixelSelector ::= [ Expression , Expression ]
	private void pixelSelector() throws SyntaxException{
		match(LSQUARE);
		expression();
		match(COMMA);
		expression();
		match(RSQUARE);
	}

	public void type() throws SyntaxException{
		switch (t.getKind()) {
			case KW_int:
				match(KW_int);
				break;
			case KW_float:
				match(KW_float);
				break;
			case KW_boolean:
				match(KW_boolean);
				break;
			case KW_filename:
				match(KW_filename);
				break;
			case KW_image:
				match(KW_image);
				break;
		}
	}

//	Expression ::=  OrExpression  ?  Expression  :  Expression
//	               |   OrExpression
	public void expression() throws SyntaxException{
			orExpression();
			if (t.getKind() == OP_QUESTION) {
				match(OP_QUESTION);
				expression();
				match(OP_COLON);
				expression();
			}
		}

//	OrExpression  ::=  AndExpression   (  |  AndExpression ) *
	public void orExpression() throws SyntaxException{
		andExpression();
		while(t.getKind() == OP_OR) {
			match(OP_OR);
			andExpression();
		}
	}

//	AndExpression ::=  EqExpression ( & EqExpression )*
	public void andExpression() throws SyntaxException{
		eqExpression();
		while(t.getKind() == OP_AND) {
			match(OP_AND);
			eqExpression();
		}
	}

// EqExpression ::=  RelExpression  (  (== | != )  RelExpression )*
	public void eqExpression() throws SyntaxException{
		relExpression();
		while(t.getKind() == OP_EQ | t.getKind() == OP_NEQ) {
			switch (t.getKind()) {
				case OP_EQ:
					match(OP_EQ);
					break;
				case OP_NEQ:
					match(OP_NEQ);
					break;
			}
			relExpression();
		}
	}

//	RelExpression ::= AddExpression (  (<  | > |  <=  | >= )   AddExpression)*
	public void relExpression() throws SyntaxException{
		addExpression();
		while(t.getKind() == OP_LT |
				t.getKind() == OP_LE |
				t.getKind() == OP_GT |
				t.getKind() == OP_GE) {
			switch(t.getKind()) {
				case OP_LT:
					match(OP_LT);
					break;
				case OP_LE:
					match(OP_LE);
					break;
				case OP_GT:
					match(OP_GT);
					break;
				case OP_GE:
					match(OP_GE);
					break;
			}
			addExpression();
		}
	}

//	AddExpression ::= MultExpression   (  ( + | - ) MultExpression )*
	public void addExpression() throws SyntaxException{
		multExpression();
		while(t.getKind() == OP_PLUS | t.getKind() == OP_MINUS) {
			switch (t.getKind()) {
				case OP_PLUS:
					match(OP_PLUS);
					break;
				case OP_MINUS:
					match(OP_MINUS);
					break;
			}
			multExpression();
		}

	}

//	MultExpression := PowerExpression ( ( * | /  | % ) PowerExpression )*
	public void multExpression() throws SyntaxException{
		powerExpression();
		while(t.getKind() == OP_TIMES | t.getKind() == OP_DIV | t.getKind() == OP_MOD) {
			switch (t.getKind()) {
				case OP_TIMES:
					match(OP_TIMES);
					break;
				case OP_DIV:
					match(OP_DIV);
					break;
				case OP_MOD:
					match(OP_MOD);
					break;
			}
			powerExpression();
		}
	}

//	PowerExpression := UnaryExpression  (** PowerExpression | ε)
	public void powerExpression() throws SyntaxException{
		unaryExpression();
		if(t.getKind() == OP_POWER) {
			match(OP_POWER);
			powerExpression();
		}
	}
//	UnaryExpression ::= + UnaryExpression | - UnaryExpression | UnaryExpressionNotPlusMinus
	public void unaryExpression() throws SyntaxException{
		if(t.getKind() == OP_PLUS | t.getKind() == OP_MINUS) {
			switch (t.getKind()) {
				case OP_PLUS:
					match(OP_PLUS);
					break;
				case OP_MINUS:
					match(OP_MINUS);
					break;
			}
			unaryExpression();
		}
		else if (isKind(firstUnaryExpressionNotPlusOrMinus)) {
			unaryExpressionNotPlusOrMinus();
		}
		else throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
	}

//	UnaryExpressionNotPlusMinus ::=  ! UnaryExpression  | Primary
	public void unaryExpressionNotPlusOrMinus() throws SyntaxException{
		switch (t.getKind()) {
			case OP_EXCLAMATION:
				match(OP_EXCLAMATION);
				unaryExpression();
				break;
			default:
				if (isKind(firstPrimary))
					primary();
		}
	}

//	PixelExpression ::= IDENTIFIER PixelSelector
	public void pixelExpression() throws SyntaxException{
		match(IDENTIFIER);
		pixelSelector();
	}

//	FunctionApplication ::= FunctionName ( Expression )  | FunctionName  [ Expression , Expression ]
	public void functionApplication() throws SyntaxException{
		functionName();
		switch(t.getKind()) {
			case LPAREN:
				match(LPAREN);
				expression();
				match(RPAREN);
				break;
			case LSQUARE:
				match(LSQUARE);
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
				break;
		}
	}

//	PredefinedName ::= Z | default_height | default_width
	public void predefinedName() throws SyntaxException{
		switch (t.getKind()) {
			case KW_Z:
				match(KW_Z);
				break;
			case KW_default_height:
				match(KW_default_height);
				break;
			case KW_default_width:
				match(KW_default_width);
				break;
		}
	}

//	PixelConstructor ::=  <<  Expression , Expression , Expression , Expression  >>
	public void pixelConstructor() throws SyntaxException{
		match(LPIXEL); expression(); match(COMMA); expression(); match(COMMA); expression(); match(COMMA); expression(); match(RPIXEL);
	}

//	Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL |
//			( Expression ) | FunctionApplication  | IDENTIFIER | PixelExpression |
//	PredefinedName | PixelConstructor
	public void primary() throws SyntaxException{
		switch (t.getKind()) {
			case INTEGER_LITERAL:
				match(INTEGER_LITERAL);
				break;
			case BOOLEAN_LITERAL:
				match(BOOLEAN_LITERAL);
				break;
			case FLOAT_LITERAL:
				match(FLOAT_LITERAL);
				break;
			case IDENTIFIER:
				match(IDENTIFIER);
				if(isKind(firstPixelSelector))
					pixelSelector();
				break;
			case LPAREN:
				match(LPAREN);
				expression();
				match(RPAREN);
				break;
			default:
				if (isKind(firstPixelExpression))
					pixelExpression();
				else if (isKind(firstFunctionApplication))
					functionApplication();
				else if (isKind(firstPredefinedName))
					predefinedName();
				else if (isKind(firstPixelConstructor))
					pixelConstructor();
		}
	}

//	FunctionName ::= sin | cos | atan | abs | log | cart_x | cart_y | polar_a | polar_r
//	int | float | width | height | Color
	public void functionName() throws SyntaxException{
		switch (t.getKind()) {
			case KW_sin:
				match(KW_sin);
				break;
			case KW_cos:
				match(KW_cos);
				break;
			case KW_atan:
				match(KW_atan);
				break;
			case KW_abs:
				match(KW_abs);
				break;
			case KW_log:
				match(KW_log);
				break;
			case KW_cart_x:
				match(KW_cart_x);
				break;
			case KW_cart_y:
				match(KW_cart_y);
				break;
			case KW_polar_a:
				match(KW_polar_a);
				break;
			case KW_polar_r:
				match(KW_polar_r);
				break;
			case KW_int:
				match(KW_int);
				break;
			case KW_float:
				match(KW_float);
				break;
			case KW_width:
				match(KW_width);
				break;
			case KW_height:
				match(KW_height);
				break;
			default:
				color();
		}
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
			throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
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

