Embeddable Java Web Framework
==============================

[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/io.github.tanin47/embeddable-java-web-framework/badge.png?8cb1875)](https://central.sonatype.com/artifact/io.github.tanin47/embeddable-java-web-framework)

Embeddable Java Web Framework (EJWF) is a Java project template for building a website with a tiny footprint. 
It is suitable for <ins>a sidecar-style website embeddable on a larger JVM system</ins> and a standalone lightweight website.

The main selling point of EJWF is that it comes with productive and useful conventions and libraries such as:

1. Support Typescripts + Svelte + Tailwind + DaisyUI with Hot-Reload Module (HMR).
2. Support packaging a fat JAR with [shading](https://stackoverflow.com/questions/13620281/what-is-the-maven-shade-plugin-used-for-and-why-would-you-want-to-relocate-java). 
   The JAR is 350KB in size, has *zero* external dependencies, and eliminates any potential dependency conflict when embedding into another JVM system.
3. Avoid Java reflection and magic. This is largely a feature of [Minum](https://github.com/byronka/minum). Any potential runtime errors and conflicts are minimized, which is important when embedding into a larger system.
4. Browser tests are setup and ready to go.
5. Github actions for testing, code coverage reporting, and publishing have been implemented.

In contrast, most of the lightweight web frameworks focus on being a bare metal web server serving HTML and JSON. 
They don't provide support for any frontend framework like React or Svelte; you would have to do it yourself. This is exactly what EJWF provides.

Initially, EJWF was built as a foundation for [embeddable-java-web-framework](https://github.com/tanin47/embeddable-java-web-framework), a self-hosted database querying and editing tool, where
you can embed it into your larger application like SpringBoot or PlayFramework.

How to develop
---------------

1. Run `npm install` to install all dependencies.
2. Run `./gradlew run` to run the web server.
3. On a separate terminal, run `npm run hmr` in order to hot-reload the frontend code changes.


Publish JAR
------------

This flow has been set up as the Github Actions workflow: `publish-jar`.

EJWF is a template repository with collections of libraries and conventions. It's important that you understand
each build process and are able to customize to your needs.

Here's how you can build your fat JAR:

1. Run `./gradlew clean publish`. This step is IMPORTANT to clean out the previous versions.

The far JAR is built at `./build/libs/embeddablee-java-web-framework-VERSION.jar`

You can run your server with: `java -jar ./build/libs/embeddable-java-web-framework-VERSION.jar`

To publish to a Maven repository, please follow the below steps:

1. Set up `~/.jreleaser/config.toml` with `JRELEASER_MAVENCENTRAL_USERNAME` and `JRELEASER_MAVENCENTRAL_PASSWORD`
2. Run `./gradlew jreleaserDeploy`


Publish Docker
---------------

This flow has been set up as a part of the Github Actions workflow: `create-release-and-docker`.

1. Run `docker buildx build --platform linux/amd64,linux/arm64 -t embeddable-java-web-framework:v1.0.0-rc1 .`
2. Test locally with:
   `docker run -p 9090:9090 --entrypoint "" embeddable-java-web-framework:v1.0.0-rc1 java -jar embeddable-java-web-framework-1.0.0-rc1.jar -port 9090`
3. Run: `docker tag embeddable-java-web-framework:v1.0.0-rc1 tanin47/embeddable-java-web-framework:v1.0.0-rc1`
4. Run: `docker push tanin47/embeddable-java-web-framework:v1.0.0-rc1`
5. Go to Render.com, sync the blueprint, and test that it works

Release a new version
----------------------

1. Create an empty release with a new tag. The tag must follow the format: `vX.Y.Z`.
2. Go to Actions and wait for the `create-release-and-docker` (which is triggered automatically) workflow to finish.
3. Test the docker with
   `docker run -p 9090:9090 --entrypoint "" tanin47/embeddable-java-web-framework:v1.0.0-rc1 java -jar embeddable-java-web-framework-1.0.0-rc1.jar -port 9090`.
4. Go to Actions and trigger the workflow `publish-jar` on the tag `vX.Y.Z` in order to publish the JAR to Central
   Sonatype.

Embed your website into a larger system
----------------------------------------

After you've built your application on top of this framework and publish your fat jar,
your customer can follow the below steps in order to embed your website into their applications.

1. The larger system should include your fat JAR as a dependency by adding the below dependency:

```
<dependency>
    <groupId>io.github.tanin47</groupId>
    <artifactId>embeddable-java-web-framework</artifactId>
    <version>0.1.1</version>
</dependency>
```


2. Instantiate the website with the port 9090 when the larger system initializes:

```java
var main = new tanin.ejwf.Main();
main.start(9090);
```

3. Visit http://localhost:9090 to confirm that the embedded website is working.

FAQ
-----

### Why is Minum chosen? 

Minum is the smallest web framework written in pure Java with *zero* dependencies. One of its goals is to avoid reflection and magic, which is great for embeddability.

I've looked at a couple other options:

* Javalin requires Kotlin runtime, which adds 2-3MB to the JAR.
* Vert.x is not a minimal web framework. It focuses on reactivity.
* Blade is comparable but doesn't seem to focus on avoiding reflection and magic.

The above options also have external dependencies.

### What if we cannot open another port?

Some services like Render or Heroku allow only one port to be served.

What you can do here is to designate a path e.g. `/ejwf` where it proxies to EWJF.

An example proxy code that requires no dependency would look like below:

```java
// In your endpoint of /ejwf
var client = HttpClient.newHttpClient();
var httpRequest = HttpRequest
    .newBuilder()
    .uri(URI.create("http://localhost:9090" + path)) // The path without /ejwf
    .method("GET", HttpRequest.BodyPublishers.ofByteArray(new byte[0])) // Set the method and body in bytes
    .headers(/* ... */) // Forward the headers as-is.    
    .build();
var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

// Return the response as-is
```
