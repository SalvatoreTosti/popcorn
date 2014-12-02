;gui.clj Overview:
;Contains high level functions for the creation of
;buttons, panels and the actual frame of the program.
;Also contains central method for execution of the program, 'run'.
;Note:
;Assertion errors are treated as 'silent' error.
;In the context of a button, if an assertion error occurs
;the action is simply discarded and no explicit notice is given to the user.

(ns manager.gui
  (:gen-class)
  (:require
      [seesaw.core :as seesaw]
      [seesaw.table :as tab]
      [seesaw.swingx :as swingx])
  (:use
   [manager.backend :only (massage-into-table,read-from-disk,database-location)]
   [manager.wrappers]))

(defn display [content]
  "General display function, based on lecture slides, builds Jframe for program."
  (let [window (seesaw/frame
                :title "Movie Database Manager"
                :on-close :exit
                ;:menubar menu-bar
                :content content
                :width 400
                :height 400)]
    ;(seesaw/native!)
    (seesaw/show! window)))

(defn get-display-filter [kword]
  "Selects which movies to display, either rented, available or all."
  (cond
   (= kword :show-available) (fn [x] (filter #(< ((val %) :rented) ((val %) :quantity)) x))
   (= kword :show-rented) (fn [x] (filter #(pos? ((val %) :rented)) x))
   :else
   (fn [x] (filter #(not (nil? %))x))))

(defn current-display-filter [visibility-mode]
  "Returns display filter function for radio button list filtering."
  (->> (seesaw/selection visibility-mode)
       (seesaw/id-of)
       (get-display-filter)))

(defn visibility-id-key [visibility-mode]
  "Returns keyword, which can be used to get current filter function for radio button list filtering."
  (->> (seesaw/selection visibility-mode)
       (seesaw/id-of)))

(defn movie-table-builder [db]
  "A construction method for making table-x's out of maps of maps."
  (let [model (tab/table-model :columns [{:key :id :text "ID"}
                                         {:key :title :text "TITLE"}
                                         {:key :price :text "PRICE"}
                                         {:key :quantity :text "QUANTITY"}
                                         {:key :rented :text "RENTED"}
                                         {:key :renters :text "RENTERS"}]
                                :rows (massage-into-table db))]
    (swingx/table-x
     :horizontal-scroll-enabled? true
     :model model)))

(defn view-mode-update [atom-db movie-table visibility-mode]
  "Updates the visible table to reflect any changes in the database, applies current filter as well."
  (let [id-key (visibility-id-key visibility-mode)
        filter-function (current-display-filter visibility-mode)
        filtered-movies (filter-function @atom-db)
        filtered-model (tab/table-model :columns [{:key :id :text "ID"}
                                                  {:key :title :text "TITLE"}
                                                  {:key :price :text "PRICE"}
                                                  {:key :quantity :text "QUANTITY"}
                                                  {:key :rented :text "RENTED"}
                                                  {:key :renters :text "RENTERS"}]
                                      :rows (massage-into-table filtered-movies))]
    (seesaw/config! movie-table :model filtered-model)))


;Button builders

(defn selection-button [{function :function
                         text :text
                         atom-db :atom-db
                         movie-list :movie-list
                         visibility-mode :visibility-mode}]
  "Builds a button with action based on information taken from a selection."
  (seesaw/button
   :text text
   :listen [:action (fn [e]
                      (try
                        (assert (not (nil? (seesaw/selection movie-list))))
                        (let [selection (tab/value-at movie-list (seesaw/selection movie-list))
                              selection-ID (selection :id)] ;(movie-list-selection movie-list)]
                          (swap! atom-db #(function selection-ID %))
                          (save-wrapper (database-location) @atom-db)
                          (view-mode-update atom-db movie-list visibility-mode))
                        ;(catch AssertionError e (println "Invalid Operation."))
                        (catch AssertionError e nil)
                        (catch Exception e (seesaw/alert (.getMessage e)))))]))

(defn independent-button [{function :function
                           text :text
                           atom-db :atom-db
                           movie-list :movie-list
                           visibility-mode :visibility-mode}]
  "Builds a button with action based on information in the table, but not a single selection."
  (seesaw/button
   :text text
   :listen [:action (fn [e]
                      (try
                        (swap! atom-db #(function %))
                        (save-wrapper (database-location) @atom-db)
                        (view-mode-update atom-db movie-list visibility-mode)
                        ;(catch AssertionError e (println "Invalid Operation."))
                        (catch AssertionError e nil)
                        (catch Exception e (seesaw/alert (.getMessage e)))))]))


(defn IO-button  [{function :function
                                text :text
                                atom-db :atom-db}]
  "Builds a button with action based on IO, does not alter table or require selection."
  (seesaw/button
   :text text
   :listen [:action (fn [e]
                      (try
                        (function (database-location) @atom-db)
                        ;(catch AssertionError e (println "Invalid Operation."))
                        (catch AssertionError e nil)
                        (catch Exception e (seesaw/alert (.getMessage e)))))]))

;Panel Builders

(defn build-filter-panel [group]
  (let[ all-visible (seesaw/radio :id :show-all :text "All" :group group :selected? true)
        rent-visible (seesaw/radio :id :show-rented :text "Rented" :group group)
        avail-visible (seesaw/radio :id :show-available :text "Available" :group group)]
    (seesaw/flow-panel :items [rent-visible  avail-visible all-visible])))


(defn button-side-panel [db movie-list visibility-mode]
  "Creates a series of buttons and returns a vertical panel which contains them."
  (let [rent-button (selection-button {:function rent-movie-wrapper
                                 :text "Rent"
                                 :atom-db db
                                 :movie-list movie-list
                                 :visibility-mode visibility-mode})

        return-button (selection-button {:function return-movie-wrapper
                                         :text "Return"
                                         :atom-db db
                                         :movie-list movie-list
                                         :visibility-mode visibility-mode})

        remove-button (selection-button {:function remove-movie-wrapper
                                         :text "Remove"
                                         :atom-db db
                                         :movie-list movie-list
                                         :visibility-mode visibility-mode})

        additional-copy-button (selection-button {:function additional-copy-wrapper
                                                  :text "Add Copy"
                                                  :atom-db db
                                                  :movie-list movie-list
                                                  :visibility-mode visibility-mode})

        change-price-button (selection-button {:function change-price-wrapper
                                               :text "Adjust Price"
                                               :atom-db db
                                               :movie-list movie-list
                                               :visibility-mode visibility-mode})

        add-movie-button (independent-button {:function add-movie-wrapper
                                               :text "Add Movie"
                                               :atom-db db
                                               :movie-list movie-list
                                               :visibility-mode visibility-mode})

        find-button (independent-button {:function find-wrapper
                                         :text "Find"
                                         :atom-db db
                                         :movie-list movie-list
                                         :visibility-mode visibility-mode})

        save-button (IO-button {:function save-wrapper
                                :text "Save"
                                :atom-db db})]

        (seesaw/vertical-panel :items [rent-button
                                       return-button
                                       remove-button
                                       add-movie-button
                                       additional-copy-button
                                       find-button
                                       change-price-button])))
                                       ;save-button])))

(defn combine-panels [movie-list radio-panel button-panel]
  "Combines movie table, action buttons and radio buttons into a single panel."
  (let [main-panel (seesaw/left-right-split movie-list button-panel :divider-location 300)]
    (seesaw/top-bottom-split main-panel radio-panel :divider-location 320)))

(defn run []
  "Central method which initializes program and begins execution."
  (let [db (atom (read-from-disk (database-location) {}))
        movie-list (movie-table-builder @db)
        movie-list-scroll (seesaw/scrollable movie-list)

        visibility-mode (seesaw/button-group)
        radio-panel (build-filter-panel visibility-mode)
        button-panel (button-side-panel db movie-list visibility-mode)
        final-panel (combine-panels movie-list-scroll radio-panel button-panel)
        ;menubar not implemented, but left for future reference.
        ;menu-bar (seesaw/menubar
        ;          :items [(seesaw/menu :text "File"
        ;                               :items (seesaw/action :name "Add New Movie...")
        ;                               :key "menu N"
        ;                               :handler (])
        ]

    (seesaw/listen visibility-mode :action
                (fn [e](view-mode-update db movie-list visibility-mode)))

    (display final-panel)))
