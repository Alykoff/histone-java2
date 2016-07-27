{{var a = 5}}

{{macro doSome()}}
    {{return a + 10}}
{{/macro}}

ololololo

{{var b = doSome()}}

ololololol

{{return [
    variable: b,
    doSome: doSome
]}}