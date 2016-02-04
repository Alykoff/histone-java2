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

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Created by gali.alykoff on 22/01/16.
 */
public interface Irtti {
    HistoneType getType(EvalNode node);

    Function getFunc(HistoneType type, String funcName);

    void register(HistoneType type, String funcName, Function func);

    void unregistered(HistoneType type, String funcName);

    CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, HistoneType type, String funcName, List<EvalNode> args);

    CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, EvalNode node, String funcName, List<EvalNode> args);
}
