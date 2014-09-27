(ns subbox.styles
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [px]]))


(def subscriptions-bar-width (px 300))


(defstyles block-list
  {:padding 0}
  [:>li
   {:list-style-type :none}])

(defstyles clearfix
  [:&:after
   {:content "\"\""
    :display :table
    :clear   :both}])

(defstyles heading-font
  {:font {:family "Nunito"
          :weight 400}})

(defstyles screen
  [:body
   {:font {:family "Open Sans"
           :weight 400
           :size (px 16)}}]

  [:.app
   {:position :relative
    :margin {:top (px 30)}}

   [:>ul.subscriptions
    block-list
    {:position :absolute
     :top 0
     :bottom 0
     :left 0
     :width subscriptions-bar-width}

    [:>li
     {:cursor :pointer
      :height (px 50)}
     [:&.selected
      {:color :blue}]
     [:>img.thumbnail
      (let [size (px 40)]
        {:width size
         :height size
         :margin (px 10)
         :vertical-align :middle})]
     [:>span.title
      {:vertical-align :middle}]]]

   [:>section.main
    {:position :absolute
     :top 0
     :bottom 0
     :left subscriptions-bar-width
     :right 0}

    [:>h1
     heading-font
     {:font-size (px 64)
      :margin {:bottom (px 40)}}]

    [:>ul.videos
     block-list
     [:>li
      {:margin {:bottom (px 40)}}
      [:article.video
       clearfix
       [:>img.thumbnail
        {:float :left
         :margin {:right (px 20)}}]
       [:>div.info
        {:float :left
         :width (px 500)}
        [:>.title
         heading-font
         {:font-size (px 32)
          :line-height (px 28)
          :margin {:bottom (px 20)}}]
        [:>.description
          {:line-height (px 20)}
         [:>p
          {:margin {:bottom (px 10)}}]]]]]]]])
