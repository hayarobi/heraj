[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![LoC](https://tokei.rs/b1/github/aergoio/heraj)](https://github.com/aergoio/heraj)
[![Travis_ci](https://travis-ci.org/aergoio/heraj.svg?branch=master)](https://travis-ci.org/aergoio/heraj)
[![codecov.io](http://codecov.io/github/aergoio/heraj/coverage.svg?branch=master)](http://codecov.io/github/aergoio/heraj?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/a0aa6cecd0067bddc770/maintainability)](https://codeclimate.com/github/aergoio/heraj/maintainability)

# Introduction
The hera is the client-side framework for the aergo.
This repository, heraj is java implementation for hera.

## Download

### Maven
```sh
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com</url>
  </repository>
</repositories>
...
<dependencies>
  <dependency>
    <groupId>io.aergo</groupId>
    <artifactId>hera-transport</artifactId>
    <version>${RECENT_VERSION}</version>
  </dependency>
</dependencies>
```

### Gradle
```sh
repositories {
  jcenter()
}
...
dependencies {
  implementation 'io.aergo:hera-transport:${RECENT_VERSION}'
}
```

The heraj provides the next:
* Utilities
* Aergo client(both low and high level API)
* Integration to other useful frameworks.
* Rapid development tools
* Boilerplate and examples

## Modules
The repository contains next:
* core/annotation
* core/util
* core/common
* core/protobuf
* core/transport

# Integration
TBD

# Build
## Prerequisites
* [JDK 8](http://openjdk.java.net/)

## Clone
```console
$ git clone --recurse-submodule https://github.com/aergoio/heraj.git
```

## Build and package
* Initialize submodule (if not initialized)
```console
$ git submodule init
```

* Update submodule
```console
$ git submodule update
```

* Clean
```console
$ ./build.sh clean
```

* Run gradle
```console
$ ./build.sh gradle
```

# Test
## Kind of test
### Unit test
They are classes with 'Test' suffix.

### Integration test
They are classes with 'IT' suffix meaning integration test.

### Benchmark test
They are classes with 'Benchmark' suffix, which using jmh.

## Run tests
```console
$ ./build.sh test
```

# Documentation
We provides next in https://aergoio.github.io/heraj
* JavaDoc
* Test Coverage

## How to build documents
```console
$ ./build.sh docs
```

# Contribution

Guidelines for any code contributions:

1. Any changes should be accompanied by tests. It's guaranteed by travis ci.
2. Code coverage should be maintained. Any requests dropping down code coverage significantly will be not confirmed.
3. All contributions must be licensed MIT and all files must have a copy of statement indicating where license is (can be copied from an existing file).
4. All java files should be formatted according to [Google's Java style guide](http://google.github.io/styleguide/javaguide.html). You can use checkstyle plugin for [eclipse](https://checkstyle.org/eclipse-cs/#!/) or [IntelliJ](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea). And you can check by running `./build.sh gradle`
5. All java files must have a well-formed java docs. Make sure `./build.sh docs` generates the right page.
6. Please squash all commits for a change into a single commit (this can be done using git rebase -i). Make sure to have a meaningful commit message for the change.
