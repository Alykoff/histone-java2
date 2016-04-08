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

package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringReplace extends AbstractFunction {
    public static final String NAME = "replace";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() == 1) {
            return CompletableFuture.completedFuture(args.get(0));
        }

        String str = getValue(args, 0);

        EvalNode searchNode = args.get(1);

        if (args.size() > 2) {
            EvalNode replaceNode = args.get(2);
            Matcher matcher;
            if (searchNode.getType() == HistoneType.T_REGEXP) {
                matcher = ((HistoneRegex) searchNode.getValue()).getPattern().matcher(str);
            } else {
                matcher = Pattern.compile((String) RttiUtils.callToString(context, searchNode).join().getValue()).matcher(str);
            }


            if (replaceNode.getType() != HistoneType.T_MACRO) {
                String replaceStr = (String) RttiUtils.callToString(context, replaceNode).join().getValue();
                String replaced = matcher.replaceAll(replaceStr);
                return EvalUtils.getValue(replaced);
            } else {
                int lastIndex = 0;
                StringBuilder sb = new StringBuilder();
                while (matcher.find(lastIndex)) {
                    if (matcher.start() > lastIndex) {
                        sb.append(str.substring(lastIndex, matcher.start()));
                    }
                    String found = matcher.group();
                    Context ctx = context.cloneEmpty();
                    List<EvalNode> macroArgs = Arrays.asList(replaceNode, EvalUtils.createEvalNode(found));
                    EvalNode val = MacroCall.staticExecute(ctx, macroArgs).join();
                    String macroResult = (String) RttiUtils.callToString(context, val).join().getValue();
                    sb.append(macroResult);
                    lastIndex = matcher.start() + found.length();
                }
                if (lastIndex < str.length()) {
                    sb.append(str.substring(lastIndex, str.length()));
                }

                return EvalUtils.getValue(sb.toString());
            }
        } else {
            if (searchNode.getType() == HistoneType.T_REGEXP) {
                Matcher matcher = ((HistoneRegex) searchNode.getValue()).getPattern().matcher(str);
                return EvalUtils.getValue(matcher.replaceAll(""));
            }

            String replaced = str.replaceAll((String) RttiUtils.callToString(context, searchNode).join().getValue(), "");
            return EvalUtils.getValue(replaced);
        }
    }
}
