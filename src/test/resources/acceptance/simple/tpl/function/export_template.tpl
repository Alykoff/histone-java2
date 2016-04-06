hidden
{{var a = 10}}
{{macro foo}}foo{{/macro}}
{{return}}ret{{a}}{{macro bar}}bar{{/macro}}{{foo()}}{{bar()}}{{/return}}