#!/bin/bash

mvn clean compile assembly:single
mv target/heisenberg-jar-with-dependencies.jar target/heisenberg.jar 