(ns html-spec.resume-selection
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [ttt-spec :as ttt-spec]))

(describe "resume-selection"
  (with-stubs)
  (before (.reset ttt-spec/out))

  (it "asks if user wants to resume previous game if in-progress"
    (with-redefs [game-logs/get-last-in-progress-game
                  (stub :last {:return {:game-id 8}})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
      (should-contain (str "Set-Cookie: state=" ttt-spec/resume-state "\n\n")
                      (str ttt-spec/out))
      (should-contain "<p>There&apos;s an unfinished game! Would you like resume?</p>" (str ttt-spec/out))
      (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
      (should-contain
        "<select name=\"resume-selection\"><option value=\"1\">Yes</option><option value=\"2\">No</option></select>"
        (str ttt-spec/out))
      (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
      (should-contain #"<html>.*</html>" (str ttt-spec/out))
      (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
      (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))))
