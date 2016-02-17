{{for in [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]}}
    <div style="color: {{self.index != self.last ? (
        self.index % 2 ? "red" : "black"
    ) : "blue"}}">{{self->toJSON}}</div>
{{/for}}