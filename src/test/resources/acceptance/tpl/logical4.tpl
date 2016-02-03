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
    [foo: "bar"]
]}}
{{for op1 in values}}
{{for op2 in range(0, self.last)}}
{{var op2 = values[op2]}}{{op1->toJSON}}^{{op2->toJSON}}={{(op1 ^ op2)->toJSON}}
{{/for}}
{{/for}}