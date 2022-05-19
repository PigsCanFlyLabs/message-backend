## Client Service

This module is developed using Akka HTTP with Twilio and Swarm integration to execute send and retrieve messages.

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

More details about project libraraies (e.g. version etc..) can be found in files:
**build.sbt**
**Dependencies**
**CommonSettings**
**plugins.sbt**


## Routes


````
URL <- localhost:8080
````

#### Get Messages
###### Request: GET <- /messages

````
Response: OK
{
    "messageResponse": [
        {
            "packetId": 0,
            "deviceType": 0,
            "deviceId": 0,
            "deviceName": "string",
            "dataType": 0,
            "userApplicationId": 0,
            "len": 0,
            "data": "string",
            "ackPacketId": 0,
            "status": 0,
            "hiveRxTime": "2022-04-24T06:05:11.181Z"
        }
    ]
}
````



#### Send Message  
###### Request: POST <- /messages
###### Body: form-data
````
From  :  sender 
To    :  receiver
Body  :  message
````


##### Source files that are implementing this functionality are in packages:
* actor
* configs
* controllers
* httpClient
* scheduler
* service
* util
* persistence module

## END
