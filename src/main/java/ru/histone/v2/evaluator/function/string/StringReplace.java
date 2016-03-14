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

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.RegexEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        checkMinArgsLength(args, 3);

        String str = getValue(args, 0);

        EvalNode searchNode = args.get(1);
        EvalNode replaceNode = args.get(2);

        if (searchNode.getType() == HistoneType.T_STRING) {
            String searchStr = EvalUtils.escape((String) searchNode.getValue());
            final Pattern pattern = Pattern.compile(searchStr);
            searchNode = new RegexEvalNode(new HistoneRegex(true, pattern));
        }

        if (searchNode.getType() != HistoneType.T_REGEXP) {
            return EvalUtils.getValue(str);
        }

        if (replaceNode.getType() != HistoneType.T_MACRO) {
            String replaceStr = (String) RttiUtils.callToString(context, replaceNode).join().getValue();
            String replaced = str.replace(((HistoneRegex) searchNode.getValue()).getPattern().pattern(), replaceStr);
            return EvalUtils.getValue(replaced);
        }

        throw new NotImplementedException("Yeah, we not implemented string replace function with macro");
//        var result = '', lastPos = 0;
//        Utils_loopAsync(function(next) {
//
//            var match = search.exec(self);
//
//            if (match) {
//
//                if (lastPos < match.index)
//                    result += self.slice(lastPos, match.index);
//
//                lastPos = match.index + match[0].length;
//
//                replace['call'] ([match[0]], scope, function(replace) {
//                    result += replace;
//                    next();
//                });
//
//
//            } else next(true);
//
//        },function() {
//            ret(result);
//        });
    }
}
