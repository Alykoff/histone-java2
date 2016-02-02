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
package ru.histone.evaluator;

import ru.histone.evaluator.functions.global.GlobalFunctionsManager;
import ru.histone.evaluator.functions.node.NodeFunctionsManager;
import ru.histone.evaluator.nodes.GlobalObjectNode;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.OldParser;
import ru.histone.resourceloaders.ResourceLoader;

/**
 * Bootstrap class for constructing evaluator
 */
public class EvaluatorBootstrap {
    private OldParser parser;
    private NodeFactory nodeFactory;
    private ResourceLoader resourceLoader;
    private GlobalFunctionsManager globalFunctionsManager;
    private NodeFunctionsManager nodeFunctionsManager;
    private GlobalObjectNode global;

    public OldParser getParser() {
        return parser;
    }

    public void setParser(OldParser parser) {
        this.parser = parser;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

	public GlobalFunctionsManager getGlobalFunctionsManager() {
        return globalFunctionsManager;
    }

    public void setGlobalFunctionsManager(GlobalFunctionsManager globalFunctionsManager) {
        this.globalFunctionsManager = globalFunctionsManager;
    }

    public NodeFunctionsManager getNodeFunctionsManager() {
        return nodeFunctionsManager;
    }

    public void setNodeFunctionsManager(NodeFunctionsManager nodeFunctionsManager) {
        this.nodeFunctionsManager = nodeFunctionsManager;
    }

    public GlobalObjectNode getGlobal() {
        return global;
    }

    public void setGlobal(GlobalObjectNode global) {
        this.global = global;
    }
}
