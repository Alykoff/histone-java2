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
package ru.histone.evaluator.nodes;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Special class, used in AST optimizer module
 */
public class AstNode extends Node {
    private JsonNode value;

    public static AstNode create(JsonNode element) {
        return new AstNode(element);
    }

    protected AstNode(JsonNode value) {
        super(null);
        this.value = value;
    }

    public JsonNode getValue() {
        return value;
    }

    @Override
    public BooleanHistoneNode getAsBoolean() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public NumberHistoneNode getAsNumber() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public StringHistoneNode getAsString() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public ObjectHistoneNode getAsObject() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public JsonNode getAsJsonNode() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_add(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_mul(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_div(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_mod(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_negate() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_sub(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_not() {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_equal(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_greaterThan(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_greaterOrEqual(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_lessThan(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }

    @Override
    public Node oper_lessOrEqual(Node right) {
        throw new UnsupportedOperationException("AstNode doesn't support this method");
    }
}
