Waves postoffice layered monolith migration scripts

REQUIRES the previously supplied fix_waves_postoffice.py applied to the original layout first.
Run from the waves-postoffice repository root, one stage at a time:
  python3 ./migration/01_move_packages.py
  ./gradlew :contract:clean :contract:build --no-daemon
  python3 ./migration/02_create_ports_and_adapters.py
  ./gradlew :contract:clean :contract:build --no-daemon
  python3 ./migration/03_extract_services.py
  ./gradlew :contract:clean :contract:build --no-daemon
  python3 ./migration/04_tests_and_ci.py
  ./gradlew :contract:clean :contract:build --no-daemon

Commit between stages. Do not push secrets; review git diff --cached.
Layer migration keeps the SDK action names, argument types, old state key names and list-based layout.
It does not change double financial amounts to long; that requires migration of serialized state.
Test on a local disposable chain before using any existing deployed contract.
