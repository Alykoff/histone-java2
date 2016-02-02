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
package ru.histone.tokenizer;

import java.util.Collection;
import java.util.HashSet;

/**
 * OldToken definition holder
 */
public class TokenDef {
    /**
     * OldToken type
     */
    private TokenType type;

    /**
     * OldToken regexp
     */
    private String regexp;

    /**
     * Context where token appears
     */
    private Collection<TokenContext> contexts = new HashSet<TokenContext>();

    /**
     * OldToken kind
     */
    private TokenKind kind;

    /**
     * Context to switch to if this token appears in input sequence
     */
    private TokenTransitionCallback transitionCallback;

    public TokenDef(TokenType type, TokenKind kind, TokenContext context, String regexp) {
        this(type, kind, context, null, regexp);
    }

    public TokenDef(TokenType type, TokenKind kind, TokenContext context, TokenContext transition, String regexp) {
        this.type = type;
        this.kind = kind;
        this.contexts.add(context);
        this.regexp = regexp;
        this.transitionCallback = new DefaultTokenTransitionCallback(transition);
    }

    public TokenDef(TokenType type, TokenKind kind, Collection<TokenContext> contexts, TokenTransitionCallback transitionCallback, String regexp) {
        if(transitionCallback == null){
            throw new RuntimeException("Error, transitionCallback can't be null");
        }
        this.type = type;
        this.kind = kind;
        this.contexts.addAll(contexts);
        this.regexp = regexp;
        this.transitionCallback = transitionCallback;
    }

    public TokenType getType() {
        return type;
    }

    public String getRegexp() {
        return regexp;
    }

    public Collection<TokenContext> getContexts() {
        return contexts;
    }

    public TokenKind getKind() {
        return kind;
    }

    public TokenTransitionCallback getTransitionCallback() {
        return transitionCallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenDef tokenDef = (TokenDef) o;

        if (contexts != tokenDef.contexts) return false;
        if (kind != tokenDef.kind) return false;
        if (!regexp.equals(tokenDef.regexp)) return false;
        if (transitionCallback != tokenDef.transitionCallback) return false;
        if (type != tokenDef.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + regexp.hashCode();
        result = 31 * result + contexts.hashCode();
        result = 31 * result + kind.hashCode();
        result = 31 * result + (transitionCallback != null ? transitionCallback.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TokenDef(");
        sb.append("type=" + type + ", ");
        sb.append("contexts.size=" + contexts.size() + ", ");
        sb.append("transitionCallback=" + transitionCallback);
        sb.append(")");

        return sb.toString();
    }

}
