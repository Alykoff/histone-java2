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

package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.rtti.HistoneType.*;

/**
 * Created by gali.alykoff on 22/01/16.
 */
public interface Irtti {
    static HistoneType getType(EvalNode node) {
        if (node == null) {
            throw new NullPointerException();
        }
        if (node instanceof NullEvalNode) {
            return T_NULL;
        } else if (node instanceof FloatEvalNode) {
            final Float valueFloat = ((FloatEvalNode) node).getValue();
            if (valueFloat == null || Float.isNaN(valueFloat) || !Float.isFinite(valueFloat)) {
                return T_UNDEFINED;
            }
            return T_NUMBER;
        } else if (node instanceof LongEvalNode) {
            return T_NUMBER;
        } else if (node instanceof MapEvalNode) {
            return T_ARRAY;
        } else if (node instanceof BooleanEvalNode) {
            return T_BOOLEAN;
        } else if (node instanceof StringEvalNode) {
            return T_STRING;
        } else if (node instanceof EmptyEvalNode) {
            return T_UNDEFINED;
        } else if (node instanceof RegexEvalNode) {
            return T_REGEXP;
        } else if (node instanceof MacroEvalNode) {
            return T_MACRO;
        } else if (node instanceof GlobalEvalNode) {
            return T_GLOBAL;
        }

        throw new NotImplementedException(node.toString());
    }

    Function getFunc(HistoneType type, String funcName);

    void register(HistoneType type, String funcName, Function func);

    void unregistered(HistoneType type, String funcName);

    CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, HistoneType type, String funcName, List<EvalNode> args);

    CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, EvalNode node, String funcName, List<EvalNode> args);
}
