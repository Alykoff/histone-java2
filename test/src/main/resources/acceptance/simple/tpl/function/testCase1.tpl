{{macro listFormat(array)}}{{
    for x in array
}}{{
    self.index ?
    self.index = self.last ?
    ' и ' : ', '
}}"{{x}}"{{/for}}{{/macro}}

{{return [
    listFormat: listFormat
]}}