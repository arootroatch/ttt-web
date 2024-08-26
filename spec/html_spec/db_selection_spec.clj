(ns html-spec.db-selection-spec
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [ttt-spec :as ttt-spec]
            [ttt :as ttt]))

(describe "db-selection"
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
    (should-contain (str "Set-Cookie: state=" (assoc ttt/initial-state :db :sql :current-screen :mode-selection) "\n\n")
                    (str ttt-spec/out)))

  (it "sets edn-db selection to cookie"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/edn-post) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" (assoc ttt/initial-state :db :edn :current-screen :mode-selection) "\n\n")
                    (str ttt-spec/out))))
