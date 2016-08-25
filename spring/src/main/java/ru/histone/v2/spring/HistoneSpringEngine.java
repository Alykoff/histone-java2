package ru.histone.v2.spring;

import ru.histone.v2.HistoneEngine;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.parser.node.ExpAstNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Aleksander Melnichnikov
 */
public interface HistoneSpringEngine extends HistoneEngine {

    String process(String template, String baseUri, Map<String, Object> params);

    String process(String template);

    ExpAstNode parseTemplateToAST(String templateData, String baseURI);

    CompletableFuture<String> evaluateAST(String baseUri, ExpAstNode ast, Map<String, Object> params) throws HistoneException;
}
