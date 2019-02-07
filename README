*** SQS-Simulation
This project is to design and implement a AWS SQS simulation that you can run locally and test against.

*** Background
Amazon's SQS do not come with an offline implementation suitable for local development and testing.
The context of this project is to resolve this deficiency by designing a simple
message-queue API that supports three implementations:

 - an in-memory queue, suitable for same-JVM producers and consumers;

 - a file-based queue, suitable for same-host producers and consumers, but
   potentially different JVMs; and

 - an adapter for a production queue service, such as SQS.

*** Behavior
In particular, the message queue simulation is the following properties:

 - multiplicity
   A queue supports many producers and many consumers.

 - delivery
   A queue strives to deliver each message exactly once to exactly one consumer,
   but guarantees at-least once delivery (it can re-deliver a message to a
   consumer, or deliver a message to multiple consumers, in rare cases).

 - order
   A queue strives to deliver messages in FIFO order, but makes no guarantee
   about delivery order.

 - reliability
   When a consumer receives a message, it is not removed from the queue.
   Instead, it is temporarily suppressed (becomes "invisible").  If the consumer
   that received the message does not subsequently delete it within within a
   timeout period (the "visibility timeout"), the message automatically becomes
   visible at the head of the queue again, ready to be delivered to another
   consumer.
   
*** Interface
The QueueService interface to cater for the essential actions:
 - push     pushes messages onto a specified queue
 
 - pull     receives a single message from a specified queue
 
 - delete   deletes a received message
 
*** Description of Implement
***** In-memory queue

***** File-based queue
