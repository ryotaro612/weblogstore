(ns nginxlogstore.core
  (:require [clojure.java.io :as io]
            [clj-time.format :as tfmt]
            [clojure.edn :as edn]
            [korma.db :as kdb]
            [korma.core :as kc]
            [clojure.string :as str])
  (:import (java.util Locale)))


(def credentials (edn/read-string (slurp "resources/credentials.edn")))
(kdb/defdb defaultdb (kdb/mysql {:user (:user credentials) :password (:password credentials)}))

(kc/defentity access-log
              (kc/table :ranceworks.access_log)
              (kc/database defaultdb))

(defn bulk-insert [filepath]
  (with-open [rdr (io/reader filepath)]
    (letfn [(extract-val [tag line] (str/replace (first (re-seq (re-pattern (str tag ":[^\t]+")) line)) #"^[^:]+:" ""))
            (cut-str [str max-siz] (.substring str 0 (min max-siz (.length str))))
            (todate [str] (tfmt/parse (tfmt/with-locale (tfmt/formatter "E, dd-MMM-YYYY HH:mm:ss z") Locale/US) str))]
      (kc/insert access-log (kc/values
                              (for [l (line-seq rdr)]
                                (let [datetime (.toDate (todate (extract-val "date_gmt" l)))]
                                  {:date_gmt datetime
                                   :time_gmt datetime
                                   :uri (cut-str (extract-val "uri" l) 255)
                                   :city (cut-str (extract-val "geoip_city" l) 64)
                                   :country_code (extract-val "geoip_city_country_code3" l)
                                   :status_code (extract-val "status" l)
                                   :remote_addr  (extract-val "realip_remote_addr" l)
                                   :http_referer (cut-str (extract-val "http_referer" l) 512)
                                   :http_user_agent (cut-str (extract-val "http_referer" l) 255)})))))))
