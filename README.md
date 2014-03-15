# ameba

Disclaimer: This is a work in progress, subject to complete change at any time. 

Ameba is a set of functions and schema definitions to support
document-centric social web sites using Clojure and Datomic. It is
targeted towards on-line sites in which users view, search, annotate,
create, and share content in the form of "documents". A document could
be, for examples, a blog posting or a recipe. The originating use case
was the concept of a recipe sharing site, which would support
annotation and cloning of recipes.

The general design is this: Datomic will be used to represent the
structure of documents, relationships between documents, annotations,
users, comments, etc. Actual textual and media content will be stored
in a separate repository, such as S3. All data is immutable. (High
potential for caching.) Search will be supported via solr.

The library will include support for:

* Database schema
* Clojure functions and datomic rules supporting database query operations
* Clojure functions specific to the content storage strategy implemented by the library (S3 content storage, based on unique ID's, caching). 
* Solr schema and search-related clojure functions
* User management, security, openid, authentication

## Motivation

Primarily this project is a vehicle for learning about Datomic and Clojure web site development. 

## Project status

Exploring / prototyping. Not useful yet.

## License

Copyright (C) 2012, 2014

Distributed under the Eclipse Public License, the same as Clojure.
