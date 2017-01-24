/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static ru.histone.v2.utils.ParserUtils.canBeLong;

/**
 * @author Alexey Nevinsky
 */
public class AstJsonProcessor {

    private static final Function<Object, AstNode> nodeConvertFunction = obj -> {
        if (obj instanceof List) {
            return convert((List) obj);
        } else if (obj instanceof Integer) {
            return new LongAstNode((Integer) obj);
        } else if (obj instanceof Long) {
            return new LongAstNode((Long) obj);
        } else if (obj instanceof Double) {
            return new DoubleAstNode((Double) obj);
        } else if (obj instanceof Boolean) {
            return new BooleanAstNode((Boolean) obj);
        } else {
            return new StringAstNode((String) obj);
        }
    };

    //todo add AstOptimizer here
    public static ExpAstNode read(String str) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List ast = objectMapper.readValue(str, List.class);
        AstNode res = convert(ast);
        new SsaOptimizer().process(res);
        return (ExpAstNode) res;
    }

    private static AstNode convert(List list) {
        AstType type = AstType.fromId((int) list.get(0));
        final ExpAstNode expAstNode;
        if (type == AstType.AST_CALL) {
            expAstNode = new CallExpAstNode(CallType.SIMPLE);
            expAstNode.add(nodeConvertFunction.apply(list.get(1)));
            if (list.size() > 2) {
                if (list.get(2) instanceof String) {
                    expAstNode.add(new StringAstNode((String) list.get(2)));
                } else {
                    CallType callType = CallType.fromId((int) list.get(2));
                    ((CallExpAstNode) expAstNode).setCallType(callType);
                }
            }
            if (list.size() > 3) {
                for (int i = 3; i < list.size(); i++) {
                    expAstNode.add(nodeConvertFunction.apply(list.get(i)));
                }
            }
        } else if (type == AstType.AST_MACRO) {
            expAstNode = new ExpAstNode(type);
            expAstNode.add(new LongAstNode(0));
            for (int i = 2; i < list.size(); i++) {
                expAstNode.add(nodeConvertFunction.apply(list.get(i)));
            }
        } else {
            expAstNode = new ExpAstNode(type);
            for (int i = 1; i < list.size(); i++) {
                expAstNode.add(nodeConvertFunction.apply(list.get(i)));
            }
        }
        return expAstNode;
    }

    public static String write(AstNode node) {
        StringBuffer sb = new StringBuffer();
        nodeToString(sb, node);
        return sb.toString();
    }

    private static void nodeToString(StringBuffer sb, AstNode node) {
        if (node.hasValue()) {
            Object nodeValue = ((ValueNode) node).getValue();
            if (nodeValue instanceof Number || nodeValue instanceof Boolean || nodeValue == null) {
                if (nodeValue instanceof Double) {
                    Double v = (Double) nodeValue;
                    if (v.isInfinite()) {
                        sb.append("null");
                    } else if (canBeLong((Double) nodeValue)) {
                        sb.append(((Double) nodeValue).longValue());
                    } else {
                        sb.append(nodeValue);
                    }
                } else {
                    sb.append(nodeValue);
                }
            } else {
                String v = (String) nodeValue;
                AggregateTranslator translator = new AggregateTranslator(
                        new LookupTranslator(
                                new String[][]{
                                        {"\"", "\\\""},
                                        {"\\", "\\\\"}
                                }),
                        new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())
                );
                v = translator.translate(v);
                sb.append("\"").append(v).append("\"");
            }
        } else if (node instanceof CallExpAstNode) {
            List<AstNode> nodes = ((ExpAstNode) node).getNodes();
            sb.append("[").append(node.getTypeId()).append(",");
            if (nodes.size() > 0) {
                nodeToString(sb, nodes.get(0));
                CallType type = ((CallExpAstNode) node).getCallType();
                if (type != CallType.SIMPLE) {
                    sb.append(",").append(((CallExpAstNode) node).getCallType().getId());
                }
                if (nodes.size() > 1) {
                    for (AstNode child : nodes.subList(1, nodes.size())) {
                        sb.append(",");
                        nodeToString(sb, child);
                    }
                }
            }
            sb.append("]");
        } else {
            List<AstNode> nodes = ((ExpAstNode) node).getNodes();
            sb.append("[").append(node.getTypeId());
            if (nodes.size() > 0) {
                for (AstNode child : nodes) {
                    sb.append(",");
                    nodeToString(sb, child);
                }
            }
            sb.append("]");
        }
    }
}
