
## AegisServer — Custom Java HTTP Server

AegisServer is a custom-built, multithreaded `HTTP/1.1` server implemented entirely from `Scratch` in Java using raw TCP sockets. 

The project focuses on understanding and implementing `low-level backend systems` concepts without relying on web frameworks such as Spring or Express.

The goal of AegisServer : How web servers work internally,
 `networking, concurrency, protocol handling, data persistence, and deployment.`

## Run with Docker
### 1. Build the Docker image

From the project root:

```bash
docker build -t aegis-server .
```

### 2. Run the container

```bash
docker run -p 8080:8080 -v $(pwd):/app aegis-server
```

This setup:

* Exposes the server on port `8080`
* Persists the SQLite database across restarts using a volume


## 3. Verify

* Open browser:

  ```
  http://localhost:8080/
  ```
# Architecture
## UML Diagram
![High Level Diaram](/docs/UML.png)

---
## System Design
![High Level Diaram](/docs/system-design.png)

## In detail File Workout

### WorkerPool.java
- Accept the Threads
- Queue acts as a waiting room
- Workers run continuously until there is nothing in queue
- If Queue is full then it blocks new requests
- Also Implemented graceful Shutdown controls server lifecycle
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
```
[REQ] method=GET path=/ status=200 time_ms=4
```

## REST API INTEGRATION 

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

### server/db/Database.java
- creating the sqlite Library
- using jdbc for connection and slf4j-simple,sl4j-api for logging purpose
- aegis.db creates an database

### server/ratelimit/rateLimiter.java
- added rate limiting to the server
OUTPUT:
```shell
CMD : for i in {1..5}; do curl -i http://localhost:8080/; done
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
### server/staticfiles.java
- mapping the index.html ,style.css and app.js files 

### static 
- index.html --> html files
- style.css --> styles for css
- app.js --> javascript code
---
### Screenshots
![Screenshots](/docs/screenshot.png)
### Summary
- Designed and implemented a custom multithreaded HTTP/1.1 server in Java using raw TCP sockets, without relying on web frameworks.

- Built a thread-safe worker pool and connection lifecycle management to handle concurrent client requests efficiently.

- Implemented full HTTP request/response handling including manual parsing, deterministic routing, error handling, and response writing.

- Developed RESTful APIs backed by SQLite using JDBC, with transaction management, repository abstraction, and manual JSON serialization.

- Added IP-based rate limiting, structured logging, and graceful shutdown to improve reliability and abuse protection.

- Served a static frontend (HTML/CSS/JS) directly from the server and Dockerized the application using a multi-stage build for portable deployment.

