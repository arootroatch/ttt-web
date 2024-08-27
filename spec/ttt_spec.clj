(ns ttt-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.tui.get-selection :as selection]
            [ttt :refer :all])
  (:import (java.util HashMap)
           (org.apache.commons.io.output ByteArrayOutputStream)))


(def route (->TTT))
(def connData {"request"  "GET /ttt HTTP/1.1\r\n\r\n"
               "resource" "/ttt"})
(def out (ByteArrayOutputStream.))

(def edn-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" initial-state "\r\n\r\ndb=edn"))
(def sql-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" initial-state "\r\n\r\ndb=sql"))
(def resume-state (assoc initial-state :current-screen :resume-selection :db :sql))
(def resume-post-2 (str "POST /ttt HTTP/1.1\r\nContent-Length: 18\r\nCookie: state="
                        ttt-spec/resume-state "\r\n\r\nresume-selection=2"))
(def resume-post-1 (str "POST /ttt HTTP/1.1\r\nContent-Length: 18\r\nCookie: state="
                        ttt-spec/resume-state "\r\n\r\nresume-selection=1"))
(def mode-state (assoc resume-state :current-screen :mode-selection :resume-selection 2 :game-id 23))
(def mode-post-2 (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" mode-state "\r\n\r\nmode=2"))
(def mode-post-1 (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" mode-state "\r\n\r\nmode=1"))
(def mode-post-3 (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" mode-state "\r\n\r\nmode=3"))
(def mode-post-4 (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state=" mode-state "\r\n\r\nmode=4"))
(def board-state (assoc mode-state :current-screen :board-selection :mode 2 :human? true))
(def board-post-1 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state=" board-state "\r\n\r\nboard=1"))
(def board-post-2 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state=" board-state "\r\n\r\nboard=2"))
(def board-post-2-mode-1 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state="
                              (assoc board-state :mode 1) "\r\n\r\nboard=2"))
(def board-post-1-mode-1 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state="
                              (assoc board-state :mode 1) "\r\n\r\nboard=1"))
(def board-post-mode-4 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state="
                            (assoc board-state :mode 4) "\r\n\r\nboard=2"))
(def board-post-mode-3 (str "POST /ttt HTTP/1.1\r\nContent-Length: 7\r\nCookie: state="
                            (assoc board-state :mode 3) "\r\n\r\nboard=2"))
(def level-state (assoc board-state :board selection/initial-3x3-board :current-screen :level-selection))
(def level-post-mode-4 (str "POST /ttt HTTP/1.1\r\nContent-Length: 16\r\nCookie: state="
                            (assoc level-state :mode 4) "\r\n\r\nfirst-ai-level=2"))
(def second-level-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 16\r\nCookie: state="
                            (assoc level-state :mode 4 :first-ai-level 2) "\r\n\r\nsecond-ai-level=2"))
(def level-post (str "POST /ttt HTTP/1.1\r\nContent-Length: 16\r\nCookie: state="
                     level-state "\r\n\r\nfirst-ai-level=2"))

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