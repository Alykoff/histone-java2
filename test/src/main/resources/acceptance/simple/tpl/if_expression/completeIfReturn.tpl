{{macro doSome(a)}}
    {{if a = 10}}
        {{return "if"}}
    {{else}}
        {{return "else"}}
    {{/if}}
{{/macro}}
{{doSome(10)}}{{doSome(1)}}