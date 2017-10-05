# Demo link store

This is an example implementation for storing links (and metadata) into a triple store.


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

For backwards compatibility, two types of URLs can be used
```
http://link.belgif.be/link?url=http://www.fedict.be
https://id.belgium.be/link-lod/link?s=http://www.fedict.be
```
Note: the value of the `url` / `s` parameter should be URL-encoded.

## Retrieving all info about a graph / group of links (GET)

If the data was stored in a named graph / context, it is possible to retrieve
these triples all at once.

```
https://id.belgium.be/link-lod/link?g=https:%2F%2Fid.belgium.be%2Fgraph%2Fbe454d3d-f01b-4807-b1b7-ece2f70b2f23
```
Note: the value of the `g` parameter should be URL-encoded.


## Filtering link(s) (GET)

```
http://link.belgif.be/link/_filter?theme=http://publications.europa.eu/resource/authority/data-theme/TECH (all technology-related links)
https://id.belgium.be/link-lod/link/_filter?theme=http://publications.europa.eu/resource/authority/data-theme/TECH
```

Note again that, especially when the ID contains a '#' (which is not passed to the server), URL-encoding might be necessary.
```
http://link.belgif.be/link/_filter?theme=http://vocab.belgif.be/be-theme/WORK%23id
https://id.belgium.be/link-lod/link/_filter?theme=http://vocab.belgif.be/be-theme/WORK%23id
```

## Adding information about a link (PUT)

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, the previously mentioned
file can be uploaded using curl (or any other HTTP-tool) using the following command: 
```
curl -v -T test.ttl -H "Content-Type: text/turtle" --basic http://user:pass@link.belgif.be/link
curl -v -T test.ttl -H "Content-Type: text/turtle" --basic https://user:pass@id.belgium.be/link-lod/link
```

Accepted formats are Turtle (ttl), N-Triples (nt) and JSON-LD.


## Removing a link (DELETE)

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, 
all information about the link `http://www.fedict.be` can be deleted using the following command:

```
curl --request DELETE -v http://user:pass@link.belgif.be/link?url=http://www.fedict.be
curl --request DELETE -v https://user:pass@id.belgium.be/link-lod/link?url=http://www.fedict.be
```
Note: the value of the `url` parameter must be URL-encoded.

## Removing a group of links / graph (DELETE)

If the data was stored in a named graph / context, it is possible to delete
these triples all at once.

This requires HTTP basic authentication (i.e. a username and password)

Assuming the username is `user` and the password `pass`, 
all information about the graph `https://id.belgium.be/graph/be454d3d-f01b-4807-b1b7-ece2f70b2f23` 
can be deleted using the following command:

```
curl --request DELETE -v http://user:pass@link.belgif.be/link?g=https:%2F%2Fid.belgium.be%2Fgraph%2Fbe454d3d-f01b-4807-b1b7-ece2f70b2f23
```
Note: the value of the `g` parameter must be URL-encoded.
