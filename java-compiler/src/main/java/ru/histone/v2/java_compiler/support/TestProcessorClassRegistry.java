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

package ru.histone.v2.java_compiler.support;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.parser.node.StringAstNode;
import ru.histone.v2.utils.PathUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Nevinsky
 */
public class TestProcessorClassRegistry {

    private Map<Path, String> pathsMap = new ConcurrentHashMap<>();

    private Path basePath;

    public TestProcessorClassRegistry(Path basePath) {
        this.basePath = basePath;
    }

    public void processAst(Path tplPath, AstNode node) {
        if (node.hasValue()) {
            return;
        }

        if (checkCallIsRequire((ExpAstNode) node)) {
            processRequireNode(tplPath, (ExpAstNode) node);
        } else {
            for (AstNode n : ((ExpAstNode) node).getNodes()) {
                processAst(tplPath, n);
            }
        }
    }

    private void processRequireNode(Path tplPath, ExpAstNode node) {
        String requirePath = ((StringAstNode) node.getNode(2)).getValue();
        Path p = Paths.get(PathUtils.resolveUrl(requirePath, tplPath.toString()));
        Path relativeClassPath = basePath.relativize(p);

        String name = "Template" + relativeClassPath.getFileName().toString().replace(".", "");
        String className = "class:" + relativeClassPath.getParent().toString().replace("/", ".") + "." + name;
        pathsMap.putIfAbsent(relativeClassPath, className);

        node.getNodes().set(2, new StringAstNode(className));
    }

    private boolean checkCallIsRequire(ExpAstNode node) {
        if (node.size() < 3) {
            return false;
        }

        AstNode fNameNode = node.getNode(1);
        return fNameNode.hasValue()
                && fNameNode instanceof StringAstNode
                && ((StringAstNode) fNameNode).getValue() != null
                && ((StringAstNode) fNameNode).getValue().equals("require");
    }

    public String getClassName(Path path) {
        return null;
    }
}
