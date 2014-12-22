(ns dataikutest.core.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [ring.middleware.json :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clostache.parser :refer [render-resource]]
            [clojure.java.jdbc :as jdbc]))

(def mysql-db {:classname "org.sqlite.JDBC"
               :subprotocol "sqlite"
               :subname "./resources/us-census.db" })

(def table "census_learn_sql")
(def dist-template "public/partials/distributions.mustache")

(defn columns
  []
  (->> (jdbc/query mysql-db [(str "SELECT * FROM `" table "` LIMIT 1;")])
       (first)
       (keys)
       (map name)
       (remove #{"age"})))

(defn count-all
  []
  (->> (jdbc/query mysql-db [(str "SELECT Count(*) as count FROM `" table "`;")])
       (first)
       (:count)))

(defn count-values
  [column]
  (->> (jdbc/query mysql-db [(str "SELECT Count(*) as count_values FROM 
                                  (SELECT `" column "`
                                       FROM `" table "`
                                       GROUP BY `" column "`);")])
       (first)
       (:count_values)))

(defn group-by-age
  [column]
  (jdbc/query mysql-db 
              [(str "SELECT AVG(age) as avg, COUNT(*) as count, `" 
                    column 
                    "` as value from `" 
                    table 
                    "` GROUP BY `" 
                    column 
                    "` ORDER BY COUNT(*) LIMIT 100;")]))

(defn column-exists?
  [column]
  ((into #{} (columns)) column))

(defroutes app-routes
  (GET "/" [] 
    (let [cols (columns)
          col (first cols)
          dist (into [] (group-by-age col))]
       (render-resource "public/index.mustache" 
           {:columns (map (fn [x] {:col x :selected (= x col)}) cols)
            :distributions-template (slurp (io/file (io/resource dist-template)))})))
  (GET "/api/byage/:column" [column] 
    (let [dist (group-by-age column)
          countdist (reduce + (map :count dist))]
       (if (column-exists? column)  
         {:body 
          {:distributions dist 
           :all_count (- (count-values column) (count dist))
           :left (- (count-all) countdist)}}
         {:status 404 :body (str column " column doesn't exist in the db")})))
  (route/resources "/public/")
  (route/not-found "Not Found"))

(def app
  (->  app-routes
      (wrap-json-body)
      (wrap-json-response)))
