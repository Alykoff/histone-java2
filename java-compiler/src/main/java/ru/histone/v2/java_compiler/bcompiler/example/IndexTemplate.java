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

package ru.histone.v2.java_compiler.bcompiler.example;

import ru.histone.v2.java_compiler.bcompiler.data.BContext;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.java_compiler.bcompiler.data.Value;

import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class IndexTemplate implements Template {

    private static final String AST_TREE = "";

    @Override
    public CompletableFuture<Value> render(BContext ctx) {
//        CompletableFuture<Object> ui = StdLibrary.require(CompletableFuture.completedFuture("bla.tpl"), ctx);
//
//        StringBuilder sb = new StringBuilder();
//
//        return ui
//                .thenCompose(uiObject -> sb.append(StdLibrary.doString(StdLibrary.doCall(uiObject))));
        return null;
    }

    @Override
    public String getStringAst() {
        return AST_TREE;
    }
}
