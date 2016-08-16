{{* simple loop *}}
{{for value in this.collection}}
    {{value}}
{{/for}}

{{* for loop with key and value *}}
{{for key:value in this.collection}}
    {{key}} = {{value}}
{{/for}}