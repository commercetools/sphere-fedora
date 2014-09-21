SPHERE.IO - Fedora
=====================

[![Build Status](https://travis-ci.org/commercetools/sphere-fedora.png)](https://travis-ci.org/commercetools/sphere-fedora)

This is a fully functional example web store for the [SPHERE.IO](http://sphere.io) PaaS.

## Live demo
Visit a live demo of SPHERE.io fedora store at [fedora.sphere.io](http://fedora.sphere.io/).

## Getting started

### Set it up
- Install at least JDK 6 on your machine. We recommend using [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html).
- [Clone](http://git-scm.com/book/en/Git-Basics-Getting-a-Git-Repository#Cloning-an-Existing-Repository) sphere-fedora project from GitHub. or download it as [zip file](https://github.com/commercetools/sphere-fedora/archive/master.zip).
- Run `./sbt run` or `sbt.bat run` (Windows) command in root project directory.
- Open your browser and point it to [http://localhost:9000](http://localhost:9000).

### Configure it

#### SPHERE.IO data
- Point to [SPHERE Login](https://admin.sphere.io/login) or register a new account with [SPHERE Signup](https://admin.sphere.io/signup).
- Create a new project, preferably with sample data.
- Go to `Developers -> API Clients` to retrieve your project data.
![API Backend](https://raw.github.com/commercetools/sphere-fedora/master/public/images/mc_api.png)
- To use your SPHERE.IO project, modify [sphere.project](https://github.com/commercetools/sphere-fedora/blob/master/conf/application.conf#L24), [sphere.clientId](https://github.com/commercetools/sphere-fedora/blob/master/conf/application.conf#L26) and [sphere.clientSecret](https://github.com/commercetools/sphere-fedora/blob/master/conf/application.conf#L28) in [conf/application.conf](https://github.com/commercetools/sphere-fedora/blob/master/conf/application.conf).

[More about the ecommerce PaaS SPHERE.IO.](http://dev.sphere.io)

## Deployment

### heroku

To run this SPHERE.IO example web shop on [heroku](https://www.heroku.com) just click the button:

<a href="https://heroku.com/deploy?template=https://github.com/commercetools/sphere-fedora"><img src="https://www.herokucdn.com/deploy/button.png" alt="Deploy"></a>

## Development

### Getting IDE settings from Play! framework

- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Generate configuration files for your chosen IDE, following [these instructions](http://www.playframework.com/documentation/2.2.x/IDE).
- Run `./sbt` command in root project directory.
- Inside the SBT shell, type `clean test` for compiling and testing it.
- Start SBT with `./sbt -jvm-debug 5005` to enable debugging with port 5005

### Use Typesafe Activator

- Typesafe Activator allows you to run and compile your code using a nice UI
- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Download Typesafe Activator mini-package setup, following [this link](https://typesafe.com/platform/getstarted).
- Run Typesafe Activator in root project directory using `$ ~/path/to/activator/folder/activator ui`
- Import project to your IDE using Activator GUI: `Code -> Settings -> Open Project in ...`
- Currently supports [IntelliJ IDEA](http://www.jetbrains.com/idea/) and [Eclipse](https://www.eclipse.org/)

### Special info: Eclipse

- [Eclipse](https://www.eclipse.org/) has no native support of Scala/Play applications
- Please use this prepackaged version of Eclipse: [Scala-IDE](http://scala-ide.org)
