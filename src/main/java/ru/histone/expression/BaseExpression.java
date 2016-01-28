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

package ru.histone.expression;

import ru.histone.tokenizer.Tokens;
import ru.histone.utils.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.histone.tokenizer.Tokens.T_TOKEN;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public class BaseExpression implements Expression {

    private final List<Integer> ids;
    private final String expression;

    public BaseExpression(String expression, Integer... ids) {
        if (ids != null && ids.length > 0) {
            this.ids = Arrays.asList(ids);
        } else {
            this.ids = Arrays.asList(T_TOKEN.getId());
        }
        Assert.notNull(expression);
        this.expression = expression;
    }

    public BaseExpression(String expression, Tokens... tokens) {
        this.ids = Arrays.stream(tokens).map(Tokens::getId).collect(Collectors.toList());
        Assert.notNull(expression);
        this.expression = expression;
    }

    @Override
    public List<Integer> getIds() {
        return ids;
    }

    @Override
    public String getExpression() {
        return "(" + expression + ")";
    }
}
