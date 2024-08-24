(ns main_spec
  (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [main :refer :all])
  (:import (MyServer Main)))

(describe "serving ttt"
  (tags :main)

  (it "serves ttt at /ttt"
    (-main "-p" "1234")
    (let [response (client/get "http://localhost:1234/ttt")
          body (:body response)]
      (should= 200 (:status response))
      (should-contain "<h1>Tic-Tac-Toe</h1>" body))
     ))