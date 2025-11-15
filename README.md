# AbyssChat : Decentralized Chat System

AbyssChat is a decentralized LAN Chat System running under Java.

## Design

### Goal

The goal of this project is to make a decentralized LAN chat system
where users are able to choose usernames, chat to each other, and
discover each other using a contact list.

![progress bar](progressbar.png)

## How to run

### Compilation

To compile the project, from the project directory, run:
```bash
mvn compile
```

### Running 

To run the app, from the project directory, run:
```bash
mvn exec:java -D exec.mainClass="net.chatsystem.Client"
```

### Unit tests 

To run unit tests, from the project directory, run:
```bash
mvn test
```

### Usage

- After running, input your username to login.
- Then use one of these commands

| Command           | Description                       |
|-------------------|-----------------------------------|
| `/help`           | Show list of available commands   |
| `/contacts`       | Show your contact list            |
| `/me`             | Show your profile                 |
| `/changeusername` | Self-explanatory                  |
| `/disconnect`     | Disconnect from the chat          |

- You may also close the chat directly, it will automatically disconnect you



