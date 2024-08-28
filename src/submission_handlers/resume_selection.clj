(ns submission-handlers.resume-selection
  (:require [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.gui.play :as play]
            [tic-tac-toe.gui.resume-selection :as resume]
            [handle-submission :refer :all]
            [helpers :as helpers]))

(defn- set-resume-state [state old-game]
  (if (= :sql (:db state)) (assoc state :game-log old-game) (assoc state :filepath old-game)))

(defmethod handle-submission :resume-selection [_ post-value outputStream state]
  (let [old-game (game-logs/get-last-in-progress-game {:db (:db state) :ds sql/ds :dir-path helpers/edn-in-progress-dir})
        old-state (resume/handle-resume {:n post-value :state (set-resume-state state old-game)})
        new-state (assoc old-state :ui :web)]
    (if (false? (:human? new-state))
      (helpers/write-output outputStream (play/ai-turn new-state))
      (helpers/write-output outputStream new-state))))
