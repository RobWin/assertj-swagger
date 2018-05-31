= assertj-swagger
:author: Robert Winkler
:version: 0.9.0
:hardbreaks:

image:https://travis-ci.org/RobWin/assertj-swagger.svg["Build Status", link="https://travis-ci.org/RobWin/assertj-swagger"] image:https://coveralls.io/repos/RobWin/assertj-swagger/badge.svg?branch=master["Coverage Status", link="https://coveralls.io/r/RobWin/assertj-swagger"] image:https://api.bintray.com/packages/robwin/maven/assertj-swagger/images/download.svg[link="https://bintray.com/robwin/maven/assertj-swagger/_latestVersion"] image:http://img.shields.io/badge/license-ASF2-blue.svg["Apache License 2", link="http://www.apache.org/licenses/LICENSE-2.0.txt"] image:https://img.shields.io/badge/Twitter-rbrtwnklr-blue.svg["Twitter", link="https://twitter.com/rbrtwnklr"]

== Overview

assertj-swagger is a https://github.com/joel-costigliola/assertj-core[assertj] library which compares a contract-first https://github.com/swagger-api/swagger-spec[Swagger] YAML/JSON file with a code-first Swagger JSON output (e.g. from https://github.com/springfox/springfox[springfox] or https://github.com/swagger-api/swagger-core/wiki/Java-JAXRS-Quickstart[JAX-RS Swagger]). assertj-swagger allows to validate that the API implementation is in compliance with a contract specification for two test patterns: Documentation Driven Contracts and Consumer Driven Contracts.

The Documentation Driven Contracts test pattern, useful for public APIs, validates using `#isEqualTo` and will validate that the design first documentation contract matches the implementation in its entirety.

The Consumer Driven Contracts test pattern, useful for internal microservice APIs, validates using `#satisfiesContract` and will validate that the implementation provides, at minimum, the requirements of the design first consumer contract.  This pattern allows for extension points in the API resources, resource methods, and models.

The library supports the Swagger v2.0 specification. assertj-swagger compares Swagger objects like Paths, Parameters and Definitions. It does not compare __unimportant__ Swagger objects like info, descriptions or summaries.

== Usage guide

=== Adding assertj-swagger to your project
The project is published in JCenter and Maven Central.

==== Maven

[source,xml, subs="specialcharacters,attributes"]
----
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.robwin</groupId>
    <artifactId>assertj-swagger</artifactId>
    <version>{version}</version>
</dependency>
----

==== Gradle

[source,groovy, subs="attributes"]
----
repositories {
    jcenter()
}

compile "io.github.robwin:assertj-swagger:{version}"
----

=== Using assertj-swagger in an integration test

Using assertj-swagger is simple. For example, if you are using https://github.com/spring-projects/spring-boot[Spring Boot] and https://github.com/springfox/springfox[springfox] or https://github.com/swagger-api/swagger-core/wiki/Java-JAXRS-Quickstart[JAX-RS Swagger], you can validate your Swagger JSON in an integration test.

The following code sample shows how to validate an API using the Documentation Driven Contract test pattern:

[source, java]
----
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@WebAppConfiguration
public class AssertjSwaggerDocumentationDrivenTest {
    @Test
    public void validateThatImplementationMatchesDocumentationSpecification(){
        String designFirstSwagger = SwaggerAssertTest.class.getResource("/swagger.yaml").getPath();
        SwaggerAssertions.assertThat("http://localhost:8080/v2/api-docs")
            .isEqualTo(designFirstSwagger);
    }
}
----

The following code sample shows how to validate an API using the Consumer Driven Contract test pattern:

[source, java]
----
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@WebAppConfiguration
public class AssertjSwaggerConsumerDrivenTest {
    @Test
    public void validateThatImplementationSatisfiesConsumerSpecification(){
        String designFirstSwagger = SwaggerAssertTest.class.getResource("/swagger-consumer1.yaml").getPath();
        SwaggerAssertions.assertThat("http://localhost:8080/v2/api-docs")
            .satisfiesContract(designFirstSwagger);
    }
}
----

==== Example output

For Documentation Driven Contract tests, Assertj-swagger fails a test if it finds differences between the implementation and the specification.

[source]
----
The following 4 assertions failed:
1) [Checking Paths]
Expecting:
  <["/api/pet", "/api/pet/findByStatus", "/api/pet/findByTags", "/api/pet/{petId}", "/api/store/order", "/api/store/order/{orderId}", "/api/user", "/api/user/createWithArray", "/api/user/createWithList", "/api/user/login", "/api/user/logout", "/api/user/{username}"]>
to contain only:
  <["/pets", "/pets/findByStatus", "/pets/findByTags", "/pets/{petId}", "/stores/order", "/stores/order/{orderId}", "/users", "/users/createWithArray", "/users/createWithList", "/users/login", "/users/logout", "/users/{username}"]>
elements not found:
  <["/pets/findByTags", "/users/logout", "/users", "/stores/order", "/users/createWithArray", "/pets", "/users/createWithList", "/pets/findByStatus", "/pets/{petId}", "/users/{username}", "/stores/order/{orderId}", "/users/login"]>
and elements not expected:
  <["/api/store/order", "/api/user", "/api/user/createWithList", "/api/pet", "/api/pet/findByTags", "/api/user/createWithArray", "/api/user/login", "/api/pet/{petId}", "/api/store/order/{orderId}", "/api/user/{username}", "/api/pet/findByStatus", "/api/user/logout"]>

2) [Checking properties of definition 'Order']
Expecting:
  <["complete", "id", "identifier", "petId", "quantity", "shipDate", "status"]>
to contain only:
  <["id", "petId", "quantity", "shipDate", "status", "complete"]>
elements not found:
  <[]>
and elements not expected:
  <["identifier"]>

3) [Checking properties of definition 'User']
Expecting:
  <["email", "firstName", "id", "identifier", "lastName", "password", "phone", "userStatus", "username"]>
to contain only:
  <["id", "username", "firstName", "lastName", "email", "password", "phone", "userStatus"]>
elements not found:
  <[]>
and elements not expected:
  <["identifier"]>

4) [Checking properties of definition 'Pet']
Expecting:
  <["category", "id", "identifier", "name", "photoUrls", "status", "tags"]>
to contain only:
  <["id", "category", "name", "photoUrls", "tags", "status"]>
elements not found:
  <[]>
and elements not expected:
  <["identifier"]>
----

For Consumer Driven Contract tests,  Assertj-swagger fails a test if it finds missing resources, methods, models, or properties in the implementation which are required by the consumer specification.

[source]
----
The following 4 assertions failed:
1) [Checking Paths]
Expecting:
 <["/pets", "/pets/findByStatus", "/pets/findByTags", "/pets/{petId}", "/stores/order", "/stores/order/{orderId}", "/users", "/users/createWithArray", "/users/createWithList", "/users/login", "/users/logout", "/users/{username}"]>
to contain:
 <["/animals/{animalId}", "/pets", "/pets/findByStatus", "/pets/{petId}"]>
but could not find:
 <["/animals/{animalId}"]>

2) [Checking Definitions]
Expecting:
 <["User", "Category", "Pet", "Tag", "Order"]>
to contain:
 <["Category", "Pet", "Animal", "Tag"]>
but could not find:
 <["Animal"]>

3) [Checking properties of definition 'Pet']
Expecting:
 <["id", "category", "name", "photoUrls", "tags", "status"]>
to contain:
 <["photoUrls", "extraProperty", "name", "id", "category", "tags", "status"]>
but could not find:
 <["extraProperty"]>

4) [Checking property 'extraProperty' of definition 'Pet']
Expecting actual not to be null
----

=== Using assertj-swagger in a unit test

If you are using the https://github.com/spring-projects/spring-framework[spring-framework] and https://github.com/springfox/springfox[springfox], Spring's MVC Test framework can also be used to validate the Swagger JSON output against your contract-first Swagger specification.
That way you can make sure that the implementation is in compliance with the design specification.

The following code sample shows how to write a unit test using the Documentation Driven Contract test pattern:

[source, java]
----
@Test
public void validateThatImplementationFitsDesignSpecification() throws Exception {
    String designFirstDocumentationSwaggerLocation = Swagger2MarkupTest.class.getResource("/swagger.yaml").getPath();

    MvcResult mvcResult = this.mockMvc.perform(get("/v2/api-docs")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String springfoxSwaggerJson = mvcResult.getResponse().getContentAsString();
    SwaggerAssertions.assertThat(new SwaggerParser().parse(springfoxSwaggerJson)).isEqualTo(designFirstDocumentationSwaggerLocation);
}
----

The following code sample shows how to write a unit test using the Consumer Driven Contract test pattern:

[source, java]
----
@Test
public void validateThatImplementationFitsDesignSpecification() throws Exception {
    String designFirstConsumerSwaggerLocation = Swagger2MarkupTest.class.getResource("/swagger-consumer1.yaml").getPath();

    MvcResult mvcResult = this.mockMvc.perform(get("/v2/api-docs")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String springfoxSwaggerJson = mvcResult.getResponse().getContentAsString();
    SwaggerAssertions.assertThat(new SwaggerParser().parse(springfoxSwaggerJson)).satisfiesContract(designFirstConsumerSwaggerLocation);
}
----

=== Customizing assertj-swagger's behaviour

For most use cases, the default behaviour will be sufficient.  However, you can override the default behaviour in various ways by placing a Java property file, `/assertj-swagger.properties`, at the root of your classpath.  It is also possible to override the configuration in your tests; construct an instance of the `SwaggerAssert` class with a custom configuration if this is required.

The following overrides are available:

==== Disable various types of checks which are enabled by default

* `assertj.swagger.validateDefinitions=false`: disable all validation of definitions
** `assertj.swagger.validateProperties=false`: disable validation of properties of definitions
*** `assertj.swagger.validateRefProperties=false`: disable validation of reference (`$ref`) properties of definitions
*** `assertj.swagger.validateArrayProperties=false`: disable validation of array properties of definitions
*** `assertj.swagger.validateByteArrayProperties=false`: disable validation of byte-array properties of definitions
*** `assertj.swagger.validateStringProperties=false`: disable validation of string properties of definitions
** `assertj.swagger.validateModels=false`: disable validation of models
* `assertj.swagger.validatePaths=false`: disable all validation of endpoint definitions
* `assertj.swagger.validateResponseWithStrictlyMatch=false`: allow actual contract return extra return code

==== Enable various types of checks which are disabled by default

The following settings are disabled by default, as they will cause schema comparisions to be too brittle for many users. They can be enabled if required.

* `assertj.swagger.validateInfo=true`: enable comparison of the info section
* `assertj.swagger.validateVersion=true`: enable comparison of the schema version numbers

==== Disable checks for certain paths or definitions in 'actual' schema

This feature is useful in development situations, where you have written a contract-first schema by hand, and are validating a contract-last schema generated by a partially-implemented API.

To ignore unimplemented endpoints, try something like:

[source]
----
assertj.swagger.pathsToIgnoreInExpected=\
   /v1/friends/{id},\
   /v1/groups/{groupId}
----

To ignore unimplemented definitions, use something like:

[source]
----
assertj.swagger.definitionsToIgnoreInExpected=\
   Foo,\
   Bar
----

To ignore unimplemented properties, use something like:

[source]
----
assertj.swagger.propertiesToIgnoreInExpected=\
   Foo.prop1,\
   Bar.prop2
----


==== Comparing expected and actual paths in schemas

It is occasionally useful to be able to compare schemas, where due to limitations in tools and libraries, endpoint
paths don't align. Specifying a `basePath` setting in your design-first schema here won't work -- it's only used by
Swagger tooling to generate paths at runtime, and does *not* form part of the logical pathname of your endpoints.
For instance, in your design-first schema, you may specify a set of endpoints and a `basePath`, while your generated
schema (generated from, say, Springfox) has a common prefix prepended on the endpoint paths; e.g.:

[source]
----
/pets/findByStatus       ! design-first schema
----

and

[source]
----
/v2/pets/findByStatus    ! actual schema
----

To ensure that assertj-swagger is comparing like-with-like in this situation, you could use the following in your
configuration file:

[source]
----
assertj.swagger.pathsPrependExpected=/v2
----


== License

Copyright 2015 Robert Winkler

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
