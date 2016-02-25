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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.Histone;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.AstNodeType;
import ru.histone.v2.exceptions.HistoneException;


@Deprecated
public class AstImportMarker {
   private static final Logger log = LoggerFactory.getLogger(AstImportMarker.class);
   private NodeFactory nodeFactory = new NodeFactory(new ObjectMapper());

    public ArrayNode mark(ArrayNode ast) throws HistoneException {
        ImportResolverContext context = new ImportResolverContext();
        return markInternal(ast, context);
    }

    private ArrayNode markInternal(ArrayNode ast, ImportResolverContext context) throws HistoneException {
        ArrayNode result = nodeFactory.jsonArray();

        for (JsonNode element : ast) {
            JsonNode node = markNode(element, context);
            result.add(node);
        }

        return result;
    }

    private JsonNode markNode(JsonNode element, ImportResolverContext context) throws HistoneException {
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
                return markImport(astArray.get(1), context);

            default:
                return astArray;
        }
    }

    private JsonNode markImport(JsonNode name, ImportResolverContext context) throws HistoneException {
//        if (!isString(pathElement)) {
//            Histone.runtime_log_warn("Invalid path to imported template: '{}'", pathElement.toString());
//            return AstNodeFactory.createNode(AstType.STRING, "");
//        }
//        String path = pathElement.getAsJsonPrimitive().getAsString();
//        Resource resource = null;
//        InputStream resourceStream = null;
//        try {
//            URI currentBaseURI = getContextBaseURI(context);
//            URI resourceFullPath = currentBaseURI == null ? resourceLoaderManager.getFullPath(path) : resourceLoaderManager.getFullPath(currentBaseURI, path);
//
//            if (context.hasImportedResource(resourceFullPath.toString())) {
//                Histone.runtime_log_info("Resource already imported.");
//                return AstNodeFactory.createNode(AstType.STRING, "");
//            } else {
//                resource = currentBaseURI == null ? resourceLoaderManager.load(path, null) : resourceLoaderManager.load(currentBaseURI, path, null);
//
//            }
//        } finally {
//            IOUtils.closeQuietly(resourceStream, log);
//            IOUtils.closeQuietly(resource, log);
//        }
        throw new RuntimeException("UNIMPLEMENTED");
    }

    private boolean isString(JsonNode element) {
        return element.isTextual();
    }

    private int getNodeType(ArrayNode astArray) {
        return astArray.get(0).asInt();
    }

}
