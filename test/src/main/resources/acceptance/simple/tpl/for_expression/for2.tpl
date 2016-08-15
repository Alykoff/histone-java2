{{* simple loop *}}
{{for value in collection}}
    {{value}}
{{/for}}

{{* for loop with key and value *}}
{{for key:value in collection}}
    {{key}} = {{value}}
{{/for}}