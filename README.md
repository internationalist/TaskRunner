# TaskRunner
A queue based processing engine that runs tasks added to an internal queue.
TaskRunner can run these tasks on any give frequency and utilizing multiple threads based upon an internal thread pool.
Typical usage of the TaskRunner can be illustrated in the following scenarios:

1) Automated data processing batch jobs that run periodically.

2) Rate limited http calls used to query information from remote APIs.

3) Scheduler service that runs periodically performing certain tasks.

In order to do its job TaskRunner requires the following inputs:

1) A block of code that contains the actual processing logic.

2) An optional piece of code that populates the task queue.

3) A configuration object that contains various configuration items like frequency of processing, maximum task processed, maximum number of threads etc.