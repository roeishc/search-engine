# Search Engine

This project allows searching for texts in the web. A high level explanation for the method is:
1. Given a base URL, the program crawls on it and all of the URLs found within its webpage.
2. For each webpage, its contents are saved in Elasticsearch, allowing for quick searching, filtering, etc.
<br>

### In-depth explanation
- For crawling, the program uses Depth-First Search (DFS) for finding additional webpages. The algorithm has 3 conditions that can stop it: runtime (in seconds), depth, or total URLs visited.<br>
- All visited URLs are saved in Redis to prevent visiting them again, and allowing concurrency.<br>
- Everytime a new URL is found, it is added to a Kafka topic to allow multiple threads/containers to access the queue.<br>
<br>
The project exposes API to create a new crawl, which returns a crawl ID (while the crawl continues on a different thread). Another request returns the crawl's status (given its ID).<br><br>

In order to run this project on your machine, you'll need an account in OpenSearch (or Elasticsearch), and add your API key and your index to the application properties. Then, clone the project, and from the project's root folder, run the following command:<br>

```
docker-copmose up -d
```

Make sure that all the containers are up and running (Redis, Zookeeper, Kafka):

```
docker ps
```

And run the project if all containers are up and running.<br><br>

## Example

Creating a new crawl on www.cnn.com via Swagger UI (notice the crawl ID in the response body):<br><br>
<div align="center">
  <img width="726" src="https://github.com/roeishc/search-engine/assets/95538414/6c6cbde3-45d1-4f2a-96b9-619f7ad9df9c"><br><br>
</div>
<br>
Getting the crawl's status (notice for the stop reason - timeout of 60 seconds):<br><br>
<div align="center">
  <img src="https://github.com/roeishc/search-engine/assets/95538414/cc482dd8-00ec-4182-b3ad-6808dbabe612"><br><br>
</div>
<br>
And searching in OpenSearch (Elasticsearch):<br><br>
<div align="center">
  <img src="https://github.com/roeishc/search-engine/assets/95538414/1b330d80-2ff1-438b-915e-3c5aef7c1721"><br><br>
</div>
