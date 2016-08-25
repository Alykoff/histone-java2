{{macro summFormat(number, value, fraction)}}{{var result}}
	{{if number = ''}}
		--
	{{else}}
		{{var value = value ? formatNumberText(number, value) : '<i class="rub">&#8381;</i>'}}
		{{var number = number->isNumber ? number : number->toNumber}}
		{{var prefix = number < 0 ? '&minus;'}}
		{{var number = number->toAbs->toString->split('.')}}
		{{var first = number[0]}}
		{{var second = number[1]}}
		{{var second = second ? (',' + second + '0')->slice(0, 3)}}

		{{var value = ' ' + value}}

		{{var first}}{{for ch in first->split}}{{first[self.last - self.index]}}{{/for}}{{/var}}
		{{var first}}{{for ch in first->split}}{{if self.index % 3 = 0}} {{/if}}{{ch}}{{/for}}{{/var}}
		{{var first}}{{for ch in first->split}}{{first[self.last - self.index]}}{{/for}}{{/var}}

		{{if prefix}}<i class="red">{{/if}}
		{{if fraction}}
			{{var second = second = null ? ',00' : second}}
			{{prefix + first->strip}}<span>{{second}}</span>{{value}}
		{{else}}
			{{prefix + first->strip + second}}{{value}}
		{{/if}}
		{{if prefix}}</i>{{/if}}

	{{/if}}
{{/var}}{{result->strip}}{{/macro}}

{{return [
    summFormat: summFormat
]}}