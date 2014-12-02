(ns assignment5.backend-test
  (:require [clojure.test :refer :all]
            [assignment5.backend :refer :all]))

;;Note:
;;long tests are written as follows
;;Final result
;;  input line 1
;;  input line 2


(deftest add-new-movie-test
  (testing "add-new-movie function"
    (are [x movie datab] (= x (add-new-movie movie datab))
         {1 {:title "test" :price 1.50 :quantity 1 :rented 0 :renters {}}}
           {:ID 1 :title "test" :price 1.50 :quantity 1} {}

         {1 {:title "test" :price 1.50 :quantity 1 :rented 1 :renters {"steve" "1/02/15"}}
          2 {:title "test2" :price 2.50 :quantity 2 :rented 0 :renters {}}}
           {:ID 2 :title "test2" :price 2.50 :quantity 2}
           {1 {:title "test" :price 1.50 :quantity 1 :rented 1 :renters {"steve" "1/02/15"}}})

    (is (thrown? AssertionError (add-new-movie [] {})))
    (is (thrown? AssertionError (add-new-movie {} {})))
    (is (thrown? AssertionError (add-new-movie {:a 1} {})))
    (is (thrown? AssertionError (add-new-movie nil {})))
    (is (thrown? AssertionError (add-new-movie {:ID 1 :title "test" :price 1.50 :quantity 1} nil)))))

(deftest remove-movie-test
  (testing "remove-movie function"
    (are [x ID-key datab] (= x (remove-movie ID-key datab))
       {1 {:title "test" :price 1.50 :quantity 1 :rented 1}}
         2
         {1 {:title "test" :price 1.50 :quantity 1 :rented 1}
          2 {:title "test2" :price 2.50 :quantity 2 :rented 0}})
    (is (thrown? AssertionError (remove-movie 3 {1 {:title "test" :price 1.50 :quantity 1 :rented 1}})))))

(deftest rent-movie-test
  (testing "rent-movie function"
    (are [x ID-key datab] (= x (rent-movie ID-key datab))
         {1 {:title "test" :price 1.50 :quantity 1 :rented 1}}
           1 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})
    (is (thrown? AssertionError (rent-movie 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))
    (is (thrown? Exception (rent-movie 1 {1 {:title "test" :price 1.50 :quantity 1 :rented 1}})))))

(deftest return-movie-test
   (testing "rent-movie function"
     (are [x ID-key datab] (= x (return-movie ID-key datab))
          {1 {:title "test" :price 1.50 :quantity 1 :rented 0}}
            1 {1 {:title "test" :price 1.50 :quantity 1 :rented 1}})
     (is (thrown? AssertionError (return-movie 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))
     (is (thrown? Exception (return-movie 1 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))))

(deftest add-additional-copy-test
  (testing "add-additional-copy function"
    (are [x ID-key datab] (= x (add-additional-copy ID-key datab))
          {1 {:title "test" :price 1.50 :quantity 2 :rented 1}}
          1 {1 {:title "test" :price 1.50 :quantity 1 :rented 1}})
    (is (thrown? AssertionError (add-additional-copy 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))))

(deftest change-price-test
  (testing "change-price function"
    (are [x new-price ID-key datab] (= x (change-price new-price ID-key datab))
          {1 {:title "test" :price 2.35 :quantity 1 :rented 1}}
          2.35 1 {1 {:title "test" :price 1.50 :quantity 1 :rented 1}})
    (is (thrown? AssertionError (change-price 2.50 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))
    (is (thrown? AssertionError (change-price "2.50" 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))))

(deftest quantity-of-movie-test
  (testing "quantity-of-movie function"
    (are [x ID-key datab] (= x (quantity-of-movie ID-key datab))
         3 1 {1 {:title "test" :price 2.35 :quantity 3 :rented 1}})
    (is (thrown? AssertionError (quantity-of-movie 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))))

(deftest find-price-of-movie-test
  (testing "price-of-movie function"
    (are [x ID-key datab] (= x (price-of-movie ID-key datab))
         2.35 1 {1 {:title "test" :price 2.35 :quantity 3 :rented 1}})
    (is (thrown? AssertionError (price-of-movie 2 {1 {:title "test" :price 1.50 :quantity 1 :rented 0}})))))

(deftest record-from-title-test
   (testing "record-from-title function"
    (are [x title datab] (= x (record-from-title title datab))
         '([1 {:title "test", :price 1.50, :quantity 1, :rented 1}])
         "test"
         {1 {:title "test" :price 1.50 :quantity 1 :rented 1}
          2 {:title "test2" :price 2.50 :quantity 2 :rented 0}}

         '()
         "not here"
         {1 {:title "test" :price 1.50 :quantity 1 :rented 1}
          2 {:title "test2" :price 2.50 :quantity 2 :rented 0}})))

(deftest make-movie-record-test)
(deftest read-from-disk-test)
(deftest write-to-disk-test)



