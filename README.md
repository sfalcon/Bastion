# Overview

Bastion is a library that eases the development of end-to-end tests for HTTP APIs. A typical user would write 
tests using Bastion based off of some sort of API specification. This API specification can be anything like a 
WADL file, RAML file or a JSON schema. A test engineer would prepare requests and responses based on these specifications 
to test the overall process of calling these APIs.

# Usage

## Test

* Simple Get Request:
```java
Bastion.request("Get the Restaurant's Name", GeneralRequest.get("http://localhost:9876/restaurant"))
        .withAssertions((statusCode, response, model) -> assertThat(model).isEqualTo("The Sushi Parlour"))
        .call();
```

* Simple Post Request:
```java
Bastion.request("Change the Restaurant's Name", GeneralRequest.post("http://localhost:9876/restaurant", "The Fish Parlour"))
        .withAssertions((statusCode, response, model) -> assertThat(model).isEqualTo("The Fish Parlour"))
        .call();
```

* Simple JSON Assertion: (property order in response does not affect test)
```java
Bastion.request("Get Nigiri Info", GeneralRequest.get("http://localhost:9876/nigiri"))
        .withAssertions(JsonResponseAssertions.fromString(200, "{ \"id\":5, \"name\":\"Salmon Nigiri\", \"price\":23.55 }"))
        .call();
```

## Dependency

Use a dependency management tool such as Maven to download the Bastion library for use in your project. Add the following
dependency to your POM file:
```xml
<dependency>
    <groupId>rocks.bastion</groupId>
    <artifactId>bastion</artifactId>
    <version>0.3-SNAPSHOT</version>
    <scope>test</scope>
<dependency>
```

# Building

Use Maven to build Bastion and run the associated tests. After checking out the repository 
use the following command to build and test the source code.

    mvn install

# Contributing

Bastion is an open-source project which means that we will gladly accept contributions. Simply 
fork the repository and submit pull requests for review. Do use standard good coding practice 
in your submissions and read the [roadmap](https://github.com/KPull/Bastion/wiki/Roadmap) which 
is an indication of what we really need next.
