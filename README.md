# Nano Payment Server (Java Implementation)

## TODO
- Release
    - Remove debugging params from application.properties
    - Build and release executable JAR (including properties file)
    - Include documentation on REST endpoints offered by this app
- Improvements
    - Refactor circular service dependency between `PaymentService` <--> `BlockWatcherService`
    - Add unit tests where appropriate
    - Add `/api/payment/wait` REST function which blocks until completed/failed

## API
### New payment
***POST*** `/api/payment/new`
```json
{
    "account": "nano_38rkxdc6dr4wap9kamsu7k8cqy8bj1ougrx8fifwuzeydzch9dtcmt66mrcc",
    "amount": "0.12"
}
```
<details><summary>Example response</summary>

```json
{
  "id": "ef3cb924-4eb1-4c2a-8b1c-d6a4fd4d56f5",
  "handoff": "eyJpZCI6ImVmM2NiOTI0LTRlYjEtNGMyYS04YjFjLWQ2YTRmZDRkNTZmNSIsImFkZHJlc3MiOiJuYW5vXzM4cmt4ZGM2ZHI0d2FwOWthbXN1N2s4Y3F5OGJqMW91Z3J4OGZpZnd1emV5ZHpjaDlkdGNtdDY2bXJjYyIsImFtb3VudCI6IjEyMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsIm1ldGhvZHMiOlt7InR5cGUiOiJodHRwcyIsInVybCI6ImxvY2FsaG9zdDo4MDgwL2hhbmRvZmYifV19"
}
```
</details>

### Retrieve status
***GET*** `/api/payment/:id`
<details><summary>Example response</summary>

```json
{
  "status": "awaiting_handoff",
  "reqAccount": "nano_38rkxdc6dr4wap9kamsu7k8cqy8bj1ougrx8fifwuzeydzch9dtcmt66mrcc",
  "reqAmount": "0.12",
  "reqAmountRaw": "120000000000000000000000000000",
  "timeRemaining": 565,
  "active": true,
  "failed": false,
  "statusMessage": "Awaiting block handoff",
  "successful": false
}
```
</details>