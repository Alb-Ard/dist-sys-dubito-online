# Dubito Online

- [Alberto Arduini](mailto:alberto.arduini3@studio.unibo.it)
- [Andrea Bianchi](mailto:andrea.bianchi10@studio.unibo.it)

### AI Disclaimer (if needed)

```
"During the preparation of this work, the author(s) used Claude.Ai to generate small 
pieces of code to develop the Game Board for Dubito Online.
After generating the initial prompt, the author(s) then reviewed and edited the
content as needed and take(s) full responsibility for the content of the
final report/artifact."
```

## Abstract

This project's objective is to recreate a version of the famous card game called "Dubito", following the rules and ideas of the videogames "Liar's Bar" and "Master Bluff". 

The game rules are the following:
- At the start of each round each player received 5 cards and a specific type of card is declared (Kings, Queens, Aces);
- Players during their turn can do one of the following actions:
    - Discard up to 3 cards from their hand facedown, following the same type that was declared at the start of the round or not. 
    - Call the previous player "liar", if the player is not the first one playing in the round.
- When a player is called a liar, the cards that he discarded must be shown, and:
    - If that player did lie, he loses 1 point.
    - If that player did not lie, who called the bluff loses 1 point.
- Each player starts with 3 points, and the last player that has points winning the game.

## Concept

Here you should explain:
- The type of product developed with that project, for example (non-exhaustive):
    - Application (with GUI, be it mobile, web, or desktop)
    - Command-line application (CLI could be used by humans or scripts)
    - Library
    - Web-service(s)

- Use case collection
    - _where_ are the users?
    - _when_ and _how frequently_ do they interact with the system?
    - _how_ do they _interact_ with the system? which _devices_ are they using?
    - does the system need to _store_ user's __data__? _which_? _where_?
    - most likely, there will be _multiple_ __roles__

### Artifacts
The project will result in a *group* of applications that will let the users host, join and play games of dubito with other users.

The output is comprised of two applications:
- The LobbyServer, a CLI application that will host a server where users can connect to create and join lobbies.
- The GameApp, a Swing graphical application that the users can use to connect to a LobbyServer to create or find lobbies, join them and play the game.

### Interactions and use cases
All uses will be able to:
- Use the application to create or find a game to play with other users
- Participate in a game by doing the actions expressed in the rules

## Requirements

The following sections group the system requirements by type and by domain functionality.
Each requirement will be followed by its own acceptance criteria(s).

### 1. Functional

#### 1.1 Lobby Management

- 1.1.1: The app must let an user see available lobbies created by other users.
    - The user must be able to see a list of all lobbies with their name, an indicator if the lobby is password protected and the number of users in the lobby.
- 1.1.2: The app must let an user join an existing lobby, even if password-protected, while it is not in a lobby.
    - The user must be able to join a lobby from the list (see Req. 1.1.1) if the lobby has not reached the maximum number of participants.
    - If the lobby is password protected the app must prompt the user for the password. 
    - When an user succesfully joins a lobby, all other users should be notifyed of the updated participant count for the lobby.
    - The funtionality must be available only when the user is not in a lobby already.
    - The user must not be able to join multiple lobbies concurrently.
- 1.1.3: The app must let an user create a new lobby, while the user is not in a lobby.
    - The user must be able create a new lobby that other users will be able to see and join.
    - The funtionality must be available only when the user is not in a lobby already.
    - When a user creates a lobby, it becomes also it's owner.
    - The user can't be whe owner of multiple lobbies concurrently.
- 1.1.4: The app must let a lobby owner set the lobby name and password.
    - The the lobby owner must be able to insert or change the lobby name and the password.
    - The name must not be empty, while the password *may* be empty.
    - When the changes are applied, all other users (both in the lobby and outside) must receive the updated information.
- 1.1.5: The app must let a user that is in a lobby leave it.
    - The app must let a user exit from the lobby it is in, notifying all other users of the updated user count for the lobby.
    - The funtionality must be available only when the user is in a lobby.
- 1.1.6: The app must let the lobby owner delete its lobby.
    - The lobby owner must be able to delete its lobby, notifying all users that the lobby doesn't exist anymore and kicking out the users who where in the lobby.
- 1.1.7: The app must let the lobby owner start the game with all the lobby users as participants.
    - The app must let the lobby owner start a game match where the users that are in the lobby become players in the game.
    - Also, no more players must be able to join the lobby.
- 1.1.8: The app must show, when an user is in a lobby, the other participants.
    - The user must be able to see a list of all the participants in the lobby it is currently in, with their name.
    - The app *may* emphasize who is the local user and who is the lobby owner.

#### 1.2 Gameplay

- 1.2.1 **TODO**

#### 1.3 Miscellaneous

- 1.3.1 The app must let a user set its display name.
    - The user must be able to set a name that will be shown to other users.
    - No restriction should be applied if the name is alreday used, so two users can have the same name.

### 2. Non-functional

- 2.1: The lobby management should not have network partition protection systems in place.
    - If the lobby management system has network problems or is not available, then the users can not access it.
    - By consequence, if a single user loses connection to the lobby management system, no
- 2.2: The lobby management must provide always consistency and correctness of data.
    - The information the users receve from the lobby management system must always be corrent and up-to-date, taking into account network delays.
- 2.3: 

### 3. Implementation

- The requirements must explain __what__ (not how) the software being produced should do. 
    * you should not focus on the particular problems, but exclusively on what you want the application to do.

- Requirements must be clearly identified, and possibly numbered

- Requirements are divided into:
    - **Functional**: some functionality the software should provide to the user
    - **Non-functional**: requirements that do not directly concern behavioural aspects, such as consistency, availability, etc.
    - **Implementation**: constrain the entire phase of system realization, for instance by requiring the use of a specific programming language and/or a specific software tool
        + these constraints should be adequately justified by political / economic / administrative reasons...
        + ... otherwise, implementation choices should emerge _as a consequence of_ design

- If there are domain-specific terms, these should be explained in a glossary

- Each requirement must have its own __acceptance criteria__
    + these will be important for the validation phase

## Design

This chapter explains the strategies used to meet the requirements identified in the analysis.
Ideally, the design should be the same, regardless of the technological choices made during the implementation phase.

> You can re-order the sections as you prefer, but all the sections must be present in the end

### Architecture

- Which architectural style? 
    + why?

### Infrastructure

- are there _infrastructural components_ that need to be introduced? _how many_?
    * e.g. _clients_, _servers_, _load balancers_, _caches_, _databases_, _message brokers_, _queues_, _workers_, _proxies_, _firewalls_, _CDNs_, _etc._

- how do components	_distribute_ over the network? _where_?
    * e.g. do servers / brokers / databases / etc. sit on the same machine? on the same network? on the same datacenter? on the same continent?

- how do components _find_ each other?
    * how to _name_ components?
    * e.g. DNS, _service discovery_, _load balancing_, _etc._

> Component diagrams are welcome here

### Modelling

- which __domain entities__ are there?
    * e.g. _users_, _products_, _orders_, _etc._

- how do _domain entities_ __map to__ _infrastructural components_?
    * e.g. state of a video game on central server, while inputs/representations on clients
    * e.g. where to store messages in an IM app? for how long?

- which __domain events__ are there?
    * e.g. _user registered_, _product added to cart_, _order placed_, _etc._

- which sorts of __messages__ are exchanged?
    * e.g. _commands_, _events_, _queries_, _etc._

- what information does the __state__ of the system comprehend
    * e.g. _users' data_, _products' data_, _orders' data_, _etc._

> Class diagram are welcome here

### Interaction

- how do components _communicate_? _when_? _what_? 
- _which_ __interaction patterns__ do they enact?

> Sequence diagrams are welcome here

### Behaviour

- how does _each_ component __behave__ individually (e.g. in _response_ to _events_ or messages)?
    * some components may be _stateful_, others _stateless_

- which components are in charge of updating the __state__ of the system? _when_? _how_?

> State diagrams are welcome here

### Data and Consistency Issues

- Is there any data that needs to be stored?
    * _what_ data? _where_? _why_?

- how should _persistent data_ be __stored__?
    * e.g. relations, documents, key-value, graph, etc.
    * why?

- Which components perform queries on the database?
    * _when_? _which_ queries? _why_?
    * concurrent read? concurrent write? why?

- Is there any data that needs to be shared between components?
    * _why_? _what_ data?

### Fault-Tolerance

- Is there any form of data __replication__ / federation / sharing?
    * _why_? _how_ does it work?

- Is there any __heart-beating__, __timeout__, __retry mechanism__?
    * _why_? _among_ which components? _how_ does it work?

- Is there any form of __error handling__?
    * _what_ happens when a component fails? _why_? _how_?

### Availability

- Is there any __caching__ mechanism?
    * _where_? _why_?

- Is there any form of __load balancing__?
    * _where_? _why_?

- In case of __network partitioning__, how does the system behave?
    * _why_? _how_?

### Security

- Is there any form of __authentication__?
    * _where_? _why_?

- Is there any form of __authorization__?
    * which sort of _access control_?
    * which sorts of users / _roles_? which _access rights_?

- Are __cryptographic schemas__ being used?
    * e.g. token verification, 
    * e.g. data encryption, etc.

--- 
<!-- Riparti da qui  -->

## Implementation

- which __network protocols__ to use?
    * e.g. UDP, TCP, HTTP, WebSockets, gRPC, XMPP, AMQP, MQTT, etc.
- how should _in-transit data_ be __represented__?
    * e.g. JSON, XML, YAML, Protocol Buffers, etc.
- how should _databases_ be __queried__?
    * e.g. SQL, NoSQL, etc.
- how should components be _authenticated_?
    * e.g. OAuth, JWT, etc.
- how should components be _authorized_?
    * e.g. RBAC, ABAC, etc.

### Technological details

- any particular _framework_ / _technology_ being exploited goes here

## Validation

### Automatic Testing

- how were individual components **_unit_-test**ed?
- how was communication, interaction, and/or integration among components tested?
- how to **_end-to-end_-test** the system?
    * e.g. production vs. test environment

- for each test specify:
    * rationale of individual tests
    * how were the test automated
    * how to run them
    * which requirement they are testing, if any

> recall that _deployment_ __automation__ is commonly used to _test_ the system in _production-like_ environment

> recall to test corner cases (crashes, errors, etc.)

### Acceptance test

- did you perform any _manual_ testing?
    * what did you test?
    * why wasn't it automatic?


## Release

- how where components organized into _inter-dependant modules_ or just a single monolith?
    * provide a _dependency graph_ if possible

- were modules distributed as a _single archive_ or _multiple ones_?
    * why?

- how were archive versioned? 

- were archive _released_ onto some archive repository (e.g. Maven, PyPI, npm, etc.)?
    * how to _install_ them?

## Deployment

- should one install your software from scratch, how to do it?
    * provide instructions
    * provide expected outcomes

## User Guide

- how to use your software?
    * provide instructions
    * provide expected outcomes
    * provide screenshots if possible


## Self-evaluation

- An individual section is required for each member of the group
- Each member must self-evaluate their work, listing the strengths and weaknesses of the product
- Each member must describe their role within the group as objectively as possible. 
It should be noted that each student is only responsible for their own section