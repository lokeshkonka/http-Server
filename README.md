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