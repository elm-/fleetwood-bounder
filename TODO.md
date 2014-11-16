# In progress

- [x] simple api 
  - [x] easy to use fluent api
  - [x] easy programmable creation and deployment of process models
  - [x] jackson json support for process engine interface
- [x] process parsing with error & warning reporting
  - [x] location support (java & file) 
  - [x] prepared to add i18n on top
- [x] easy plugin architecture
  - [x] activity types 
  - [x] data types
  - [x] services
  - [ ] script functions
  - [x] service loading of pluggable types
  - [x] programmable registration of pluggable types
- [x] activity in / output parameters
  - [x] static value 
  - [x] variable binding 
  - [x] expression binding in any script language 
- [x] process execution
  - [x] easy to understand activity instance model
  - [x] support for BPMN default semantics
  - [x] synchonous and asynchronoux execution of activities
- [x] delegate validation of parameters to pluggable activities
- [ ] process engine java interface
  - [x] jackson (de)serializable to json
  - [x] must be possible to build rest interface on top 1-1
  - [x] enable support for multiple process languages
- [ ] pluggable types
  - [ ] atomic types like text, long, date, etc
  - [ ] object types like ... 
  - [ ] list type
  - [ ] reference type
- [x] pluggable persistence architecture 

# Roadmap

- [ ] bpmn serialization and parsing
- [ ] bpmn process logic coverage
- [ ] mongodb persistence
- [ ] jdbc persistence
- [ ] type declarations in process definition 
- [ ] split up into multiple modules
  - [ ] one for the api
  - [ ] one for impl + spi
  - [ ] one for each integration
  - [ ] maybe an mvn command (on the parent project?) to bundle them in a single jar
  - [ ] this way we get the minimal classpaths tested
  - [ ] ensure jackson lib is not required if json is not used
- [ ] change ProcessEngine.startProcessInstance return value into StartProcessInstanceResponse
  - [ ] include process instance full state (as is returned now)
  - [ ] add all (or some) process events as a kind of logs
- [ ] load testing
- [ ] activity types
  - [ ] HTTP invocation
  - [ ] Send email
  - [ ] Remote implemented activity (http)
  - [ ] Script (through ScriptEngine)
- [ ] timers
- [ ] data flow (only start an activity when the input data becomes available)
- [ ] derived variables
- [ ] static persistable process variables
- [ ] transient execution context variables
- [ ] process debugger service (separate top level interface required)
  - [ ] based on the in memory process engine
  - [ ] add breakpoints
  - [ ] ensure all async work is executed synchronous 

# Design principles

* Minimal library dependencies

# Conventions

* properties called 'name' refers to a user defined identifier. Names are typically unique within a certain scope like eg the process definition.
* properties called 'id' refers to a process engine generated identifier.  Ids are globally unique and refer to database identifiers.
* properties ending in RefId or RefName means this property is a reference to another object

# Design topics

* Typed id's vs strings
* multi language support ?
