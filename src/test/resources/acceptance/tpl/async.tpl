{{1+1}}
START async
{{if loadJson(1000)=1000}}
1k
{{loadJson(500)}}
{{/if}}
{{if loadJson(5000) != 1000}}
2k
{{loadJson(700)}}
{{/if}}
{{if loadJson(3000) = 1000}}
3k
{{loadJson(100)}}
{{/if}}
{{if loadJson(1000) != 1000}}
'not1k'
{{loadJson(200)}}
{{/if}}
STOP
{{45 +2}}dgfdgdgfdgfdgfd