;General notes
;Movie records are represented as key value pairs, where the ID number is the key and the movie 'information' is the value.
;It's important to note that the 'information' is also a map.

;Movie map abstraction:
;{ID-number {:title "title" :price price :quantity quantity :rented rented :renters {}}}
;Note: database is a map of maps (movies)
;Within movie records, renters are represented as a map,
;where keys are renter names (strings) and values are due dates (strings).

(ns manager.backend
  (:gen-class)
  (:require
        [clj-time.core :as t]
        [clj-time.format :as tform]
        [me.raynes.fs :as fs]))

(defn add-new-movie [{:keys [ID title price quantity] :as movie-listing} database]
  {:pre [(map? movie-listing)
         (not (empty? movie-listing))
         (= 4 (count movie-listing))
         (not (nil? database))]}
  "Adds a map abstraction of a movie to the given database."
  (let [new-ID (movie-listing :ID)
        new-record (dissoc movie-listing :ID)]
    (->> (assoc new-record :rented 0 :renters {}) ;;add rented and renters as a fields
         (assoc database new-ID ))))

(defn remove-movie [ID-key database]
  {:pre [(contains? database ID-key)]}
  (dissoc database ID-key))

(defn rent-movie [ID-key database]
  {:pre [(contains? database ID-key)]}
  "Locates movie in database given ID-key, increases renters by one.
  TODO: adjust exception, so that it quietly throws assertion error.
  NOTE: Current implementation has protections in a wrapper method, exception will never be thrown here."
  (let [movie-record (database ID-key)
        rented (movie-record :rented)
        number-available (- (movie-record :quantity) (movie-record :rented))]
    (if (pos? number-available)
      (assoc-in database [ID-key :rented] (inc (movie-record :rented)))
      (throw (IllegalArgumentException. "Error, no copies available for rental")))))

(defn remove-renter [renter-name ID-key database]
  {:pre [(contains? database ID-key)
         (contains? ((database ID-key) :renters) renter-name)]}
  "Locates record from given ID-key and database, removes renter / due date combination."
  (let [movie-record (database ID-key)
        renters (movie-record :renters)]
    (update-in database [ID-key :renters] dissoc renter-name)))

(defn add-renter [renter-name due-date ID-key database]
  {:pre [(contains? database ID-key)]}
  "Adds new key / value pair, renter / due-date, to given record via ID-key and database"
  (let [movie-record (database ID-key)
         renters (movie-record :renters)]
    (assoc-in database [ID-key :renters renter-name] due-date)))

(defn get-renters [ID-key database]
   {:pre [(contains? database ID-key)]}
   (let [movie-record (database ID-key)
         renters (movie-record :renters)]
     renters))

(defn return-movie [ID-key database]
  {:pre [(contains? database ID-key)]}
   "Locates movie in database given ID-key, decreases renters by one.
  TODO: adjust exception, so that it quietly throws assertion error.
  NOTE: Current implementation has protections in a wrapper method, exception will never be thrown here."
  (let [movie-record (database ID-key)
        rented (movie-record :rented)]
        (if (pos? rented)
          (assoc-in database [ID-key :rented] (dec (movie-record :rented)))
          (throw (IllegalArgumentException. "Error, no copies are rented")))))

(defn calc-due-date[num-days]
  "Formats due date given a number of days, produced date is relative to the current day."
  (->> num-days
       (t/days)
       (t/from-now)
       (tform/unparse (tform/formatters :date))))

(defn add-additional-copy [ID-key database]
  {:pre [(contains? database ID-key)]}
  (let [movie-record (database ID-key)
        movie-count (movie-record :quantity)]
  (assoc-in database [ID-key :quantity] (inc movie-count))))

(defn change-price [new-price ID-key database]
  {:pre [(contains? database ID-key)
         (number? new-price)]}
  (assoc-in database [ID-key :price] new-price))

(defn quantity-of-movie [ID-key database]
  {:pre [(contains? database ID-key)]}
  "returns total number of copies for a given movie."
  (let [movie-record (database ID-key)]
       (movie-record :quantity)))

(defn price-of-movie [ID-key database]
  {:pre [(contains? database ID-key)]}
  (let [movie-record (database ID-key)]
       (movie-record :price)))

(defn record-from-title [title database]
  "A basic search function which filters database based on a full title.
  TODO: Replace with a more robust searching feature."
  (filter #(= title ((val %) :title)) database))

(defn record-from-id [id database]
  "A basic search function which filters database based on a full ID number.
  TODO: Replace with a more robust searching feature."
  (filter #(= id (key %)) database))

(defn make-movie-record
  "Movie record helper"
  ([[title price quantity]]
  (into {} [{:title title} {:price price} {:quantity quantity}]))
  ([title price quantity]
  (into {} [{:title title} {:price price} {:quantity quantity}])))

(defn join-string-map [mp]
  (let [ks (keys mp)
        vs (vals mp)]
  (if (empty? mp) ""
  (->> (map #(clojure.string/join " " %) mp)
       (clojure.string/join ", ")))))

(defn massage-into-table [coll]
  "formats data for display in a swing (seesaw) table."
  (let [ks (keys coll)
        vs (vals coll)]
    (->> (map (fn [record id] (assoc record :id id)) vs ks)
         (map #(assoc-in % [:renters] (join-string-map ( % :renters)))) ;puts renters into strings seperated by commas.
         )))

(defn write-to-disk [location coll]
    (spit location coll))

(defn read-from-disk [location coll]
  "Protects against non-existent database file"
  (if (fs/exists? location)
    (->> (slurp location)
         (read-string )
         (into coll))
    coll))

(defn database-location []
  "Returns system specific location of database file.
  NOTE: Currently database location is hard coded in this function."
  (let [sep (System/getProperty "file.separator")
        path (str "resources" sep "database.txt")]
    path))
