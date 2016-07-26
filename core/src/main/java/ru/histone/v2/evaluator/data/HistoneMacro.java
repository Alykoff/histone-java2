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

package ru.histone.v2.evaluator.data;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.parser.node.AstNode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gali Alykoff
 */
public class HistoneMacro implements Cloneable {
    public static final boolean MACRO_IS_WRAPPED_GLOBAL_FUNC_FLAG = true;
    public static final boolean MACRO_IS_NOT_WRAPPED_GLOBAL_FUNC_FLAG = false;
    private final WrappingType wrappingType;
    private AstNode body;
    private EvalNode result = null;
    private Context context;
    private List<String> args = new ArrayList<>();
    private Map<String, CompletableFuture<EvalNode>> defaultValues = new LinkedHashMap<>();
    private List<EvalNode> bindArgs = new ArrayList<>();

    public HistoneMacro(EvalNode result) {
        this.result = result;
        this.wrappingType = WrappingType.NONE;
    }

    public HistoneMacro(
            List<String> args,
            AstNode body,
            Context context,
            Map<String, CompletableFuture<EvalNode>> defaultValues,
            WrappingType wrappingType
    ) {
        this.args.addAll(args);
        this.body = body;
        this.context = context;
        this.defaultValues = defaultValues;
        this.wrappingType = wrappingType;
    }

    public HistoneMacro(
            List<String> args,
            AstNode body,
            Context context,
            List<EvalNode> bindArgs,
            Map<String, CompletableFuture<EvalNode>> defaultValues,
            WrappingType wrappingType
    ) {
        this.args.addAll(args);
        this.body = body;
        this.context = context;
        this.bindArgs = bindArgs;
        this.defaultValues = defaultValues;
        this.wrappingType = wrappingType;
    }

    public HistoneMacro(
            List<String> args,
            AstNode body,
            Context context,
            List<EvalNode> bindArgs,
            Map<String, CompletableFuture<EvalNode>> defaultValues,
            EvalNode result,
            WrappingType wrappingType
    ) {
        this(args, body, context, bindArgs, defaultValues, wrappingType);
        this.result = result;
    }

    public void addBindArgs(List<EvalNode> bindArgs) {
        if (bindArgs.size() > 0) {
            List<EvalNode> list = new ArrayList<>();
            list.addAll(bindArgs);
            list.addAll(this.bindArgs);
            this.bindArgs = list;
        } else {
            this.bindArgs.addAll(bindArgs);
        }
    }

    @Override
    public HistoneMacro clone() {
        final ArrayList<String> copyArgs = new ArrayList<>(this.args.size());
        copyArgs.addAll(this.args);
        final Map<String, CompletableFuture<EvalNode>> copyValues = new LinkedHashMap<>(this.defaultValues);

        final ArrayList<EvalNode> copyBindArgs = new ArrayList<>(this.bindArgs.size());
        copyBindArgs.addAll(this.bindArgs);

        if (result != null) {
            return new HistoneMacro(result);
        }

        return new HistoneMacro(
                copyArgs,
                this.body,
                this.context.clone(),
                copyBindArgs,
                copyValues,
                this.wrappingType
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoneMacro that = (HistoneMacro) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(context, that.context) &&
                Objects.equals(args, that.args) &&
                Objects.equals(bindArgs, that.bindArgs) &&
                Objects.equals(defaultValues, that.defaultValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, context, args, bindArgs, defaultValues);
    }

    @Override
    public String toString() {
        return "{\"HistoneMacro\": {" +
                "\"body\":" + body +
                ", \"context\":" + context +
                ", \"args\":[" + args + "]\"" +
                ", \"bindArgs\":[" + bindArgs + "]\"}}";
    }

    public Map<String, CompletableFuture<EvalNode>> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map<String, CompletableFuture<EvalNode>> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public AstNode getBody() {
        return body;
    }

    public void setBody(AstNode body) {
        this.body = body;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<EvalNode> getBindArgs() {
        return bindArgs;
    }

    public void setBindArgs(List<EvalNode> bindArgs) {
        this.bindArgs = bindArgs;
    }

    public EvalNode getResult() {
        return result;
    }

    public WrappingType getWrappingType() {
        return wrappingType;
    }

    public enum WrappingType {
        NONE, GLOBAL, VALUE;
    }
}
