(ns html-spec.board-selection-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [tic-tac-toe.tui.get-selection :as selection]
            [ttt-spec :as ttt-spec]))

(describe "board-selection"
  (with-stubs)
  (redefs-around [game-logs/get-new-game-id (stub :game-id {:return 23})
                  edn/create-in-progress-game-file (stub :create-file)
                  edn/log-game-id (stub :log-id)
                  sql/log-game-state (stub :log-game)])
  (before (.reset ttt-spec/out))

  (it "asks for board selection after receiving mode selection"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-2) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" ttt-spec/board-state) (str ttt-spec/out))
    (should-contain "<p>Please select board:</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"board\"><option value=\"1\">3x3</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">4x4</option>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "sets 3x3 board to cookie"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-1) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" ttt-spec/level-state)
                    (str ttt-spec/out)))

  (it "sets 4x4 board to cookie"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-2) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" (assoc ttt-spec/level-state :board selection/initial-4x4-board))
                    (str ttt-spec/out)))

  (it "set screen to play if mode is 1 (HvH)"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-2-mode-1-sql) ttt-spec/out)
    (should-contain ":current-screen :play" (str ttt-spec/out)))

  (it "logs game state if mode is 1 (HvH) - sql"
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-2-mode-1-sql) ttt-spec/out)
      (should-contain ":game-id 23" (str ttt-spec/out))
      (should-contain ":current-screen :play" (str ttt-spec/out))
      (should-have-invoked :log-game))

  (it "logs game state if mode is 1 (HvH) - edn"
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-2-mode-1-edn) ttt-spec/out)
      (should-contain ":game-id 23" (str ttt-spec/out))
      (should-contain ":current-screen :play" (str ttt-spec/out))
      (should-have-invoked :log-id)
      (should-have-invoked :create-file))
  )