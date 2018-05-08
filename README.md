[![CircleCI](https://circleci.com/gh/djabry/sner.svg?style=svg)](https://circleci.com/gh/djabry/sner)
# Serverless Stanford Named Entity Recognizer
This project enables you to deploy the [Stanford Named Entity Recognizer (NER)](https://nlp.stanford.edu/software/CRF-NER.shtml) to a "serverless" environment based on [AWS Lambda](https://aws.amazon.com/lambda/) and [API Gateway](https://aws.amazon.com/api-gateway/).

## Why?
The general advantages of [serverless computing](https://en.wikipedia.org/wiki/Serverless_computing) include cost, scalability and productivity. Specifically, these translate to:
* The ability to analyse text in virtually any environment - most notably from the browser
* Processing a large number of texts [concurrently](https://docs.aws.amazon.com/lambda/latest/dg/concurrent-executions.html) - potentially thousands
* Ease and speed of iteration - just deploy with one [command](#Deploying-to-AWS) after making changes to your models or label interpretation logic

## How?
### Getting started
1. Make sure you have the following installed on your machine:

    * [Docker](https://docs.docker.com/install/)

    Or
    
    * [Node](https://nodejs.org/en/) >= 8
    * [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) >= 8
    * [Maven](https://maven.apache.org/what-is-maven.html)   

2. Sign up for an [AWS](https://aws.amazon.com/) account

3. Configure your [AWS credentials](https://serverless.com/framework/docs/providers/aws/guide/credentials/) for deployment with the [Serverless framework](https://serverless.com/). Make sure these are set up as the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` if working with docker.

4. Install dependencies: 
    * With docker:
        ```docker
        docker build -t sner .
        ```
          
        Or 
    
    * With Node/JDK/Maven: Install the [Serverless](https://serverless.com/) dependencies using the command in the project root directory: 
       ```
        npm install
       ```

### Deploying to AWS

With docker:

```
docker run --rm -it  -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY sner npm run deploy -- --stage=dev
``` 

Or 

With Node/JDK/Maven:

```
npm run deploy -- --stage=dev
``` 

You should see your POST and GET endpoints displayed after a successful deployment e.g.
```
...
endpoints:
  POST - https://xxxxxx.execute-api.xx-xxxx-x.amazonaws.com/dev/entities
  GET - https://xxxxxx.execute-api.xx-xxxx-x.amazonaws.com/dev/entities
...
```

### Trying it out
You can try using the GET endpoint by simply appending the query parameter "text" to it along with the text you wish to analyse e.g.

```
https://xxxxxx.execute-api.xx-xxxx-x.amazonaws.com/dev/entities?text=Stanford University is located in Silicon Valley and was founded in November 1885
```
Response:
```json
{
  "ORGANIZATION": [
    {
      "name": "Stanford University",
      "count": 1
    }
  ],
  "LOCATION": [
    {
      "name": "Silicon Valley",
      "count": 1
    }
  ],
  "DATE": [
    {
      "name": "November 1885",
      "count": 1
    }
  ]
}
```

Example payload for the POST endpoint:
```json
{
  "text": "Stanford University is located in Silicon Valley and was founded in November 1885"
}
```

## What?
### Label interpretation logic
The "business logic" lives in the [EntityExtractor](src/main/java/com/github/djabry/sner/EntityExtractor.java) class and processes text in the following way:

1. Finds labels associated with each word in a string using the [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) library
2. Filters the labels to leave only those corresponding to named entities 
3. Extracts the names, types and number of times each entity occurs in the text from the remaining labels
4. Groups the entity names and counts by their types

### Configuration
The [pom.xml](pom.xml) and [serverless.yml](serverless.yml) files contain most of the important settings in this project.

* Select the [models](https://nlp.stanford.edu/software/CRF-NER.shtml#Models) you wish to use in the [pom.xml](pom.xml) `<properties>` and `<build>` sections:

```xml
<project>
<!--...-->
<properties>
    <!--...-->
    <ner.model1>english.all.3class.distsim</ner.model1>
    <ner.model2>english.conll.4class.distsim</ner.model2>
    <ner.model3>english.muc.7class.distsim</ner.model3>
    <!--...-->
</properties>
<!--...-->
  <build>
    <plugins>
      <!--...-->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <!--...-->
        <configuration>
          <!--...-->
          <filters>
            <filter>
              <!--...-->
              <includes>
                <include>${ner.prefix}${ner.model1}.*</include>
                <include>${ner.prefix}${ner.model2}.*</include>
                <include>${ner.prefix}${ner.model3}.*</include>
              </includes>
            </filter>
          </filters>
        </configuration>
        <!--...-->
      </plugin>
    <!--...-->
    </plugins>
  </build>
  <!--...-->
</project>


```

* Update the [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) library version in the [pom.xml](pom.xml) `<properties>` section:
```xml
<properties>
    <nlp.version>3.9.1</nlp.version>
    <!--...-->
</properties>
```

* Change the AWS Lambda name, memory, region in the [serverless.yml](serverless.yml) file

* [Configure your endpoints](https://serverless.com/framework/docs/providers/aws/events/apigateway/) in the [serverless.yml](serverless.yml) file 
