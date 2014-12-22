(ns dataikutest.ui.handler
  (:require
    [ajax.core :refer [GET POST]]
    [dommy.core :refer [listen! set-html! html text] :refer-macros [sel sel1]]
    [hipo :as hipo :include-macros true]))

(defn update-table [d]
 (let [rendered (.render js/Mustache (text (sel1 :#table-template)) d)]
   (set-html! (sel1 :#table-div) rendered)))

(defn handler-by-age [response]
  (update-table (clj->js {:distributions response})))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn set-variable [value]
  (GET (str "/api/byage/" value) {:handler handler-by-age :error-handler error-handler}))

(defn select-handler [e]
  (set-variable (.-value (.-target e))))

(defn init [e]
  (let [selected (text (sel1 "#columns option[selected]"))]
    (set-variable selected)
    (set! (.-selectedIndex (sel1 "#columns")) 0)))

(listen! (sel1 :#columns) :click select-handler)
(listen! js/window :load init)


