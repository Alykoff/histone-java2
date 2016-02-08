/**
 * Copyright 2013 MegaFon
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.histone.v2.exceptions;

/**
 * This exception is used to provide detailed error information from custom global function to Histone evaluator
 */
public class FunctionExecutionException extends RuntimeException {
    public FunctionExecutionException(String message) {
        super(message);
    }

    public FunctionExecutionException(String template, Object... args) {
        super(String.format(template, args));
    }

    public FunctionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionExecutionException(Throwable cause) {
        super(cause);
    }
}
