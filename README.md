### Message-Queue
This project is to design and implement a AWS SQS simulation that you can run locally and test against.

### Background
Amazon's SQS do not come with an offline implementation suitable for local development and testing.
The context of this project is to resolve this deficiency by designing a simple
message-queue API that supports three implementations:

 - an in-memory queue, suitable for same-JVM producers and consumers

 - a file-based queue, suitable for same-host producers and consumers, but
   potentially different JVMs

 - an adapter for a production queue service, such as SQS.

### Behavior
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
   
### Interface
The QueueService interface to cater for the essential actions:

 - push     pushes messages onto a specified queue
 
 - pull     receives a single message from a specified queue
 
 - delete   deletes a received message
 
### Description of Implement
#### In-memory queue
```
        +-------------------------+  ----> InMemoryQueueService
        | +---------------------+ |  -----
        | |       queue 1       | |    |
        | +---------------------+ |    |
        | +---------------------+ |    |
        | |       queue 2       | |  ConcurrentHashMap<queueName, InMemoryQueue>
        | +---------------------+ |    |
        |           ...           |    |
        | +---------------------+ |    |
        | |       queue n       | |    |
        | +----------|----------+ |  -----     
        +------------|------------+
                     |
                InMemoryQueue
                     |
                     |     |--------------> pull from top
                     |     |                when pull, tag the message with a non-nulll visible date
                     |     |                return the message, but not pop the message from queue
                     |     |
                     |     |
                    \|/    |                
 ------ +------------------|-------+  ----> InMemoryQueue
    |   |           top    |       |
    |   | +----------------------+ |  -----
    |   | |     message 1        | |    |
    |   | +----------------------+ |    |
    |   | +----------------------+ |    |
    |   | |     message 2        |-+----+--> delete a specified message 
  timer | +----------------------+ |    |    when delete the message
    |   |                          |    |    the message should have a non-null visible date
    |   |          ...             |    |
    |   |                          |    |
    |   |                          |  ConcurrentLinkedQueue<Message>
    |   | +----------------------+ |    | 
    |   | |     message n        | |    |
    |   | +------|---------------+ |  -----
    |   |        |         ^       |
    |   |        | bottom  |       |
 ------ +--------|---------|-------+
    |            |         |--------------- push message to bottom
    |            |                          the message has a null visible date
    |            |                          which means the message is visible
    |            |
    |------------+------------------------> timer use to scan the queue
                 |                          if a message is in invisible state
                 |                          and the visible date is a past time
                 |                          set the message to visible state
                 |                          which means the message can be pulled again
              Message                       
                 |
                \|/
        +-----------------------+  -----> Message
        |         data       ---+-------> message content
        |-----------------------|
        |     receiptHandle  ---+-------> unique Id, when push message to queue, assign one
        |-----------------------|
        |      visibleDate   ---+-------> the visible date is generated when the pull action happens 
        +-----------------------+         use the date when pull happend plus a visibilityTimeout
                                          to generate a visible date
        
```
#### File-based queue
```
       +------------------------+  -----> InFileQueueService
       |    sqs/queue1/message -+-------> each message file is a queue 
       |    sqs/queue1/.lock/   |         the subfolder under sqs/ named as the queue name
       |    sqs/queue2/message  |
       |    sqs/queue2/.lock/   |
       |          ...           |
   +---+--- sqs/queueN/message  |
   |   |    sqs/queueN/.lock/  -+-------> .lock/ folder use to make sure not only thread-safe
   |   |                        |         in a single VM, but also inter-process safe when used
   |   |                        |         concurrently in multiple VMs
   |   +------------------------+
   |
   +----------------+         +---------> when pull message, create a new file, read each line of message file
                    |         |           the first message in visible state is the message can be pulled.
                    |         |           modify the message's visible date to the time when pull happened plus a visibilityTimeout
                    |         |           write the message string to new file. The rest lines write into new file one by one
                    |         |           rename the new file to message
                    |         |
                    |         |---------> when delete message, create a new file, read each line of message file
                    |         |           if the message id is the one to be deleted, and message is in invisible state
                    |         |           means the message can be deleted, which means discard this line
                    |         |           other lines write in new file one by one
                    |         |           rename the new file to message
                   \|/        |
 ----- +----------------------|---------------------------------------------+  -----> InFileQueue
   |   |12345$1549521076609$hello                                           |         This is a file
   |   |23456$0$world                                                       |         each line is a message
 timer |...                                                                 |
   |   |56789$0$Java                                                        |
   |   |messageId$visibleDateInMillisecond$messageContent                   |
 ----- +----------|---------------------------^-----------------------------+
   |              |          /|\              |
   |              |           |               +-------- push message in to file end
   |              |           |                         the visibleDateInMillisecond is 0
   |              |           |                         which means the message is visible can be pulled
   |              |           |
   +--------------+-----------+-----------------------> timer use to scan the mesage file              
                  |           |                         find out the message which is in invisible date
                  |           |                         but the visible date is a past time
                  |           |                         modify the message's visibeDate to 0
                  |           |                         means the message is visible and can be pulled again
                  |           |                         write message to new file
                  |           |                         other lines write into new file one by one
                  |           |                         rename the new file to message
                  |           |
               Message      Message covert to string to store in file                 
                  |           |
                 \|/          |
         +-----------------------+  -----> Message
         |         data       ---+-------> message content
         |-----------------------|
         |     receiptHandle  ---+-------> unique Id, when push message to queue, assign one
         |-----------------------|
         |      visibleDate   ---+-------> the visible date is generated when the pull action happens 
         +-----------------------+         use the date when pull happend plus a visibilityTimeout
                                           to generate a visible date
        
```
