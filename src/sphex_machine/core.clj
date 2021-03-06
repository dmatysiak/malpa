(ns sphex-machine.core
  (:require [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log])
  ;; Third-party
  (:require [compojure.core :refer [defroutes GET POST PUT]])
  (:require [ring.adapter.jetty :refer [run-jetty]]))


(def app-version
  (merge {:name "sphex-machine"}
         (zipmap [:major :minor :micro]
                 (str/split
                  (first (str/split (System/getProperty "sphex-machine.version") #"-"))
                  #"\."))))

(def version-string
  (format "%s-%s.%s.%s"
          (:name app-version)
          (:major app-version)
          (:minor app-version)
          (:micro app-version)))

;;
;; Session and user infos
;;
(def model-ids (atom {}))

;;
;; Utils
;;
(defn new-uuid
  "Generates a new UUID."
  []
  (str (java.util.UUID/randomUUID)))

(defn json-serializer
  "Serializes particular types needing serialization to JSON."
  [__ val]
  (cond (instance? java.util.Date val) (str val)
        (instance? org.joda.time.DateTime val) (str val)
        :else val))

(defn pack-json
  "Serialize objects in maps that are serialized to JSON."
  [payload]
  (json/write-str payload :value-fn json-serializer))

(defn unpack-json
  "Unserialize JSON."
  [query]
  (walk/keywordize-keys (json/read-str query :key-fn keyword)))

(defn unpack-request
  "Destructures the request."
  [body]
  (let [body-map (unpack-json (slurp body))
        {request :request payload :payload} body-map]
    [(keyword request) payload]))

(defn response
  "Wraps contents in response header and generates JSON response to
  query."
  ([payload]
   (response payload 200))
  ([payload status]
   {:status status
    :headers {"Content-Type" "application/json; charset=utf-8"}
    :body (pack-json payload)}))

(defn no-response
  "Send JSON back as a sign that execution has completed and
  processed.  This is used in the case of void-like, impure
  functions."
  ([]
   (no-response 200))
  ;; No response should probably always return a status of 200;
  ;; errors should be reported. -dmatysiak
  ([status]
   {:status status
    :headers {"Content-Type" "application/json; charset=utf-8"}
    :body (pack-json {:success true})}))

(defn not-found-error
  "Generates an error response."
  [msg & args]
  (response {:error [{"msg" (apply format msg (map name args))}]}
            404))

;;
;; Actions
;;
(defn model-handler
  [request]
  (let [action (:action request)
        payload (:payload request)]
    (case action
      :create-model (let [model-id (new-uuid)]
                      (swap! model-ids assoc model-id nil)
                      (response {:model-id model-id}))
      (not-found-error "No such resource '%s'" action))))

;;
;; API
;;
(defroutes routes
  (GET "/version"
       []
       (response {:version app-version}))
  (GET "/models"
       []
       (response {:models @model-ids}))
  (POST "/models"
        {body :body}
        (model-handler (unpack-request body))))

;;
;; Main
;;
(def cli-options
  [["-v" "--version"]
   ["-d" "--debug-mode"]
   ["-h" "--help"]
   ["-p" "--port PORT" "Port number"
    :parse-fn read-string
    :validate [#(and (< 1 %) (< % 65535))
               "Port number is out of range."]]])

(defn -main
  "Start an instance of Sphex-Machine."
  [& args]
  
  (let [parsed-opts (parse-opts args cli-options)
        errors (:errors parsed-opts)        
        options (:options parsed-opts)
        ;; Options
        port (:port options)
        debug-mode (:debug-mode options)]
    (cond 
      (> (count errors) 0)
      (do
        (doseq [e errors] (println e))
        (System/exit -1))
      
      (:version options)
      (printf "%s\n" version-string)
      
      (not port)
      (printf "%s\n%s\n" version-string (:summary parsed-opts))
      
      :else
      (do
        (when debug-mode
          (System/setProperty "sphex-machine.debug_mode" debug-mode))
        (run-jetty #'routes {:port port :join? false})))))












