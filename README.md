# Java UDP Broadcast Server
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

An exploration into network communication using Java, implementing a **server** that manages incoming messages over the **User Datagram Protocol (UDP)** and broadcasts them to all connected clients.

## 🌟 Features

* Asynchronous message handling via UDP sockets.
* Simple Graphical User Interface (GUI) built with Swing for basic messaging.
* Accepts the string `"end"` command to safely terminate the server process.

---

## 🛠️ Installation

This project is built with **Apache Maven**.

### Prerequisites

* **Java Development Kit (JDK) 21+**
* **Apache Maven 3.9**

### Steps

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/rbcurzon/broadcast-server.git](https://github.com/rbcurzon/broadcast-server.git)
    cd broadcast-server
    ```

2.  **Build the Project:**
    Use Maven to compile, test, and package the application into a runnable JAR file.
    ```bash
    mvn clean install
    ```
    The runnable JAR will be located in the `target/` directory.

---

## 💻 Usage

The application runs as a standalone server with a simple Swing Frame (GUI).

### Running the App

1.  **Execute the packaged JAR file:**
    ```bash
    java -jar target/*.jar
    ```
      * The server will start listening for messages on **Port 4445**.
      * A **Simple Application Frame (GUI)** will open.
2.  **Server Control:**
    To **safely shut down** the server, send the exact string `"end"` from a client application.
    