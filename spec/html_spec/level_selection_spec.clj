(ns html-spec.level-selection-spec
  (:require [html.level-selection :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]))

(describe "level-selection"
  (with-stubs)
  (redefs-around [edn/create-in-progress-game-file (stub :create-file)
                  edn/log-game-id (stub :log-id)
                  sql/log-game-state (stub :log-game)
                  game-logs/log-move (stub :log-move)
                  game-logs/log-completed-game (stub :log-completed)])
  (before (.reset ttt-spec/out))

  (it "prompts for first ai level selection as player X if mode is 3"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-mode-3) ttt-spec/out)
    (should-contain "<p>Please select level for player X:</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"first-ai-level\"><option value=\"1\">Easy</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">Medium</option>" (str ttt-spec/out))
    (should-contain "<option value=\"3\">Unbeatable</option>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "prompts for first ai level selection as player X if mode is 4"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-mode-4) ttt-spec/out)
    (should-contain "<p>Please select level for player X:</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"first-ai-level\"><option value=\"1\">Easy</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">Medium</option>" (str ttt-spec/out))
    (should-contain "<option value=\"3\">Unbeatable</option>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "prompts for first ai level selection as player O if mode is 2"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-1) ttt-spec/out)
    (should-contain "<p>Please select level for player O:</p>" (str ttt-spec/out)))

  (it "prompts for second ai level selection if mode is 4 and first ai has been selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-mode-4) ttt-spec/out)
    (should-contain ":first-ai-level 2" (str ttt-spec/out))
    (should-contain "<p>Please select level for player O:</p>" (str ttt-spec/out))
    (should-contain "<select name=\"second-ai-level\"><option value=\"1\">Easy</option>" (str ttt-spec/out)))

  (it "sets current-screen to play if mode is 2 and level is selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post) ttt-spec/out)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "sets current-screen to play if mode is 3 and level is selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-mode-3) ttt-spec/out)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "triggers ai-move if mode is 3 and level is selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-mode-3) ttt-spec/out)
    (should-contain ":player :o" (str ttt-spec/out))
    (should-contain ":human? true" (str ttt-spec/out))
    (should-not-contain ":board [1 2 3 4 5 6 7 8 9]" (str ttt-spec/out)))

  (it "logs game if mode is 2 and level is selected - sql"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post) ttt-spec/out)
    (should-have-invoked :log-game)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "logs game if mode is 3 and level is selected - sql"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-mode-3) ttt-spec/out)
    (should-have-invoked :log-game)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "logs game if mode is 2 and level is selected - edn"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-edn) ttt-spec/out)
    (should-have-invoked :create-file)
    (should-have-invoked :log-id)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "logs game if mode is 3 and level is selected - edn"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/level-post-edn-mode-3) ttt-spec/out)
    (should-have-invoked :create-file)
    (should-have-invoked :log-id)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "sets current-screen to play if mode is 4 and both levels are selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post) ttt-spec/out)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "triggers ai move if mode is 4 and both levels are selected"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post) ttt-spec/out)
    (should-contain ":human? false" (str ttt-spec/out))
    (should-not-contain ":board [1 2 3 4 5 6 7 8 9]" (str ttt-spec/out)))

  (it "mode 4 plays ai vs ai game"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post) ttt-spec/out)
    (should-contain ":human? false" (str ttt-spec/out))
    (should-not-contain ":game-state :in-progress" (str ttt-spec/out))
    (should-not-contain ":board [1 2 3 4 5 6 7 8 9]" (str ttt-spec/out)))

  (it "ai vs ai logs completed game"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post) ttt-spec/out)
    (should-have-invoked :log-completed))

  (it "logs game if mode is 4 and both levels are selected - sql"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post) ttt-spec/out)
    (should-have-invoked :log-game)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "logs game if mode is 4 and both levels are selected - edn"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/second-level-post-edn) ttt-spec/out)
    (should-have-invoked :create-file)
    (should-have-invoked :log-id)
    (should-contain ":current-screen :play" (str ttt-spec/out))))