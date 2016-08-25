{{* for loop with {{elseif}} and {{else}} blocks *}}
{{var collection = null}}
{{for key:value in collection}}
    {{value}}
{{elseif this.some_condition}}
    collection is empty and some_condition
{{else}}
    collection is empty
{{/for}}