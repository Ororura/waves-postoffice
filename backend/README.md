# PostOffice backend (local sandbox)

Java 17 / Spring Boot 3.5.16 / Waves Enterprise node REST API.

## Start

```bash
cd ~/Desktop/waves-postoffice
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export WE_NODE_URL=http://127.0.0.1:6862
export WE_CONTRACT_ID=En8e514ghDoNHuRJ11Bc5pW1tNXW1ajmcoGBvkRjTQix
./gradlew :backend:bootRun
```

Swagger UI: http://127.0.0.1:8081/swagger-ui/index.html

Available read-only endpoints:

- GET /api/v1/health/blockchain
- GET /api/v1/contract/info
- GET /api/v1/contract/state
- GET /api/v1/users
- GET /api/v1/users/{address}
- GET /api/v1/transfers
- GET /api/v1/offices

## Local sandbox write API (NOT for production)

The server remains bound to 127.0.0.1. `actor` selects one of TWO locally configured
sandbox keystore accounts, not a logged-in user. Anyone who can call this HTTP API
can request writes signed with those sandbox accounts. Never expose this backend or
its Swagger port publicly, do not enable permissive CORS, and do not use real keys.

Set WE_OWNER_ADDRESS, WE_OWNER_NODE_URL, WE_OWNER_PASSWORD and
WE_RECIPIENT_ADDRESS, WE_RECIPIENT_NODE_URL, WE_RECIPIENT_PASSWORD.
Passwords are read from environment only; don't put them in application.yml or git.
An exported empty password is allowed if that is how the sandbox keystore was made.

New endpoints (all POST/PATCH return HTTP 202 with transactionId; this is NOT an
execution success):
- POST /api/v1/users {actor,name,homeAddress}
- PATCH /api/v1/users/me {actor,name,homeAddress}
- POST /api/v1/users/{address}/credit {amount} (always OWNER)
- POST /api/v1/transfers {actor,to,amount}
- POST /api/v1/transfers/{id}/accept {actor}
- POST /api/v1/transfers/{id}/reject {actor}
- GET /api/v1/transactions/{transactionId}/status (check node execution result!)

The client reads contractVersion dynamically from GET /contracts/info/{id}; no hardcoded
version after transaction 107. Smart contract owns all validation and balances.
`fee=0` is valid only for our current local network configuration.
