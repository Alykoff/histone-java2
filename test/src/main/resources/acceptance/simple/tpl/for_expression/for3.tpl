{{* for loop with using self.index и self.last *}}
{{var collection = [1, 2, 3, 4]}}
{{for key:value in collection}}
    << {{self.index}}, {{self.last}} >>
{{/for}}