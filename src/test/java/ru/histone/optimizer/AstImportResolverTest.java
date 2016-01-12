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
package ru.histone.optimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.histone.Histone;
import ru.histone.HistoneBuilder;
import ru.histone.HistoneException;
import ru.histone.HistoneTokensHolder;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.OldParser;
import ru.histone.resourceloaders.DefaultResourceLoader;
import ru.histone.resourceloaders.ResourceLoader;
import ru.histone.tokenizer.TokenizerFactory;
import ru.histone.utils.IOUtils;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

@Ignore
@Deprecated
public class AstImportResolverTest {
    private ObjectMapper om = new ObjectMapper();
    private Histone histone;
    private AstImportResolver astImportResolver;

    @Test
    public void example1() throws HistoneException, URISyntaxException {
        String input = input("example1/base.tpl");

        String baseURI = AstImportResolverTest.class.getClassLoader().getResource("optimizer/imports_resolving/example1/").toURI().toString();

        ArrayNode ast = histone.parseTemplateToAST(new StringReader(input));
        ArrayNode finalAst = astImportResolver.resolve(baseURI, ast);

        String astS = histone.evaluateAST(baseURI, ast, om.createObjectNode());
        String finalAstS = histone.evaluateAST(baseURI, finalAst, om.createObjectNode());

        assertEquals(astS, finalAstS);
    }

    @Before
    public void init() throws HistoneException {
        HistoneBuilder histoneBuilder = new HistoneBuilder();
        histone = histoneBuilder.build();
        astImportResolver = buildAstImportResolver();
    }

    private AstImportResolver buildAstImportResolver() {
        NodeFactory nodeFactory = new NodeFactory(new ObjectMapper());
        TokenizerFactory tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
        OldParser parser = new OldParser(tokenizerFactory, nodeFactory);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        return new AstImportResolver(parser, resourceLoader, nodeFactory);
    }

    private String input(String filename) {
        try {
            StringWriter sw = new StringWriter();
            InputStream is = getClass().getClassLoader().getResourceAsStream("optimizer/imports_resolving/" + filename);
            IOUtils.copy(is, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
