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

package ru.histone.v2.parser.tokenizer;

import ru.histone.v2.utils.Tuple;

import java.util.*;

/**
 * Class used for wrapping generic tokenizer. If you want to add tokens for ignore - you must to create wrapper base on
 * tokenizer or wrapper and add tokens.
 *
 * @author Alexey Nevinsky
 */
public class TokenizerWrapper {
    private Tokenizer tokenizer;
    private List<Integer> ignored;
    private boolean isFor = false;
    private boolean isVar = false;
    private boolean isReturn = false;
    private List<String> labels = new ArrayList<>();

    private Deque<Scope> scopes = new ArrayDeque<>();
    private boolean isMacroScope = false;

    /**
     * Construct wrapper without tokens for ignoring
     *
     * @param tokenizer for wrapping
     */
    public TokenizerWrapper(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.ignored = Collections.emptyList();
    }

    /**
     * Construct wrapper based on another wrapper. Ignored tokens list will be empty.
     *
     * @param wrapper for wrapping
     */
    public TokenizerWrapper(TokenizerWrapper wrapper) {
        this(wrapper, Collections.emptyList());
    }

    /**
     * Construct wrapper based on another wrapper with a list of ignored tokens.
     *
     * @param wrapper for wrapping
     * @param ignored list of ignored tokens ids
     */
    public TokenizerWrapper(TokenizerWrapper wrapper, List<Integer> ignored) {
        this.tokenizer = wrapper.tokenizer;
        this.isFor = wrapper.isFor;
        this.labels = wrapper.labels;
        this.ignored = ignored;
        this.scopes = wrapper.scopes;
        this.isMacroScope = wrapper.isMacroScope;
    }


    /**
     * @return base uri from tokenizer
     */
    public String getBaseURI() {
        return tokenizer.getBaseURI();
    }

    public TokenizerWrapper setBaseURI(String baseURI) {
        this.tokenizer.setBaseURI(baseURI);
        return this;
    }

    /**
     * @param selector
     * @return
     */
    public TokenizerResult next(Integer... selector) {
        return tokenizer.next(ignored, selector);
    }

    public TokenizerResult next(Tokens token) {
        return next(token.getId());
    }

    public TokenizerResult test(Integer... selector) {
        return tokenizer.test(ignored, selector);
    }

    public int getLineNumber(long index) {
        return tokenizer.getLineNumber(index);
    }

    public void startMacro() {
        isMacroScope = true;
    }

    public void endMacro() {
        isMacroScope = false;
    }

    public void enter() {
        scopes.addFirst(new Scope());
    }

    public void leave() {
        scopes.pop();
    }

    public TokenizerWrapper getCleanWrapper() {
        final TokenizerWrapper newWrapper = new TokenizerWrapper(tokenizer);
        newWrapper.ignored = new ArrayList<>();
        newWrapper.isFor = false;
        newWrapper.isVar = false;
        newWrapper.isReturn = false;
        newWrapper.labels = new ArrayList<>();
        newWrapper.scopes = new ArrayDeque<>();
        newWrapper.scopes.add(new Scope());
        newWrapper.isMacroScope = false;
        return newWrapper;
    }

    public Long getVarName(String varName) {
        Scope scope = scopes.peek();

        Var var = scope.vars.get(varName);
        if (var == null) {
            var = new Var();
            var.addName(scope.counter++);
            scope.vars.put(varName, var);
        } else if (var.used) {
            var.addName(scope.counter++);
        }

        return var.lastName();
    }

    public Tuple<Long, Long> getRefPair(final String name) {
        Iterator<Scope> iterator = scopes.iterator();
        long i = 0;
        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            Var var = scope.vars.get(name);
            if (var != null) {
                var.used = true;
                return new Tuple<>(i, var.lastName());
            }
            i++;
        }
        return null;
    }

    public static class Scope {
        private long counter = 0;
        private Map<String, Var> vars = new HashMap<>();
    }

    public static class Var {
        private boolean used = false;
        private List<Long> names = new ArrayList<>();

        void addName(Long name) {
            names.add(name);
        }

        Long lastName() {
            return names.get(names.size() - 1);
        }
    }

    public boolean isFor() {
        return isFor;
    }

    public void setFor(boolean aFor) {
        this.isFor = aFor;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }

    public boolean isVar() {
        return isVar;
    }

    public void setVar(boolean var) {
        isVar = var;
    }

    public void addLabel(String labelString) {
        labels.add(labelString);
    }

    public void removeLabel(String labelString) {
        labels.remove(labelString);
    }

    public boolean labelExists(String labelString) {
        return labels.contains(labelString);
    }

}
