;wrappers.clj Overview:
;Contains functions which provide additional
;protections or capabilities to the backend functions.
;Some of these functions also wrap special user input, i.e. renter selection,
;onto the backend functions.

(ns manager.wrappers
  (:gen-class)
  (:require
      [seesaw.core :as seesaw]
      [seesaw.table :as tab])
  (:use
   [manager.backend]))

(defn safe-input [prompt]
  {:post [(not (empty? %))]}
    "Protects against user inputting nothing.
    TODO: Throw a real error here?"
  (seesaw/input prompt))

(defn prompt-rental-info []
  "Note: rental period is hard coded for 2 weeks."
  (let [patron-name (safe-input "Patron Name")
        rental-period  14]
   (vector patron-name rental-period)))

(defn rent-movie-wrapper [ID-key database]
  {:pre [(contains? database ID-key)
         (< ((database ID-key) :rented) ((database ID-key) :quantity))]}
  "Combines and adds protections around add-renter and rent-movie functionality."
  (let [[patron rental-prd] (prompt-rental-info)
        due-date (calc-due-date rental-prd)]
    (->>(rent-movie ID-key database)
        (add-renter patron due-date ID-key))))

(defn return-renter-select [patron-list]
  {:pre [(not (empty? patron-list))]
   :post [(not (nil? %))]}
  (seesaw/input "Returned By" :choices patron-list))

(defn return-movie-wrapper [ID-key database]
  {:pre [(contains? database ID-key)
         (pos? ((database ID-key) :rented))]}
  "Combines and adds protections around remove-renter and return-movie functionality."
  (let [patron-list (map first ((database ID-key) :renters))
        selected-patron (return-renter-select patron-list)]
    (->> (return-movie ID-key database)
         (remove-renter selected-patron ID-key))))

(defn remove-movie-wrapper [ID-key database]
  "Provides protection around remove-movie, also helps handle special cases."
  (let [selection-title ((database ID-key) :title)
        num-rented ((database ID-key) :rented)
        quant ((database ID-key) :quantity)
        response (seesaw/dialog :content (str "Remove " selection-title "?")
                                :option-type :yes-no
                                :height 150
                                :width 300)]

        (cond
         (= num-rented quant) (throw (IllegalArgumentException. "All copies are currently rented."))
         (= quant 1) (let [destroy? (= :success (seesaw/show! (seesaw/pack! response)))]
                            (if destroy? (remove-movie ID-key database)
                              database))
         (< num-rented quant) (let [destroy? (= :success (seesaw/show! (seesaw/pack! response)))]
         (if destroy? (assoc-in database [ID-key :quantity] (dec ((database ID-key) :quantity)))
           database)))))

(defn max-ID-helper [database]
  (if (empty? database) 0
    (apply max (keys database))))

(defn add-movie-wrapper [database]
  "Provides protection around add-movie, and helps handle ID assignment."
  (let [title (safe-input "Enter Movie Title")
        price (read-string (safe-input "Enter Price"))
        max-ID (max-ID-helper database)]         ;max-ID (apply max (keys database))]
    (if (number? price) (add-new-movie {:ID (inc max-ID) :title title :price price :quantity 1} database)
      (throw (IllegalArgumentException. "Error, entered price not a number.")))))

(defn additional-copy-wrapper [ID-key database]
   {:pre [(contains? database ID-key)]}
  (add-additional-copy ID-key database))

(defn change-price-wrapper [ID-key database]
  {:pre [(contains? database ID-key)]}
  "Provides protection for change-price."
  (let[input (read-string (safe-input "Enter New Price"))]
    (if (number? input) (change-price input ID-key database)
    (throw (IllegalArgumentException. "Error, entered price not a number.")))))

(defn search-type []
  {:post [(not (nil? %))]}
  (seesaw/input "Search by"
                :choices ["ID" "Title"]))

(defn find-search-helper [search-type query database]
  (if (= "ID" search-type) (record-from-id query database)
    (record-from-title (str query) database)))

(defn find-result-helper [search-type query database]
 (let [results (find-search-helper search-type query database)
       results-model (tab/table-model :columns [{:key :id :text "ID"}
                                                {:key :title :text "TITLE"}
                                                {:key :price :text "PRICE"}
                                                {:key :quantity :text "QUANTITY"}
                                                {:key :rented :text "RENTED"}
                                                {:key :renters :text "RENTERS"}]
                                      :rows (massage-into-table results))
          results-table (seesaw/table :model results-model)]
   (if-not (empty? results)
      (seesaw/show! (seesaw/pack! (seesaw/frame
                                   :title "Search Results"
                                   :content (seesaw/scrollable results-table)
                                   :width 400
                                   :height 400)))
     (throw (IllegalArgumentException. "No results found.")))))

(defn find-wrapper [database]
  (let [s-type (search-type)
        s-query (read-string (safe-input "Search query"))]
    (find-result-helper s-type s-query database)
  database)) ;;returns database to satisfy swap in button wrapper.

(defn save-wrapper [location database]
  (write-to-disk location database))
