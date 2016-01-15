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