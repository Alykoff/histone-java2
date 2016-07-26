{{var PIPE_PREFIX = 'expectedString'}}

{{macro svg(href, class)}}
	{{if self.arguments}}{{elseif x}}{{/if}}
{{/macro}}

{{macro formatNumberText(count, x)}}
	{{if x}}
		{{var a=8}}
	{{else}}
		{{return 3}}
	{{/if}}
{{/macro}}

{{macro getPrefix}}
    {{var url = PIPE_PREFIX}}
    {{return url}}
{{/macro}}

{{var helpers = [
	getPrefix: (pipe, data) => getPrefix(pipe, 'GET', data)
]}}

{{return helpers}}