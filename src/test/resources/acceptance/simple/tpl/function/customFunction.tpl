{{var x = getRand}}

{{macro foo}}
    RETURN
    {{if true}}
        {{return x}}
    {{/if}}
{{/macro}}

{{var x = 900}}

{{return [
    foo: foo,
    x: f,
    thisParam: this,
    a:this.a,
    b:this.b,
    c:this.c()
]}}