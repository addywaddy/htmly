(ns htmly.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

;; (def app (atom {:title ["Hello"]}))

(defn editable [ctx owner]
  (reify
  om/IRender
  (render [this]
    (dom/textarea #js {:className "form-control"
                       :onChange (fn [e] (om/transact! ctx [0] (fn [str] (.. e -target -value))))
                       :value (first ctx)
                       }))))

;; (defn title [ctx owner]
;;   (reify
;;   om/IRender
;;   (render [this]
;;     (dom/h1 nil (first ctx))
;;     )
;;     )
;;   )

;; (defn application [ctx owner]
;;   (reify
;;     om/IRender
;;     (render [this]
;;       (dom/div nil
;;                (om/build title (:title ctx))
;;                (om/build editable (:title ctx))
;;                ))))

;; (defn main []
;;   (om/root
;;    application
;;    app
;;    {:target (. js/document (getElementById "app"))}))

(def old-app (atom {:steps [
                              {:title "Heading" :description "Description" :markup {:tag dom/h2 :content ["Deine Name kommt hier"]}}
                              {:title "Paragraph" :description "Description" :markup {:tag dom/p :content ["Hier kannst du etwas über dich selbst schreiben."]}}
                              {:title "List" :description "Description" :markup {:tag dom/li :content ["Erste list punkt"]}}
                              {:title "Table" :description "Description" :markup {:tag dom/table :content [""]}}
                              {:title "Forms" :description "Description" :markup {:tag dom/form :content [""]}}
                              {:title "Images" :description "Description" :markup {:tag dom/img :content [""]}}
                              ]}))

(def app (atom {:columns [[
                           {:function (fn [] image)
                            :src: "img/image.png"}

                           {:function (fn [] title)
                            :default "Deine Name"}

                           {:function (fn [] intro)
                            :default "Etwas text über dich, der dich grob beschreibt. Es muss nicht all zu lang sein, aber genug um einen Eindruck von dir zu bekommen."}

                           {:function (fn [] table)
                            :rows [{:th "Alter" :td "10"}
                                      {:th "Große" :td "1,30m"}
                                      {:th "Haarfarbe" :td "Braun"}
                                      {:th "Augenfarbe" :td "Blau"}]}]
                          [
                           {:function (fn [] ulist)
                            :title "Daumen Hoch"
                            :intro "Diese Sachen finde ich cool:"
                            :items [{:content "One"}
                                      {:content "Two"}
                                      {:content "Three"}]}

                           {:function (fn [] ulist)
                            :title "Daumen Runter"
                            :intro "Diese Sachen finde ich schlecht:"
                            :items [{:content "One"}
                                      {:content "Two"}
                                      {:content "Three"}]}

                           {:function (fn [] olist)
                            :title "Mein Top Lieder"
                            :intro "Diese Songs finde ich der Hammer:"
                            :items [{:content "One"}
                                      {:content "Two"}
                                      {:content "Three"}]}

                           {:function (fn [] olist)
                            :title "Mein Top Filme"
                            :intro "Diese Filme sind genial:"
                            :items [{:content "One"}
                                      {:content "Two"}
                                      {:content "Three"}]}]
                          [
                           {:function (fn [] form)
                            :title "Quiz"
                            :intro "Rate mal, was mein Lieblingstier ist"
                            :checkboxes [{:content "Giraffe"}
                                      {:content "Elefant"}
                                      {:content "Tiger"}
                                      {:content "Schlange"}
                                      {:content "Hai"}]
                            :button-text "Raten"}]]}))

(defn console-log [obj]
  (.log js/console (pr-str obj)))

(defn h2 [])

(defn element [ctx owner]
  (reify
    om/IRender
    (render [this]
      ((:tag ctx) nil (first (:content ctx))))
    ))

;; (defn handle-input [e owner {:keys [content]}]
;;   (println content)
;;   )


;; (defn handle-change [e state korks]
;;   (println e)
;;   (do
;;     (println (.. e -target -value))
;;     (.preventDefault e)
;;     (put! step-update-ch (.. e -target -value))
;;     )
;;   )

;; (defn handle-change-new [e owner state]
;;   (println (om/get-state owner [:markup :content]))
;;   (println (.. e -target -value))
;;   (om/set-state! owner [:markup :content] (.. e -target -value))
;;   )

(defn old-step [ctx owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "row"}
               (dom/div #js {:className "col-md-6"}
                        (dom/div #js {:className "panel panel-info"}
                                 (dom/div #js {:className "panel-heading"} (:title ctx))
                                 (dom/div #js {:className "panel-body"}
                                          (dom/p nil (:description ctx))
                                          (om/build editable (get-in ctx [:markup :content]))
                                          ;; (dom/textarea #js {:className "form-control" :value (get-in ctx [:markup :content]) :onChange #(handle-change-new % owner state)})
                                          )))
               (dom/div #js {:className "col-md-6"}
                        (om/build element (:markup ctx))
                        )

               ))
    )
  )

(defn old-tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
      (apply
       dom/div #js {}
       (om/build-all old-step (:steps app)))
      )))


(defn image [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/img #js {:className "img-rounded" :alt "foo" :src "img/image.png" :width "100%" :height "360px"}))))

(defn title [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/h2 nil (:default details)))))

(defn intro [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/p #js {:className "lead"} (:default details)))))

(defn table-row [row owner]
  (reify
    om/IRender
    (render [this]
      (dom/tr nil
       (dom/th nil (:th row))
       (dom/td nil (:td row))))))

(defn table [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/table #js {:className "table table-bordered"}
                 (apply
                  dom/tbody nil
                  (om/build-all table-row (:rows details))
                  )
                 ))))

(defn list-item [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil (:content details)))
    ))

(defn ulist [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (dom/h2 nil (:title details))
               (dom/p nil (:intro details))
               (apply
                dom/ul nil
                (om/build-all list-item (:items details)))
               )
      )))

(defn olist [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (dom/h2 nil (:title details))
               (dom/p nil (:intro details))
               (apply
                dom/ol nil
                (om/build-all list-item (:items details)))
               )
      )))

(defn checkbox [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "checkbox"}
               (dom/label nil
                          (dom/input #js {:type "checkbox"})
                          (:content details)
                ))
      )))

(defn form [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/form nil
                (dom/h3 nil (:title details))
                (dom/p nil (:intro details))
                (apply
                 dom/fieldset
                 (om/build-all checkbox (:checkboxes details)))
                (dom/button #js {:type "submit" :className "btn btn-default"} (:button-text details))
                )
      )))

(defn step [details owner]
  (reify
    om/IRender
    (render [this]
      (om/build ((:function details)) details)
      )))

(defn column [steps owner]
  (reify
    om/IRender
    (render [this]
      (apply
       dom/div #js {:className "col-md-4"}
       (om/build-all step steps)))))

(defn tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
      (apply
       dom/div #js {:className "row"}
       (om/build-all column (:columns app))
       )
      )))

(defn main []
  (om/root
    old-tutorial-view
    old-app
    {:target (. js/document (getElementById "old-app"))})

  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))})

  )
