{{var a = 5}}

{{macro doSome()}}
    {{return a + 10}}
{{/macro}}

{{return [
    doSome: doSome
]}}