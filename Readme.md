# Process

 1. cleanText 
 2. TF-IDF
 3. CalculateSimilarityMatrix
 4. clusterStoriesWithKMeans

# Pom
```
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>8.11.1</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analyzers-common</artifactId>
    <version>8.11.1</version>
</dependency>
```

# Todo
- Verwijderen van speciale tekens en leestekens: Alle niet-alfabetische tekens, zoals leestekens, symbolen en speciale tekens, worden verwijderd om de tekst te normaliseren.
- Omzetten naar kleine letters: Om consistentie te waarborgen, wordt de hele tekst omgezet naar kleine letters.
- Verwijderen van getallen: Cijfers kunnen vaak irrelevant zijn voor tekstanalyse en worden daarom verwijderd.
- Lemmatization: Het terugbrengen van woorden naar hun grondvorm (lemma), bijvoorbeeld "running" wordt "run".
- Stemming: Het reduceren van woorden tot hun wortelvorm, bijvoorbeeld "running" wordt "run".
- Verwijderen van witruimtes: Overtollige witruimtes worden verwijderd.
- Tokenization: Het splitsen van de tekst in afzonderlijke woorden (tokens).
- Verwijderen van zeldzame woorden of veelvoorkomende woorden: Woorden die te weinig of te vaak voorkomen kunnen worden verwijderd, afhankelijk van de toepassing.
