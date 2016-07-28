{{macro insertCSS(cssFiles)}}{{cssFiles -> toJSON}}{{/macro}}
{{return [
    insertCSS:insertCSS
]}}