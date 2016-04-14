{{var items = [
    [value: 10, type: 'A'],
    [value: 20, type: 'A'],
    [value: 30, type: 'C'],
    [value: 40, type: 'B'],
    [value: 50, type: 'B']
]}}


foo = {{items->group(item => item.type)->toJSON}}