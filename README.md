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
- Hand sockets to the 

### ServerConfig.java
- immutable startup configuration

### connectionHandler.java
- Configure socket (timeouts)
- Read bytes into a buffer
- Detect end-of-stream or timeout
- Print bytes (temporary)
- Close socket in finally