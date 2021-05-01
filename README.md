# Nano Payment Server (Java Implementation)

## TODO
- Release
  - Remove debugging params from application.properties
  - Build and release executable JAR (including properties file)
  - Include documentation on REST endpoints offered by this app
- Improvements
  - Refactor circular service dependency between `PaymentService` &hoarr; `BlockWatcherService`
  - Add unit/integration tests where appropriate
  - REST API
    - Add *wait* endpoint which blocks until payment is finalized
    - Add *cancel* endpoint which cancels an ongoing payment
  - Add additional real-time checks for invalidated blocks (eg. fork block is confirmed) instead
    of only relying on timeout


## Running & deployment
- It is recommended that you run a proxy in front of this application to restrict access to API endpoints (`/payment/`)
- Block handoff URL `/handoff` must be made accessible from remote connections
- File `application.properties` must be configured before running the server


## HTTP API
The following REST-like endpoints are made available by the application: 

### Create new payment
<details><summary><b><i>POST</i></b> <code>/payment/new</code></summary>

#### Request
Attribute | Description
--- | ---
`account` | The destination account for the payment
`amount` | The amount of the payment, in decimal Nano (alternative to `amountRaw`)
`amountRaw` | The amount of the payment, in raw (alternative to `amount`)

<details><summary>Example JSON</summary>

```json
{
    "account": "nano_38rkxdc6dr4wap9kamsu7k8cqy8bj1ougrx8fifwuzeydzch9dtcmt66mrcc",
    "amount": "0.12"
}
```
</details>

#### Response
Key | Description
--- | ---
`id` | The unique ID of the payment
`handoff` | The encoded handoff specification of this payment (recommendation is to prepend `nanopay:` URI scheme when presenting to customer)

<details><summary>Example JSON</summary>

```json
{
  "id": "ef3cb924-4eb1-4c2a-8b1c-d6a4fd4d56f5",
  "handoff": "eyJpZCI6ImVmM2NiOTI0LTRlYjEtNGMyYS04YjFjLWQ2YTRmZDRkNTZmNSIsImFkZHJlc3MiOiJuYW5vXzM4cmt4ZGM2ZHI0d2FwOWthbXN1N2s4Y3F5OGJqMW91Z3J4OGZpZnd1emV5ZHpjaDlkdGNtdDY2bXJjYyIsImFtb3VudCI6IjEyMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsIm1ldGhvZHMiOlt7InR5cGUiOiJodHRwcyIsInVybCI6ImxvY2FsaG9zdDo4MDgwL2hhbmRvZmYifV19"
}
```
</details></details>

### Retrieve payment status
<details><summary><b><i>GET</i></b> <code>/payment/:id</code></summary>

#### Request
`id` property in URL should be the payment ID string returned by the new payment request.

*Request body should be empty.*

#### Response
Key | Description
--- | ---
`status` | The current status of the payment
`statusMessage` | Generic friendly message of the payment status
`timeRemaining` | Number of seconds remaining until expiry (not present if finalized)
`active` | `true` if payment is ongoing (not finalized)
`successful` | `true` if payment was successful and has been confirmed
`failed` | `true` if payment has failed and is inactive
`reqAccount` | The requested destination account of the payment
`reqAmount` | The requested amount of the payment, in Nano
`reqAmountRaw` | The requested amount of the payment, in raw

<details><summary>Example JSON</summary>

```json
{
  "status": "awaiting_handoff",
  "statusMessage": "Awaiting block handoff",
  "timeRemaining": 565,
  "active": true,
  "failed": false,
  "successful": false,
  "reqAccount": "nano_38rkxdc6dr4wap9kamsu7k8cqy8bj1ougrx8fifwuzeydzch9dtcmt66mrcc",
  "reqAmount": "0.12",
  "reqAmountRaw": "120000000000000000000000000000"
}
```
</details></details>