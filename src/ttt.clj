(ns ttt
  (:require [clojure.string :as string]
            [hiccup2.core :as h])
  (:import MyServer.Route))

(def state {:current-screen  :db-selection
            :mode            nil
            :board           nil
            :first-ai-level  nil
            :second-ai-level nil
            :player          :x
            :human?          nil
            :game-state      :in-progress
            :ui              :web})

(defn- db-selection []
  (str (h/html [:html
                [:div {:style "margin:0 auto;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p "Please select your database"]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "db"}
                   [:option {:value :edn} "EDN"]
                   [:option {:value :sql} "PostgreSQL"]]
                  [:input {:type "submit"} "Update"]]]])
       )
  )

(defn- write-output [outputStream content state]
  (.write outputStream (.getBytes (str "HTTP/1.1 200 OK\n")))
  (.write outputStream (.getBytes (str "Content-Type: text/html\n")))
  (.write outputStream (.getBytes (str "Content-Length: " (count (content)) "\n")))
  (.write outputStream (.getBytes (str "Server: My MacBook Pro\n")))
  (.write outputStream (.getBytes (str "Set-Cookie: state=" state "\n\n")))
  (.write outputStream (.getBytes (content)))
  (.flush outputStream))

(defn- parse-submission [request]
  (let [value (-> request
                  (string/split #"\r\n\r\n")
                  (second)
                  (string/split #"=")
                  (second)
                  (read-string))]
    (if (number? value) value (keyword value))))


(deftype TTT []
  Route
  (serve [this connData outputStream]
    (if (string/includes? (get connData "request") "POST")
      (let [post (parse-submission (get connData "request"))]
        (write-output outputStream db-selection (assoc state :db post :current-screen :resume-selection)))
      (write-output outputStream db-selection state)))
  )