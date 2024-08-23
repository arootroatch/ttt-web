(ns ttt-spec
  (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [ttt :refer :all])
  (:import (MyServer Main)
           (java.util HashMap)
           (org.apache.commons.io.output ByteArrayOutputStream)
           (ttt TTT)))

(def route (->TTT))
(def connData {"request" "GET /ttt HTTP/1.1\r\n\r\n"
               "resource" "/ttt"})
(def out (ByteArrayOutputStream.))

(def resume-html)

(describe "tests"
  #_(before-all
    (Main/setRoute "/ttt" (TTT.))
    (Main/main (into-array ["-p" "1234"])))

  (before (.flush out))

  #_(it ""
    (let [my-map (HashMap.)]
      (.put my-map "blah1" "blah2")
      (let [{:strs [blah1]} my-map]
        (prn "blah1:" blah1))))

  (it "asks for db preference"
    (.serve route connData out)
    (should-contain "<p>Please select your database</p>" (str out))
    (should-contain "<p>Please select your database</p>" (str out))
    )


  )