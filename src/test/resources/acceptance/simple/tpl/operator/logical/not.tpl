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
    [],
    [1],
    [foo: "bar"]
]}}
{{for value in values}}
!{{value->toJSON}}={{(!value)->toJSON}}
{{/for}}