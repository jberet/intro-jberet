
## Overview

This sample application demonstrates the basic Java batch processing in
WildFly Jakarta EE and OpenShift cloud environment. This application follows
the standard batch programming model as defined in 
[JSR 352](https://jcp.org/en/jsr/detail?id=352) 
(Batch Applications for the Java Platform). 
Project [JBeret](https://github.com/jberet/jsr352) implements this specification
and additional features to provide batch processing capability in Java SE,
WildFly, and JBoss EAP.
 
## Batch Job Definition 

 The batch job used in this application is defined in `csv2db.xml`. 
 The batch job performs the following:
 
 * creates the output table (`MOVIES`) in Postgresql database, if not exist;
 * delete all rows from table `MOVIES`, to avoid any conflict between repeated runs;
 * reads movies data from online resource via `csvItemReader`, component from
 `jberet-support` library;
 * each movie is converted into `java.util.Map` object and returned from `csvItemReader`;
 * after 10 (default item-count in `csv2db.xml`) movie objects have been
 read, `jdbcItemWriter` (another component from `jberet-support` library) 
 writes them to Postgresql database.
 
## Reader Configuration

 `csvItemReader` is configured with the following batch properties:
 
 * `resource`: the CSV resource to read
 * `beanType`: the java type of the object from reading a single row of data
 * `nameMapping`: the object field or map keys corresponding to each column of CSV data
 * `cellProcessors`: data formatting rules for when reading each column of CSV data
 
 See [csvItemReader](http://docs.jboss.org/jberet/latest/javadoc/jberet-support/org/jberet/support/io/CsvItemReader.html) 
 for details.
  
## Writer Configuration

 `jdbcItemWriter` is configured with the following batch properties:
 
 * `url`: connection URL to connect to Postgresql database server 
 
    * the host part defaults to `postgresql`
    * the database name part defaults to `sampledb`
 * `user`: Postgresql db user name, defaults to `jberet`
 * `password`: Postgresql db user password, defaults to `jberet`
 * `sql`: the sql statement to insert a single date item to Postgresql
 * `parameterNames`: parameter names used in the above insert sql statement
 * `parameterTypes`: parameter types for the above parameter names
 * `beanType`: java type of each data item passed to this writer class
 
 See [jdbcItemWriter javadoc](http://docs.jboss.org/jberet/latest/javadoc/jberet-support/org/jberet/support/io/JdbcItemWriter.html)
 for details.
 
 
## Build and Run locally with WildFly or JBoss EAP
 
### To clean, build and package the application into a WAR file `intro-jberet.war`
 
 ``` 
 mvn clean install
 ```
 
And make sure WildFly and Postgresql database server are already running.

### To deploy the application WAR file to WildFly:

```
$JBOSS_HOME/bin/jboss-cli.sh -c "deploy --force target/intro-jberet.war"
```

### To undeploy the application from WildFly:
    $JBOSS_HOME/bin/jboss-cli.sh -c "undeploy intro-jberet.war"

### To test that the application has been deployed and ready, check the following URL:

```
curl http://localhost:8080/intro-jberet/
```

It should return the simple `index.html` file with the body content:
    Welcome to project JBeret!
    
### To start running the `csv2db.xml` job:
 
If all Postgresql db connection params are the same as defaults used in `csv2db.xml`:

```
curl -X POST -H 'Content-Type:application/json' 'http://localhost:8080/intro-jberet/api/jobs/csv2db/start'
```

If db.host, db.name, db.user, or db.password is different from the defaults,
then specify them as query params:

```
curl -X POST -H 'Content-Type:application/json' 'http://localhost:8080/intro-jberet/api/jobs/csv2db/start?db.host=localhost'
```

Sample output from starting a job execution:
```json
{
   "startTime":null,
   "endTime":null,
   "batchStatus":"STARTING",
   "exitStatus":null,
   "executionId":4,
   "href":"http://localhost:8080/intro-jberet/api/jobexecutions/4",
   "createTime":1506284588752,
   "lastUpdatedTime":1506284588752,
   "jobParameters":null,
   "jobName":"csv2db",
   "jobInstanceId":4
}
```

The start operation is async, and returns while the submitted job is being processed.

### To get details and status of a job execution, just follow the href link given above:

```
curl http://localhost:8080/intro-jberet/api/jobexecutions/4
```

### To get all step executions belonging to a job execution:

```
curl http://localhost:8080/intro-jberet/api/jobexecutions/4/stepexecutions/
```

### To get details for a step execution:

```
curl http://localhost:8080/intro-jberet/api/jobexecutions/4/stepexecutions/5
```
```json
{
   "startTime":1506284589019,
   "endTime":1506284591953,
   "batchStatus":"COMPLETED",
   "exitStatus":"COMPLETED",
   "stepExecutionId":5,
   "stepName":"csv2db.step2",
   "metrics":[
      {
         "type":"WRITE_COUNT",
         "value":100
      },
      {
         "type":"ROLLBACK_COUNT",
         "value":0
      },
      {
         "type":"FILTER_COUNT",
         "value":0
      },
      {
         "type":"WRITE_SKIP_COUNT",
         "value":0
      },
      {
         "type":"PROCESS_SKIP_COUNT",
         "value":0
      },
      {
         "type":"COMMIT_COUNT",
         "value":11
      },
      {
         "type":"READ_SKIP_COUNT",
         "value":0
      },
      {
         "type":"READ_COUNT",
         "value":100
      }
   ]
}
```

### To restart a failed job execution:

The following job execution will fail due to connection refused to Postgresql server,
because `db.host` is not specified as query param:

    curl -X POST -H 'Content-Type:application/json' 'http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobs/csv2db/start'
    
To check the status (`FAILED`) of the above job execution:

```json
curl http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/2

{
   "startTime":1506346579710,
   "endTime":1506346579755,
   "batchStatus":"FAILED",
   "exitStatus":"FAILED",
   "executionId":2,
   "href":"http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/2",
   "createTime":1506346579708,
   "lastUpdatedTime":1506346579755,
   "jobParameters":null,
   "jobName":"csv2db",
   "jobInstanceId":2
}
```

To restart the above failed job execution id `2`, with correct `db.host` query param:

```json
curl -X POST -H 'Content-Type:application/json' 'http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/2/restart?db.host=172.30.245.228'

# wait a bit for the job execution to complete

curl http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/3

{
   "startTime":1506346732383,
   "endTime":1506346733135,
   "batchStatus":"COMPLETED",
   "exitStatus":"COMPLETED",
   "executionId":3,
   "href":"http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/3",
   "createTime":1506346732381,
   "lastUpdatedTime":1506346733135,
   "jobParameters":{
      "db.host":"172.30.245.228"
   },
   "jobName":"csv2db",
   "jobInstanceId":2
}
```
Note that the previously failed job execution `2`, and the successful restart job execution `3`
both belong to the same job instance id `2`.

### To stop a running job execution:

```json
curl -X POST -H 'Content-Type:application/json' 'http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/2/stop'

{
  "type":"javax.batch.operations.JobExecutionNotRunningException",
  "message":"JBERET000612: Job execution 2 has batch status FAILED, and is not running.",
  "stackTrace":"javax.batch.operations.JobExecutionNotRunningException: ..."
}
```

You can only stop a running job execution. Because job execution `2` has already 
completed, therefore the above stop operation failed.

### To abandon a finished job execution:

    curl -X POST -H 'Content-Type:application/json' 'http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobexecutions/2/abandon'
    
You can only abandon a finished job execution (not running job executions). Once it is abandoned,
it may not be restarted.

### To schedule a job execution

```json
curl -X POST -H 'Content-Type:application/json' -d '{"jobName":"csv2db", "initialDelay":5, "jobParameters":{"db.host":"172.30.245.228"}}' 'http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/jobs/csv2db/schedule'

{
   "id":"1",
   "jobScheduleConfig":{
      "jobName":"csv2db",
      "jobExecutionId":0,
      "jobParameters":{
         "db.host":"172.30.245.228"
      },
      "scheduleExpression":null,
      "initialDelay":5,
      "afterDelay":0,
      "interval":0,
      "persistent":false
   },
   "createTime":1506349717648,
   "status":"SCHEDULED",
   "jobExecutionIds":[
   ]
}
```
The above command schedules to start running job `csv2db` after 5 minutes, 
with job parameter `db.host = 172.30.245.228`.  More advanced scheduling
are also supported by JBeret by customizing `jobScheduleConfig` field,
such as repeated execution, interval between executions, and calendar-based
cron-like schedules.

To check the status of the schedule:

    curl http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/schedules
    
```json
[
   {
      "id":"1",
      "jobScheduleConfig":{
         "jobName":"csv2db",
         "jobExecutionId":0,
         "jobParameters":{
            "db.host":"172.30.245.228"
         },
         "scheduleExpression":null,
         "initialDelay":5,
         "afterDelay":0,
         "interval":0,
         "persistent":false
      },
      "createTime":1506349717648,
      "status":"SCHEDULED",
      "jobExecutionIds":[

      ]
   }
]

# after the schedule execution is executed, the status of the schedule changes to `DONE`,
# and includes the job execution id

[
   {
      "id":"1",
      "jobScheduleConfig":{
         "jobName":"csv2db",
         "jobExecutionId":0,
         "jobParameters":{
            "db.host":"172.30.245.228"
         },
         "scheduleExpression":null,
         "initialDelay":5,
         "afterDelay":0,
         "interval":0,
         "persistent":false
      },
      "createTime":1506349717648,
      "status":"DONE",
      "jobExecutionIds":[
         4
      ]
   }
]
```

### To cancel a scheduled job execution
    curl http://intro-jberet-cfang-p1.1d35.starter-us-east-1.openshiftapps.com/intro-jberet/api/schedules/2/cancel

### To query Postgresql `MOVIES` table to verify the output:

    psql sampledb jberet --host=127.0.0.1 --port=5432

```
select * from MOVIES;

 rank |                        tit                        |      grs      |    opn
------+---------------------------------------------------+---------------+------------
    1 | Marvel's The Avengers                             | 623357910.000 | 2012-05-04
    2 | The Dark Knight Rises                             | 448139099.000 | 2012-07-20
    3 | The Hunger Games                                  | 408010692.000 | 2012-03-23
    4 | Skyfall                                           | 304360277.000 | 2012-11-09
    5 | The Hobbit: An Unexpected Journey                 | 303003568.000 | 2012-12-14
...
```

## Build and Run in cloud with WildFly in OpenShift

What differs from local execution (on-premise) is how to provision WildFly, Postgresql and
application.  Once the application is ready for service, steps to run the batch application
stay the same. For details, refer to [OpenShift Tutorials](https://learn.openshift.com/).
