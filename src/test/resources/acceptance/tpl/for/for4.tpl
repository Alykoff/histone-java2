{{* for loop without key:value part *}}
{{for in [1, 2, 3]}}
    <div>{{self.key}} = {{self.value}}</div>
{{/for}}