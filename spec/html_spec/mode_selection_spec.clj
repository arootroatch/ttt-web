(ns html-spec.mode-selection-spec
  (:require [clojure.test :refer :all]
            [next.jdbc :as jdbc]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.gui.resume-selection :as resume]
            [html.mode-selection :refer :all]
            [ttt-spec :as ttt-spec]))

(def db-test {:dbtype "postgres" :dbname "ttt-test"})
(def ds-test (jdbc/get-datasource db-test))
(def edn-logs-path-test "spec/game-logs-test.edn")

(describe "mode-selection"
  (with-stubs)
  (before (.reset ttt-spec/out))

  (it "asks for mode-selection"
    (with-redefs [game-logs/get-last-in-progress-game (stub :last)
                  resume/handle-resume (stub :handle-resume {:return (assoc ttt-spec/resume-state
                                                                       :current-screen :mode-selection)})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post-2) ttt-spec/out))
    (should-contain "<p>Please select game mode (X always plays first):</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"mode\"><option value=\"1\">Human vs Human</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">Human vs Computer (Human plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"3\">Computer vs Human (Computer plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"4\">Computer vs Computer</option></select>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "sets human? to true if mode is 1"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-1) ttt-spec/out)
    (should-contain "human? true" (str ttt-spec/out)))

  (it "sets human? to true if mode is 2"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-2) ttt-spec/out)
    (should-contain "human? true" (str ttt-spec/out)))

  (it "sets human? to false if mode is 3"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-3) ttt-spec/out)
    (should-contain "human? false" (str ttt-spec/out)))

  (it "sets human? to false if mode is 4"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-4) ttt-spec/out)
    (should-contain "human? false" (str ttt-spec/out)))

  (context "replay selection"
    (it "gets sql game ids of replayable games"
      (should= [2 3 7 9 10 11 12 13 16 17 18 19 20 21 22 25 28] (get-game-ids {:db :sql :ds ds-test})))

    (it "gets end game ids of replayable games"
      (should= [1 2 3 4 5 6 7 8 9 13 14 15 16 18 19 20 21 22] (get-game-ids {:db :edn :path edn-logs-path-test})))

    (it "asks for replay selection"
      (with-redefs [game-logs/get-last-in-progress-game (stub :last)
                    resume/handle-resume (stub :handle-resume {:return (assoc ttt-spec/resume-state
                                                                         :current-screen :mode-selection)})]
        (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post-2) ttt-spec/out))
      (should-contain "<p>Choose a game to replay</p>" (str ttt-spec/out))
      (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
      (should-contain "<select name=\"replay\">" (str ttt-spec/out))
      (should-contain "<option" (str ttt-spec/out))
      (should-contain "</option>" (str ttt-spec/out))
      (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
      (should-contain "<p>OR</p>" (str ttt-spec/out)))
    ))
