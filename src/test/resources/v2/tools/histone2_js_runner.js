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

'use strict';
const Histone = require('histone');
const FS = require('fs');
const DEFAULT_SETTINGS = {
    expression: '',
    isReturnNodes: false,
    isHelp: true,
    info: ''
};
const FILE_ENCODING = 'utf8';

var settings = getSettings();
var template = Histone(settings.expression);

if (settings.isHelp) {
    console.log(settings.info);
}

if (settings.isReturnNodes) {
    console.log(JSON.stringify(template.b));
} else {
    template.render(console.log);
}

function getSettings() {
    const INFO = 'Options:\n' +
        '\t--file=index.tpl\tHistone template in file\n' +
        '\t--tpl="{{2 * 2}}"\tHistone template\n' +
        '\t-returnNode, -n\t\tReturn Nodes\n' +
        '\t-help, -about\t\tHelp info\n';
    const TPL_ARG_FLAG = '--tpl=';
    const FILE_ARG_FLAG = '--file=';
    const RETURN_NODE_ARG_FLAG = '-returnNode';
    const RETURN_NODE_SHORT_ARG_FLAG = '-n';
    const HELP_ARG_FLAG = '-help';
    const ABOUT_ARG_FLAG = '-about';

    var settings = Object.assign({}, DEFAULT_SETTINGS);
    settings.info = INFO;
    var usingHelpFlag = false;
    for (var key in process.argv) {
        var value = process.argv[key];
        if (value.startsWith(TPL_ARG_FLAG)) {
            var tplContent = value.substring(TPL_ARG_FLAG.length, value.length);
            var isQuotes = tplContent.startsWith('"') && tplContent.endsWith('"');
            if (isQuotes) {
                tplContent = tplContent.substring(1, tplContent.length);
                tplContent = tplContent.substring(0, tplContent.length - 1);
            }
            settings.expression = tplContent;
            settings.isHelp = false;
        } else if (value.startsWith(FILE_ARG_FLAG)) {
            var path = value.substring(FILE_ARG_FLAG.length, value.length);
            settings.expression = FS.readFileSync(path, FILE_ENCODING, (err, data) => {
                if (err) {
                    throw Error(err);
                }
            }).toString();
            settings.isHelp = false;
        } else if (value === RETURN_NODE_ARG_FLAG || value === RETURN_NODE_SHORT_ARG_FLAG) {
            settings.isReturnNodes = true;
            settings.isHelp = false;
        } else if (value === HELP_ARG_FLAG || value === ABOUT_ARG_FLAG) {
            usingHelpFlag = true;
        }
    };
    settings.isHelp = settings.isHelp || usingHelpFlag;
    return settings;
}