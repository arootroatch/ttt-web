(ns ttt-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.tui.print-utils :as print-utils]
            [ttt :refer :all])
  (:import (MyServer Main)
           (java.util HashMap)
           (org.apache.commons.io.output ByteArrayOutputStream)
           (ttt TTT)))

(def route (->TTT))
(def connData {"request"  "GET /ttt HTTP/1.1\r\n\r\n"
               "resource" "/ttt"})
(def out (ByteArrayOutputStream.))
(def edn-post "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\n\r\ndb=edn")
(def sql-post "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\n\r\ndb=sql")
(def initial-state {:current-screen  :db-selection
                    :mode            nil
                    :board           nil
                    :first-ai-level  nil
                    :second-ai-level nil
                    :player          :x
                    :human?          nil
                    :game-state      :in-progress
                    :ui              :web})

(def resume-state (assoc initial-state :current-screen :resume-selection :db :sql))

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
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str out))
    (should-contain
      "<select name=\"db\"><option value=\"edn\">EDN</option><option value=\"sql\">PostgreSQL</option></select>"
      (str out))
    (should-contain "<input type=\"submit\">Update</input></form></div>" (str out))
    (should-contain #"<html>.*</html>" (str out))
    (should-contain "<div style=\"margin:0 auto;\">" (str out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str out)))

  (it "sets response headers"
    (.serve route connData out)
    (should-contain "HTTP/1.1 200 OK\n" (str out))
    (should-contain "Content-Type: text/html\n" (str out))
    (should-contain #"Content-Length: .*\n" (str out))
    (should-contain "Server: My MacBook Pro\n" (str out))
    (should-contain (str "Set-Cookie: state=" initial-state "\n\n") (str out))
    )

  (it "sets sql-db selection to cookie"
    (.serve route (assoc connData "request" sql-post) out)
    (should-contain (str "Set-Cookie: state=" (assoc initial-state :db :sql :current-screen :resume-selection) "\n\n")
                    (str out)))

  (it "asks if user wants to resume previous game if in-progress"
      (.serve route (assoc connData "request" sql-post) out)
      (should-contain (str "<p>" (first print-utils/resume-prompt) "</p>") (str out))
      )
  )