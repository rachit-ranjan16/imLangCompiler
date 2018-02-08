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

	public static class UnSupportedOperationException extends Exception {
//		Token t;
		public UnSupportedOperationException(String message) {
			super(message);
//			this.t = t;
		}
	}



	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException, UnSupportedOperationException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException, UnSupportedOperationException {
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstType = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha};
	Kind[] firstPixelSelector = {LSQUARE};
	Kind[] firstColor = {KW_red, KW_green, KW_blue, KW_alpha};
	Kind[] firstLHS = {IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha };

	public void block() throws SyntaxException, UnSupportedOperationException {
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
	/*
	Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	 */
	public void declaration() throws SyntaxException, UnSupportedOperationException {
		if (isKind(firstType)) {
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
	}

	/*
	Statement ::= StatementInput | StatementWrite | StatementAssignment
		| StatementWhile | StatementIf | StatementShow | StatementSleep
	 */
	public void statement() throws SyntaxException, UnSupportedOperationException {
		switch (t.getKind()) {
			// StatementInput ::= input IDENTIFIER from @ Expression
			case KW_input: match(KW_input);match(IDENTIFIER);match(KW_from);match(OP_AT);expression();break;
			// StatementWrite ::= write IDENTIFIER to IDENTIFIER
			case KW_write: match(KW_write);match(IDENTIFIER);match(KW_to);match(IDENTIFIER);break;
			// StatementWhile ::=  while (Expression ) Block
			case KW_while: match(KW_while);match(LPAREN);expression();match(RPAREN);block();break;
			// StatementIf ::=  if ( Expression ) Block
			case KW_if: match(KW_if);match(LPAREN);expression();match(RPAREN);block();break;
			// StatementShow ::=  show Expression
			case KW_show: match(KW_show);expression();break;
			// StatementSleep ::=  sleep Expression
			case KW_sleep: match(KW_sleep);expression();break;
		}
		// StatementAssignment ::=  LHS := Expression
		if (isKind(firstLHS)) { lhs();match(OP_ASSIGN);expression(); }
	}

	/*
	LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector )
	 */
	public void lhs() throws SyntaxException, UnSupportedOperationException {
		if (t.getKind() == IDENTIFIER) { match(IDENTIFIER); if (isKind(firstPixelSelector)) pixelSelector();}
		else if(isKind(firstColor)) color();
	}
	/*
	Color ::= red | green | blue | alpha
	 */
	public void color() throws SyntaxException, UnSupportedOperationException {
		switch (t.getKind()) {
			case KW_red: match(KW_red);match(LPAREN);match(IDENTIFIER);pixelSelector();match(RPAREN);break;
			case KW_green: match(KW_green);match(LPAREN);match(IDENTIFIER);pixelSelector();match(RPAREN);break;
			case KW_blue: match(KW_blue);match(LPAREN);match(IDENTIFIER);pixelSelector();match(RPAREN);break;
			case KW_alpha: match(KW_alpha);match(LPAREN);match(IDENTIFIER);pixelSelector();match(RPAREN);break;
		}
	}

	/*
	PixelSelector ::= [ Expression , Expression ]
	 */
	private void pixelSelector() throws SyntaxException, UnSupportedOperationException {
		match(LSQUARE); expression(); match(COMMA); expression(); match(RSQUARE);
	}

	//Kind[] firstStatement = { KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha};
	public void type() throws SyntaxException, UnSupportedOperationException {
		switch (t.getKind()) {
			case KW_int: match(KW_int);break;
			case KW_float: match(KW_float);break;
			case KW_boolean: match(KW_boolean);break;
			case KW_filename: match(KW_filename);break;
			case KW_image: match(KW_image);break;
			default: throw new SyntaxException(t,"Syntax Error while parsing Token=" + t.getText() + " " + t.line() + ":" + t.posInLine());
		}
	}


	public void expression() throws SyntaxException, UnSupportedOperationException {
		//TODO Add Implementation
		throw new UnSupportedOperationException("Expression not yet supported");
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
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	

}

