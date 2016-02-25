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

import java.util.Collections;
import java.util.List;

/**
 * Class used for wrapping generic tokenizer. If you want to add tokens for ignore - you must to create wrapper base on
 * tokenizer or wrapper and add tokens.
 *
 * @author alexey.nevinsky
 */
public class TokenizerWrapper {
    private Tokenizer tokenizer;
    private List<Integer> ignored;

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
        this.ignored = ignored;
    }

    /**
     * @return base uri from tokenizer
     */
    public String getBaseURI() {
        return tokenizer.getBaseURI();
    }

    /**
     *
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
}
