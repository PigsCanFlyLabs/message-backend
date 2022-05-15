## Client Service

This module is developed using Akka HTTP with Twilio and Swarm intergration to send and get messages.

### clean

This command cleans the sbt project by deleting the target directory. The command output relevant messages.
````
sbt clean
````

### compile

This command compiles the scala source classes of the sbt project.
````
sbt compile
````
### run

Enter the project folder and enter sbt run command:
````
sbt "project client" run
````

More details about project libraries (e.g. version etc.) can be found in files:
**build.sbt**
**Dependencies**
**CommonSettings**
**plugins.sbt**


## Routes

###### Note: json file (Admin Service.postman_collection) for the routes is added in the repository.


````
URL <- localhost:8080
````


###### Request: POST <- /admin/login
###### Body: raw JSON

###### Admin Login:

````
{
    "email": "admin@admin.com",
    "password": "C+/:~Bp75bU?ays:",
    "role": "admin"
}
````

#### Create Admin User  (accessible by admin/ super admin)
###### Request: POST <- /admin/create-admin-user
###### Header: requires admin/ super admin bearer token
###### Body: raw JSON
````
{
    "email": "",
    "password": "",
    "role": "admin"
}
````


##### Source files that are implementing this functionality are in packages:
* actor
* cache
* flyway
* handler
* models
* routes
* persistence module

## END
