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

import ru.histone.tokenizer.Tokens;

import java.util.Collections;
import java.util.List;

/**
 * Created by inv3r on 15/01/16.
 */
public class TokenizerWrapper {
    private Tokenizer tokenizer;
    private List<Integer> ignored;

    public TokenizerWrapper(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.ignored = Collections.emptyList();
    }

    public TokenizerWrapper(TokenizerWrapper wrapper) {
        this(wrapper, Collections.emptyList());
    }

    public TokenizerWrapper(TokenizerWrapper wrapper, List<Integer> ignored) {
        this.tokenizer = wrapper.tokenizer;
        this.ignored = ignored;
    }

    public String getBaseURI() {
        return tokenizer.getBaseURI();
    }

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
}
