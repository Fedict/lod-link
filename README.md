# Link store

This is an example implementation for store links (and metadata) into the triple store.

## Format / structure

The structure is extremely simple:

 * the link is used as identifier (RDF subject)
 * links can have a `rdfs:label` in multiple languages
 * the `dcat:theme` property is used to categorize the links


An example in Turtle looks like:
```
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix dcat: <http://www.w3.org/ns/dcat#> .
@prefix mdr: <http://publications.europa.eu/resource/authority/data-theme/> .

<http://www.fedict.be> rdfs:label "Website van Fedict"@nl, "Site web de Fedict"@fr, "Fedict web site"@en ;
	dcat:theme mdr:TECH .

<http://www.statbel.be> rdfs:label "Website van Statbel"@nl, "Site web de Statbel"@fr, "Statbel web site"@en ;
	dcat:theme mdr:ECON .
```


## Content-negotiation

An HTTP client can use various  RDF 1.1 serializations.

For GET requests, the HTTP `Accept` header should be set

For PUT requests, the HTTP `Content-Type` header must be set, and UTF-8 encoding must be used.

  * `application/ld+json`: JSON-LD
  * `text/turtle`: Turtle
  * `application/n-triples`: N-Triples

## Retrieving all info about a link (GET)

```
http://link.belgif.be/link?url=http://www.fedict.be
```
Note: the value of the `url` parameter should be URL-encoded.

## Filtering link(s) (GET)

```
http://link.belgif.be/link/_filter?theme=http://publications.europa.eu/resource/authority/data-theme/TECH (al technology-related links)
```

## Adding information about a link (PUT)

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, the previously mentioned
file can be uploaded using curl (or any other HTTP-tool) using the following command: 
```
curl -v -T test.ttl -H "Content-Type: text/turtle" --basic http://user:pass@link.belgif.be/link
```

## Updating full text search index

The (Lucene) full text search index is not updated automatically,
one has to update the FTS after one or more PUTs.

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, the index can be incrementally 
updated using curl (or any other HTTP-tool) using the following command: 
```
curl -v --request PATCH --basic http://user:pass@link.belgif.be/link/_reindex
```

## Removing a link (DELETE)

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, 
all information about the link `http://www.fedict.be` can be deleted using the following command:

```
curl --request DELETE -v http://user:pass@link.belgif.be/link?url=http://www.fedict.be
```
Note: the value of the `url` parameter must be URL-encoded.

