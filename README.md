# http-Server
## Health Check

Client
  ↓
Kernel backlog
  ↓
accept()
  ↓
TcpListener
  ↓
WorkerPool.submit(Socket)
  ↓
ConnectionHandler (later)

### WorkerPool.java
- Accept the Threads
- Queue acts as a waiting room
- Workers run continuously until there is nothing in queue
- If Queue is full then it blocks new requests

### TcpListener.java
- Bind to a port
- Accept incoming TCP connections
- Hand sockets and routes

### ServerConfig.java
- immutable startup configuration

### connectionHandler.java
- Configure socket (timeouts)
- Read bytes into a buffer
- Detect end-of-stream or timeout
- reads exact content-length
- Parses HttpRequest
- Routes request via Router
- Writes response via HttpWriter

read → parse → route → execute → write → close

### HttpParser.java
- Accepts the headers and body
- //s+ correct start-line parsing 
- headers are normalised

### HttpRequest.java
- creates an proper request block


TCP → ConnectionHandler → HttpParser → Router → HttpHandler → HttpWriter

### Router.java
- maps the HTTP_METHOD to HttpHandler
- Read-only at runtime

### HttpHandler.java
- handles the request & response

### HttpResponse.java
- prevents handlers from writing raw HTTP
-  Keeps protocols serialization
- Avoids duplicate response logics

### HttpWriter.java
- ensures the protocol is correct 

### Logger.java
- Logs the Requests properly
Format :

[REQ] method=GET path=/ status=200 time_ms=4

---

--- REST API INTEGRATION ---

### server/item/Item.java
- Model of the item

### server/item/ItemStore.java
- create()
- FindAll()
- findById(id)
- delete(id)

### server/item/ItemService.java
- Business Logic
- validation logics

### server/item/ItemHandler.java
- handling the http requests by HttpHandler
- proper listing the items
- get() ,delete()
### server/itemItemRepository.java
- handling the transactions properly with rollbacks
- WAL (Write Ahead Logging) 
- 
## Routes for Items
- POST /items "item1"
- GET  /items
- GET /items/{id}
- Delete /items/{id}

``` shell
curl -X POST http://localhost:8080/items -d "Item1"
curl -X POST http://localhost:8080/items -d "Item2"

curl http://localhost:8080/items/1
curl http://localhost:8080/items/2

```

### server/db/Database.java
- creating the sqlite Library
- using jdbc for connection and slf4j-simple,sl4j-api for logging purpose
- aegis.db creates an database

### server/ratelimit/rateLimiter.java
- added rate limiting to the server
OUTPUT:
```shell
command : for i in {1..5}; do curl -i http://localhost:8080/; done
// Temporarily in Main its 3

TTP/1.1 200 OK
Content-Length: 23
Connection: close

Too Many RequestsHTTP/1.1 429 
Content-Length: 17
Connection: close


Server Console :
[ERR] status=429 message="Rate limit exceeded for ip=0:0:0:0:0:0:0:1"
[ERR] status=429 message="Rate limit exceeded for ip=0:0:0:0:0:0:0:1"


```