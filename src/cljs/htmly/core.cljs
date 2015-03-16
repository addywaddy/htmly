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
    (dom/textarea #js {:className ""
                       :onChange (fn [e] (om/transact! ctx [0] (fn [str] (.. e -target -value))))
                       :value (first ctx)
                       }))))
(defn editable-input [ctx owner]
  (reify
  om/IRender
  (render [this]
    (dom/input #js {:className ""
                       :onChange (fn [e] (om/transact! ctx [0] (fn [str] (.. e -target -value))))
                       :value (first ctx)
                       }))))

(def app (atom {:current-step 8
                :show-help true
                :columns [[
                           {:function (fn [] image)
                            :default ["img/image.png"]
                            :help {:title "Image"
                                   :content "Was wäre eine Webseite ohne Bilder? Langweilig! Damit deine Seite was persönliches hat werden mit einen Bild anfangen"
                                   }}

                           {:function (fn [] title)
                            :default ["Deine Name"]
                            :help {:title "Heading"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<h2>" :close "</h2>"}
                                   }}

                           {:function (fn [] intro)
                            :default ["Etwas text über dich, der dich grob beschreibt. Es muss nicht all zu lang sein, aber genug um einen Eindruck von dir zu bekommen."]
                            :help {:title "Paragraph"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}

                           {:function (fn [] table)
                            :default [
                                   [["Alter"] ["10"]]
                                   [["Große"] ["1,30m"]]
                                   [["Haarfarbe"] ["Braun"]]
                                   [["Augenfarbe"] ["Blau"]]]
                            :help {:title "Table"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}
                           ]
                          [
                           {:function (fn [] ulist)
                            :title ["Daumen Hoch"]
                            :intro ["Diese Sachen finde ich cool:"]
                            :icon "thumbs-up"
                            :default [
                                      ["One"]
                                      ["Two"]
                                      ["Three"]]
                            :help {:title "Unordere list 1"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}

                           {:function (fn [] ulist)
                            :title ["Daumen Runter"]
                            :intro ["Diese Sachen finde ich schlecht:"]
                            :icon "thumbs-down"
                            :default [
                                      ["One"]
                                      ["Two"]
                                      ["Three"]]
                            :help {:title "Unordered list 2"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}

                           {:function (fn [] olist)
                            :title ["Mein Top Lieder"]
                            :intro ["Diese Songs finde ich der Hammer:"]
                            :icon "music"
                            :default [
                                      ["One"]
                                      ["Two"]
                                      ["Three"]]
                            :help {:title "Ordered list 1"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}


                           {:function (fn [] olist)
                            :title ["Mein Top Filme"]
                            :intro ["Diese Filme sind genial:"]
                            :icon "film"
                            :default [
                                      ["One"]
                                      ["Two"]
                                      ["Three"]]
                            :help {:title "Ordered list 2"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}
                                   }}]
                          [
                           {:function (fn [] form)
                            :title ["Quiz"]
                            :intro ["Rate mal, was mein Lieblingstier ist"]
                            :default [["Giraffe"]
                                      ["Elefant"]
                                      ["Tiger"]
                                      ["Schlange"]
                                      ["Hai"]]
                            :button-text "Raten"
                            :help {:title "Form"
                                   :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                   :tag {:open "<p>" :close "</p>"}}
                            }]]}))

(defn console-log [obj]
  (.log js/console (pr-str obj)))

(defn edit-title-and-intro [details]
  (dom/span nil
            (dom/span nil "<h3>\n  ")
            (om/build editable-input (:title details))
            (dom/span nil "\n<h3>\n")
            (dom/span nil "<p>\n  ")
            (om/build editable (:intro details))
            (dom/span nil "\n<p>\n")
  ))

(defn source-code [& args]
  (apply
   dom/pre #js {:className "text-primary"}
   args))

(defn image [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
         (dom/span nil "<img src='")
         (om/build editable (:default details))
         (dom/span nil "'/>")
         )
        (dom/img #js {:className "img-rounded" :src (-> details :default first) :width "100%" :height "360px"})
        )
      )))

(defn title [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
         (dom/span nil "<h1>\n  ")
         (om/build editable-input (:default details))
         (dom/span nil "\n<h1>")
         )
        (dom/h2 nil (-> details :default first))
        (dom/h2 nil (-> details :default first))
        )
      )))

(defn intro [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
         (dom/span nil "<p>\n  ")
         (om/build editable (:default details))
         (dom/span nil "\n</p>")
         )
        (dom/p #js {:className "lead"} (-> details :default first))
        (dom/p #js {:className "lead"} (-> details :default first))
        )
      )))

(defn table-row [row owner]
  (reify
    om/IRender
    (render [this]
      (dom/tr nil
       (dom/th nil (-> row first first))
       (dom/td nil (-> row last first))))))

(defn table-row-help [row owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "    <tr>\n")
                (dom/span nil "      <th>")
                (om/build editable-input (first row))
                (dom/span nil "</th>\n")
                (dom/span nil "      <td>")
                (om/build editable-input (last row))
                (dom/span nil "</td>\n")
                (dom/span nil "    </tr>\n")
                ))))

(defn table [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (console-log state)
      (if (:help state)
        (source-code
                 (dom/span nil "<table>\n")
                 (dom/span nil "  <tbody>\n")
                 (apply
                  dom/span nil
                  (om/build-all table-row-help (:default details))
                  )
                 (dom/span nil "  </tbody>\n")
                 (dom/span nil "</table>"))

        (dom/table #js {:className "table table-bordered"}
                   (apply
                    dom/tbody nil
                    (om/build-all table-row (:default details))))))))

(defn list-item [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil (first details)))))

(defn list-item-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <li>")
                (om/build editable-input details)
                (dom/span nil "</li>\n")))))

(defn list-help [details tag]
  (dom/span nil
            (dom/span nil (str "<" tag ">\n")
                      (apply
                       dom/span nil
                       (om/build-all list-item-help (:default details))
                       )
                      (dom/span nil (str "</" tag ">")))))

(defn icon-title [details]
  (dom/h3 nil
          (dom/span #js {:className (str "glyphicon glyphicon-" (:icon details))} "")
          (dom/span nil (str " " (-> details :title first)))))

(defn ulist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
                  (edit-title-and-intro details)
                  (list-help details "ul")
                  )
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ul nil
                  (om/build-all list-item (:default details)))))
      )))

(defn olist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
         (edit-title-and-intro details)
         (list-help details "ul")
         )
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ol nil
                  (om/build-all list-item (:default details)))))
      )))

(defn checkbox [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "checkbox"}
               (dom/label nil
                          (dom/input #js {:type "checkbox"})
                          (first details))))))

(defn form [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/form nil
                (dom/h3 nil (-> details :title first))
                (dom/p nil (-> details :intro first))
                (apply
                 dom/fieldset
                 (om/build-all checkbox (:default details)))
                (dom/button #js {:type "submit" :className "btn btn-default"} (:button-text details))))))

(defn step [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (om/build ((:function details)) details {:init-state state}))))

(defn column [steps owner]
  (reify
    om/IRender
    (render [this]
      (apply
       dom/div #js {:className "col-md-4"}
       (om/build-all step steps)))))

(defn help-text [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (dom/h3 nil (get-in details [:help :title]))
               (dom/p nil (get-in details [:help :content]))))))

(defn step-with-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "row"}
               (dom/div #js {:className "col-md-8"}
                        (om/build help-text details)
                        (om/build step details {:init-state {:help true}})
                        )
               (dom/div #js {:className "col-md-4"}
                        (om/build step details)
                        )
               )
      ))
  )

(defn tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
      (let [current-step (:current-step app)
            cols (:columns app)
            steps (subvec (-> cols flatten vec) 0 (+ 1 current-step))
            ]
        (if (:show-help app)
          (dom/div #js {:className "tutorial"}
                 (apply
                  dom/div nil
                   (om/build-all step-with-help steps)
                   )
                 )
          (apply
           dom/div #js {:className "row"}
           (om/build-all column (:columns app)))

          )

        ))))

(defn main []
  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))}))
