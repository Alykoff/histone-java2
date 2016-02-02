{{* values used for testing *}}
{{var values = [
    undefined,
    null,
    true,
    false,
    0,
    10,
    "",
    "string",
    [],
    [1],
    [foo: 'bar']
]}}

{{* displays results of applying operator "not" for the values of every data type *}}
{{for value in values}}
    <div>
        <strong>!{{value->toJSON}}</strong>
        <span> = {{(!value)->toJSON}}</span>
    </div>
{{/for}}

{{* displays results of applying operator "or" for the values of every data type *}}
{{for op1 in values}}
    {{for op2 in range(0, self.last)}}
        {{var op2 = values[op2]}}
        <div>
            <strong>{{op1->toJSON}} || {{op2->toJSON}}</strong>
            <span> = {{(op1 || op2)->toJSON}}</span>
        </div>
    {{/for}}
{{/for}}

{{* displays results of applying operator "and" for the values of every data type *}}
{{for op1 in values}}
    {{for op2 in range(0, self.last)}}
        {{var op2 = values[op2]}}
        <div>
            <strong>{{op1->toJSON}} && {{op2->toJSON}}</strong>
            <span> = {{(op1 && op2)->toJSON}}</span>
        </div>
    {{/for}}
{{/for}}