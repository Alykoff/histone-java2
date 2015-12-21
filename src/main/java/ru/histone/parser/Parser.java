/**
 *    Copyright 2013 MegaFon
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.tokenizer.OldTokenizer;
import ru.histone.tokenizer.TokenizerFactory;

/**
 * Parser wrapper class - takes tokens from tokenizer and process them generating Abstract Syntax AstNodeFactory (AST) from input sequence
 */
public class Parser {
    private final static Logger log = LoggerFactory.getLogger(Parser.class);
    private TokenizerFactory tokenizerFactory;
    private NodeFactory nodeFactory;

    /**
     * Constructs parser with {@link TokenizerFactory} dependency
     *
     * @param tokenizerFactory tokenizer factory
     */
    public Parser(TokenizerFactory tokenizerFactory, NodeFactory nodeFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.nodeFactory = nodeFactory;
    }

    /**
     * Parse input sequence into AST
     *
     * @param input input sequence
     * @return JSON representation of AST
     * @throws ParserException in case of parse error
     */
    public ArrayNode parse(CharSequence input) throws ParserException {
        log.debug("parse(): input={}", new Object[]{input});

        OldTokenizer tokenizer = tokenizerFactory.match(input);
        ParserImpl parser = new ParserImpl(tokenizer, nodeFactory);
        ArrayNode astTree = parser.parseTemplate();
        log.debug("parse(): astTree={}", astTree);

        return astTree;
    }
}
