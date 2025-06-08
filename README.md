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
# Database access (h2-console)
- Driver class: org.h2.Driver
- DB url: jdbc:h2:file:/app/data/testdb
- Username: sa
