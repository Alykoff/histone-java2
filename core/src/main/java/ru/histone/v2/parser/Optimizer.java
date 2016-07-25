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

package ru.histone.v2.parser;

import ru.histone.v2.parser.node.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Nevinsky
 */
public class Optimizer {
    public AstNode mergeStrings(AstNode rawNode) {
        if (rawNode.hasValue()) {
            return rawNode;
        }
        final ExpAstNode node = (ExpAstNode) rawNode;
        final List<AstNode> innerNodes = node.getNodes();
        if (node.getType() == AstType.AST_NODELIST || node.getType() == AstType.AST_NODES) {
            final List<AstNode> newInnerNodes = getNewNodes(innerNodes);
            node.rewriteNodes(newInnerNodes);
        } else {
            innerNodes.forEach(this::mergeStrings);
        }

        return node;
    }

    private List<AstNode> getNewNodes(List<AstNode> innerNodes) {
        final StringBuilder accStringNode = new StringBuilder();
        final List<AstNode> res = new ArrayList<>(innerNodes.size());

        for (int i = 0; i < innerNodes.size(); i++) {
            AstNode current = innerNodes.get(i);
            if (current.getType() == AstType.AST_NOP) {
                continue;
            }
            boolean isStringLeaf = current.hasValue();

            if (isStringLeaf) {
                accStringNode.append(((ValueNode) current).getValue());
            }

            if (!isStringLeaf && accStringNode.length() > 0) {
                res.add(new StringAstNode(accStringNode.toString()));
                accStringNode.setLength(0);
            }

            if (i == innerNodes.size() - 1 && accStringNode.length() > 0) {
                res.add(new StringAstNode(accStringNode.toString()));
                accStringNode.setLength(0);
            }

            if (!isStringLeaf) {
                current = mergeStrings(current);
                res.add(current);
            }
        }
        if (accStringNode.length() > 0) {
            res.add(new StringAstNode(accStringNode.toString()));
        }
        return res;
    }
}
