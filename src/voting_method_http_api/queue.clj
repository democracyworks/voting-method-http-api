(ns voting-method-http-api.queue
  (:require [clojure.tools.logging :as log]
            [langohr.core :as rmq]
            [kehaar.core :as k]
            [kehaar.wire-up :as wire-up]
            [kehaar.rabbitmq]
            [voting-method-http-api.channels :as channels]
            [voting-method-http-api.handlers :as handlers]
            [turbovote.resource-config :refer [config]]))

(defn initialize []
  (let [max-retries 5
        rabbit-config (config [:rabbitmq :connection])
        connection (kehaar.rabbitmq/connect-with-retries rabbit-config max-retries)]
    (let [incoming-events []
          incoming-services [(wire-up/incoming-service
                              connection
                              "voting-method-http-api.ok"
                              (config [:rabbitmq :queues "voting-method-http-api.ok"])
                              channels/ok-requests
                              channels/ok-responses)]
          external-services [(wire-up/external-service
                              connection
                              ""
                              "voting-method-works.voting-method.search"
                              (config [:rabbitmq :queues "voting-method-works.voting-method.search"])
                              5000
                              channels/voting-method-search)
                             (wire-up/external-service
                              connection
                              ""
                              "voting-method-works.voting-method-preference.read"
                              (config [:rabbitmq :queues "voting-method-works.voting-method-preference.read"])
                              5000
                              channels/voting-method-preference-read)
                             (wire-up/external-service
                              connection
                              ""
                              "voting-method-works.voting-method-preference.create"
                              (config [:rabbitmq :queues "voting-method-works.voting-method-preference.create"])
                              5000
                              channels/voting-method-preference-create)
                             (wire-up/external-service
                              connection
                              ""
                              "voting-method-works.voting-method-preference.delete"
                              (config [:rabbitmq :queues "voting-method-works.voting-method-preference.delete"])
                              5000
                              channels/voting-method-preference-delete)]
          outgoing-events []]

      (wire-up/start-responder! channels/ok-requests
                                channels/ok-responses
                                handlers/ok)

      {:connections [connection]
       :channels (vec (concat
                       incoming-events
                       incoming-services
                       external-services
                       outgoing-events))})))

(defn close-resources! [resources]
  (doseq [resource resources]
    (when-not (rmq/closed? resource) (rmq/close resource))))

(defn close-all! [{:keys [connections channels]}]
  (close-resources! channels)
  (close-resources! connections))
