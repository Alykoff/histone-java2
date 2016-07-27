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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.bcompiler.data.BFunction;
import ru.histone.v2.java_compiler.bcompiler.data.Template;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class BlaTemplate implements Template {

    private static final String AST_TREE = "";

    @Override
    public CompletableFuture<EvalNode> render(Context ctx) {
        CompletableFuture<StringBuilder> csb = CompletableFuture.completedFuture(new StringBuilder());

        final CompletableFuture<EvalNode> a = EvalUtils.getValue(5);

        final BFunction doSome = new BFunction() {
            @Override
            public CompletableFuture<EvalNode> apply(List<CompletableFuture<EvalNode>> args) {
//                return StdLibrary.add(a, EvalUtils.getValue(10));
                return null;
            }
        };

//        csb = StdLibrary.append(ctx, csb, "ololololol");

        final CompletableFuture<EvalNode> b = doSome.apply(Collections.emptyList());

//        csb = StdLibrary.append(ctx, csb, "ololololol1ß");

        return StdLibrary.arr("variable", b, "doSome", doSome);
    }

    @Override
    public String getStringAst() {
        return AST_TREE;
    }
}
