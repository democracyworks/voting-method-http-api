(ns voting-method-http-api.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :refer [interceptor]]
            [ring.util.response :as ring-resp]
            [turbovote.resource-config :refer [config]]
            [pedestal-toolbox.params :refer :all]
            [pedestal-toolbox.content-negotiation :refer :all]
            [kehaar.core :as k]
            [clojure.core.async :refer [go alt! timeout]]
            [bifrost.core :as bifrost]
            [bifrost.interceptors :as bifrost.i]
            [voting-method-http-api.channels :as channels]))

(def ping
  (interceptor
   {:enter
    (fn [ctx]
      (assoc ctx :response (ring-resp/response "OK")))}))

(defroutes routes
  [[["/"
     ^:interceptors [(body-params)
                     (negotiate-response-content-type ["application/edn"
                                                       "application/transit+json"
                                                       "application/transit+msgpack"
                                                       "application/json"
                                                       "text/plain"])]
     ["/ping" {:get [:ping ping]}]
     ["/preference/:user-id"
      {:get [:voting-method-preference-read
             (bifrost/interceptor
              channels/voting-method-preference-read)]}
      {:put [:voting-method-preference-create
             (bifrost/interceptor
              channels/voting-method-preference-create)]}
      {:delete [:voting-method-preference-delete
                (bifrost/interceptor
                 channels/voting-method-preference-delete)]}
      ^:interceptors [(bifrost.i/update-in-request [:path-params :user-id]
                                                   #(java.util.UUID/fromString %))]]
     ["/:state" {:get [:voting-method-search
                       (bifrost/interceptor channels/voting-method-search)]}
      ^:interceptors [(bifrost.i/update-in-response [:body :voting-methods]
                                                    [:body] identity)]]]]])

(defn service []
  {::env :prod
   ::bootstrap/router :linear-search
   ::bootstrap/routes routes
   ::bootstrap/resource-path "/public"
   ::bootstrap/allowed-origins (if (= :all (config [:server :allowed-origins]))
                                 (constantly true)
                                 (config [:server :allowed-origins]))
   ::bootstrap/host (config [:server :hostname])
   ::bootstrap/type :immutant
   ::bootstrap/port (config [:server :port])})
