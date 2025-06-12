# Service types
There's two main service types: Ollama (open-source) and OpenAI (proprietary).

## Ollama
It uses the models located at ```backend/src/main/resources/Modelfiles```. The files contain the model it's pulling, system prompt, and parameters.  
The main models for (the ones the API endpoint uses) are those with the prefix _cs-_ (e.g., _cs-problemGenerator_). 
Those that do not have this prefix (e.g., _Extractor_) are used for CHASE-like problem generation, and are solely for research purposes (not called by API).

## OpenAI
It uses the same modelfiles at ```backend/src/main/resources/Modelfiles```, but since OpenAI does not admit the same format as Ollama they are being parsed inside ```backend/src/main/java/OpenAI/InterfaceOpenAI.java``` for convenience. This means you can make edits in those modelfiles for tweaking system prompts and parameters, and this will affect both Ollama and OpenAI models.  
The OpenAI key should be added as an environmental variable called ```OPENAI_API_KEY```.

# How to build?
All builds should be done from the root directory, the one that contains the two (backend, frontend) Maven projects.
## From scratch / day-to-day starting
```
docker compose up -d
```
In this case if building from scratch, you have to wait for models to download, those will appear in docker logs.
Now for stop the running container but preserve modules in volume.
```
docker compose down
```
## Only backend changes
```
docker compose build backend
```
## WARNINGS:
The following deletes volumes with all models.
```
docker compose down -v
```
The following command is unsupported, ollama builds with normal builds, only backend has special container initialization:
```
docker compose build ollama
```
# UML Diagrams


# Database access (h2-console)
- Driver class: ```org.h2.Driver```
- DB url: ```jdbc:h2:file:/app/data/testdb```
- Username: ```sa```
