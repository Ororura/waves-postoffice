#!/usr/bin/env python3
"""Mechanical move only. Does not change on-chain state keys or SDK API action names."""
from pathlib import Path
import subprocess

root = Path.cwd()
src = root / 'contract/src/main/java/com/ororura'
assert (src/'app/PostContract.java').is_file(), 'Expected pre-migration app/PostContract.java; run from repo root.'
assert (src/'Dispatcher.java').is_file(), 'Expected pre-migration Dispatcher.java'
assert 'private final Mapping<String> ownerMapping' in (src/'app/PostContract.java').read_text(), (
    'First apply the previously provided security fix to the original layout, then run stage 01.'
)

moves = {
    'app/PostContract.java': 'api/PostContract.java',
    'Dispatcher.java': 'bootstrap/Dispatcher.java',
    'utils/CalculateTotalCost.java': 'domain/pricing/CalculateTotalCost.java',
    'utils/GenerateData.java': 'domain/tracking/GenerateData.java',
}
for path in (src/'model').glob('*.java'):
    moves[str(path.relative_to(src))] = 'domain/model/' + path.name
for old, new in moves.items():
    (src/new).parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(['git','mv',str(src/old),str(src/new)], check=True)

for file in src.rglob('*.java'):
    s = file.read_text()
    s = s.replace('com.ororura.model', 'com.ororura.domain.model')
    s = s.replace('com.ororura.utils.CalculateTotalCost', 'com.ororura.domain.pricing.CalculateTotalCost')
    s = s.replace('com.ororura.utils.GenerateData', 'com.ororura.domain.tracking.GenerateData')
    s = s.replace('com.ororura.app.PostContract', 'com.ororura.api.PostContract')
    if file == src/'api/PostContract.java':
        s = s.replace('package com.ororura.app;', 'package com.ororura.api;')
    elif file == src/'bootstrap/Dispatcher.java':
        s = s.replace('package com.ororura;', 'package com.ororura.bootstrap;')
    elif file.parent == src/'domain/model':
        s = s.replace('package com.ororura.model;', 'package com.ororura.domain.model;')
    elif file == src/'domain/pricing/CalculateTotalCost.java':
        s = s.replace('package com.ororura.utils;', 'package com.ororura.domain.pricing;')
    elif file == src/'domain/tracking/GenerateData.java':
        s = s.replace('package com.ororura.utils;', 'package com.ororura.domain.tracking;')
    file.write_text(s)

build = root/'contract/build.gradle.kts'
s = build.read_text().replace('com.ororura.Dispatcher', 'com.ororura.bootstrap.Dispatcher')
build.write_text(s)
print('Stage 1 done. Only file locations, package names and entrypoint changed.')
