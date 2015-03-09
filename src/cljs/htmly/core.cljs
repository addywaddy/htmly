(ns htmly.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(def app (atom {:steps [
                              {:title "Heading" :description "Description" :markup {:tag dom/h2 :content "Hello"}}
                              {:title "Paragraph" :description "Description" :markup {:tag dom/p :content ""}}
                              {:title "List" :description "Description" :markup {:tag dom/li :content ""}}
                              {:title "Table" :description "Description" :markup {:tag dom/table :content ""}}
                              {:title "Forms" :description "Description" :markup {:tag dom/form :content ""}}
                              {:title "Images" :description "Description" :markup {:tag dom/img :content ""}}
                              ]}))

(defn console-log [obj]
  (.log js/console (pr-str obj)))

(defn element [ctx owner]
  (reify
    om/IRender
    (render [this]
      ((:tag ctx) nil (:content ctx)))
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

(defn handle-change-new [e owner state]
  (println (om/get-state owner [:markup :content]))
  (println (.. e -target -value))
  (om/set-state! owner [:markup :content] (.. e -target -value))
  )

(defn step [ctx owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "row"}
               (dom/div #js {:className "col-md-6"}
                        (dom/div #js {:className "panel panel-info"}
                                 (dom/div #js {:className "panel-heading"} (:title ctx))
                                 (dom/div #js {:className "panel-body"}
                                          (dom/p nil (:description ctx))
                                          (dom/textarea #js {:className "form-control" :value (get-in ctx [:markup :content]) :onChange #(handle-change-new % owner state)})
                                          )))
               (dom/div #js {:className "col-md-6"}
                        (om/build element (:markup ctx))
                        )

               ))
    )
  )

(defn tutorial-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:step-update-ch (chan)})
    om/IWillMount
    (will-mount [_]
      (let [step-update-ch (om/get-state owner :step-update-ch)]
        (go
          (loop []
            (let [step-no (<! step-update-ch)]
              (println step-no)
              (om/update! app step-no)
              (recur)
              )
          )
        ))
      )
    om/IRenderState
    (render-state [this {:keys [step-update-ch]}]
      (apply
       dom/div #js {}
       (om/build-all step (:steps app) {:init-state {:step-update-ch step-update-ch}}))
      )))

(defn main []
  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))}))
