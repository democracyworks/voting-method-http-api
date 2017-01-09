(ns voting-method-http-api.channels
  (:require [clojure.core.async :as async]))

(defonce voting-method-search (async/chan 1000))

(defonce voting-method-preference-read (async/chan 1000))
(defonce voting-method-preference-create (async/chan 1000))
(defonce voting-method-preference-delete (async/chan 1000))

(defn close-all! []
  (doseq [c [voting-method-search
             voting-method-preference-read
             voting-method-preference-create
             voting-method-preference-delete]]
    (async/close! c)))
