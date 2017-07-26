{{var r1 = loadJSON('http://127.0.0.1:4442/testCache', [cache: true]).requestCount}}
{{var r2 = loadJSON('http://127.0.0.1:4442/testCache', [cache: true]).requestCount}}
{{var r3 = loadJSON('http://127.0.0.1:4442/testCache', [cache: true, method: 'POST']).requestCount}}

{{var r4 = loadJSON('http://127.0.0.1:4442/testCache', [cache: true]).requestCount}}
{{return [
    r1: r1,
    r2: r2,
    r3: r3,
    r4: r4
]}}