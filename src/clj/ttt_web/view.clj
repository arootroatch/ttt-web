(ns ttt-web.view (:require [clojure.string :refer [upper-case]] [tic-tac-toe.prompts :as prompts]
                           [ttt-web.game :as game]))

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
      "Restart"])
   (when (not= :in-progress (:game-state state))
     [:button {:id           "new-game"
               :hx-get       "/mode"
               :hx-swap      "outerHTML"
               :hx-target    "#board-wrapper"
               :hx-push-url  "true"}
      "New Game"])])

(defn selection-button [state-attr number label]
  [:button {:id (str state-attr "-" number)
            :class "selection-btn"
            :hx-post (str "/" state-attr "/" number)
            :hx-target "#screen"
            :hx-swap "outerHTML"}
   label])

(defn mode-selection []
  (into [:div {:id "screen" :class "screen"}
         [:h2 (first prompts/mode-prompt)]]
        (for [n [1 2 3 4]]
          (selection-button "mode" n (nth prompts/mode-prompt n)))))

(defn board-selection []
  (into [:div {:id "screen" :class "screen"}
         [:h2 (first prompts/board-prompt)]]
        (for [n [1 2]]
          (selection-button "board" n (nth prompts/board-prompt n)))))

(defn level-prompt [{:keys [mode] :as state}]
  (nth prompts/level-prompt
       (case (game/next-level-key state)
         :second-ai-level 1
         :first-ai-level (if (= 4 mode) 0 2)
         2)))

(defn level-selection [state]
  (into [:div {:id "screen" :class "screen"}
         [:h2 (level-prompt state)]]
        (for [[level label] [[1 3] [2 4] [3 6]]]
          (selection-button "level" level (nth prompts/level-prompt label)))))
