(ns ttt-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [ttt :refer :all])
  (:import (MyServer Main)
           (java.util HashMap)
           (org.apache.commons.io.output ByteArrayOutputStream)))


(def route (->TTT))
(def connData {"request"  "GET /ttt HTTP/1.1\r\n\r\n"
               "resource" "/ttt"})
(def out (ByteArrayOutputStream.))
(def edn-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" initial-state "\r\n\r\ndb=edn"))
(def sql-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" initial-state "\r\n\r\ndb=sql"))
(def resume-state (assoc initial-state :current-screen :resume-selection :db :sql))
(def resume-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 18\r\nCookie: state="
                      resume-state "\r\n\r\nresume-selection=2"))
(def mode-state (assoc resume-state :current-screen :mode-selection :resume-selection 2))
(def mode-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 18\r\nCookie: state="
                      mode-state "\r\n\r\nmode=2"))

(describe "tests"
  (with-stubs)
  (before (.reset out))

  #_(it ""
      (let [my-map (HashMap.)]
        (.put my-map "blah1" "blah2")
        (let [{:strs [blah1]} my-map]
          (prn "blah1:" blah1))))

  (it "sets response headers"
    (.serve route connData out)
    (should-contain "HTTP/1.1 200 OK\n" (str out))
    (should-contain "Content-Type: text/html\n" (str out))
    (should-contain #"Content-Length: .*\n" (str out))
    (should-contain "Server: My MacBook Pro\n" (str out))
    (should-contain (str "Set-Cookie: state=" initial-state "\n\n") (str out)))
  )