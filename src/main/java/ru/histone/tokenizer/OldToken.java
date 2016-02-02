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

/**
 * OldToken holder object<br/>
 * Stores all information about token: it's type, position in input sequence and it's content (if any)
 */
public class OldToken {
    public static final OldToken EOF_TOKEN = new OldToken(TokenType.T_EOF, -1, "EOF");

    /**
     * OldToken content. Not all tokens has their content.
     */
    private String content;

    /**
     * OldToken type
     */
    private TokenType type;

    /**
     * OldToken position in input sequence
     */
    private int pos;

    public OldToken(TokenType type, int pos, String content) {
        super();
        this.type = type;
        this.content = content;
        this.pos = pos;
    }

    public OldToken(OldToken original) {
        this(original.getType(), original.getPos(), original.getContent());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("OldToken(");
        sb.append("type=" + type + ", ");
        sb.append("content=" + ((content != null) ? content.replaceAll("\n", "\\\\n") : content));
        sb.append(")");

        return sb.toString();
    }
}
