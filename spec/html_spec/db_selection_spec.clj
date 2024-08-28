(ns html-spec.db-selection-spec
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [ttt :as ttt]
            [ttt-spec :as ttt-spec]))

(describe "db-selection"
  (with-stubs)
  (redefs-around [game-logs/get-last-in-progress-game (stub :last)
                  game-logs/get-new-game-id (stub :game-id {:return 23})])
  (before (.reset ttt-spec/out))

  (it "asks for db preference"
    (.serve ttt-spec/route ttt-spec/connData ttt-spec/out)
    (should-contain "<p>Please select your database</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain
      "<select name=\"db\"><option value=\"edn\">EDN</option><option value=\"sql\">PostgreSQL</option></select>"
      (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "sets sql-db selection to cookie"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
    (should-contain ":db :sql" (str ttt-spec/out)))

  (it "sets edn-db selection to cookie"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/edn-post) ttt-spec/out)
    (should-contain ":db :edn" (str ttt-spec/out)))

  (it "creates new game-id - edn"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/edn-post) ttt-spec/out)
    (should-contain ":game-id 23" (str ttt-spec/out)))

  (it "creates new game-id - sql"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
    (should-contain ":game-id 23" (str ttt-spec/out)))

  (it "adds new edn temp filepath to state"
       (with-redefs [edn/create-new-filepath (stub :create-filepath {:return "test-path"})]
         (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/edn-post) ttt-spec/out)
         (should-have-invoked :create-filepath {:with [ttt/edn-in-progress-dir 23]})
         (should-contain ":filepath \"test-path\"" (str ttt-spec/out))))

  (it "sets screen to resume if there's an in-progress game"
    (with-redefs [game-logs/get-last-in-progress-game
                  (stub :last {:return {:games/id 8 :games/board (str (range 9))}})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
      (should-contain ":current-screen :resume-selection" (str ttt-spec/out))
      (should-have-invoked :last)))

  (it "does not ask to resume if board is 3x3x3"
    (with-redefs [game-logs/get-last-in-progress-game
                  (stub :last {:return {:games/id 8 :games/board (str (range 27))}})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
      (should-contain ":current-screen :mode-selection" (str ttt-spec/out))))

  (it "sets screen to mode-selection if no in-progress game"
    (with-redefs [game-logs/get-last-in-progress-game (stub :last {:return nil})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
      (should-contain ":current-screen :mode-selection" (str ttt-spec/out)))))
