# LogbackBaseInit
Simplify and unify Logback initialization between projects

When working with multiple projects against a common logging paradigm, it is a sane approach to unify system properties used for logging settings and extract common logging initializers as a separate library.

This is an example of using a single Groovy script class as a base initializer for concrete Logback initializing scripts in multiple projects.

 Using Groovy scripting magic, @BaseScript allows to extend the script class. The closure annotated with @BaseScript is treated as implementation of the only abstract method from the base class. The rest is done using script class' fields and property bindings.

 In this example, 'logging' module contains a base initializer (LogbackBaseInit class) which offers a micro-dsl for setting up file and [Splunk](http://www.splunk.com/) appenders. An example implementation is also provided. It's easy to imagine multiple modules or projects using this approach to unify logging standards between in a set microservices.