# Roadmap

* types
* json (de)serialization
  * process instance
  * updates & operations
  * types
* type system 
* expressions
* activity in / output parameters
* bpmn process logic coverage
* bpmn serialization and parsing
* mongodb persistence
* jdbc persistence
* load testing
* activity pluggability (java)
* activity pluggability (remote/HTTP)
* activity types
  * HTTP invocation
  * Send email
* timers
* object type declarations in process definition 
* static persistable process variables
* transient execution context variables
* ensure jackson lib is not required if json is not used

Unsure if in scope:
* multi language support ?

# Deployment use cases

+--------------------------------------------------+
| Test case                                        |
| +----------------------+   +-------------------+ |
| | Effektif PVM library |-->| In memory objects | |
| +----------------------+   +-------------------+ |
+--------------------------------------------------+

  +--------------------------+
+--------------------------+ |
| User Java App            | |         +----------------------+   +----------------+
| +----------------------+ | |--HTTP-->| Effektif PVM Server  |   | JDBC / MongoDB |
| | Effektif PVM library | |----HTTP-->| containing library   |-->| Database       |
| +----------------------+ |-+         +----------------------+   +----------------+
+--------------------------+

+--------------------------+
| User Java App            |
| +----------------------+ |      +-------------------------+
| | Effektif PVM library |------->| JDBC / MongoDB Database |
| +----------------------+ |      +-------------------------+
+--------------------------+

+--------------------------+          +--------------------------+
| User Java App            |        +--------------------------+ |
| +----------------------+ |      +--------------------------+ |-+
| | Effektif PVM library |------->| Sharded MongoDB Database |-+
| +----------------------+ |      +--------------------------+
+--------------------------+

# Design principles

* Minimal library dependencies

# Design topics

* Typed id's vs strings