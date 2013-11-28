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
package ru.histone.spring.stubs;

import ru.histone.evaluator.functions.global.GlobalFunction;
import ru.histone.evaluator.functions.global.GlobalFunctionExecutionException;
import ru.histone.evaluator.nodes.Node;
import ru.histone.evaluator.nodes.NodeFactory;

/**
 * @author P.Salnikov <p.salnikov@gmail.com>
 */
public class GlobalUserFunction_A extends GlobalFunction{
    protected GlobalUserFunction_A(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    @Override
    public String getName() {
        return "globalA";
    }

    @Override
    public Node execute(Node... args) throws GlobalFunctionExecutionException {
        return getNodeFactory().string("global_a_result");
    }
}
