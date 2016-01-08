# voting-method-http-api

HTTP API gateway for voting methods.

## Configuration

* ALLOWED_ORIGINS
    * This env var controls the cross-origin resource sharing (CORS) settings.
    * It should be set to one of the following:
        * `:all` to allow requests from any origin
        * an EDN seq of allowed origin strings
        * an EDN map containing the following keys and values
            * :allowed-origins - sequence of strings
            * :creds - true or false, indicates whether client is allowed to send credentials
            * :max-age - a long, indicates the number of seconds a client should cache the response from a preflight request
            * :methods - a string, indicates the accepted HTTP methods.  Defaults to "GET, POST, PUT, DELETE, HEAD, PATCH, OPTIONS"
    * For example: `ALLOWED_ORIGINS=["http://foo.example.com" "http://bar.example.com"]`

## Usage

### State voting-methods

To find the ways in which a resident of a state may typically vote there, send
a GET request to the `/:state` endpoint with any 2-letter state postal
abbreviation (case insensitive).

On success it will respond with a set of maps like this (exact contents will
vary by state):

```clojure
#{{:type :in-person, :primary true, :other-key "state-specific details"}
  {:type :by-mail, :primary false, excuse-required true, ...}}
```

### User voting-method preferences

#### Create

To store users' voting-method preference (how they typically prefer to vote),
send a PUT request to the `/preference/:user-id` endpoint and a body like:

```clojure
{:preference :by-mail} ; or :in-person
```

On success it will respond with a 201 response like:

```clojure
{:user-id #uuid "..."
 :preference :by-mail}
```

These will upsert based on the `:user-id`.

#### Read

To read one back out, send a GET request to the `/preference/:user-id` endpoint.

On success it will respond with a 200 response like:

```clojure
{:user-id #uuid "..."
 :preference :by-mail} ; or :in-person
```

#### Delete

To delete a preference, send a DELETE request to the
`/preference/:user-id` endpoint.

On success it will respond with a 200 response like:

```clojure
{:status :ok}
```

## Running

### With docker-compose

Build it:

```
> docker-compose build
```

Run it:

```
> docker-compose up
```

TODO: If any more env vars are needed, add them to docker-compose command above.

### Running in CoreOS

There is a voting-method-http-api@.service.template file provided in the repo. Look
it over and make any desired customizations before deploying. The
DOCKER_REPO, IMAGE_TAG, and CONTAINER values will all be set by the
build script.

The `script/build` and `script/deploy` scripts are designed to
automate building and deploying to CoreOS.

1. Run `script/build`.
1. Note the resulting image name and push it if needed.
1. Set your FLEETCTL_TUNNEL env var to a node of the CoreOS cluster
   you want to deploy to.
1. Make sure rabbitmq service is running.
1. Run `script/deploy`.

## License

Copyright © 2015 Democracy Works, Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
