# Waves PostOffice

**Waves PostOffice** — учебный проект почтовой системы на Waves Enterprise: Java-смарт-контракт хранит пользователей, денежные переводы, почтовые отделения и посылки в состоянии блокчейна; Spring Boot предоставляет REST API для чтения состояния и отправки транзакций.

> **Статус:** локальный прототип для Waves Enterprise Sandbox. Денежные переводы проверены сквозным сценарием через backend. Почтовые операции реализованы в контракте, но ещё не выведены в REST API backend. Публичное развёртывание и использование реальных ключей не поддерживаются текущей схемой подписи.

## Содержание

- [Возможности](#возможности)
- [Архитектура](#архитектура)
- [Технологии](#технологии)
- [Структура репозитория](#структура-репозитория)
- [Требования](#требования)
- [Быстрый запуск](#быстрый-запуск)
- [Конфигурация](#конфигурация)
- [REST API](#rest-api)
- [Пример: денежный перевод](#пример-денежный-перевод)
- [Смарт-контракт и состояние](#смарт-контракт-и-состояние)
- [Сборка и обновление Docker-контракта](#сборка-и-обновление-docker-контракта)
- [Тесты и CI](#тесты-и-ci)
- [Безопасность и ограничения](#безопасность-и-ограничения)
- [Планы развития](#планы-развития)

## Возможности

| Область | Смарт-контракт | Backend REST API |
| --- | --- | --- |
| Инициализация владельца и семи отделений | Реализовано | Чтение информации и состояния |
| Регистрация пользователя | Реализовано | `POST /api/v1/users` |
| Изменение личных данных | Реализовано | `PATCH /api/v1/users/me` |
| Начисление баланса владельцем контракта | Реализовано | `POST /api/v1/users/{address}/credit` |
| Создание денежного перевода | Реализовано | `POST /api/v1/transfers` |
| Принятие / отклонение перевода получателем | Реализовано | `POST /api/v1/transfers/{id}/accept`, `/reject` |
| Получение пользователей, переводов и отделений | Состояние блокчейна | `GET`-методы |
| Назначение сотрудника отделения | Реализовано | Пока нет |
| Отправка и перемещение посылки | Реализовано | Пока нет |
| Отслеживание статуса исполнения транзакции | API ноды | `GET /api/v1/transactions/{id}/status` |
| Web UI и полноценная пользовательская авторизация | — | Пока нет |

Операции с балансом и переводами выполняются **в контракте**, а не путём изменения локальной базы данных backend.

## Архитектура

```text
HTTP-клиент / Swagger UI
          │
          ▼
Spring Boot backend :8081
  ├─ Controllers / DTO / validation
  ├─ ContractReadService
  ├─ ContractWriteService
  ├─ WavesNodeClient
  └─ WavesTransactionClient
          │ REST
          ▼
Waves Enterprise Sandbox
  ├─ node-0 :6862 — владелец (OWNER)
  ├─ node-1 :6872 — дополнительная нода
  └─ node-2 :6882 — тестовый получатель (RECIPIENT)
          │ транзакция 104
          ▼
Docker-контракт (Java 17 / gRPC)
          │
          ▼
Состояние блокчейна: пользователи, переводы,
почтовые отделения, посылки
```

`backend` использует REST API нод. `contract` исполняется внутри Waves Enterprise через gRPC. Модули **не зависят друг от друга на уровне Java-классов**: backend обменивается JSON с нодой и не загружает контракт в свой процесс.

## Технологии

| Компонент | Стек |
| --- | --- |
| Смарт-контракт | Java 17, Waves Enterprise Contract SDK gRPC, Gradle, Shadow JAR |
| Backend | Java 17, Spring Boot 3.5.16, Spring Web `RestClient`, Jakarta Validation |
| Документация API | Springdoc OpenAPI / Swagger UI |
| Тесты | JUnit 5, Spring Boot Test, Mockito |
| Сборка | Gradle Wrapper 8.5, Kotlin DSL |
| Исполнение контракта | Docker, локальный Registry, Waves Enterprise Sandbox |

У контракта и backend разные наборы зависимостей: старые версии SDK и BOM контракта не нужно заменять версиями Spring Boot backend.

## Структура репозитория

```text
waves-postoffice/
├── contract/
│   ├── src/main/java/com/ororura/
│   │   ├── api/                 # PostOfficeContract, DTO, реализация действий
│   │   ├── application/         # сервисы, права, контекст вызова
│   │   ├── domain/              # модели, правила, интерфейсы репозиториев
│   │   ├── infrastructure/      # доступ к состоянию блокчейна
│   │   └── bootstrap/           # создание и запуск обработчика контракта
│   ├── src/test/
│   ├── Dockerfile
│   ├── build_and_push_to_docker.sh
│   └── build.gradle.kts
├── backend/
│   ├── src/main/java/com/ororura/postoffice/
│   │   ├── api/                 # контроллеры, DTO, обработка ошибок
│   │   ├── application/         # чтение и вызов контракта
│   │   ├── config/              # параметры и HTTP-клиенты
│   │   └── infrastructure/blockchain/
│   ├── src/main/resources/application.yml
│   ├── src/test/
│   └── build.gradle.kts
├── .github/workflows/contract-ci.yml
├── settings.gradle.kts
├── build.gradle.kts
└── gradlew
```

## Требования

- JDK **17**; Docker Desktop (для запуска и сборки контракта); Python 3 для команд форматирования JSON в примерах.
- Запущенный Waves Enterprise Sandbox с доступным REST API ноды. В примерах: `127.0.0.1:6862` и `127.0.0.1:6882`.
- Уже созданный и активный экземпляр контракта в сети; его `contractId` задаётся через `WE_CONTRACT_ID`.
- Для операций записи — настроенные keystore-аккаунты владельца и получателя в соответствующих нодах Sandbox.

Sandbox — **отдельная среда**: одного `./gradlew bootRun` недостаточно, если блокчейн-ноды не запущены. Contract ID, адреса аккаунтов и пароли зависят от конкретной сети; значения ниже относятся только к проверенному локальному примеру.

## Быстрый запуск

### 1. Проверить Java и собрать проект

На macOS:

```bash
cd ~/Desktop/waves-postoffice
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version

./gradlew :contract:build :backend:build --no-daemon
```

### 2. Проверить доступность ноды

```bash
curl -fsS http://127.0.0.1:6862/blocks/height | python3 -m json.tool
```

### 3. Настроить подключение к контракту

```bash
export WE_NODE_URL="http://127.0.0.1:6862"
export WE_CONTRACT_ID="En8e514ghDoNHuRJ11Bc5pW1tNXW1ajmcoGBvkRjTQix"
```

Приведённый ID — экземпляр из локального тестового Sandbox, **не универсальный ID проекта**. Если сеть пересоздана, подставьте ID нового контракта.

Для **только чтения** этого достаточно. Для отправки транзакций в текущем Sandbox дополнительно:

```bash
export WE_OWNER_ADDRESS="3P1pzX1xqzGRajMuYWUNFjnWUiQMHNMgYfP"
export WE_OWNER_NODE_URL="http://127.0.0.1:6862"

export WE_RECIPIENT_ADDRESS="3NqUjVU9kPK72DDfUCqxNt7EUTkverCZrUo"
export WE_RECIPIENT_NODE_URL="http://127.0.0.1:6882"
```

В **zsh** пароли можно ввести без отображения на экране:

```zsh
read -rs "WE_OWNER_PASSWORD?Owner keystore password: "
echo
export WE_OWNER_PASSWORD

read -rs "WE_RECIPIENT_PASSWORD?Recipient keystore password: "
echo
export WE_RECIPIENT_PASSWORD
```

Укажите эти переменные **до запуска** JVM. Не записывайте пароли в README, `application.yml`, историю команд или Git. Если keystore создан с пустым паролем, в локальном Sandbox допустимо экспортировать пустую строку.

### 4. Запустить backend

```bash
./gradlew :backend:bootRun
```

По умолчанию backend слушает **только** `127.0.0.1:8081`.

- Swagger UI: http://127.0.0.1:8081/swagger-ui/index.html
- OpenAPI JSON: http://127.0.0.1:8081/v3/api-docs
- Проверка ноды:

```bash
curl -fsS http://127.0.0.1:8081/api/v1/health/blockchain | python3 -m json.tool
```

Пример ответа:

```json
{"status":"UP","height":320}
```

Высота блока в реальной сети будет другой.

## Конфигурация

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `WE_NODE_URL` | REST API ноды для чтения состояния и статусов | `http://127.0.0.1:6862` |
| `WE_CONTRACT_ID` | ID развернутого экземпляра контракта | Нет |
| `WE_API_KEY` | `X-API-Key`, если включена авторизация на ноде | Пустая строка |
| `WE_OWNER_ADDRESS` | Адрес тестового подписанта `OWNER` | Нет |
| `WE_OWNER_NODE_URL` | Нода с keystore владельца | `http://127.0.0.1:6862` |
| `WE_OWNER_PASSWORD` | Пароль keystore владельца | Не настроен |
| `WE_RECIPIENT_ADDRESS` | Адрес тестового подписанта `RECIPIENT` | Нет |
| `WE_RECIPIENT_NODE_URL` | Нода с keystore получателя | `http://127.0.0.1:6882` |
| `WE_RECIPIENT_PASSWORD` | Пароль keystore получателя | Не настроен |

Конфигурация находится в `backend/src/main/resources/application.yml` и читается через `BlockchainProperties`.

## REST API

### Чтение

| Метод | Маршрут | Назначение |
| --- | --- | --- |
| `GET` | `/api/v1/health/blockchain` | Доступность и высота блокчейна |
| `GET` | `/api/v1/contract/info` | Метаданные контракта, образ, версия |
| `GET` | `/api/v1/contract/state` | Сырые записи состояния контракта |
| `GET` | `/api/v1/users` | Пользователи и балансы |
| `GET` | `/api/v1/users/{address}` | Пользователь по blockchain-адресу |
| `GET` | `/api/v1/transfers` | Список переводов |
| `GET` | `/api/v1/offices` | Почтовые отделения |
| `GET` | `/api/v1/transactions/{transactionId}/status` | Результат исполнения транзакции |

```bash
curl -fsS http://127.0.0.1:8081/api/v1/users | python3 -m json.tool
curl -fsS http://127.0.0.1:8081/api/v1/transfers | python3 -m json.tool
curl -fsS http://127.0.0.1:8081/api/v1/offices | python3 -m json.tool
```

### Запись (только локальный Sandbox)

| Метод | Маршрут | Тело запроса | Действие контракта |
| --- | --- | --- | --- |
| `POST` | `/api/v1/users` | `{ "actor": "OWNER", "name": "...", "homeAddress": "..." }` | `createUser` |
| `PATCH` | `/api/v1/users/me` | `{ "actor": "OWNER", "name": "...", "homeAddress": "..." }` | `changePersonalData` |
| `POST` | `/api/v1/users/{address}/credit` | `{ "amount": 100 }` | `creditUser` (подпись владельцем) |
| `POST` | `/api/v1/transfers` | `{ "actor": "OWNER", "to": "...", "amount": 10 }` | `transferMoney` |
| `POST` | `/api/v1/transfers/{id}/accept` | `{ "actor": "RECIPIENT" }` | `acceptTransfer` |
| `POST` | `/api/v1/transfers/{id}/reject` | `{ "actor": "RECIPIENT" }` | `deniedTransfer` |

`actor` может иметь только значения `OWNER` или `RECIPIENT`. Это **выбор заранее настроенного тестового подписанта**, а не аутентификация пользователя. Указанное в теле запроса значение не доказывает, кто отправил HTTP-запрос.

Операции записи возвращают **HTTP 202 Accepted** и, при успешном приёме транзакции нодой, тело вида:

```json
{
  "transactionId": "<blockchain-transaction-id>",
  "contractId": "<contract-id>",
  "action": "transferMoney",
  "statusUrl": "/api/v1/transactions/<blockchain-transaction-id>/status"
}
```

**HTTP 202 при отправке не означает успех исполнения контракта.** Результат следует отдельно проверять по `statusUrl`.

Если применено исправление обработчика статусов `improve-postoffice-transaction-status.sh`, `GET .../status` возвращает:

```json
{
  "transactionId": "<blockchain-transaction-id>",
  "state": "SUCCESS",
  "executions": [
    { "status": "Success", "message": "Contract transaction successfully mined" }
  ]
}
```

Возможные `state`: `SUCCESS`, `FAILED`, `NOT_AVAILABLE_YET`, `UNRECOGNIZED`. При `NOT_AVAILABLE_YET` API возвращает HTTP 202 и `Retry-After: 2`; это значит лишь, что записи исполнения пока нет (ID также может быть неверным). До применения исправления endpoint возвращает исходный JSON-массив от Waves Enterprise. Идентификатор блокчейн-транзакции (`transactionId`) **не равен** идентификатору денежного перевода (`id`).

## Пример: денежный перевод

> Следующий POST создаёт **реальную транзакцию в текущем Sandbox** и меняет состояние сети. Не запускайте его повторно, если ответ потерян: сначала проверьте историю и статус по имеющемуся ID транзакции.

В примере `OWNER` переводит `10` внутренних денежных единиц пользователю `RECIPIENT`.

```bash
curl --fail-with-body -sS \
  -X POST 'http://127.0.0.1:8081/api/v1/transfers' \
  -H 'Content-Type: application/json' \
  -d '{
    "actor": "OWNER",
    "to": "3NqUjVU9kPK72DDfUCqxNt7EUTkverCZrUo",
    "amount": 10
  }' | python3 -m json.tool
```

Сохраните `transactionId` и дождитесь `SUCCESS` (либо исходного `status: Success`, если обработчик статусов ещё не обновлён). Затем посмотрите `/api/v1/transfers`: у нового перевода должен быть `PENDING`. Его внутренний числовой `id` определяется счётчиком контракта; не угадывайте ID из суммы или `transactionId`.

Для **полученного** числового ID, например `3`:

```bash
curl --fail-with-body -sS \
  -X POST 'http://127.0.0.1:8081/api/v1/transfers/3/accept' \
  -H 'Content-Type: application/json' \
  -d '{"actor":"RECIPIENT"}' | python3 -m json.tool
```

И это отдельная транзакция: дождитесь её исполнения, затем проверьте `/api/v1/users` и `/api/v1/transfers`. До принятия перевод имеет статус `PENDING`, при принятии контракт списывает сумму у отправителя и зачисляет получателю. При отклонении статус становится `REJECTED`, балансы не изменяются.

## Смарт-контракт и состояние

Контрактный интерфейс: `contract/src/main/java/com/ororura/api/PostOfficeContract.java`.

| Метод | Назначение |
| --- | --- |
| `init()` | Записывает владельца и семь исходных отделений |
| `createUser(user)` | Регистрирует адрес вызвавшего аккаунта |
| `changePersonalData(user)` | Меняет профиль вызвавшего аккаунта |
| `creditUser(user, amount)` | Начисляет средства; доступно владельцу |
| `transferMoney(money)` | Создаёт ожидающий перевод |
| `acceptTransfer(id)` | Принимает перевод; доступно адресату |
| `deniedTransfer(id)` | Отклоняет перевод; доступно адресату |
| `setPostmanEmployee(user, postOfficeId, status)` | Назначает / снимает сотрудника отделения; доступно владельцу |
| `sendPackage(package)` | Создаёт посылку и списывает стоимость отправления |
| `checkoutParcel(trackingNumber, nextPostId)` | Передаёт посылку из отделения дальше по маршруту; доступно сотруднику |

Исходные отделения контракта: `344000` (сортировочный центр), `346770`, `346771`, `347900`, `347901`, `347902`, `347903`.

Текущая схема состояния использует имена `CONTRACT_META`, `USERS_MAPPING`, `TRANSFER_V2`, `TRANSFER_COUNTER_V2`, `OFFICE_MAPPING__` и `PARCEL_V2`. Например, `TRANSFER_V2_0` — перевод № 0, а `TRANSFER_COUNTER_V2_NEXT` — следующий свободный ID. Значения `amount` в API контракта указаны **в сотых долях токена**, поэтому `10` соответствует `0.10` токена по модели данных; в JSON backend значение остаётся целым числом `10`.

Схема `V2` **не совместима с состоянием и вызовами `V1`**: не направляйте старые payload к новому контракту. Обновление Docker-образа при сохранении состояния допустимо только при совместимости схемы с уже записанными данными.

## Сборка и обновление Docker-контракта

Собрать и протестировать контракт:

```bash
./gradlew :contract:spotlessCheck :contract:clean :contract:build :contract:shadowJar --no-daemon
```

Скрипт проекта собирает JAR, Docker-образ `linux/amd64` и отправляет его в доступный Registry:

```bash
bash contract/build_and_push_to_docker.sh localhost:5000/waves-postoffice:my-version
```

Для Mac с Apple Silicon передавайте `linux/amd64`: текущий Sandbox использует эту архитектуру для образа контракта. При создании (тип `103`) или обновлении (тип `107`) контракта Waves Enterprise нужны `image` и **корректный `imageHash` образа**. Перепроверяйте хеш для той платформы, которую собрали:

```bash
docker image inspect --platform linux/amd64 \
  --format '{{.Id}}' localhost:5000/waves-postoffice:my-version
```

Удалите префикс `sha256:` при передаче хеша в транзакции.

В испытанной локальной сети обновление существующего контракта выполнялось транзакцией **107 версии 1**: версия 2 отклонялась, потому что функция блокчейна `Sponsored fees support` (ID `120`) не была активирована. Версия *транзакции обновления* и поле `version` в `/contracts/info/{id}` — разные понятия. Обновление уже созданного экземпляра сохраняет его `contractId` и состояние; после успешного обновления проверяйте `/contracts/status/{txId}` и `/contracts/info/{contractId}`.

Backend получает актуальную `contractVersion` через `/contracts/info/{id}` при каждом вызове, поэтому не требует ручной замены версии после обновления образа. Это не отменяет необходимость проверять совместимость сохранённых данных.

## Тесты и CI

```bash
# Контракт
./gradlew :contract:spotlessCheck :contract:test :contract:shadowJar --no-daemon

# Backend
./gradlew :backend:test :backend:build --no-daemon

# Оба модуля
./gradlew :contract:build :backend:build --no-daemon
```

Существующий `.github/workflows/contract-ci.yml` собирает и проверяет **контракт**. Запуск тестов backend в CI — отдельная задача; не считайте его уже подключённым к GitHub Actions только потому, что локальная сборка работает.

## Безопасность и ограничения

1. **Не публикуйте текущий backend в интернет.** Swagger и write API сейчас не защищены пользовательской авторизацией; сервер специально привязан к `127.0.0.1`.
2. `actor` выбирает ключ подписи из конфигурации сервера. Любой процесс, имеющий доступ к этому HTTP API, может попросить подписать транзакцию тестовым `OWNER` или `RECIPIENT`.
3. Текущий локальный клиент использует `/transactions/signAndBroadcast` и пароль keystore ноды. Для реальных пользователей нужна иная модель учётных записей и подписи; реальные пароли нельзя передавать через публичный API.
4. `fee: 0` используется только потому, что это допускает текущий Sandbox. Не переносите его без проверки в другую сеть.
5. При сбое между отправкой транзакции и получением HTTP-ответа **не повторяйте POST вслепую**: операция уже могла выполниться. Сначала проверьте блокчейн, затем решайте вопрос повторной отправки.
6. Backend не ведёт отдельную БД. Состояние и баланс читаются из контракта; перезапуск backend сам по себе их не сбрасывает, но пересоздание Sandbox или деплой нового экземпляра контракта меняет контекст данных.

## Планы развития

- Добавить в backend REST API для `sendPackage`, `checkoutParcel`, назначения сотрудников, получения посылок и истории отслеживания.
- Вынести модель подписи из локальных тестовых аккаунтов в безопасную архитектуру с авторизацией и разграничением ролей.
- Добавить интеграционные тесты с реальным Sandbox и проверку idempotency / повторной отправки транзакций.
- Расширить GitHub Actions тестами и сборкой backend, добавить контейнеризацию backend.
- Реализовать frontend для пользователей и сотрудников почты.
