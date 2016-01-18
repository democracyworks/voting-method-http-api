FROM quay.io/democracyworks/didor:latest

RUN mkdir -p /usr/src/voting-method-http-api
WORKDIR /usr/src/voting-method-http-api

COPY project.clj /usr/src/voting-method-http-api/

RUN lein deps

COPY . /usr/src/voting-method-http-api

RUN lein test
RUN lein immutant war --name voting-method-http-api --destination target --nrepl-port=11954 --nrepl-start --nrepl-host=0.0.0.0
