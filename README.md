# Nano Handoff Payment Server
A [Nano](https://nano.org) payment processor application which implements the new "Block Handoff" proof-of-concept.

This project is simply a proof of concept at this stage, and **should not be used in production**. Various features are
missing, and the application has not been optimised or protected from potential race conditions.

This currently implements revision 17 of the specification.


## TODO
- Implement websocket confirmation watcher for real-time updates
- Implement transactional and locking mechanisms
- Implement cancel method
- Add real-time callback feature (via HTTP or websocket?)
- Reduce database size by re-using account destinations; is this necessary?
- Convert transaction ID (block hash) to blob type for efficiency; Hibernate disallows AttributeConverter's on ID columns
- Order transactions list by process date


## Running & deployment
- A reverse proxy (ie. Apache) should be operated in front of this server
  - The `/api/` directory should be blocked (or restricted through authentication)
  - The `/handoff` directory *must* be exposed to remote connections, with SSL configured
- A local `application.properties` file *must* be configured before running the server

## HTTP API
Still a work in progress, yet to be documented; see `PaymentController` class.