An algorithm to implement CLABSI.

## Run with java
Specify a folder where you will have a file 
```bash
java -jar target/clabsi-algorithm-1.0-SNAPSHOT.jar <FOLDER>
java -jar target/clabsi-algorithm-1.0-SNAPSHOT.jar praise-mds
```

## Run with dockerhub (easiest)

```bash
FOLDER=/tmp/hugdata-in-praise-format
docker run --rm -v $FOLDER:/data bsi-algo /data
```

## Build and run the image with docker from jar pre-compiled
```bash
docker build -t bsi-algo .
FOLDER=`pwd`/praise-mds
docker run --rm -v $FOLDER:/data bsi-algo /data
```

## Build and run the image with docker from sources
```bash
docker build -t bsi-algo-src -f DockerfileSRC .
FOLDER=`pwd`/praise-mds
docker run --rm -v $FOLDER:/data bsi-algo-src /data
```

# Build the fat jar
mvn package shade:shade

# GLOSSARY 

### copy strains

COPY STRAINS exists for HOB and COB (COmmunity Onset )

### solitary commensal

•	A « solitary commensal » is the same as a « contamination » for you ?


### simple rules

o	An episode can either be a « solitary commensal » or a « OB » = onset-bacteremia.
o	In case of beeing a « OB » it can be a « COB » or a « HOB «
o	Only OBs (COBs and HOBs) can have polymicrobial episodes
o	Only OBs (COBs and HOBs) can generate  copy strains
