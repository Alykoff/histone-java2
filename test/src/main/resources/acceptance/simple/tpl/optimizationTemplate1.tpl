{{var list = [['id':1, 'descr':'bla1'],['id':2, 'descr':'bla2'],['id':3, 'descr':'bla3']]}}
{{for option in list}}
{{if option.descr}}
{{var first = option.descr}}
{{var second = option.descr}}
{{option->toJSON}}
{{/if}}
{{/for}}