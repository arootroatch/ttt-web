(ns ttt-web.view (:require [clojure.string :refer [upper-case]] [ttt-web.game :as game]))

(defn square [{:keys [board] :as state} n]
  (let [token (nth board n)]
    (cond-> [:button (cond-> {:id (str "cell-" n)
                              :class "board-square"
                              :hx-post (str "/move/" n)
                              :hx-swap "outerHTML"
                              :hx-target "#board-wrapper"}
                       (not (game/playable? state n)) (assoc :disabled :disabled))]
      (keyword? token) (conj (-> token name upper-case)))))

(defn board [{:keys [board] :as state}]
  (let [class (if (> (count board) 9) "four-grid" "three-grid")]
    (into [:div {:id "board" :class class}] (map #(square state %) (range (count board))))))

(defn heading [{:keys [player game-state]}]
  [:h2 {:id "play-heading"}
   (if (= :in-progress game-state)
     (str (-> player name upper-case) "'s turn!")
     game-state)])

(defn board-wrapper-options [state]
  (cond-> {:id "board-wrapper"}
    (game/ai-turn? state) (merge {:hx-post "/ai-move"
                                  :hx-trigger "load delay:300ms"
                                  :hx-swap "outerHTML"})))

(defn board-wrapper [state]
  [:div (board-wrapper-options state)
   (heading state)
   (board state)
   (when (not= :in-progress (:game-state state))
     [:button {:id        "restart"
               :hx-post   "/restart"
               :hx-swap   "outerHTML"
               :hx-target "#board-wrapper"}
      "Restart"])])

