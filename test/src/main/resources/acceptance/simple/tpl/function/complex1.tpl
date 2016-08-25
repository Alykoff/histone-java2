{{macro phoneNumberFormat(number, prefix)}}
    {{var number = number->toString->replace(/[^0-9]+/g)->replace(/^[78]/)}}
    {{return [
        '+' + (prefix->toString || '7'),
        number->slice(0, 3),
        number->slice(3, 3),
        number->slice(6, 4)
    ]->join(' ')}}
{{/macro}}

{{return phoneNumberFormat('9324000993')}}