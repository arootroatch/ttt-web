(ns submission-handlers.default
  (:require [helpers :as helpers]
            [handle-submission :refer :all]))

(defmethod handle-submission :default [post-key post-value outputStream state]
  (let [post-state (assoc state post-key post-value)
        new-state (assoc post-state :current-screen (helpers/set-screen post-state))]
    (helpers/write-output outputStream new-state)))
