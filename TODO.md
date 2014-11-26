# Tasks

- [ ] Figure out how to secure java script for our own servers:  Check out Rhino's SandboxShutter
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
- [ ] extend activity worker patter scalability
      We start with one scalable job executor.  But that is one scale for all jobs.
      When we add the ability to dedicate certain job executors to specific activities, then 
      we are on par with SWF
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
