# dataikutest

Test application. It allows to choose any column in a database
and it displays for all the possible values for the column the number
of rows with that value and the average age of the rows.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

## Download test database

```
 curl http://dev.dataiku.com/\~cstenac/dev-recruiting/us-census.db.gz -o resources/us-census.db.gz && gunzip resources/us-census.db.gz
```

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```
lein ring server
```

## Building clojurescript

To compile automatically all clojurescript files, run:

```
lein cljsbuild auto
```

## License

Copyright © 2014 FIXME
