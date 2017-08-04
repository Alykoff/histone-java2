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
package ru.histone.v2.evaluator.resource.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStreamResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.utils.BOMInputStream;
import ru.histone.v2.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class FileLoader implements Loader {

    private final static Logger LOG = LoggerFactory.getLogger(FileLoader.class);
    public static final String FILE_SCHEME = "file";

    @Override
    public CompletableFuture<Resource> loadResource(Context ctx, URI url, Map<String, Object> params) {
        InputStream stream = readFile(url);
        BOMInputStream bomStream = IOUtils.readBomStream(url, stream);
        Resource res = new HistoneStreamResource(bomStream, url.toString(), ContentType.TEXT.getId());
        return CompletableFuture.completedFuture(res);
    }

    @Override
    public String getScheme() {
        return FILE_SCHEME;
    }

    protected InputStream readFile(URI location) {
        InputStream stream;
        File file = new File(location);
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            LOG.error(String.format("Can't read file '%s'", location.toString()));
            throw new RuntimeException(new FileNotFoundException(String.format("Can't read file '%s'", location.toString())));
        }
        return stream;
    }

}
