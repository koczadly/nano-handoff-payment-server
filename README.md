# Nano Payment Server (Java Implementation)

## TODO
- Release
  - Remove debugging params from application.properties
  - Build and release executable JAR (including properties file)
  - Include documentation on REST endpoints offered by this app
- Improvements
  - Refactor circular service dependency between `PaymentService` <--> `BlockWatcherService`
  - Add unit/integration tests where appropriate
  - Add `/payment/wait` REST function which blocks until payment is finalized
  - Add additional real-time checks for invalidated blocks (eg. fork block is confirmed) instead
    of only relying on timeout

## HTTP API
### New payment
***POST*** `/payment/new`

#### Request
Attribute | Description
--- | ---
`account` | The destination account for the payment
`amount` | The amount of the payment, in decimal Nano (alternative to `amount_raw`)
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
</details>

### Retrieve status
***GET*** `/payment/:id`

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

<details><summary>Example response JSON</summary>

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
</details>