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
import ru.histone.v2.parser.node.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Alexey Nevinsky
 */
public class AstJsonProcessor {

    public static ExpAstNode read(String str) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List ast = objectMapper.readValue(str, List.class);
        AstNode res = convert(ast);
        return (ExpAstNode) res;
    }

    private static AstNode convert(List list) {
        AstType type = AstType.fromId((int) list.get(0));
        final ExpAstNode expAstNode;
        if (type == AstType.AST_CALL) {
            expAstNode = new CallExpAstNode(CallType.SIMPLE);
            Object first = list.get(1);
            final AstNode n;
            if (first instanceof List) {
                n = convert((List) first);
            } else if (first instanceof Integer) {
                n = new LongAstNode((Integer) first);
            } else if (first instanceof Long) {
                n = new LongAstNode((Long) first);
            } else if (first instanceof Double) {
                n = new DoubleAstNode((Double) first);
            } else if (first instanceof Boolean) {
                n = new BooleanAstNode((Boolean) first);
            } else {
                n = new StringAstNode((String) first);
            }
            expAstNode.add(n);
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
                    Object o = list.get(i);
                    final AstNode node;
                    if (o instanceof List) {
                        node = convert((List) o);
                    } else if (o instanceof Integer) {
                        node = new LongAstNode((Integer) o);
                    } else if (o instanceof Long) {
                        node = new LongAstNode((Long) o);
                    } else if (o instanceof Double) {
                        node = new DoubleAstNode((Double) o);
                    } else if (o instanceof Boolean) {
                        node = new BooleanAstNode((Boolean) o);
                    } else {
                        node = new StringAstNode((String) o);
                    }
                    expAstNode.add(node);
                }
            }
        } else {
            expAstNode = new ExpAstNode(type);
            for (int i = 1; i < list.size(); i++) {
                Object o = list.get(i);
                final AstNode node;
                if (o instanceof List) {
                    node = convert((List) o);
                } else if (o instanceof Integer) {
                    node = new LongAstNode((Integer) o);
                } else if (o instanceof Long) {
                    node = new LongAstNode((Long) o);
                } else if (o instanceof Double) {
                    node = new DoubleAstNode((Double) o);
                } else if (o instanceof Boolean) {
                    node = new BooleanAstNode((Boolean) o);
                } else {
                    node = new StringAstNode((String) o);
                }
                expAstNode.add(node);
            }
        }
        return expAstNode;
    }
}
