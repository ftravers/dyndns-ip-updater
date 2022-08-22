;; new dyndns updater
;;
;; Change to NOT use long running process but actually
;; a babashka script.
;;

(ns dyndns-ip-updater.dyndns-updater
  (:require
   ;; [clj-http.client :as client]
   ;; [clojure.data.json :as json]
   [babashka.curl :as curl]

   [taoensso.timbre :as timbre])
  )

(defn get-ip-last-sent-2-dyndns [filename]
  (let [exists (.exists (clojure.java.io/as-file filename))]
    (if exists
      (slurp filename)
      (do (timbre/debug "No last-sent-to-dyndns IP file found: " filename)))))

(defn update-dyndns-ip [url]
  (timbre/debug "Updating afraid.org ip address with url: " url)
  (curl/get url)
  ;; (client/get url)
  )

(defn determine-ip []
  (-> "https://api.ipify.org?format=json"
      client/get
      :body
      (json/read-str :key-fn keyword)
      :ip))

(defn check-ip-change [url ip-filename]
  (let [curr-ip (determine-ip)
        last-ip (get-ip-last-sent-2-dyndns ip-filename)]
    (if (not (= last-ip curr-ip))
      (do (timbre/debug "Last IP not = Curr IP: " last-ip "!=" curr-ip)
          (update-dyndns-ip url)
          (timbre/debug "New IP addy found, persisting: " curr-ip)
          (spit ip-filename curr-ip))
      (timbre/debug "IP Addy same do nothing."))))

(defn set-interval [callback ms] 
  (future (while true (do (Thread/sleep ms) (callback)))))

(def filename "last-sent-ip-dyndns.edn")

(def update-url "http://freedns.afraid.org/dynamic/update.php?YldPeU50V2tsZXNOdWhIdW5ZV0E6MTcxMTM4MDk=")

(def update-url-out (client/get update-url))

(def job (set-interval #(check-ip-change update-url filename) (* 60 1000)))


(defn -main [& args]
  (println "abc"))
