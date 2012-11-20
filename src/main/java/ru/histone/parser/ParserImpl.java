/**
 *    Copyright 2012 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.tokenizer.Token;
import ru.histone.tokenizer.TokenType;
import ru.histone.tokenizer.Tokenizer;
import ru.histone.utils.StringEscapeUtils;
import ru.histone.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Parser implementation
 */
public class ParserImpl {
	private final static Logger log = LoggerFactory.getLogger(ParserImpl.class);

	private Tokenizer tokenizer;
    private NodeFactory nodeFactory;

    /**
     * Constructs parser with {@link ru.histone.tokenizer.Tokenizer} dependency
     *
     * @param tokenizer tokenizer factory
     */
    public ParserImpl(Tokenizer tokenizer, NodeFactory nodeFactory) {
        this.tokenizer = tokenizer;
        this.nodeFactory=nodeFactory;
	}

    /**
     * Parse input sequence into AST
     *
     * @return JSON representation of AST
     * @throws ParserException in case of parse error
     */
	public ArrayNode parseTemplate() throws ParserException {
		if(tokenizer == null){
			throw new ParserException("Initialize tokenizer before parsing template");
		}
		return parse();
	}

	private ArrayNode parse(TokenType... breakOn) throws ParserException {
		log.trace("parse(TokenType): breakOn={}", new Object[] { breakOn });
		ArrayNode tree = nodeFactory.jsonArray();

		while (true) {
			Token token = tokenizer.next();

			if (token.getType() == TokenType.T_COMMENT_START) {
				log.trace("parse(TokenType): dive into comment block");
				while (!tokenizer.isNext(TokenType.T_COMMENT_END) && !tokenizer.isNext(TokenType.T_EOF)) {
					tokenizer.next();
				}
				if (tokenizer.next(TokenType.T_COMMENT_END) == null) {
					throw expectedFound("*}}", tokenizer.next());
				}
				log.trace("parse(TokenType): comment block finished");
			} else if (token.getType() == TokenType.T_LITERAL_START) {
				log.trace("parse(TokenType): dive into literal block");
				StringBuilder sb = new StringBuilder();
				while (!tokenizer.isNext(TokenType.T_LITERAL_END) && !tokenizer.isNext(TokenType.T_EOF)) {
					sb.append(tokenizer.next().getContent());
				}
				if (tokenizer.next(TokenType.T_LITERAL_END) == null) {
					throw expectedFound("%}}", tokenizer.next());
				}
				tree.add(nodeFactory.jsonString(sb.toString()));
				log.trace("parse(TokenType): literal block finished");
			} else if (token.getType() == TokenType.T_BLOCK_START) {
				log.trace("parse(TokenType): dive into block");
				boolean foundBreakOnToken = false;
				for (TokenType breakOnToken : breakOn) {
					if (tokenizer.isNext(breakOnToken)) {
						foundBreakOnToken = true;
						log.trace("parse(TokenType): breakOnToken={}", new Object[] { breakOnToken });
						break;
					}
				}

				if (foundBreakOnToken) {
					break;
				} else {
					if (tokenizer.next(TokenType.T_BLOCK_END) != null) {
						continue;// skip empty block
					}

					JsonNode block = parseBlock();
					if (block != null) {
						tree.add(block);
					}
				}
				log.trace("parse(TokenType): block finished");
			} else if (token.getType() == TokenType.T_BLOCK_END) {
				// do nothing
			} else if (token.getType() == TokenType.T_FRAGMENT) {
				tree.add(nodeFactory.jsonString(token.getContent()));
			} else if (token.getType() == TokenType.T_EOF) {
				// end of input reached, exit while loop
				break;
			} else {
				throw expectedFound("{{*, {{, }}, fragment, EOF", tokenizer.next());
			}
		}

		log.trace("parse(TokenType): result={}", new Object[] { tree });

		return tree;
	}

	private JsonNode parseBlock() throws ParserException {
		JsonNode tree = null;

		if (tokenizer.isNext(TokenType.EXPR_VAR)) {
			tree = parseVar();
		} else if (tokenizer.isNext(TokenType.EXPR_MACRO)) {
			tree = parseMacro();
		} else if (tokenizer.isNext(TokenType.EXPR_FOR)) {
			tree = parseFor();
		} else if (tokenizer.isNext(TokenType.EXPR_IF)) {
			tree = parseIf();
		} else if (tokenizer.isNext(TokenType.EXPR_CALL)) {
			tree = parseCall();
		} else if (tokenizer.isNext(TokenType.EXPRT_IMPORT)) {
            tree = parseImport();
        } else if (tokenizer.isNext(TokenType.EXPR_DIV)) {
		} else {
			tree = parseExpression();

			if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
				throw expectedFound("}}", tokenizer.next());
			}
		}

		return tree;
	}

    private JsonNode parseImport() throws ParserException {
        Token blockToken = tokenizer.next(TokenType.EXPRT_IMPORT);
        Token pathToken = tokenizer.next(TokenType.EXPR_STRING);
        if(pathToken == null) {
            throw expectedFound("string", tokenizer.next());
        }
        if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
            throw expectedFound("}}", tokenizer.next());
        }
        String path = unescapeString(StringUtils.stripSuroundings(pathToken.getContent(), "'\""));
        return nodeFactory.jsonArray(AstNodeType.IMPORT, path);
    }

    private JsonNode parseCall() throws ParserException {
		log.trace("parseCall(): >>>");
		Token blockToken = tokenizer.next(TokenType.EXPR_CALL);

		Token nameToken = tokenizer.next(TokenType.EXPR_IDENT);
		if (nameToken == null) {
			throw expectedFound("identifier", tokenizer.next());
		}
		JsonNode name = nodeFactory.jsonString(nameToken.getContent());

		ArrayNode args = nodeFactory.jsonArray();
		if (tokenizer.next(TokenType.EXPR_LPAREN) != null) {
			if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
				while (true) {
					JsonNode expr = parseExpression();
					if (expr != null) {
						args.add(expr);
					}
					if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
						break;
					}
				}
				if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
					throw expectedFound(")", tokenizer.next());
				}
			}
		}

		if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
			throw expectedFound("}}", tokenizer.next());
		}

		JsonNode callBlock = nodeFactory.jsonArray(AstNodeType.STATEMENTS, parse(TokenType.EXPR_DIV));
		args.add(callBlock);

		if ((tokenizer.next(TokenType.EXPR_DIV) == null) || (tokenizer.next(TokenType.EXPR_CALL) == null) || (tokenizer.next(TokenType.T_BLOCK_END) == null)) {
			throw expectedFound("{{/call}}", tokenizer.next());
		}

		JsonNode result = nodeFactory.jsonArray(AstNodeType.CALL, null, name, args);
		// tree.setPos(blockToken.getPos());

		log.trace("parseCall(): <<<");
		return result;
	}

	private JsonNode parseVar() throws ParserException {
		log.trace("parseVar(): >>>");

		// skip 'var' token
		Token blockToken = tokenizer.next(TokenType.EXPR_VAR);

		Token nameToken = tokenizer.next(TokenType.EXPR_IDENT);
		if (nameToken == null) {
			throw expectedFound("identifier", tokenizer.next());
		}
		JsonNode name = nodeFactory.jsonString(nameToken.getContent());

		JsonNode expression;
		if (tokenizer.next(TokenType.EXPR_ASSIGN) == null) {
			if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
				throw expectedFound("}}", tokenizer.next());
			}

			expression = nodeFactory.jsonArray(AstNodeType.STATEMENTS, parse(TokenType.EXPR_DIV));

			if ((tokenizer.next(TokenType.EXPR_DIV) == null) || (tokenizer.next(TokenType.EXPR_VAR) == null) || (tokenizer.next(TokenType.T_BLOCK_END) == null)) {
				throw expectedFound("{{/var}}", tokenizer.next());
			}
		} else {
			expression = parseExpression();

			if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
				throw expectedFound("}}", tokenizer.next());

			}
		}

		JsonNode result = nodeFactory.jsonArray(AstNodeType.VAR, name, expression);
		// tree.setPos(blockToken.getPos());

		log.trace("parseVar(): <<<");

		return result;
	}

    private JsonNode parseMacro() throws ParserException {
		log.trace("parseMacro(): >>>");

		// skip 'macro' token
		Token blockToken = tokenizer.next(TokenType.EXPR_MACRO);

		Token nameToken = tokenizer.next(TokenType.EXPR_IDENT);
		if (nameToken == null) {
			throw expectedFound("identifier", tokenizer.next());
		}
		JsonNode name = nodeFactory.jsonString(nameToken.getContent());

		ArrayNode args = nodeFactory.jsonArray();
		if (tokenizer.next(TokenType.EXPR_LPAREN) != null) {
			if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
				while (true) {
					if (!tokenizer.isNext(TokenType.EXPR_IDENT)) {
						throw expectedFound("identifier", tokenizer.next());
					}

					args.add(nodeFactory.jsonString(tokenizer.next(TokenType.EXPR_IDENT).getContent()));

					if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
						break;
					}
				}
				if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
					throw expectedFound(")", tokenizer.next());
				}

			}
		}

		if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
			throw expectedFound("}}", tokenizer.next());
		}

		ArrayNode macroBlock = parse(TokenType.EXPR_DIV);

		if ((tokenizer.next(TokenType.EXPR_DIV) == null) || (tokenizer.next(TokenType.EXPR_MACRO) == null) || (tokenizer.next(TokenType.T_BLOCK_END) == null)) {
			throw expectedFound("{{/macro}}", tokenizer.next());
		}

		JsonNode result = nodeFactory.jsonArray(AstNodeType.MACRO, name, args, macroBlock);
		// tree.setPos(blockToken.getPos());

		log.trace("parseMacro(): <<<");
		return result;
	}

	private JsonNode parseFor() throws ParserException {
		log.trace("parseFor(): >>>");
		// skip 'for' token
		Token blockToken = tokenizer.next(TokenType.EXPR_FOR);

		Token valueToken = tokenizer.next(TokenType.EXPR_IDENT);
		if (valueToken == null) {
			throw expectedFound("identifier", tokenizer.next());
		}

		Token keyToken = null;
		if (tokenizer.next(TokenType.EXPR_COLON) != null) {
			keyToken = valueToken;
			valueToken = tokenizer.next(TokenType.EXPR_IDENT);
			if (valueToken == null) {
				throw expectedFound("identifier", tokenizer.next());
			}
		}

		if (tokenizer.next(TokenType.EXPR_IN) == null) {
			throw expectedFound("in", tokenizer.next());
		}

		JsonNode expression = parseExpression();

		if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
			throw expectedFound("}}", tokenizer.next());
		}

		ArrayNode forBlock = parse(TokenType.EXPR_DIV, TokenType.EXPR_ELSE);

		ArrayNode elseBlock = null;
		if (tokenizer.next(TokenType.EXPR_ELSE) != null) {
			if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
				throw expectedFound("}}", tokenizer.next());
			}

			elseBlock = parse(TokenType.EXPR_DIV);
		}

		if ((tokenizer.next(TokenType.EXPR_DIV) == null) || (tokenizer.next(TokenType.EXPR_FOR) == null) || (tokenizer.next(TokenType.T_BLOCK_END) == null)) {
			throw expectedFound("{{/for}}", tokenizer.next());
		}

		ArrayNode iterator = nodeFactory.jsonArray();
		iterator.add(nodeFactory.jsonString(valueToken.getContent()));
		if (keyToken != null) {
			iterator.add(nodeFactory.jsonString(keyToken.getContent()));
		}

		ArrayNode statements = nodeFactory.jsonArray();
		statements.add(forBlock);
		if (elseBlock != null) {
			statements.add(elseBlock);
		}

		JsonNode result = nodeFactory.jsonArray(AstNodeType.FOR, iterator, expression, statements);
		// tree.setPos(blockToken.getPos());

		log.trace("parseFor(): <<<");

		return result;
	}

	private JsonNode parseIf() throws ParserException {
		log.trace("parseIf(): >>>");
		// skip 'var' token
		Token blockToken = tokenizer.next(TokenType.EXPR_IF);

		ArrayNode conditions = nodeFactory.jsonArray();

		while (true) {
			if (tokenizer.isNext(TokenType.T_BLOCK_END)) {
				throw expectedFound("expression", tokenizer.next());
			}

			JsonNode expression = parseExpression();

            if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
                throw expectedFound("}}", tokenizer.next());
            }

			ArrayNode children = parse(TokenType.EXPR_DIV, TokenType.EXPR_ELSEIF, TokenType.EXPR_ELSE);

			ArrayNode condition = nodeFactory.jsonArray();
			// condition.add(nodeFactory.jsonArray(AstNodeType.TRUE));
			condition.add(expression);
			condition.add(children);

			conditions.add(condition);

			if (tokenizer.next(TokenType.EXPR_ELSEIF) == null) {
				break;
			}
		}

		if (tokenizer.next(TokenType.EXPR_ELSE) != null) {
            if (tokenizer.next(TokenType.T_BLOCK_END) == null) {
                throw expectedFound("}}", tokenizer.next());
            }

			ArrayNode children = parse(TokenType.EXPR_DIV);

			ArrayNode condition = nodeFactory.jsonArray();
			condition.add(nodeFactory.jsonArray(AstNodeType.TRUE));
			condition.add(children);

			conditions.add(condition);
		}

		if ((tokenizer.next(TokenType.EXPR_DIV) == null) || (tokenizer.next(TokenType.EXPR_IF) == null) || (tokenizer.next(TokenType.T_BLOCK_END) == null)) {
			throw expectedFound("{{/if}}", tokenizer.next());
		}

		JsonNode tree = nodeFactory.jsonArray(AstNodeType.IF, conditions);
		// tree.setPos(blockToken.getPos());

		log.trace("parseIf(): <<<");
		return tree;
	}

	private JsonNode parseExpression() throws ParserException {
		log.trace("parseExpression(): >>>");
		JsonNode result = parseTernaryExpression();
		log.trace("parseExpression(): result={}", result);
		log.trace("parseExpression(): <<<");
		return result;
	}

	private JsonNode parseTernaryExpression() throws ParserException {
		// log.trace("parseTernaryExpression()");

		JsonNode left = parseOrExpression();
		JsonNode thenNode, elseNode;
		while (tokenizer.next(TokenType.EXPR_QUERY) != null) {
			thenNode = parseExpression();
            if (tokenizer.next(TokenType.EXPR_COLON) == null) {
                //STE-61 we can skip part after colon
                left = nodeFactory.jsonArray(AstNodeType.TERNARY, left, thenNode);
            } else {
                elseNode = parseExpression();
                left = nodeFactory.jsonArray(AstNodeType.TERNARY, left, thenNode, elseNode);
            }
        }

        return left;
	}

	private JsonNode parseOrExpression() throws ParserException {
		// log.trace("parseOrExpression()");

		JsonNode left = parseAndExpression();
		while (tokenizer.next(TokenType.EXPR_OR) != null) {
			// int pos = left.getPos();
			left = nodeFactory.jsonArray(AstNodeType.OR, left, parseAndExpression());
			// left.setPos(pos);
		}
		return left;
	}

	private JsonNode parseAndExpression() throws ParserException {
		// log.trace("parseAndExpression()");

		JsonNode left = parseEqualityExpression();
		while (tokenizer.next(TokenType.EXPR_AND) != null) {
			// int pos = left.getPos();
			left = nodeFactory.jsonArray(AstNodeType.AND, left, parseEqualityExpression());
			// left.setPos(pos);
		}
		return left;
	}

	private JsonNode parseEqualityExpression() throws ParserException {
		// log.trace("parseEqualityExpression()");

		JsonNode left = parseRelationalExpression();
		while (tokenizer.isNext(TokenType.EXPR_IS) || tokenizer.isNext(TokenType.EXPR_NOT_EQUAL)) {
			// int pos = left.getPos();

			if (tokenizer.next(TokenType.EXPR_IS) != null) {
				left = nodeFactory.jsonArray(AstNodeType.EQUAL, left, parseRelationalExpression());
			} else if (tokenizer.next(TokenType.EXPR_NOT_EQUAL) != null) {
				left = nodeFactory.jsonArray(AstNodeType.NOT_EQUAL, left, parseRelationalExpression());
			}
			// left.setPos(pos);

		}
		return left;
	}

	private JsonNode parseRelationalExpression() throws ParserException {
		// log.trace("parseRelationalExpression()");

		JsonNode left = parseAdditiveExpression();
		while (tokenizer.isNext(TokenType.EXPR_LESS_OR_EQUAL) || tokenizer.isNext(TokenType.EXPR_LESS_THAN) || tokenizer.isNext(TokenType.EXPR_GREATER_OR_EQUAL)
				|| tokenizer.isNext(TokenType.EXPR_GREATER_THAN)) {
			// int pos = left.getPos();

			if (tokenizer.next(TokenType.EXPR_LESS_OR_EQUAL) != null) {
				left = nodeFactory.jsonArray(AstNodeType.LESS_OR_EQUAL, left, parseAdditiveExpression());
			} else if (tokenizer.next(TokenType.EXPR_LESS_THAN) != null) {
				left = nodeFactory.jsonArray(AstNodeType.LESS_THAN, left, parseAdditiveExpression());
			} else if (tokenizer.next(TokenType.EXPR_GREATER_OR_EQUAL) != null) {
				left = nodeFactory.jsonArray(AstNodeType.GREATER_OR_EQUAL, left, parseAdditiveExpression());
			} else if (tokenizer.next(TokenType.EXPR_GREATER_THAN) != null) {
				left = nodeFactory.jsonArray(AstNodeType.GREATER_THAN, left, parseAdditiveExpression());
			}
			// left.setPos(pos);

		}
		return left;
	}

	private JsonNode parseAdditiveExpression() throws ParserException {
		// log.trace("parseAdditiveExpression()");

		JsonNode left = parseMultiplicativeExpression();
		while (tokenizer.isNext(TokenType.EXPR_ADD) || tokenizer.isNext(TokenType.EXPR_SUB)) {
			// int pos = left.getPos();

			if (tokenizer.next(TokenType.EXPR_ADD) != null) {
				left = nodeFactory.jsonArray(AstNodeType.ADD, left, parseMultiplicativeExpression());
			} else if (tokenizer.next(TokenType.EXPR_SUB) != null) {
				left = nodeFactory.jsonArray(AstNodeType.SUB, left, parseMultiplicativeExpression());
			}
			// left.setPos(pos);

		}
		return left;
	}

	private JsonNode parseMultiplicativeExpression() throws ParserException {
		// log.trace("parseMultiplicativeExpression()");

		JsonNode left = parseUnaryExpression();
		while (tokenizer.isNext(TokenType.EXPR_MUL) || tokenizer.isNext(TokenType.EXPR_DIV) || tokenizer.isNext(TokenType.EXPR_MOD)) {
			// int pos = left.getPos();

			if (tokenizer.next(TokenType.EXPR_MUL) != null) {
				left = nodeFactory.jsonArray(AstNodeType.MUL, left, parseUnaryExpression());
			} else if (tokenizer.next(TokenType.EXPR_DIV) != null) {
				left = nodeFactory.jsonArray(AstNodeType.DIV, left, parseUnaryExpression());
			} else if (tokenizer.next(TokenType.EXPR_MOD) != null) {
				left = nodeFactory.jsonArray(AstNodeType.MOD, left, parseUnaryExpression());
			}
			// left.setPos(pos);

		}
		return left;
	}

	private JsonNode parseUnaryExpression() throws ParserException {
		// log.trace("parseUnaryExpression()");

		JsonNode tree;

		if ((tokenizer.next(TokenType.EXPR_NOT) != null)) {
			tree = nodeFactory.jsonArray(AstNodeType.NOT, parseUnaryExpression());
		} else {
			tokenizer.next(TokenType.EXPR_ADD);
			tree = parsePrimaryExpression();
		}

		return tree;
	}

	private JsonNode parsePrimaryExpression() throws ParserException {
		log.trace("parsePrimaryExpression(): >>>");

		JsonNode left = null;
		if ((tokenizer.next(TokenType.EXPR_SUB) != null)) {
			left = nodeFactory.jsonArray(AstNodeType.NEGATE, parseSimpleExpression());
		} else {
			left = parseSimpleExpression();
		}

		while (true) {
			if (tokenizer.next(TokenType.EXPR_DOT) != null) {
				if (!tokenizer.isNext(TokenType.EXPR_IDENT)) {
					throw expectedFound("identifier", tokenizer.next());
				}
				if (!isType(AstNodeType.SELECTOR, left)) {
					left = nodeFactory.jsonArray(AstNodeType.SELECTOR, nodeFactory.jsonArray(left));
				}
				((ArrayNode)left.get(1)).add(nodeFactory.jsonString(tokenizer.next().getContent()));
			} else if (tokenizer.next(TokenType.EXPR_LBRACKET) != null) {
				if (tokenizer.next(TokenType.EXPR_RBRACKET) != null) {
					throw expectedFound("expression", "[]");
				}
				if (!isType(AstNodeType.SELECTOR, left)) {
					left = nodeFactory.jsonArray(AstNodeType.SELECTOR, nodeFactory.jsonArray(left));
				}
				((ArrayNode)left.get(1)).add(parseExpression());

				if (tokenizer.next(TokenType.EXPR_RBRACKET) == null) {
					throw expectedFound("]", tokenizer.next());
				}
			} else if (tokenizer.next(TokenType.EXPR_LPAREN) != null) {
                if(!isType(AstNodeType.SELECTOR, left)) {
                    throw expectedFound("}}", "(");
                }
                ArrayNode leftArr = (ArrayNode) left;
                JsonNode name = nodeFactory.removeLast((ArrayNode) leftArr.get(1));
                if (leftArr.get(1).size() == 0) {
                    left = nodeFactory.jsonNull();
                }

				ArrayNode args = nodeFactory.jsonArray();
				if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
					while (true) {
						args.add(parseExpression());
						if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
							break;
						}
					}
					if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
						throw expectedFound(")", tokenizer.next());
					}
				}

                /*if(name.isArrayNode()) {
                    ArrayNode nameArr = name.getAsArrayNode();
                    JsonNode nameFirstElement = nameArr.get(0);
                    if(nameFirstElement.isJsonPrimitive() && nameFirstElement.getAsJsonPrimitive().getAsNumber().equals(AstNodeType.STRING)) {
                        name = nameArr.get(1);
                    }
                }*/

				left = nodeFactory.jsonArray(AstNodeType.CALL, left, name, (args.size() == 0) ? nodeFactory.jsonNull() : args);
			} else {
				break;
			}
		}

		log.trace("parsePrimaryExpression(): <<<");

		return left;
	}

	private boolean isType(int type, JsonNode elem) {
		ArrayNode leftArray = (ArrayNode) elem;
		int elemType = leftArray.get(0).intValue();
		return type == elemType;
	}

	private JsonNode parseSimpleExpression() throws ParserException {
		log.trace("parseSimpleExpression(): >>>");

		JsonNode tree = null;

		if (tokenizer.next(TokenType.EXPR_NULL) != null) {
			tree = nodeFactory.jsonArray(AstNodeType.NULL);
		} else if ((tokenizer.next(TokenType.EXPR_TRUE) != null)) {
			tree = nodeFactory.jsonArray(AstNodeType.TRUE);
		} else if ((tokenizer.next(TokenType.EXPR_FALSE) != null)) {
			tree = nodeFactory.jsonArray(AstNodeType.FALSE);
		} else if (tokenizer.isNext(TokenType.EXPR_INTEGER)) {
			Token token = tokenizer.next();
			BigInteger val = new BigInteger(token.getContent());
			tree = nodeFactory.jsonArray(AstNodeType.INT, val);
		} else if (tokenizer.isNext(TokenType.EXPR_DOUBLE)) {
			Token token = tokenizer.next();
			BigDecimal val = new BigDecimal(token.getContent());
			tree = nodeFactory.jsonArray(AstNodeType.DOUBLE, val);
		} else if (tokenizer.isNext(TokenType.EXPR_STRING)) {
			Token token = tokenizer.next();
			String val = unescapeString(StringUtils.stripSuroundings(token.getContent(), "'\""));
			tree = nodeFactory.jsonArray(AstNodeType.STRING, val);
        } else if (tokenizer.next(TokenType.EXPR_LBRACKET) != null) {
            tree = parseMap(tokenizer);
//		} else if (tokenizer.next(TokenType.EXPR_ARRAY) != null) {
//			ArrayNode args = nodeFactory.jsonArray();
//			if (tokenizer.next(TokenType.EXPR_LPAREN) == null) {
//				throw expectedFound("(", tokenizer.next());
//			}
//			if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
//				while (true) {
//					args.add(parseExpression());
//					if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
//						break;
//					}
//				}
//				if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
//					throw expectedFound(")", tokenizer.next());
//				}
//			}
//			tree = nodeFactory.jsonArray(AstNodeType.ARRAY, args);
//		} else if (tokenizer.next(TokenType.EXPR_OBJECT) != null) {
//			if (tokenizer.next(TokenType.EXPR_LPAREN) == null) {
//				throw expectedFound("(", tokenizer.next());
//			}
//
//			ArrayNode props = nodeFactory.jsonArray();
//			String key = null;
//			if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
//				while (true) {
//
//					if (tokenizer.isNext(TokenType.EXPR_IDENT) || tokenizer.isNext(TokenType.EXPR_INTEGER)) {
//						key = tokenizer.next().getContent();
//					} else if (tokenizer.isNext(TokenType.EXPR_STRING)) {
//                        key = StringUtils.stripSuroundings(tokenizer.next().getContent(), "'\"");
//					} else {
//						throw expectedFound("identifier, string, number", tokenizer.next());
//					}
//
//					if (tokenizer.next(TokenType.EXPR_COLON) == null) {
//						throw expectedFound(":", tokenizer.next());
//					}
//
//					ArrayNode prop = nodeFactory.jsonArray();
//					prop.add(new JsonPrimitive(key));
//					prop.add(parseExpression());
//					props.add(prop);
//
//					if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
//						break;
//					}
//				}
//				if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
//					throw expectedFound(")", tokenizer.next());
//				}
//			}
//
//			tree = nodeFactory.jsonArray(AstNodeType.OBJECT, props);
		} else if (tokenizer.isNext(TokenType.EXPR_IDENT) || tokenizer.isNext(TokenType.EXPR_THIS) || tokenizer.isNext(TokenType.EXPR_SELF) || tokenizer.isNext(TokenType.EXPR_GLOBAL)) {
			Token token = tokenizer.next();
			ArrayNode val = nodeFactory.jsonArray();
			val.add(nodeFactory.jsonString(token.getContent()));
			tree = nodeFactory.jsonArray(AstNodeType.SELECTOR, val);
		} else if (tokenizer.next(TokenType.EXPR_LPAREN) != null) {
			if (tokenizer.isNext(TokenType.EXPR_RPAREN)) {
				throw expectedFound("expression", "()");
			}
			tree = parseExpression();
			if (tokenizer.next(TokenType.EXPR_RPAREN) == null) {
				throw expectedFound(")", tokenizer.next());
			}
		} else {
			throw expectedFound("expression", tokenizer.next());
		}

		log.trace("parseSimpleExpression(): <<<");

		return tree;
	}

    private JsonNode parseMap(Tokenizer tokenizer) throws ParserException {

        ArrayNode items = nodeFactory.jsonArray();
        JsonNode key, value;

        if (!tokenizer.isNext(TokenType.EXPR_RBRACKET)) {
            while (true) {

                key = null;
                value = parseExpression();

                if (tokenizer.next(TokenType.EXPR_COLON) != null) {

                    int value0 = value.get(0).intValue();
                    if (value0 != AstNodeType.STRING &&
                            value0 != AstNodeType.INT && (
                            value0 != AstNodeType.SELECTOR ||
                                    value.get(1).size() != 1
                    )) {
                        throw expectedFound("identifier, string, number", "node of type '" + value0 + "'");
                    }

                    if (value0 != AstNodeType.SELECTOR) {
                        key = value.get(1);
                    } else {
                        key = value.get(1).get(0);
                    }

                    value = parseExpression();
                }

                if (key != null && key.isNumber()){
                    key = nodeFactory.jsonString(key.asText());
                }

                ArrayNode elem = nodeFactory.jsonArray(key, value);
                items.add(elem);
                if (tokenizer.next(TokenType.EXPR_COMMA) == null) {
                    break;
                }
            }
        }
        if (tokenizer.next(TokenType.EXPR_RBRACKET) == null) {
            throw expectedFound("]", tokenizer.next());
        }
        return nodeFactory.jsonArray(AstNodeType.MAP, items);
    }

    private static String unescapeString(String val) {
		return StringEscapeUtils.unescapeJavaScript(val);
	}

	// public static String escapeString(String val) {
	// return StringEscapeUtils.escapeJavaScript(val);
	// }

	private ParserException expectedFound(String expected, Token found) {
		return expectedFound(expected, found.getContent());
	}

	private ParserException expectedFound(String expected, String found) {
		return new ParserException(tokenizer.getLineNumber(), expected, found);
	}

	// private ParserException unexpected(Token unexpected) {
	// return new ParserException(tokenizer.getLineNumber(),
	// tokenizer.getColumnNumber(), unexpected);
	// }

}
