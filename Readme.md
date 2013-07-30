# Camel Infinispan Transactional example

## Read about this example
on my [blog](http://www.ofbizian.com/2013/07/transactional-caching-for-camel-with.html)

(First get Camel Infinispan component: git clone https://github.com/bibryam/camel-infinispan.git)

## Start the example app
mvn clean install jetty:run

Create a person
curl -d "firstName=demo&lastName=person" http://localhost:8080/persons/

Check DB and Cache, both contain the new person with id 1
http://localhost:8080/persons/db/1
http://localhost:8080/persons/cache/1

Try to create a new damn person, which fails
curl -d "firstName=demo&lastName=damn" http://localhost:8080/persons/

Check DB and Cache, none contain the new person, the transaction was rolled back
curl -X GET http://localhost:8080/persons/db/1
curl -X GET http://localhost:8080/persons/cache/1

Comment out <transacted> from the route, and try to create the damn person again
curl -d "firstName=demo&lastName=damn" http://localhost:8080/persons/

Check DB and Cache, DB contains the new person, but the cache NOT. The app is in a inconsistent state
curl -X GET http://localhost:8080/persons/db/2
curl -X GET http://localhost:8080/persons/cache/2

## License

ASLv2

