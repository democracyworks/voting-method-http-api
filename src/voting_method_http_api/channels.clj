(ns voting-method-http-api.channels
  (:require [clojure.core.async :as async]))


(defonce ok-requests (async/chan))
(defonce ok-responses (async/chan))

(defonce voting-methods-search (async/chan 1000))

(defonce voting-method-preference-read (async/chan 1000))
(defonce voting-method-preference-create (async/chan 1000))
(defonce voting-method-preference-delete (async/chan 1000))

(defn close-all! []
  (doseq [c [ok-requests ok-responses voting-methods-search
             voting-method-preference-read voting-method-preference-create
             voting-method-preference-delete]]
    (async/close! c)))
