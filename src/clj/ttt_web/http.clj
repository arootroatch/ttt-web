(ns ttt-web.http
  (:require [hiccup2.core :as hiccup]
            [ring.util.response :as response]))

(defn html-ok [body]
  (-> (response/response (str (hiccup/html body)))
      (response/content-type "text/html")))

(defn html-page-ok [& content]
  (-> (response/response (str (hiccup/html content)))
      (response/content-type "text/html")))
