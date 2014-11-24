# Tasks

- [ ] Figure out how to secure java script for our own servers:  Check out Rhino's SandboxShutter
- [ ] Change Long to our own Date type.  This way, date serialization can be customized.
- [ ] Consider separate methods in the api for setting java values and setting rest json values
- [ ] http://blog.denevell.org/java-jersey-jetty-rest-gradle-tutorial-setup-quick.html

# In progress

- [x] simple api 
  - [x] easy to use fluent api
  - [x] easy programmable creation and deployment of process models
  - [x] jackson json support for process engine interface
- [x] process parsing with error & warning reporting
  - [x] location support (java & file) 
  - [x] prepared to add i18n on top
- [x] easy plugin architecture
  - [x] programmable registration of pluggable types
  - [x] service loading of pluggable types
  - [x] activity types
     - [x] user defined activity types
     - [x] engine level user defined activity types
     - [x] inline defined and configured activity types
  - [x] data types
     - [x] user defined data types
     - [x] process level configured data types
     - [x] engine level configured data types
     - [x] inline defined and configured data types
     - [ ] default types like text, ... 
     - [ ] user defined object types (without beans) 
     - [ ] reference type
  - [ ] data sources
  - [ ] script functions
- [x] activity in / output parameters
  - [x] static value 
  - [x] variable binding 
  - [x] expression binding in any script language 
- [x] process execution
  - [x] easy to understand activity instance model
  - [x] support for BPMN default semantics
  - [x] synchonous and asynchronoux execution of activities
- [x] pluggable persistence architecture 
- [x] transient execution context variables

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
- [ ] allow for easy collection of process instance logs to track what has happened
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
