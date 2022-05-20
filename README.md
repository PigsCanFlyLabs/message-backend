# message-backend

##Product Overview

This product provides communication via SMS i.e. without the internet. It uses Twilio service to send/ receive SMS and Swarm Satellite services for the communication i.e. send message via swarm satellite APIs.
For each user a twilio phone number will be provided, that will be mapped to each device ID.
When a user sends a SMS to a Twilio phone then the receiver will get a message on phone via SMS and an email linked to the device Id as well.

##Workflow

* User sends an SMS from his Twilio phone number to another user’s Twilio phone number.
* When Twilio service receives SMS, It will trigger our application’s POST message route. 
* The POST message route will first check for the user's subscription and then accordingly it will send a request to Swarm satellite to get unread messages. 
* Once messages are fetched from Swarm satellite, a request will be sent to Twilio to send messages as SMS and email to the user.


##Design

There are two microservices:

* Admin Service: https://github.com/PigsCanFlyLabs/message-backend/tree/main/admin-service
* Client Service: https://github.com/PigsCanFlyLabs/message-backend/tree/main/client/src