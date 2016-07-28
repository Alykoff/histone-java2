{{macro dateFormatStr(date, time)}}
    {{var d = date->split(' ')}}
    {{if time}}
        {{var t = date->split(' ')}}
        {{var hour = t[0]}}
        {{if hour}}
            {{hour}}
        {{/if}}
    {{/if}}
{{/macro}}
{{dateFormatStr('05.06.1898 15:33:22', true)}}