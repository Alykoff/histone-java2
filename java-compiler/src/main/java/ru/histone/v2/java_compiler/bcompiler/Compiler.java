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

package ru.histone.v2.java_compiler.bcompiler;

import ru.histone.v2.parser.node.AstNode;

/**
 * @author Alexey Nevinsky
 */
public class Compiler {

    /**
     * Method compiles AST-tree to java-bytecode.
     *
     * @param root root of AST-tree
     * @return byte array of java-bytecode
     */
    public byte[] compile(AstNode root) {
        return new byte[0];
    }


    public boolean isSimpleTemplate(AstNode node) {
        return false;
    }

}
