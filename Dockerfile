FROM ibm-semeru-runtimes:open-25-jdk

WORKDIR /app

COPY ./build/staging-deploy/io/github/tanin47/embeddable-java-web-framework/**/*.jar .
