hidden

{{var a = 10}}
{{macro foo}}foo{{/macro}}
{{return [
    a: a,
    foo: foo
]}}