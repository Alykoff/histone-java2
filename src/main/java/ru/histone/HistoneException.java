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
package ru.histone;

/**
 * Histone general exception class<br/>
 */
public class HistoneException extends RuntimeException {

    public HistoneException() {
    }

    public HistoneException(String message) {
        super(message);
    }

    public HistoneException(Throwable cause) {
        super(cause);
    }

    public HistoneException(String message, Throwable cause) {
        super(message, cause);
    }

}
