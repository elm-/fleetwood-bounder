# Roadmap

- [x] service loading of pluggable types (ActivityType, Type, Service)
- [x] programatic registration of pluggable types (ActivityType, Type, Service)
- [x] fluent process builder api
- [x] process parsing error reporting with i18n support

- [ ] process engine java interface
  - [ ] jackson (de)serializable to json
  - [ ] must be possible to build rest interface on top 1-1
- [ ] activity pluggability
- [ ] activity parameters
- [ ] javascript expressions
- [ ] types
- [ ] json (de)serialization
  - [ ] process instance
  - [ ] updates & operations
  - [ ] types
- [ ] type system 
- [ ] expressions
- [ ] ensure users can do simple versioning on top
- [ ] change startProcessInstance return value into StartProcessInstanceResponse
  - [ ] include process instance full state (as is returned now)
  - [ ] add all process events as a kind of logs
- [ ] split up into multiple modules
  - [ ] one for the api
  - [ ] one for impl + spi
  - [ ] one for each integration
  - [ ] maybe an mvn command (on the parent project?) to bundle them in a single jar
  - [ ] this way we get the minimal classpaths tested
- [ ] activity in / output parameters
- [ ] bpmn process logic coverage
- [ ] bpmn serialization and parsing
- [ ] mongodb persistence
- [ ] jdbc persistence
- [ ] load testing
- [ ] activity pluggability (java)
- [ ] activity pluggability (remote/HTTP)
- [ ] activity types
  - [ ] HTTP invocation
  - [ ] Send email
- [ ] timers
- [ ] object type declarations in process definition 
- [ ] static persistable process variables
- [ ] transient execution context variables
- [ ] ensure jackson lib is not required if json is not used
- [ ] process debugger service (separate top level interface required)
  - [ ] based on the in memory process engine
  - [ ] add breakpoints
  - [ ] ensure all async work is executed synchronous 

Unsure if in scope:
* multi language support ?

# Design principles

* Minimal library dependencies

# Conventions

* properties called 'name' refers to a user defined identifier. Names are typically unique within a certain scope like eg the process definition.
* properties called 'id' refers to a process engine generated identifier.  Ids are globally unique and refer to database identifiers.
* properties ending in RefId or RefName means this property is a reference to another object

# Design topics

* Typed id's vs strings