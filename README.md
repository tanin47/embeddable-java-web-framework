Embeddable Java Web Framework
==============================

Embeddable Java Web Framework (EJWF) is a Java project template for building a website with a tiny footprint. 
It is suitable for <ins>a sidecar-style website embeddable on a larger system</ins> and a standalone lightweight website.

The main selling point of EJWF is that it comes with productive and useful conventions and libraries such as:

1. Support Typescripts + Svelte + Tailwind + DaisyUI with Hot-Reload Module (HMR).
2. Support packaging a fat JAR with [shading](https://stackoverflow.com/questions/13620281/what-is-the-maven-shade-plugin-used-for-and-why-would-you-want-to-relocate-java). 
   The JAR is 350KB in size, has *zero* external dependencies, and eliminates any potential dependency conflict when embedding into another JVM system.
3. Support Proguard that can reduce the JAR size to be even smaller.
4. Avoid Java reflection and magic. This is largely a feature of [Minum](https://github.com/byronka/minum). Any potential runtime errors and conflicts are minimized, which is important when embedding into a larger system.

In contrast, most of the lightweight web frameworks focus on being a bare metal web server serving HTML and JSON. 
They don't provide support for any frontend framework like React or Svelte; you would have to do it yourself. This is exactly what EJWF provides.

Initially, EJWF was built as a foundation for [Backdoor](https://github.com/tanin47/backdoor), an embeddable sidecar-style JVM-based database administration tool, where
you can embed it into your larger application like SpringBoot or PlayFramework.

How to develop
---------------

1. Run `npm install` to install all dependencies.
2. Run `sbt ~reStart` in order to run the web server with hot-reload enabled on the Java side.
  * Most changes will hot-reload just fine. There might be occasional cases where you may have to restart the command.
  * A non-hot-reload alternative is `sbt run`.
3. On a separate terminal, run `npm run hmr` in order to hot-reload the frontend code changes.


Publish
--------

EJWF is a template repository with collections of libraries and conventions. It's important that you understand
each build process and are able to customize to your needs.

Here's how you can build your fat JAR:

1. Build the tailwindbase.css with: `./node_modules/.bin/postcss ./frontend/stylesheets/tailwindbase.css --config . --output ./src/main/resources/assets/stylesheets/tailwindbase.css`
2. Build the production Svelte code with: `ENABLE_SVELTE_CHECK=true ./node_modules/webpack/bin/webpack.js --config ./webpack.config.js --output-path ./src/main/resources/assets --mode production`
3. Build the fat JAR with: `sbt assembly`

The far JAR is built at `./target/scala-2.12/ejwf.jar`

You can run your server with: `java -jar ejwf.jar tanin.ejwf.Main`

To publish to a Maven repository, please follow the below steps:

1. Go into the SBT console by running `sbt`
2. Switch to the fatJar project by running `project fatJar`
3. Run `publishSigned`
4. Run `sonaUpload`
5. Go to https://central.sonatype.com and click "Publish"

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

Minum is the smallest web framework written in pure Java. One of its goals is to avoid reflection and magic, which is great for embeddability.

I've looked at a couple other options:

* Javalin requires Kotlin runtime, which adds 2-3MB to the JAR.
* Vert.x is not a minimal web framework. It focuses on reactivity.
* Blade is comparable but doesn't seem to focus on avoiding reflection and magic.

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
