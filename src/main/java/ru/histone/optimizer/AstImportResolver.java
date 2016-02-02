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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.Histone;
import ru.histone.HistoneException;
import ru.histone.evaluator.EvaluatorException;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.AstNodeType;
import ru.histone.parser.OldParser;
import ru.histone.parser.ParserException;
import ru.histone.resourceloaders.*;
import ru.histone.utils.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

@Deprecated
public class AstImportResolver {

    private static final String HISTONE = "HISTONE";
    private static final Logger log = LoggerFactory.getLogger(AstImportResolver.class);
    private NodeFactory nodeFactory;

    private ResourceLoader resourceLoader;
    private OldParser parser;

    public AstImportResolver(OldParser parser, ResourceLoader resourceLoader, NodeFactory nodeFactory) {
        this.parser = parser;
        this.resourceLoader = resourceLoader;
        this.nodeFactory = nodeFactory;
    }

    public ArrayNode resolve(ArrayNode ast) throws HistoneException {
        ImportResolverContext context = new ImportResolverContext();
        return resolveInternal(ast, context);
    }

    public ArrayNode resolve(String baseUrl, ArrayNode ast) throws HistoneException {
        ImportResolverContext context = new ImportResolverContext();
        context.setBaseURI(baseUrl);
        return resolveInternal(ast, context);
    }


    private ArrayNode resolveInternal(ArrayNode ast, ImportResolverContext context) throws HistoneException {
        ArrayNode finalResult = nodeFactory.jsonArray();

        finalResult.add(getHistonHeader(ast));

        ast = getHistoneContent(ast);
        ArrayNode result = nodeFactory.jsonArray();

        for (JsonNode element : ast) {
            JsonNode node = scanInstructions(element, context);
            if (node.isArray() && node.get(0).isArray()) {
                result.addAll((ArrayNode) node);
            } else {
                result.add(node);
            }
        }

        finalResult.add(result);
        return result;
    }

    private JsonNode scanInstructions(JsonNode element, ImportResolverContext context) throws HistoneException {
        if (isString(element)) {
            return element;
        }

        if (!element.isArray()) {
            Histone.runtime_log_warn("Invalid JSON element! Neither 'string', nor 'array'. Element: '{}'", element.toString());
            return element;
        }

        ArrayNode astArray = (ArrayNode) element;

        int nodeType = getNodeType(astArray);
        switch (nodeType) {
            case AstNodeType.IMPORT:
                return resolveImport(astArray.get(1), context);
            default:
                return astArray;
        }
    }

    private ArrayNode resolveImport(JsonNode pathElement, ImportResolverContext context) throws HistoneException {
        if (!isString(pathElement)) {
            Histone.runtime_log_warn("Invalid path to imported template: '{}'", pathElement.toString());
            return nodeFactory.jsonArray(AstNodeType.STRING);
        }
        String path = pathElement.asText();
        Resource resource = null;
//        InputStream resourceStream = null;
        try {
            String currentBaseURI = getContextBaseURI(context);
            String resourceFullPath = resourceLoader.resolveFullPath(path, currentBaseURI);

            if (context.hasImportedResource(resourceFullPath)) {
                Histone.runtime_log_info("Resource already imported.");
                return nodeFactory.jsonArray(AstNodeType.STRING);
            } else {
                if (currentBaseURI == null) {
//                    if (!resourceLoader.isCacheable(path, null)) {
//                        String fullPath = resourceLoader.resolveFullPath(path, null);
//                        return AstNodeFactory.createNode(AstType.IMPORT, fullPath);
//                    }
                } else {
//                    if (!resourceLoader.isCacheable(currentBaseURI, path)) {
//                        String fullPath = resourceLoader.resolveFullPath(path, currentBaseURI);
//                        return AstNodeFactory.createNode(AstType.IMPORT, fullPath);
//                    }
                }
                resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT});

                JsonNode ast = readAstFromResource(resource,path,currentBaseURI);


                // Add this resource full path to context
                context.addImportedResource(resourceFullPath);

                URI resourceURI = null; //TODO: refactor //resource.getURI();
                if (resourceURI != null && resourceURI.isAbsolute() && !resourceURI.isOpaque()) {
                    context.setBaseURI(resourceURI.resolve("").toString());
                }

                ArrayNode result = nodeFactory.jsonArray();
                for (JsonNode elem : getHistoneContent((ArrayNode) ast)) {
                    if (elem.isArray()) {
                        int nodeType = getNodeType((ArrayNode) elem);
                        switch (nodeType) {
                            case AstNodeType.MACRO:
                                result.add(elem);
                                break;
                            // EXPERIMENTAL: This small fix allows to import variable definitions with macroses if variable value is constant
                            case AstNodeType.VAR:
                                String varName = elem.get(1).asText();
                                JsonNode varValue = elem.get(2);
                                if (jsonNodeIsConstant(varValue)) {
                                    result.add(elem);
                                }
                                break;
                            case AstNodeType.IMPORT:
                                ArrayNode resolvedAst = resolveImport(elem.get(1), context);
                                if (resolvedAst.get(0).isArray()) {
                                    result.addAll(resolvedAst);
                                } else {
                                    result.add(resolvedAst);
                                }
                                break;
                            default:
                                //do nothing
                        }
                    }
                }

                context.setBaseURI(currentBaseURI == null ? null : currentBaseURI);
                return result;
            }
        } catch (ResourceLoadException e) {
            Histone.runtime_log_warn_e("Resource import failed! Unresolvable resource.", e);
            return nodeFactory.jsonArray(AstNodeType.STRING, nodeFactory.jsonString(""));
        } catch (ParserException e) {
            Histone.runtime_log_warn_e("Resource import failed! Resource parsing error.", e);
            return nodeFactory.jsonArray(AstNodeType.STRING, nodeFactory.jsonString(""));
        } finally {
            IOUtils.closeQuietly(resource, log);
        }
    }

    private boolean jsonNodeIsConstant(JsonNode varValue) {
        if (varValue instanceof ArrayNode) {
            ArrayNode arr = (ArrayNode) varValue;
            int nodeType = getNodeType(arr);
                    return nodeType == AstNodeType.TRUE ||
                            nodeType == AstNodeType.FALSE ||
                            nodeType == AstNodeType.NULL ||
                            nodeType == AstNodeType.INT ||
                            nodeType == AstNodeType.DOUBLE ||
                            nodeType == AstNodeType.STRING;
        } else return false;
    }

    private boolean isString(JsonNode element) {
        return element.isTextual();
    }

    private int getNodeType(ArrayNode astArray) {
        return astArray.get(0).asInt();
    }

    private String getContextBaseURI(ImportResolverContext context) {
        String value = context.getBaseURI();
        if (value == null) {
            return null;
        }
        return value;
    }

    private ArrayNode getHistoneContent(ArrayNode ast) {
        if (ast.path(0) != null && HISTONE.equals(ast.path(0).path(0).asText())) {
            return (ArrayNode) ast.path(1);
        }
        return ast;
    }

    private ArrayNode getHistonHeader(ArrayNode ast) {
        if (ast.path(0) != null && HISTONE.equals(ast.path(0).path(0).asText())) {
            return (ArrayNode) ast.path(0);
        }
        return ast;
    }

    private JsonNode readAstFromResource(Resource resource, String path, String currentBaseURI) throws EvaluatorException {
        JsonNode ast = null;
        try {
            if (resource == null) {
                throw new ResourceLoadException(MessageFormat.format("Can't import resource by path = '{}'. Resource was not found.", path));
            }

            if (!(resource instanceof StringResource) && !(resource instanceof StreamResource) && !(resource instanceof AstResource)) {
                throw new ResourceLoadException(MessageFormat.format("Can't import resource by path: {0}. Resource type {1} is unknown", path, resource.getClass()));
            }

            String templateContent = null;
            if (resource instanceof StringResource) {
                templateContent = ((StringResource) resource).getContent();
            } else if (resource instanceof StreamResource) {
                templateContent = IOUtils.toString(((StreamResource) resource).getContent());
            } else if (resource instanceof AstResource) {
                ast = ((AstResource) resource).getContent();
            } else {
                throw new ResourceLoadException(MessageFormat.format("Unsupported resource class: {0}", resource.getClass()));
            }

            if (resource instanceof StringResource || resource instanceof StreamResource) {
                if (templateContent == null) {
                    throw new ResourceLoadException(MessageFormat.format("Can't import resource by path: {0}. Resource is unreadable", path));
                }

                if (resource.getContentType() == ContentType.TEXT) {
                    ast = parser.parse(templateContent);
                } else if (resource.getContentType() == ContentType.AST) {
                    ast = nodeFactory.jsonNode(templateContent);
                } else {
                    throw new ResourceLoadException(MessageFormat.format("Unsupported content-type:{0} of resource href:{1}, baseHref:{2}", resource.getContentType(), path, currentBaseURI));
                }
            } else {
                if (ast == null) {
                    throw new ResourceLoadException(MessageFormat.format("Can't import resource by path = {0}. Resource is unreadable", path));
                }
            }
        } catch (IOException e) {
            throw new ResourceLoadException("Resource import failed! Resource reading error.", e);
        } catch (ParserException e) {
            throw new ResourceLoadException("Resource import failed! Resource parsing error.", e);
        }

        return ast;
    }
}
