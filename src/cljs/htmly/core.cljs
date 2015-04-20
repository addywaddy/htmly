(ns htmly.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [alandipert.storage-atom :refer [local-storage]]))

(enable-console-print!)

(defn console-log [obj]
  (.log js/console obj))

(defn input-width [text]
  (let [char-count (aget (str text) "length")
        weighting 0.61
        padding 0.1]
    (if (> char-count 0)
      (+ padding (* weighting char-count))
      (+ padding weighting)
      )
    )
  )

(defn text-area-height [text]
  (let [rows (.ceil js/Math (/ (input-width text) 50))
        row-height 1.43]
    (* row-height rows)
    )
  )

(defn editable [ctx owner]
  (reify
  om/IRender
  (render [this]
    (dom/textarea #js {:className ""
                       :onChange (fn [e] (om/transact! ctx [0], (fn [_] (.. e -target -value))))
                       :value (first ctx)
                       :style #js {:width "50em" :height (str (text-area-height (first ctx)) "em")}
                       }))))

(defn editable-input [ctx owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/input #js {:id (str "input-" root-node-id)
                                :className ""
                                :onChange (fn [e] (om/transact! ctx [0] (fn [_] (.. e -target -value))))
                                :value (first ctx)
                                :style #js {:width (str (input-width (first ctx)) "em")}
                                })
                ))))

(defn change-body-color [color]
  (let [body (aget js/document "body")]
    (aset body "className" color)))

(defn no-local-storage [function]
  function)

(def app (local-storage (atom {:show-help false
                                  :columns [[
                                             {:function :background
                                              :default ["pink"]
                                              :help {:title "Hintergrund"
                                                     :content "Hintergrundfarbe bestimmen"
                                                     }}

                                             {:function :canvas
                                              :default []
                                              :help {:title "Drawing"
                                                     :content "Mal ein schönes Bild von dir!"}}

                                             {:function :image
                                              :default [""]
                                              :help {:title "Image"
                                                     :content "Was wäre eine Webseite ohne Bilder? Langweilig! Damit deine Seite was persönliches hat werden mit einen Bild anfangen"
                                                     }}

                                             {:function :title
                                              :default ["Deine Name"]
                                              :help {:title "Heading"
                                                     :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                                     :tag {:open "<h2>" :close "</h2>"}
                                                     }}

                                             {:function :intro
                                              :default ["Etwas text über dich, der dich grob beschreibt. Es muss nicht all zu lang sein, aber genug um einen Eindruck von dir zu bekommen."]
                                              :help {:title "Paragraph"
                                                     :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                                     :tag {:open "<p>" :close "</p>"}
                                                     }}

                                             {:function :table
                                              :default [
                                                        [["Alter"] ["10"]]
                                                        [["Große"] ["1,30m"]]
                                                        [["Haarfarbe"] ["Braun"]]
                                                        [["Augenfarbe"] ["Blau"]]]
                                              :over [0 0]
                                              :help {:title "Table"
                                                     :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                                     :tag {:open "<p>" :close "</p>"}
                                                     }}
                                             ]
                                            [
                                             {:function :ulist
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

                                             {:function :ulist
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

                                             {:function :olist
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


                                             {:function :olist
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

                                            [{:function :linklist
                                              :title ["Meine Top Webseiten"]
                                              :intro ["Diese Seiten mag ich:"]
                                              :icon "globe"
                                              :default [
                                                        [["One"] ["http://www.youtube.com"]]
                                                        [["Two"] ["http://www.google.com"]]
                                                        [["Three"] ["http://www.kika.de"]]
                                                        ]
                                              :help {:title "Ordered list 2"
                                                     :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                                     :tag {:open "<p>" :close "</p>"}
                                                     }}

                                             {:function :form
                                              :title ["Quiz"]
                                              :intro ["Rate mal, was mein Lieblingstier ist"]
                                              :default [
                                                        ["Elefant"]
                                                        ["Giraffe"]
                                                        ["Tiger"]
                                                        ["Schlange"]
                                                        ["Hai"]]
                                              :button ["Raten"]
                                              :help {:title "Form"
                                                     :content "Jetzt braucht deine Webseite einen Titel. Wie wäre es mit deinen Name?"
                                                     :tag {:open "<p>" :close "</p>"}}
                                              }]]})))


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

(defn background-thumbnail [details classname]
  (dom/div #js {:className "col-md-2 background-thumbnails"}
                          (dom/a #js {:href "#" :className "thumbnail background"}
                                 (dom/div #js {:className classname :onClick (fn [_]
                                                                               (om/update! details [:default 0], classname)
                                                                               (change-body-color classname)
                                                                               false)}))

                          )
  )

(defn background [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div #js {:className "row"}
                 (dom/p "Background image")
                 (background-thumbnail details "pink")
                 (background-thumbnail details "green")
                 (background-thumbnail details "blue")
                 (background-thumbnail details "grey")
                 (background-thumbnail details "white")
                 )
        ))))

(defn canvass [details owner]
  (reify
    om/IRenderState
    (render-state [this owner]
      (if (:help state)
        (dom/div nil "Foo")
        (dom/canvas #js {:className "img-rounded" :width "360px" :height "360px"})))))

(defn canvas [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil)
        (dom/canvas #js {:className "img-rounded" :width "360px" :height "360px"})
        ))))

(defn image [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (.attach js/Webcam "#image-preview")) } "Take Photo")
                 (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (.snap js/Webcam (fn [data-uri] (om/update! details [:default 0], data-uri) (.reset js/Webcam)))) } "Save Photo")
                 )
        (dom/div #js {:style #js {:position "relative"}}
                 (dom/img #js {:className "img-rounded" :src (-> details :default first) :width "100%" :height "360px"})
                 (dom/div #js {:id "image-preview"})))
        )
      ))

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

(defn link-list-item [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil
              (dom/a #js {:href (-> details last first)} (-> details first first))))))

(defn list-item-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <li>")
                (om/build editable-input details)
                (dom/span nil "</li>\n")))))

(defn link-list-item-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <li>\n")
                (dom/span nil "    <a href='")
                (om/build editable-input (last details))
                (dom/span nil "'>")
                (om/build editable-input (first details))
                (dom/span nil "</a>\n")
                (dom/span nil "</li>\n")))))

(defn list-help [details tag list-item-component]
  (dom/span nil
            (dom/span nil (str "<" tag ">\n")
                      (apply
                       dom/span nil
                       (om/build-all list-item-component (:default details))
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
                  (list-help details "ul" list-item-help)
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
         (list-help details "ul" list-item-help)
         )
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ol nil
                  (om/build-all list-item (:default details)))))
      )))

(defn linklist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (source-code
         (edit-title-and-intro details)
         (list-help details "ol" link-list-item-help)
         )
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ol nil
                  (om/build-all link-list-item (:default details)))))
      )))

(defn checkbox-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
                (dom/span nil "  <label>\n    <input type='radio' name='quiz'/>\n    ")
                (om/build editable-input details)
                (dom/span nil "\n  </label>\n")))))

(defn checkbox [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "radio"}
               (dom/label nil
                          (dom/input #js {:type "radio" :name "answer" :value (first details)})
                          (-> details last first))))))

(defn button-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <button>")
                (om/build editable-input details)
                (dom/span nil "</button>")))))

(defn form-help [details]
  (dom/span nil
            (dom/span nil (str "<form>\n")
                      (apply
                       dom/span nil
                       (om/build-all checkbox-help (:default details))
                       )
                      (om/build button-help (:button details))
                      (dom/span nil (str "\n</form>")))))

(defn option-tag [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/option #js {:value (first details)} (-> details last first))
      )))

(defn form [details owner]
  (reify
    om/IRenderState

    (render-state [this state]
      (let [indexed-details (map-indexed (fn [idx itm] [idx itm]) (:default details))]
        (if (:help state)
          (dom/div nil
                   (dom/div #js {:className "form-group"}
                            (dom/label #js {:className "control-label"} "Die Lösung:")
                            (apply
                             dom/select #js {
                                             :className "form-control"
                                             :value (:answer details)
                                             :onChange (fn [e] (om/transact! details :answer (fn [_] (.. e -target -value))))
                                             }
                             (om/build-all option-tag indexed-details)))

                   (source-code
                    (edit-title-and-intro details)
                    (form-help details)
                    )
                   )

          (dom/form #js {:onSubmit js/runQuiz}
                    (dom/h3 nil (-> details :title first))
                    (dom/p nil (-> details :intro first))
                    (dom/input #js {:type "hidden" :name "quiz-answer" :value (:answer details)})
                    (apply
                     dom/fieldset nil
                     (om/build-all checkbox indexed-details))
                    (dom/button #js {:type "submit" :className "btn btn-default"} (first (:button details)))
                    ))))))

(def step-lookup
  {:background background
   :canvas canvas
   :image image
   :title title
   :intro intro
   :table table
   :ulist ulist
   :olist olist
   :linklist linklist
   :form form
   })
(defn step [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (om/build (step-lookup (:function details)) details {:init-state state}))))

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
      (dom/div #js {:className "row section"}
               (dom/div #js {:className "col-md-8"}
                        (om/build help-text details)
                        (om/build step details {:init-state {:help true}})
                        )
               (dom/div #js {:className "col-md-4 preview"}
                        (om/build step details)
                        )
               )
      ))
  )

(defn tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
      (console-log (:show-help app))
      (let [current-step (:current-step app)
            cols (:columns app)
            steps (-> cols flatten vec)]
        (dom/div nil
                 (dom/a #js {:href "#" :onClick (fn [e] (om/transact! app :show-help (fn [bool] (not bool))) false)}
                        (dom/span #js {:className "glyphicon glyphicon-eye-close"} ""))
                 (if (:show-help app)
                   (dom/div #js {:className "tutorial"}
                            (apply
                             dom/div nil
                             (om/build-all step-with-help steps)
                             )
                            )
                   (apply
                    dom/div #js {:className "row site"}
                    (om/build-all column (:columns app)))

                   ))

        ))))

(defn main []
  (let [color (-> @app :columns first first :default first)]
    (change-body-color color))

  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))}))
